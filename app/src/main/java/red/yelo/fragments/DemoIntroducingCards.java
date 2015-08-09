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

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import red.yelo.R;
import red.yelo.activities.CreateCardActivity;
import red.yelo.activities.HomeActivity;
import red.yelo.adapters.CollectionListAdapter;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableCollections;
import red.yelo.http.RetroCallback;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class DemoIntroducingCards extends AbstractYeloFragment implements
        DBInterface.AsyncDbQueryCallback, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
        , RetroCallback.RetroResponseListener, View.OnClickListener {

    public static final String TAG = "DemoIntroducingCards";


    private String mLatitude = "", mLongitude = "", mTagNameAdded;

    private LinearLayout mScreenLayout;

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private boolean mIsUpdate, mChangeScreen;

    private Toolbar mToolbar;

    private TextView mSkipButton;

    private ImageView mNextButton;


    private ListView mCardListView;
    private CollectionListAdapter mCollectionListAdapter;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_introducing_cards, container, false);


        mCardListView = (ListView) contentView.findViewById(R.id.demo_card_list);
        mSkipButton = (TextView) contentView.findViewById(R.id.skip);
        mNextButton = (ImageView) contentView.findViewById(R.id.next);
        mSkipButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mCollectionListAdapter = new CollectionListAdapter(getActivity());
        mCardListView.setAdapter(mCollectionListAdapter);
        loadCollections();
        int[] materialColors = getActivity().getResources().getIntArray(R.array.collectionListColors);


        String[] heading = new String[]{"tech", "design", "health", "hobbies", "photography"};
        String[] subHeading = new String[]{"android, web developer, nodejs ...",
                "interior, graphic, UI/UX, fashion",
                "personal trainer, yoga, swimming ...",
                "painting, sketching, sculpture, ...",
                "fashion, wedding portfolio event ..."
        };


        for (int i = 0; i < materialColors.length-1; i++) {
            ContentValues values = new ContentValues();

            values.put(DatabaseColumns.ID, i + "");
            values.put(DatabaseColumns.HEADING, heading[i]);
            values.put(DatabaseColumns.SUB_HEADING, subHeading[i]);
            values.put(DatabaseColumns.COLOR, i + "");

            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

            DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_COLLECTIONS, getTaskTag(), values
                    , TableCollections.NAME, values, selection, new String[]{i + ""}, true, this);

        }


        return contentView;

    }

    private void loadCollections() {
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_COLLECTIONS, null, this);

    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static DemoIntroducingCards newInstance(Bundle args) {
        DemoIntroducingCards f = new DemoIntroducingCards();
        f.setArguments(args);

        return f;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_COLLECTIONS) {


            return new SQLiteLoader(getActivity(), true, TableCollections.NAME, null,
                    null, null, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_COLLECTIONS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {
                mCollectionListAdapter.swapCursor(cursor);

            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        if (loader.getId() == AppConstants.Loaders.LOAD_COLLECTIONS) {
        }
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.skip) {

            SharedPreferenceHelper.set(R.string.pref_registration_screen, AppConstants.REGISTRATION_DONE);
            getActivity().finish();
            final Intent homeActivityIntent = new Intent(getActivity(),
                    HomeActivity.class);
            startActivityForResult(homeActivityIntent, AppConstants.RequestCodes.HOME);

        } else if (v.getId() == R.id.next) {
            getActivity().finish();

            SharedPreferenceHelper.set(R.string.pref_registration_screen, AppConstants.REGISTRATION_DONE);

            final Intent createCard = new Intent(getActivity(),
                    CreateCardActivity.class);
            createCard.putExtra(AppConstants.Keys.FROM_LOGIN,true);
            startActivity(createCard);
        }

    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {
        if (taskId == AppConstants.QueryTokens.UPDATE_COLLECTIONS) {
            if (updateCount == 0) {
                ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_COLLECTIONS,
                        getTaskTag(), null, TableCollections.NAME, null, values, true, this);
            }

        }
    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // performTagClickOperation(position, view);

    }

//    private void performTagClickOperation(int position, View view) {
//
//        Tags tag = mTagAdapter.getItem(position);
//        tag.toggleChecked();
//        TagsArrayAdapter.TagsViewHolder viewHolder = (TagsArrayAdapter.TagsViewHolder)
//                view.getTag();
//
//        if (tag.isChecked()) {
//            //viewHolder.getTextView().setTextColor(getResources().getColor(R.color.grass_primary));
//            //viewHolder.getTextView().setBackgroundResource(R.drawable.tag_background_select);
//        } else {
//            //viewHolder.getTextView().setTextColor(getResources().getColor(R.color.tag_text));
//            //viewHolder.getTextView().setBackgroundResource(R.drawable.tag_background);
//        }
//
//        if (tag.isChecked()) {
//
//            if (!mTextTruncate.equals("")) {
//                mTagSuggestions.getText().delete(mTagSuggestions.getText().length() - mTextTruncate.length(),
//                        mTagSuggestions.getText().length());
//                mTextTruncate = "";
//            }
//            mTagSuggestions.addObject(tag);
//            if (!tag.getId().equals(AppConstants.NO_ID)) {
//                mTagsIds.add(getTagIdFromTagName(tag.toString()));
//            }
//        } else {
//            for (int i = 0; i < mTagsIds.size(); i++) {
//                if (tag.toString().equals(getTagNameFromTagId(mTagsIds.get(i)))) {
//                    mTagsIds.remove(i);
//                    mTagSuggestions.removeObject(tag);
//
//                }
//            }
//        }
//    }


    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
    }

    @Override
    public void success(Object model, int requestId) {

    }

    @Override
    public void failure(int requestId, int errorCode, String message) {

    }
}
