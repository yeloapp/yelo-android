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
 */package red.yelo.fragments;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.facebook.android.Util;
import com.vinaysshenoy.okulus.OkulusImageView;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dev.dworks.libs.astickyheader.SimpleSectionedListAdapter;
import red.yelo.R;
import red.yelo.activities.WallPostActivity;
import red.yelo.adapters.ContactsAdapter;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.TableWallComments;
import red.yelo.data.TableWallPosts;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.http.WallPostIntentService;
import red.yelo.retromodels.TaggedUser;
import red.yelo.retromodels.request.PostWallCommentRequestModel;
import red.yelo.retromodels.response.GetWallItemResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.Logger;
import red.yelo.utils.SearchViewNetworkQueryHelper;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class TagUserFragment extends AbstractYeloFragment implements View.OnClickListener,
        TextWatcher, DBInterface.AsyncDbQueryCallback, RetroCallback.RetroResponseListener,
        AdapterView.OnItemClickListener, MenuItemCompat.OnActionExpandListener,
        SearchViewNetworkQueryHelper.NetworkCallbacks, ContactsAdapter.ContactsActionClickListener
        , LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "TagUserFragment";

    private ListView mContactList;

    private EditText mTagUserEdit, mRecommendationComment;

    private ContactsAdapter mContactsListAdapter;

    private Button mDoneButton;

    private String mWallId, mUserId;

    private SimpleDateFormat mFormatter;

    private DateFormatter mMessageDateFormatter;

    private boolean mFromWallPostFragment;

    private EditText mTagUserNameText;

    private int mTagCount;

    private String mEmail = "";

    private PostWallCommentRequestModel mPostWallCommentRequestModel;

    private Bundle mArgs;

    private TaggedUser mUser;

    private static final String ACTION_TAG = "red.yelo.http.action.TAG";

    private SearchView mSearchView;

    private SearchViewNetworkQueryHelper mSearchNetworkQueryHelper;


    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private ArrayList<SimpleSectionedListAdapter.Section> sections = new ArrayList<SimpleSectionedListAdapter.Section>();


    //private static final String SELECTION = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + SQLConstants.LIKE_ARG;

    private Toolbar mToolbar;

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION =
            {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    Build.VERSION.SDK_INT
                            >= Build.VERSION_CODES.HONEYCOMB ?
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                            ContactsContract.Contacts.DISPLAY_NAME

            };

    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;
    // The column index for the LOOKUP_KEY column
    private static final int LOOKUP_KEY_INDEX = 1;

    // Defines the text expression
    @SuppressLint("InlinedApi")
    private static final String SELECTION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" :
                    ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
    // Defines a variable for the search string
    private String mSearchString = "";
    // Defines the array to hold values that replace the ?
    private String[] mSelectionArgs = {mSearchString};

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        final View contentView = inflater
                .inflate(R.layout.fragment_tag_user, container, false);


        //((TagUserActivity)getActivity()).getSupportActionBar().set
        mTagUserEdit =
                (EditText) contentView.findViewById(R.id.tag_user);

        mTagUserEdit.addTextChangedListener(this);

        mContactList = (ListView) contentView.findViewById(R.id.list_contacts);
        mTagUserNameText = (EditText) contentView.findViewById(R.id.tag_user_name);

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);

        setToolbar(mToolbar);


        mRecommendationComment = (EditText) contentView.findViewById(R.id.recommendation);
        mDoneButton = (Button) contentView.findViewById(R.id.done_button);
        mDoneButton.setOnClickListener(this);


        mContactsListAdapter = new ContactsAdapter(getActivity(), this, false);
        //mContactList.setAdapter(mContactsListAdapter);
        mContactList.setFastScrollEnabled(true);
        mContactList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mContactList.setOnItemClickListener(this);

        Bundle extras = getArguments();

        if (extras != null) {
            mWallId = extras.getString(AppConstants.Keys.WALL_ID);
            mUserId = extras.getString(AppConstants.Keys.USER_ID);
            mTagCount = extras.getInt(AppConstants.Keys.TAG_USER_COUNT);
            if (extras.containsKey(AppConstants.Keys.FROM_WALL)) {
                mFromWallPostFragment = true;
            } else {
                mFromWallPostFragment = false;
            }
        }

        if (savedInstanceState == null) {
            getContacts(getActivity().getContentResolver(), "");
        }

        return contentView;

    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.refer_user_searchmenu, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_search);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        MenuItemCompat.setOnActionExpandListener(menuItem, this);

        if (isAttached()) {
            if (mSearchView.getChildAt(0) != null) {
                LinearLayout ll = (LinearLayout) mSearchView.getChildAt(0);
                LinearLayout ll2 = (LinearLayout) ll.getChildAt(2);
                LinearLayout ll3 = (LinearLayout) ll2.getChildAt(1);
                SearchView.SearchAutoComplete autoComplete = (SearchView.SearchAutoComplete) ll3.getChildAt(0);
// set the hint text color
                autoComplete.setTextColor(getResources().getColor(R.color.dark_yelo));
                // textView.setHintTextColor(Color.WHITE);
                //MenuItemCompat.setActionView(menuItem, mSearchView);
            }
            mSearchNetworkQueryHelper = new SearchViewNetworkQueryHelper(mSearchView, this);
            mSearchNetworkQueryHelper.setSuggestCountThreshold(0);
            mSearchNetworkQueryHelper.setSuggestWaitThreshold(400);
        }

    }

    private void loadContacts() {
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_CONTACTS, null, this);
    }

    public void getContacts(ContentResolver cr, String q) {

        //TODO: Vinay - Move to Cursor Loader
        final String query = MessageFormat.format("%{0}%", q);

        sections.clear();

//        final Cursor phones = cr.query(
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, SELECTION,
//                new String[]{query}, null);

        String selection = ContactsContract.Data.MIMETYPE + SQLConstants.EQUALS_ARG + SQLConstants.AND +
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + SQLConstants.LIKE_ARG
                + SQLConstants.AND +
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + SQLConstants.NOT_EQUALS_ARG
                + SQLConstants.AND + "length(" +
                ContactsContract.CommonDataKinds.Phone.DATA1 + ") >7";


        final Cursor phones = cr.query(ContactsContract.Data.CONTENT_URI, null,
                selection, new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, query, ""},
                "upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ")" + SQLConstants.ASCENDING);


        Logger.d(TAG, "Cursor Loaded with " + phones.getCount());

        if (q.equals("")) {
            ArrayList<Integer> headerIndex = new ArrayList<Integer>();
            ArrayList<String> headerCharacter = new ArrayList<String>();


            Cursor headerCursor = phones;
            headerCursor.moveToFirst();
            String firstChar = "";
            int counter = 0;
            for (int i = 0; i < headerCursor.getCount(); i++) {
                String nextChar;
                nextChar = (headerCursor.getString(headerCursor
                        .getColumnIndex(ContactsContract.Data.DISPLAY_NAME)).charAt(0) + "").toUpperCase();
                if (nextChar.equals(firstChar)) {
                    counter++;
                } else {
                    headerIndex.add(counter);
                    headerCharacter.add(nextChar);
                    sections.add(new SimpleSectionedListAdapter.Section(counter, nextChar));
                    counter++;
                }

                firstChar = nextChar;
                headerCursor.moveToNext();
            }


            SimpleSectionedListAdapter simpleSectionedGridAdapter = new SimpleSectionedListAdapter(getActivity(), mContactsListAdapter,
                    R.layout.list_item_header, R.id.header);
            simpleSectionedGridAdapter.setSections(sections.toArray(new SimpleSectionedListAdapter.Section[0]));

            mContactList.setAdapter(simpleSectionedGridAdapter);

        } else {
            ArrayList<Integer> headerIndex = new ArrayList<Integer>();
            ArrayList<String> headerCharacter = new ArrayList<String>();

            headerIndex.add(0);
            headerCharacter.add("Search");
            sections.add(new SimpleSectionedListAdapter.Section(headerIndex.get(0), headerCharacter.get(0)));

            SimpleSectionedListAdapter simpleSectionedGridAdapter = new SimpleSectionedListAdapter(getActivity(), mContactsListAdapter,
                    R.layout.list_item_header, R.id.header);
            simpleSectionedGridAdapter.setSections(sections.toArray(new SimpleSectionedListAdapter.Section[0]));

            mContactList.setAdapter(simpleSectionedGridAdapter);
        }
        final Cursor oldCursor = mContactsListAdapter.swapCursor(phones);
        if (oldCursor != null && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    private void showReferDialog(final String imageUri, final String userName, final String userNumber,
                                 final String userEmail, final Cursor cursor) {

        final EditText recommendationEdit, userNameEditText;
        final TextView userNameTextView, mobileNumberTextView;
        final ImageView editDetails;
        final OkulusImageView userImage;


        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.layout_dialog_tagadd, true)
                .positiveText(R.string.refer)
                .negativeText(R.string.cancelCap)
                .positiveColor(getResources().getColor(R.color.blue_link))
                .negativeColor(getResources().getColor(R.color.blue_link))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String name = ((EditText) dialog.getCustomView().findViewById(R.id.edit_contact_text)).getText().toString();
                        if (!TextUtils.isEmpty(name)) {
                            hideKeyboard(null);
                            referProcess(name,
                                    userEmail,
                                    ((EditText) dialog.getCustomView().findViewById(R.id.recommendation_text)).getText().toString(),
                                    userNumber,
                                    cursor);
                        } else {
                            Toast.makeText(getActivity(), "Please enter full details", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        hideKeyboard(null);
                    }
                }).build();

        recommendationEdit = ((EditText) dialog.getCustomView().findViewById(R.id.recommendation_text));
        userNameTextView = ((TextView) dialog.getCustomView().findViewById(R.id.contact_name));
        mobileNumberTextView = ((TextView) dialog.getCustomView().findViewById(R.id.contact_number));
        editDetails = (ImageView) dialog.getCustomView().findViewById(R.id.edit_user);
        userImage = (OkulusImageView) dialog.getCustomView().findViewById(R.id.image_contact);
        userNameEditText = (EditText) dialog.getCustomView().findViewById(R.id.edit_contact_text);

        if (!TextUtils.isEmpty(imageUri)) {
            Utils.loadCircularImage(getActivity(), userImage, imageUri, AvatarBitmapTransformation.AvatarSize.NORMAL);
        } else {
            ColorGenerator generator = ColorGenerator.DEFAULT;

            int color = generator.getColor(userName.toUpperCase());
            Resources r = getActivity().getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

            TextDrawable drawable = TextDrawable.builder()
                    .buildRoundRect((userName.charAt(0) + "").toUpperCase(), color, Math.round(px));

            userImage.setImageDrawable(drawable);
            //Utils.loadCircularImage(context, (OkulusImageView) holder.imageContact, ((Drawable)drawable), AvatarBitmapTransformation.AvatarSize.NORMAL);
        }
        dialog.show();


        userNameTextView.setText(userName);
        mobileNumberTextView.setText(userNumber);
        userNameEditText.setText(userName);
        userNameEditText.setSelection(userName.length());

        (new Handler()).postDelayed(new Runnable() {

            public void run() {
                showKeyboard(recommendationEdit);

            }
        }, 200);

        editDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userNameEditText.setVisibility(View.VISIBLE);
                userNameTextView.setVisibility(View.GONE);

                (new Handler()).postDelayed(new Runnable() {

                    public void run() {
                        showKeyboard(userNameEditText);
                        userNameEditText.requestFocus();
                        userNameEditText.setSelected(true);
                    }
                }, 200);
            }
        });


    }

    @Override
    public void onDestroy() {
        final Cursor oldCursor = mContactsListAdapter != null ? mContactsListAdapter.getCursor() : null;
        if (oldCursor != null && !oldCursor.isClosed()) {
            oldCursor.close();
        }
        super.onDestroy();
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static TagUserFragment newInstance() {
        TagUserFragment f = new TagUserFragment();
        return f;
    }


    private void referProcess(String userName, String userEmail, String recommendation, String userNumber, Cursor cursor) {

        final String name = userName;

        String mobileNumber = userNumber;
        if (!TextUtils.isEmpty(mobileNumber)) {
            mobileNumber = mobileNumber.replaceAll("\\s", "");
            mobileNumber = mobileNumber.replaceAll("[^0-9]", "");
        }

        if (!TextUtils.isEmpty(userEmail)) {
            userEmail = userEmail.replaceAll("\\s", "");
        }

        mPostWallCommentRequestModel = new PostWallCommentRequestModel();

        List<TaggedUser> tagUsersList = new ArrayList<TaggedUser>(1);

        if(mobileNumber.length()>10) {
            mobileNumber = mobileNumber.substring(mobileNumber.length() - 10);
        }

        mUser = new TaggedUser();
        mUser.setEmail(userEmail);
        mUser.setMobile_number(SharedPreferenceHelper.getString(R.string.pref_country_code)+mobileNumber);
        mUser.setName(name);

        tagUsersList.add(mUser);

        mPostWallCommentRequestModel.wall_item.setComment(recommendation);
        mPostWallCommentRequestModel.setTag_users(tagUsersList);


        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                AppConstants.WALL_DATE_FORMAT);

        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());

        final String sentAt = mFormatter.format(new Date());
        String tempId = "";
        try {
            tempId = mMessageDateFormatter.getEpoch(sentAt) + "";
        } catch (ParseException e) {
            //should not happen
            e.printStackTrace();
        }

        mPostWallCommentRequestModel.wall_item.setTmp_id(tempId);


        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.TAG_USER);
        mArgs = new Bundle();
        mArgs.putString(AppConstants.Keys.WALL_ID, mWallId);
        mArgs.putString(AppConstants.Keys.USER_ID, mUserId);
        mArgs.putInt(AppConstants.Keys.TAG_USER_COUNT, mTagCount);
        mArgs.putString(AppConstants.Keys.TEMP_ID, tempId);

        retroCallback.setExtras(mArgs);

        retroCallbackList.add(retroCallback);


        if (mUserId.equals(AppConstants.UserInfo.INSTANCE.getId())) {

            Logger.d(TAG, mobileNumber + "    " + AppConstants.UserInfo.INSTANCE.getMobileNumber());
            if (!mobileNumber.endsWith(AppConstants.UserInfo.INSTANCE.getMobileNumber())) {


                if (mobileNumber.equals("") || name.equals("")) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.tag_format_exception_message),
                            Toast.LENGTH_SHORT).show();
                } else {

                    addTagLocally(tempId, recommendation, mUser.name);
//                            mYeloApi.tagUser(mWallId,
//                                    postWallCommentRequestModel, retroCallback);


//                            final Intent wallPostIntent = new Intent(getActivity(),
//                                    WallPostActivity.class);
//                            wallPostIntent.putExtra(AppConstants.Keys.ID, mWallId);
//                            startActivity(wallPostIntent);
                }
            } else {
                Toast.makeText(getActivity(), getResources().
                        getString(R.string.you_cannot_tag_on_your_wall), Toast.LENGTH_LONG).show();
            }

        } else {
            addTagLocally(tempId, recommendation, mUser.name);


//                    mYeloApi.tagUser(mWallId,
//                            postWallCommentRequestModel, retroCallback);


//                    final Intent wallPostIntent = new Intent(getActivity(),
//                            WallPostActivity.class);
//                    wallPostIntent.putExtra(AppConstants.Keys.ID, mWallId);
//                    startActivity(wallPostIntent);
        }


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.done_button) {


        }
    }

    private void tagUserInIntentService(PostWallCommentRequestModel postWallCommentRequestModel,
                                        Bundle args, TaggedUser user) {


        Intent intent = new Intent(getActivity(), WallPostIntentService.class);

        intent.setAction(ACTION_TAG);
        intent.putExtra(HttpConstants.ARGS, args);
        intent.putExtra(HttpConstants.EMAIL, user.email);
        intent.putExtra(HttpConstants.NAME, user.name);
        intent.putExtra(HttpConstants.MOBILE_NUMBER, user.mobile_number);
        intent.putExtra(HttpConstants.COMMENT, postWallCommentRequestModel.wall_item.comment);

        getActivity().startService(intent);

        getActivity().finish();


    }

    private void addTagLocally(String tempId, String comment, String taggedUserName) {

        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());
        final String sentAt = mFormatter.format(new Date());


        MixpanelAnalytics.getInstance().onContactReferred();
        ContentValues values = new ContentValues();
        values.put(DatabaseColumns.WALL_ID, mWallId);
        values.put(DatabaseColumns.TEMP_ID, tempId);
        values.put(DatabaseColumns.COMMENT, comment);
        values.put(DatabaseColumns.STATE, AppConstants.SyncStates.SYNCING);
        values.put(DatabaseColumns.IS_PRESENT, "");
        values.put(DatabaseColumns.WALL_USER_ID, mUserId);
        values.put(DatabaseColumns.USER_ID, AppConstants.UserInfo.INSTANCE.getId());
        values.put(DatabaseColumns.USER_NAME, AppConstants.UserInfo.INSTANCE.getFirstName());
        values.put(DatabaseColumns.TAGGED_NAMES, taggedUserName);
        try {

            values.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(sentAt));

        } catch (ParseException e) {
            e.printStackTrace();
            //should not happen
        }

        String selection = DatabaseColumns.TEMP_ID + SQLConstants.EQUALS_ARG;

        DBInterface
                .updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, null,
                        values, TableWallComments.NAME, values, selection,
                        new String[]{tempId}, true, this);

        ContentValues valuesWall = new ContentValues();


        try {

            valuesWall.put(DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT, mMessageDateFormatter.getEpoch(sentAt));

        } catch (ParseException e) {
            e.printStackTrace();
            //should not happen
        }

        String selectionWall = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
        DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, null,
                valuesWall, TableWallPosts.NAME, valuesWall, selectionWall,
                new String[]{mWallId}, true, this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        getContacts(getActivity().getContentResolver(), s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

        if (taskId == AppConstants.QueryTokens.INSERT_WALLCOMMENT_IN_TAG_FRAGMENT) {

            tagUserInIntentService(mPostWallCommentRequestModel, mArgs, mUser);

            if (!mFromWallPostFragment) {

                final Intent wallPostIntent = new Intent(getActivity(),
                        WallPostActivity.class);
                wallPostIntent.putExtra(AppConstants.Keys.FROM_TAG, true);
                wallPostIntent.putExtra(AppConstants.Keys.ID, mWallId);
                startActivity(wallPostIntent);
            }
        }
    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {
        if (taskId == AppConstants.QueryTokens.UPDATE_WALLCOMMENTS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLCOMMENT_IN_TAG_FRAGMENT, getTaskTag(), null
                        , TableWallComments.NAME, null, values, true, this);
            }
        }

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }

    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.TAG_USER: {

                getActivity().setProgressBarIndeterminateVisibility(false);

                GetWallItemResponseModel getWallItemResponseModel = ((GetWallItemResponseModel) model);


                ContentValues values = new ContentValues();
                values.put(DatabaseColumns.WALL_ID, mWallId);
                values.put(DatabaseColumns.ID, getWallItemResponseModel.wall_item.id);
                values.put(DatabaseColumns.COMMENT, getWallItemResponseModel.wall_item.comment);
                values.put(DatabaseColumns.WALL_USER_ID, mUserId);
                values.put(DatabaseColumns.USER_ID, getWallItemResponseModel.wall_item.user_id);
                values.put(DatabaseColumns.USER_NAME, getWallItemResponseModel.wall_item.name);
                values.put(DatabaseColumns.IMAGE_URL, getWallItemResponseModel.wall_item.image_url);

                if (getWallItemResponseModel.wall_item.tagged_users.size() > 0) {
                    values.put(DatabaseColumns.TAGGED_USER_IDS, getWallItemResponseModel.wall_item.tagged_users.get(0).id);
                    values.put(DatabaseColumns.TAGGED_NAMES, getWallItemResponseModel.wall_item.tagged_users.get(0).name);
                    if (getWallItemResponseModel.
                            wall_item.tagged_users.get(0).details != null) {
                        values.put(DatabaseColumns.TAGGED_USER_NUMBERS, getWallItemResponseModel.
                                wall_item.tagged_users.get(0).details.mobile_number);
                        values.put(DatabaseColumns.TAGGED_USER_EMAILS, getWallItemResponseModel.
                                wall_item.tagged_users.get(0).details.email);
                    }
                    values.put(DatabaseColumns.TAGGED_IMAGE_URLS, getWallItemResponseModel.
                            wall_item.tagged_users.get(0).image_url);
                    values.put(DatabaseColumns.TAGGED_USER_IDS, getWallItemResponseModel.
                            wall_item.tagged_users.get(0).user_id);
                    values.put(DatabaseColumns.TAGGED_IDS, getWallItemResponseModel.
                            wall_item.tagged_users.get(0).id);
                }


                String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                DBInterface
                        .updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, getTaskTag(),
                                values, TableWallComments.NAME, values, selection,
                                new String[]{getWallItemResponseModel.wall_item.id}, true, this);

                ContentValues valueTagCount = new ContentValues();

                valueTagCount.put(DatabaseColumns.TAG_USER_COUNT, mTagCount + 1);

                DBInterface
                        .updateAsync(AppConstants.QueryTokens.UPDATE_TAG_COUNT, getTaskTag(),
                                values, TableWallPosts.NAME, values, selection,
                                new String[]{mWallId}, true, this);

                break;

            }
            default:
                break;
        }
    }

    @Override
    public void failure(int requestId, int errorCode, String message) {

    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {


    }

    @Override
    public void performQuery(SearchView searchView, String query) {

        //mSearchString = query;
        getContacts(getActivity().getContentResolver(), query);
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        getContacts(getActivity().getContentResolver(), "");
        return true;
    }

    @Override
    public void onContactClicked(View view, Cursor cursor, int position) {

//cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))

        ContentResolver cr = getActivity().getContentResolver();

        String contactId = cursor.getString(cursor
                .getColumnIndex(ContactsContract.Data.CONTACT_ID));

        String selection = ContactsContract.Contacts._ID + SQLConstants.EQUALS_ARG;
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                selection, new String[]{contactId}, null);
        cur.moveToFirst();
        if (Integer
                .parseInt(cur.getString(cur
                        .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
            // Query phone here. Covered next
            Cursor pCur = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                            + " = ?", new String[]{contactId}, null);
            while (pCur.moveToNext()) {
                // Do something with phones
//                String phoneNo = pCur
//                        .getString(pCur
//                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//
//                nameList.add(name); // Here you can list of contact.
//                phoneList.add(phoneNo); // Here you will get list of phone number.
//

                Cursor emailCur = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{contactId}, null);
                boolean emailAdded = false;
                while (emailCur.moveToNext()) {

                    if (emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)).contains("gmail")) {
                        mEmail = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        emailAdded = true;
                    }
                    if (!emailAdded) {
                        mEmail = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                    }

                }
                emailCur.close();
            }
        }
        String number = cursor.getString(cursor
                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1));

        showReferDialog(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI)),
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
               number,
                mEmail, cursor);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         /*
         * Makes search string into pattern and
         * stores it in the selection array
         */
        mSelectionArgs[0] = "%" + mSearchString + "%";
        // Starts the query
        return new CursorLoader(
                getActivity(),
                ContactsContract.Contacts.CONTENT_URI,
                PROJECTION,
                SELECTION,
                mSelectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == AppConstants.Loaders.LOAD_CONTACTS) {
            mContactsListAdapter.swapCursor(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AppConstants.Loaders.LOAD_CONTACTS) {
            mContactsListAdapter.swapCursor(null);
        }

    }
}
