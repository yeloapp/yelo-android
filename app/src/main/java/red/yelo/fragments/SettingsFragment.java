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

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.text.TextUtils;

import red.yelo.R;
import red.yelo.utils.SharedPreferenceHelper;


/**
 * Fragment for displaying App Settings
 * <p/>
 * Created by vinaysshenoy on 10/11/14.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    /**
     * Reference to ringtone preference to set selected notification ringtone when changed
     */
    private RingtonePreference mChatRingtonePreference;

    /**
     * Reference to ringtone preference to set selected notification ringtone when changed */
    private RingtonePreference mOtherRingtonePreference;

    //private ListPreference mLocationCityChangePreference;

    /**
     * Chat ringtone preference key
     */
    private String mChatRingtoneKey;

    /**
     * Other ringtone preference key */
    private String mOtherRingtoneKey;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        mChatRingtoneKey = getString(R.string.pref_chat_ringtone);
        mChatRingtonePreference = (RingtonePreference) findPreference(mChatRingtoneKey);
       // mLocationCityChangePreference = (ListPreference) findPreference(getActivity().getResources().getString(R.string.pref_location_settings));
        //mLocationCityChangePreference.setEntries();
        mChatRingtonePreference.setOnPreferenceChangeListener(this);
        //mLocationCityChangePreference.setOnPreferenceChangeListener(this);
        updateRingtonePreferenceSummary(mChatRingtonePreference, SharedPreferenceHelper
                .getString(R.string.pref_chat_ringtone));

        //updateLocationPreference(mLocationCityChangePreference,SharedPreferenceHelper.getString(R.string.pref_location_settings));

        mOtherRingtoneKey = getString(R.string.pref_other_ringtone);
        mOtherRingtonePreference = (RingtonePreference) findPreference(mOtherRingtoneKey);
        mOtherRingtonePreference.setOnPreferenceChangeListener(this);
        updateRingtonePreferenceSummary(mOtherRingtonePreference, SharedPreferenceHelper
                .getString(R.string.pref_other_ringtone));


    }

    /**
     * Sets the summary of the Ringtone Preference to the human readable name of the selected
     * ringtone
     *
     * @param ringtonePreference The preference to update
     * @param selectedRingtoneUriString The String version of the selected ringtone Uri
     */
    private void updateRingtonePreferenceSummary(final RingtonePreference ringtonePreference, final String selectedRingtoneUriString) {

        if (!TextUtils.isEmpty(selectedRingtoneUriString)) {
            final Ringtone selectedRingtone = RingtoneManager
                    .getRingtone(getActivity(), Uri.parse(selectedRingtoneUriString));

            if (selectedRingtone == null) {
                ringtonePreference.setSummary(null);
            } else {
                ringtonePreference.setSummary(selectedRingtone.getTitle(getActivity()));
            }
        } else {
            ringtonePreference.setSummary(null);
        }

    }

    private void updateLocationPreference(final ListPreference listPreference, final String selectedCity) {

        if (!TextUtils.isEmpty(selectedCity)) {
//            final Ringtone selectedRingtone = RingtoneManager
//                    .getRingtone(getActivity(), Uri.parse(selectedCity));

            if (selectedCity == null) {
                listPreference.setSummary(null);
            } else {
                listPreference.setSummary(selectedCity);
            }
        } else {
            listPreference.setSummary(null);
        }

    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object o) {

        if (preference.getKey().equals(mChatRingtoneKey) || preference.getKey().equals(mOtherRingtoneKey)) {
            updateRingtonePreferenceSummary((RingtonePreference) preference, (String) o);
        }
//        if(preference.getKey().equals(getActivity().getResources().getString(R.string.pref_location_settings))){
//            updateLocationPreference((ListPreference)preference,(String) o);
//        }

        return true;
    }
}
