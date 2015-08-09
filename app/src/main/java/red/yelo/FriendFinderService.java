/*
 *
 *  * Copyright (C) 2015 yelo.red
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */package red.yelo;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.TableUploadedContacts;
import red.yelo.http.api.ChatApi;
import red.yelo.http.api.YeloApi;
import red.yelo.retromodels.Contacts;
import red.yelo.retromodels.request.UploadContactRequestModel;
import red.yelo.retromodels.response.UploadContactsModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import red.yelo.utils.md5.MD5;

/**
 * Class that reads the user's phone book and uploads hashed phone numbers to server
 * <p/>
 * Created by vinaysshenoy on 09/11/14.
 */
public class FriendFinderService extends IntentService {

    private static final String TAG = "FriendFinderService";

    public static final String ACTION_FIND_FRIENDS = "red.yelo.ACTION_FIND_FRIENDS";

    private static final int UPLOAD_BATCH_SIZE = 100;

    private String mCountryCode;

    private ArrayDeque<ContactSyncEntry> mSyncQueue;

    public FriendFinderService() {
        this("Friend Finder Service");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FriendFinderService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.getAction() != null && intent.getAction().equals(ACTION_FIND_FRIENDS)) {

            /* Defensive check in the event of the service getting triggered again which it is already running */
            final long lastContactSyncTime = SharedPreferenceHelper.getLong(R.string.pref_last_contact_sync_time);
            if ((Utils.getCurrentEpochTime() - lastContactSyncTime) < AppConstants.CONTACT_SYNC_INTERVAL) {
                return;
            }

            mCountryCode = Utils.getSimCountryCode();
            final Cursor contacts = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
                    null, null);

            if (contacts.getCount() > 0) {
                mSyncQueue = new ArrayDeque<>(contacts.getCount() / UPLOAD_BATCH_SIZE + 1);
                queueUpSyncs(contacts);
            } else {
                mSyncQueue = new ArrayDeque<>();
            }
            contacts.close();
            syncQueue();
            savedSyncedNumbers();
            checkIfAllSynced();
            doneWithSync();
        }
    }

    /**
     * Checks if all the pending syncs were successful
     */
    private void checkIfAllSynced() {

        boolean completedSync = true;
        for (ContactSyncEntry entry : mSyncQueue) {
            completedSync &= entry.isSynced();
        }

        if (completedSync) {
            Logger.d(TAG, "SYNCED");
            SharedPreferenceHelper.set(R.string.pref_last_contact_sync_time, Utils.getCurrentEpochTime());
        }
    }

    /**
     * Saves the synced numbers so that we can forgo syncing them in future sync rounds
     */
    private void savedSyncedNumbers() {

        for (ContactSyncEntry eachEntry : mSyncQueue) {

            if (eachEntry.isSynced()) {


                for (int i = 0; i < eachEntry.numbersToSync.size(); i++) {
                    String eachNumber = eachEntry.numbersToSync.get(i);
                    String eachName = "";
                    if (i < eachEntry.nameToSync.size()) {
                        eachName = eachEntry.nameToSync.get(i);
                    }
                    final ContentValues values = new ContentValues(1);
                    values.put(DatabaseColumns.NUMBER, eachNumber);
                    values.put(DatabaseColumns.NAME, eachName);

                    DBInterface.insert(TableUploadedContacts.NAME, null, values, false);
                }
            }
        }
    }

    /**
     * Syncs the queued up syncs with the server
     */
    private void syncQueue() {

        Logger.d(TAG, "Pending Syncs: %d", mSyncQueue != null ? mSyncQueue.size() : 0);
        if (mSyncQueue != null && !mSyncQueue.isEmpty()) {

            final MD5 md5 = new MD5();

            for (ContactSyncEntry currentSyncEntry : mSyncQueue) {
                filterAlreadySynced(currentSyncEntry);
                Logger.d(TAG, "Numbers to sync: %d", currentSyncEntry.numbersToSync.size());

                final UploadContactRequestModel uploadContactRequestModel = new UploadContactRequestModel();
                String hashedNumber;
                String contactName = "";

                List<Contacts> contactsList = new ArrayList<Contacts>();
                for (int i = 0; i < currentSyncEntry.numbersToSync.size(); i++) {

                    Contacts contacts = new Contacts();
                    hashedNumber = numberToMd5(currentSyncEntry.numbersToSync.get(i), md5);
                    if (i < currentSyncEntry.nameToSync.size())
                        contactName = currentSyncEntry.nameToSync.get(i);


                    if (!TextUtils.isEmpty(hashedNumber)) {
                        contacts.setHash_mobile_numbers(hashedNumber);
                        contacts.setName(contactName);
                    }
                    contactsList.add(contacts);
                }

                uploadContactRequestModel.setContacts(contactsList);
                if (contactsList.size() != 0) {
                    long lastContactBatchUploaded = SharedPreferenceHelper.getLong(R.string.pref_last_contact_back_uploaded_time);
                    if ((Utils.getCurrentEpochTime() - lastContactBatchUploaded) < AppConstants.CONTACT_PER_BATCH_UPLOAD_INTERVAL) {
                    } else {


                        final YeloApi api = ((YeloApplication) getApplication()).getYeloApi();

                        try {

                            final UploadContactsModel response = api.uploadContacts(uploadContactRequestModel);
                            if ("success".equals(response.status)) {
                                currentSyncEntry.setSynced(true);

                                SharedPreferenceHelper.set(R.string.pref_last_contact_back_uploaded_time, Utils.getCurrentEpochTime());

                            }
                        } catch (Exception exception) {
                            currentSyncEntry.setSynced(false);
                        }
                    }

                }


            }

        }
    }

    /**
     * Checks which of the entries have already been synced before and removes them if they have already been synced before
     */
    private void filterAlreadySynced(ContactSyncEntry syncEntry) {

        final String selection = String.format(Locale.US, "%s IN (%s)", DatabaseColumns.NUMBER, syncEntry.getJoinedNumbersCsv());
        final Cursor cursor = DBInterface.query(false, TableUploadedContacts.NAME, null, selection, null, null, null, null, null);

        if (cursor != null) {

            //If the number has already been synced previously, don't sync it again
            while (cursor.moveToNext()) {
                syncEntry.removeNumber(cursor.getString(cursor.getColumnIndex(DatabaseColumns.NUMBER)));
            }

            cursor.close();
        }


    }

    /**
     * Method that hashes contacts and syncs them to server
     *
     * @param contactsCursor The cursor for contacts
     */
    private void queueUpSyncs(final Cursor contactsCursor) {

        ContactSyncEntry syncEntry = null;
        while (contactsCursor.moveToNext()) {

            if (syncEntry == null) {
                syncEntry = new ContactSyncEntry(UPLOAD_BATCH_SIZE);
            }

            final String number = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            final String name = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            if (!TextUtils.isEmpty(number)) {
                syncEntry.addNumber(numberToNational(number));
                syncEntry.addName(name);
                if (syncEntry.isFull()) {
                    Logger.d(TAG, "Adding sync entry to queue");
                    mSyncQueue.add(syncEntry);
                    syncEntry = null;
                }
            }
        }

    }

    /**
     * Method that converts a phone number to an MD5 Hash
     *
     * @param number The  number to convert to an MD5 Hash
     * @param md5    An instance of MD5 to use to hash the number
     */
    private String numberToMd5(String number, MD5 md5) {

        String hash = "";
        if (!TextUtils.isEmpty(number)) {
            try {

                md5.Init();
                md5.Update(number, HTTP.ISO_8859_1);
                hash = md5.asHex();

            } catch (UnsupportedEncodingException e) {
                hash = "";
            }
        }
        return hash;
    }

    /**
     * Creates a national number from a number string
     *
     * @param number The  number to convert to a national number
     */
    private String numberToNational(String number) {

        final PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            final Phonenumber.PhoneNumber phoneNumber = util.parse(
                    number, mCountryCode);
            return String.valueOf(phoneNumber.getNationalNumber());
        } catch (NumberParseException e) {
            return "";
        }
    }

    private void doneWithSync() {
        stopSelf();
    }

    /**
     * Class that holds a single contact upload operation
     */
    private static class ContactSyncEntry {

        public final List<String> numbersToSync;
        public final List<String> nameToSync;


        public final int capacity;

        private boolean mSynced;

        public ContactSyncEntry(int capacity) {
            numbersToSync = new ArrayList<String>(capacity);
            nameToSync = new ArrayList<String>(capacity);
            this.capacity = capacity;
        }

        public boolean isSynced() {
            return mSynced;
        }

        public void setSynced(boolean mSynced) {
            this.mSynced = mSynced;
        }

        public boolean isFull() {
            return numbersToSync.size() == capacity;
        }

        public void addNumber(String number) {

            if (!TextUtils.isEmpty(number) && !numbersToSync.contains(number)) {
                numbersToSync.add(number);
            }
        }

        public void addName(String name) {

            if (!TextUtils.isEmpty(name) && !nameToSync.contains(name)) {
                nameToSync.add(name);
            }
        }

        public void removeNumber(String number) {
            numbersToSync.remove(number);
        }

        public void removeName(String name) {
            nameToSync.remove(name);
        }


        public String getJoinedNumbersCsv() {

            final List<String> quotedNumbers = new ArrayList<String>(numbersToSync.size());

            for (String eachNumber : numbersToSync) {
                quotedNumbers.add(String.format(Locale.US, "'%s'", eachNumber));
            }


            return TextUtils.join(",", quotedNumbers);
        }

        public String getJoinedNamesCsv() {
            final List<String> quotedNames = new ArrayList<>(nameToSync.size());

            for (String eachName : nameToSync) {
                quotedNames.add(String.format(Locale.US, "'%s'", eachName));
            }
            return TextUtils.join(",", quotedNames);

        }

    }


}
