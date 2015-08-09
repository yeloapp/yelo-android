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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.vinaysshenoy.okulus.OkulusImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import red.yelo.R;
import red.yelo.activities.AskFriendActivity;
import red.yelo.activities.ChatScreenActivity;
import red.yelo.activities.CloseWallActivity;
import red.yelo.activities.EditWallPostActivity;
import red.yelo.activities.HomeActivity;
import red.yelo.activities.TagUserActivity;
import red.yelo.activities.UserProfileActivity;
import red.yelo.activities.WallImageActivity;
import red.yelo.adapters.WallCommentsListAdapter;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableMyWallPosts;
import red.yelo.data.TableNotifications;
import red.yelo.data.TableWallComments;
import red.yelo.data.TableWallPosts;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.request.CloseWallRequestModel;
import red.yelo.retromodels.request.Comments;
import red.yelo.retromodels.request.ReportAbuseRequestModel;
import red.yelo.retromodels.response.GetCreateWallResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.Logger;
import red.yelo.utils.Utils;
import red.yelo.widgets.CustomListView;


/**
 * Created by anshul1235 on 15/07/14.
 */
public class WallPostFragment extends AbstractYeloFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, View.OnClickListener, DBInterface.AsyncDbQueryCallback, RetroCallback.RetroResponseListener, WallCommentsListAdapter.TagActionListener, PopupMenu.OnMenuItemClickListener, PopupMenu.OnDismissListener, AbsListView.OnScrollListener {

    public static final String TAG = "WallPostFragment";


    private CustomListView mCommentList;

    private WallCommentsListAdapter mWallCommentsListAdapter;

    private TextView mNameText, mMessageText, mDateText;

    private OkulusImageView mImageUser;

    private String mName, mMessage, mDateTime, mTags, mConnection, mWallId, mImageUserUrl, mUserId, mTagId, mWallImageUrl, mTagName;

    private ArrayList<String> mWallItemIds = new ArrayList<String>();

    private TextView mChatButton, mRecommendFriendButton, mGroupNameText, mSubCategoryNameText, mConnectCountText,
                     mCommentButton;

    private View mGradientView;

    private LinearLayout mAskFriendsLayout;

    private ImageView mWallImage;

    private boolean mFromNotifications, mFromTag, mIsClosed, mFromProfile;

    private ProgressBar mProgressWheel;

    private Toolbar mToolbar, mToolbarTransparent;

    private boolean mToolBarColorToggled,mCommented;

    private Bitmap mShareImage;

    private View mSendCommentLayout;

    private Button mSendCommentButton;

    private EditText mCommentText;

    private SimpleDateFormat mFormatter;

    private DateFormatter mMessageDateFormatter;




    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);

        final View contentView = inflater.inflate(R.layout.fragment_wall_post, container, false);

        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                AppConstants.WALL_DATE_FORMAT);

        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());

        View headerView = inflater.inflate(R.layout.layout_header_wall_post, null, false);

        getSetViewIds(contentView, headerView);
        setListeners();


        mCommentList.addHeaderView(headerView);
        mCommentList.setDrawingCacheEnabled(true);
        mWallCommentsListAdapter = new WallCommentsListAdapter(getActivity(), this);
        mCommentList.setAdapter(mWallCommentsListAdapter);
        mCommentList.setVerticalScrollBarEnabled(false);

        final Bundle args = getArguments();

        if (args != null) {

            mWallId = args.getString(AppConstants.Keys.ID);
            mFromProfile = args.getBoolean(AppConstants.Keys.FROM_PROFILE);
            if (args.containsKey(AppConstants.Keys.FROM_TAG)) {
                mFromTag = args.getBoolean(AppConstants.Keys.FROM_TAG);
            }
            if (args.containsKey(AppConstants.Keys.FROM_NOTIFICATIONS)) {
                mFromNotifications = args.getBoolean(AppConstants.Keys.FROM_NOTIFICATIONS);
                if (mFromNotifications) {

                    ContentValues values = new ContentValues();
                    values.put(DatabaseColumns.NOTIFICATION_STATUS, AppConstants.NotificationStatus.READ);
                    String selection = DatabaseColumns.WALL_ID + SQLConstants.EQUALS_ARG;

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_NOTIFICATION_STATUS, getTaskTag(), null, TableNotifications.NAME, values, selection, new String[]{mWallId}, false, this);

                }
            }

            if(args.containsKey(AppConstants.Keys.COMMENT)){
                mSendCommentLayout.setVisibility(View.VISIBLE);
            }


        }

        startLoaders();

        if (args.containsKey(AppConstants.Keys.FROM_NOTIFICATIONS)) {
            fetchWall(mWallId);
        } else if (mFromTag) {
        } else {
            fetchWall(mWallId);
        }

        //mApi.getWallPost(mWallId,this);

        return contentView;

    }


    /**
     * This sets all the UI ids for the fragment
     *
     * @param contentView main fragment view
     * @param headerView  header view for the listView
     */
    private void getSetViewIds(View contentView, View headerView) {
        mChatButton = (TextView) contentView.findViewById(R.id.connect_button);
        mRecommendFriendButton = (TextView) contentView.findViewById(R.id.recommend_button);
        mNameText = (TextView) headerView.findViewById(R.id.text_user_name);
        mDateText = (TextView) headerView.findViewById(R.id.post_time);
        mMessageText = (TextView) headerView.findViewById(R.id.text_post_message);
        mImageUser = (OkulusImageView) headerView.findViewById(R.id.image_user);
        mWallImage = (ImageView) headerView.findViewById(R.id.wall_image);
        mProgressWheel = (ProgressBar) contentView.findViewById(R.id.progress_wheel);
        mToolbarTransparent = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar_transparent);
        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
        mGroupNameText = (TextView) headerView.findViewById(R.id.group_name);
        mSubCategoryNameText = (TextView) headerView.findViewById(R.id.subcategory_name);
        mConnectCountText = (TextView) headerView.findViewById(R.id.connect_count);
        mAskFriendsLayout = (LinearLayout) headerView.findViewById(R.id.ask_friends_layout);
        mCommentList = (CustomListView) contentView.findViewById(R.id.wall_post_list);
        mGradientView = headerView.findViewById(R.id.gradient_view);
        mSendCommentLayout = contentView.findViewById(R.id.comment_edit_layout);
        mSendCommentLayout.setVisibility(View.GONE);
        mSendCommentButton = (Button) contentView.findViewById(R.id.button_send);
        mCommentText = (EditText) contentView.findViewById(R.id.comment_edit);
        mCommentButton = (TextView) contentView.findViewById(R.id.comment_button);
    }


    /**
     * it sets all the listeners we are using in this fragment
     */
    private void setListeners() {
        mAskFriendsLayout.setOnClickListener(this);
        mChatButton.setOnClickListener(this);
        mRecommendFriendButton.setOnClickListener(this);
        mSendCommentButton.setOnClickListener(this);
        mCommentButton.setOnClickListener(this);
        mCommentText.setOnClickListener(this);
        mSendCommentButton.setEnabled(false);
        mCommentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s != null && !s.equals("") && !TextUtils.isEmpty(s)) {
                    mSendCommentButton.setEnabled(true);
                } else {
                    mSendCommentButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }


    /**
     * start the loaders for the fragment view
     */
    private void startLoaders() {
        loadWall();
        loadWallComments();
    }


    /**
     * loader which starts loading the wall comments for the wall
     */
    private void loadWallComments() {
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_WALL_COMMENTS, null, this);

    }


    /**
     * this loader loads the wall details
     */
    private void loadWall() {
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_WALL, null, this);
    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    /**
     * reference for the fragment to be called in any other fragment or pagers
     *
     * @return
     */
    public static WallPostFragment newInstance() {
        WallPostFragment f = new WallPostFragment();
        return f;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_WALL_COMMENTS) {

            String selection = DatabaseColumns.WALL_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), true, TableWallComments.NAME, null, selection,
                    new String[]{mWallId}, null, null, DatabaseColumns.TIMESTAMP_EPOCH + SQLConstants.ASCENDING, null);


        } else if (loaderId == AppConstants.Loaders.LOAD_WALL) {

            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

            if (mFromProfile) {
                return new SQLiteLoader(getActivity(), false, TableMyWallPosts.NAME, null, selection, new String[]{mWallId}, null, null, null, null);
            } else {
                return new SQLiteLoader(getActivity(), false, TableWallPosts.NAME, null, selection, new String[]{mWallId}, null, null, null, null);
            }
        } else {
            return null;
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_WALL_COMMENTS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {

                mWallCommentsListAdapter.swapCursor(cursor);
                int commentCount = 0;

                for (int i = mWallItemIds.size(); i < cursor.getCount(); i++) {
                    mWallItemIds.add(i, cursor.getColumnName(cursor.getColumnIndex(DatabaseColumns.ID)));

                }

                cursor.moveToFirst();
                for (int i=0;i<cursor.getCount();i++){
                    if(AppConstants.CommentType.COMMENTS.equals(cursor.getString(cursor.getColumnIndex(DatabaseColumns.TYPE)))){
                        commentCount = commentCount+1;
                    }
                    cursor.moveToNext();
                }

                mConnectCountText.setText("Connections (" + (cursor.getCount()-commentCount)  + ")"+
                         " Comments ("+ commentCount+")");

            }

            if(mCommented) {
                mCommentList.setSelection(mWallCommentsListAdapter.getCount() - 1);
                mCommented = false;
            }


        }

        if (loader.getId() == AppConstants.Loaders.LOAD_WALL) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {

                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    updateView(cursor);
                }
            }

        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AppConstants.Loaders.LOAD_WALL_COMMENTS) {
            mWallCommentsListAdapter.swapCursor(null);
        }
    }


    /**
     * updates the view for the header i.e wall details
     *
     * @param cursor includes the value set for updating the header UI
     */
    private void updateView(Cursor cursor) {

        mName = cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_NAME));
        mMessage = cursor.getString(cursor.getColumnIndex(DatabaseColumns.MESSAGE));
        mUserId = cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_ID));
        mDateTime = cursor.getString(cursor.getColumnIndex(DatabaseColumns.DATE_TIME));
        mTags = cursor.getString(cursor.getColumnIndex(DatabaseColumns.TAG_NAME));
        mTagId = cursor.getString(cursor.getColumnIndex(DatabaseColumns.TAG_ID));
        mConnection = cursor.getString(cursor.getColumnIndex(DatabaseColumns.TAG_USER_COUNT));
        mImageUserUrl = cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_IMAGE));
        mWallImageUrl = cursor.getString(cursor.getColumnIndex(DatabaseColumns.WALL_IMAGES));
        mTagName = cursor.getString(cursor.getColumnIndex(DatabaseColumns.TAG_NAME));

        mDateText.setText(Utils.getElapsedTimeFormat(Long.valueOf(cursor.getString(cursor.getColumnIndex(DatabaseColumns.TIMESTAMP_EPOCH))), cursor, getActivity()));
        mNameText.setText(mName);

        String groupName = cursor.getString(cursor.getColumnIndex(DatabaseColumns.GROUP_NAME));

        groupName = groupName.substring(0, 1).toUpperCase() + groupName.substring(1);

        mGroupNameText.setText(groupName);

        if (mTagName != null) {
            mTagName = mTagName.substring(0, 1).toUpperCase() + mTagName.substring(1);
            mSubCategoryNameText.setText(" > " + mTagName);
        } else {
            mSubCategoryNameText.setText("");
            mTagName = "";
        }


        if (cursor.getString(cursor.getColumnIndex(DatabaseColumns.ADDRESS)) == null) {

        } else {
            mDateText.setText(mDateText.getText().toString() + " at " + cursor.getString(cursor.getColumnIndex(DatabaseColumns.ADDRESS)));
        }


        mMessageText.setText(mMessage);
        mDateText.setVisibility(View.VISIBLE);

        if (mImageUserUrl.contains("assets/fallback/")) {
            mImageUserUrl = "";

        }
        mImageUser.setOnClickListener(this);


        if (mUserId.equals(AppConstants.UserInfo.INSTANCE.getId())) {

            mChatButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_action_tick_white), null, null, null);

            if (cursor.getString(cursor.getColumnIndex(DatabaseColumns.STATUS)).equals(AppConstants.WallStatus.OPEN)) {
                mIsClosed = false;
                mChatButton.setText(getResources().getString(R.string.close));
            } else {
                mIsClosed = true;
                mChatButton.setText(getResources().getString(R.string.closed));

            }

        }
        //set the image with default drawable
        ColorGenerator generator = ColorGenerator.DEFAULT;

        int color = generator.getColor((mName.charAt(0) + "").toUpperCase());
        Resources r = getActivity().getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

        TextDrawable drawable = TextDrawable.builder().buildRoundRect((mName.charAt(0) + "").toUpperCase(), color, Math.round(px));

        Utils.loadCircularImage(getActivity(), mImageUser, mImageUserUrl, AvatarBitmapTransformation.AvatarSize.NORMAL, drawable);

        if (mTags != null) {
            mTags = mTags.toUpperCase();
        }

        if (mWallImageUrl.equals("")) {
            mWallImage.setVisibility(View.GONE);
            mCommentList.setPadding(0, Math.round(Utils.convertDpToPixel(56, getActivity())), 0, 0);
            mToolbar.setBackgroundColor(getResources().getColor(R.color.primaryColor));
            mToolbar.setVisibility(View.VISIBLE);
            mGradientView.setVisibility(View.GONE);
            setToolbar(mToolbar);
            mCommentList.smoothScrollToPosition(0);


        } else {

            mToolbarTransparent.setTitleTextColor(getResources().getColor(R.color.white));
            mToolbarTransparent.setVisibility(View.VISIBLE);
            mCommentList.setOnScrollListener(this);
            mWallImage.setVisibility(View.VISIBLE);
            setToolbar(mToolbarTransparent, "Post", true);
            mGradientView.setVisibility(View.VISIBLE);

            Glide.with(this).load(mWallImageUrl).asBitmap().centerCrop().override(500, 300).animate(R.anim.fade_in).placeholder(R.color.snow_light).into(mWallImage);
        }

        getActivity().invalidateOptionsMenu();

    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        boolean handled = false;
        switch (menuItem.getItemId()) {


            case R.id.action_delete: {

                if (!TextUtils.isEmpty(mWallId)) {
                    deletePost(mWallId);
                }
                handled = true;
                break;
            }

            case R.id.action_share: {

                mShareImage = mCommentList.getDrawingCache();

                Utils.shareImageAsBitmap(mShareImage, getActivity(), "Check out new wall post if you can help" +
                        "on yelo android app \n" +
                        "-http://bit.ly/yelooo");
//                String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
//                DBInterface.queryAsync(AppConstants.QueryTokens.QUERY_WALL_DETAILS, getTaskTag(), null, true, TableWallPosts.NAME, null, selection, new String[]{mWallId}, null, null, null, null, this);
//                handled = true;
                break;
            }

            case R.id.action_report_abuse: {
                if (!TextUtils.isEmpty(mWallId)) {
                    reportAbuse(mWallId);
                }
                handled = true;
                break;
            }
        }
        return handled;
    }


    /**
     * Report abuse
     *
     * @param selectedWallId The id of the post to report
     */
    private void reportAbuse(final String selectedWallId) {

        ReportAbuseRequestModel reportAbuseRequestModel = new ReportAbuseRequestModel();
        reportAbuseRequestModel.setType(AppConstants.TYPE);
        reportAbuseRequestModel.setId(selectedWallId);

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.REPORT_ABUSE);
        retroCallbackList.add(retroCallback);

        mYeloApi.reportAbuse(reportAbuseRequestModel, retroCallback);
    }


    /**
     * Open a post for editing
     *
     * @param selectedWallId The id of the post to edit
     */
    private void editPost(final String selectedWallId) {

        final Intent editWallPostIntent = new Intent(getActivity(), EditWallPostActivity.class);
        editWallPostIntent.putExtra(AppConstants.Keys.EDIT_POST, true);
        editWallPostIntent.putExtra(AppConstants.Keys.WALL_ID, selectedWallId);

        startActivity(editWallPostIntent);
    }


    /**
     * Delete a post, along with a confirmation
     *
     * @param selectedWallId The id of the post to delete
     */
    private void deletePost(final String selectedWallId) {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title
        alertDialogBuilder.setTitle("Confirm");

        // set dialog message
        alertDialogBuilder.setMessage(getResources().getString(R.string.delete_wall_alert_message)).setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {

                callDeleteApi(selectedWallId);
                dialog.dismiss();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                // if this button is clicked, just close
                // the dialog box and do nothing
                dialog.cancel();
            }
        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    /**
     * Api to call for delete particular wall
     *
     * @param wallId id of the wall to be deleted
     */
    private void callDeleteApi(String wallId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.DELETE_WALL);
        retroCallbackList.add(retroCallback);

        mYeloApi.deleteWall(wallId, retroCallback);
        String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

        getActivity().setProgressBarIndeterminate(true);
        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_WALL_POST, getTaskTag(), null, TableWallPosts.NAME, selection, new String[]{wallId}, true, this);

        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_MY_WALL_POST, getTaskTag(), null, TableMyWallPosts.NAME, selection, new String[]{wallId}, true, this);

    }


    private void fetchWall(String wallId) {

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_PARTICULAR_WALL);
        retroCallbackList.add(retroCallback);

        mProgressWheel.setVisibility(View.VISIBLE);
        mYeloApi.getWallPost(wallId, retroCallback);

    }


    private void fetchWallComments(String wallId) {

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_WALL_COMMENTS);
        retroCallbackList.add(retroCallback);

        mProgressWheel.setVisibility(View.VISIBLE);
        mYeloApi.getWallPostComments(wallId, retroCallback);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connect_button) {
            if (mChatButton.getText().toString().equals(getResources().getString(R.string.chat))) {
                loadChat(mUserId, mWallId, mName, mTagId, mImageUserUrl, mTagName);
            } else {
                if (!mIsClosed) {
                    if (isAttached()) {

                        if (Integer.parseInt(mConnection) > 0) {
                            final Intent closeWallActivity = new Intent(getActivity(), CloseWallActivity.class);
                            closeWallActivity.putExtra(AppConstants.Keys.WALL_ID, mWallId);
                            startActivity(closeWallActivity);
                        } else {
                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                            // set title
                            alertDialogBuilder.setTitle(getString(R.string.confirm));

                            // set dialog message
                            alertDialogBuilder.setMessage(getResources().getString(R.string.close_wall_alert_message)).setCancelable(false).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int id) {

                                    closeWall(mWallId);
                                    dialog.dismiss();
                                }
                            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int id) {
                                    // if this button is clicked, just close
                                    // the dialog box and do nothing
                                    dialog.cancel();
                                }
                            });

                            // create alert dialog
                            final AlertDialog alertDialog = alertDialogBuilder.create();

                            // show it
                            alertDialog.show();
                        }
                    }
                }
            }
        } else if (v.getId() == R.id.recommend_button) {
            //showTagUserDialog();
            //TODO
            loadTagFragment();
        } else if (v.getId() == R.id.image_user) {
            loadProfile(mUserId, mName);
        } else if (v.getId() == R.id.ask_friends_layout) {
            loadAskFriend();
        }
        else if(v.getId() == R.id.button_send) {
            //mSendCommentLayout.setVisibility(View.GONE);
            final String sentAt = mFormatter.format(new Date());
            String tempId = "";
            try {
                tempId = mMessageDateFormatter.getEpoch(sentAt) + "";
            } catch (ParseException e) {
                //should not happen
                e.printStackTrace();
            }


            String message = mCommentText.getText().toString().trim();

            RetroCallback retroCallback;
            retroCallback = new RetroCallback(this);
            retroCallback.setRequestId(HttpConstants.ApiResponseCodes.COMMENT);

            Bundle mArgs = new Bundle();
            mArgs.putString(AppConstants.Keys.WALL_ID, mWallId);
            mArgs.putString(AppConstants.Keys.USER_ID, mUserId);
            mArgs.putString(AppConstants.Keys.TEMP_ID, tempId);

            retroCallback.setExtras(mArgs);

            retroCallbackList.add(retroCallback);


            Map<String,String> params = new HashMap<String,String>();

            params.put(HttpConstants.COMMENT_MESSAGE,message);
            mYeloApi.commentOnWall(mWallId, params, retroCallback);

            mCommentText.setText("");
            mCommented = true;

            addCommentLocally(message, tempId);
        }
        else if(v.getId() == R.id.comment_button) {
            if(mSendCommentLayout.getVisibility() == View.VISIBLE){

                mSendCommentLayout.setVisibility(View.GONE);
            }
            else {
                mSendCommentLayout.setVisibility(View.VISIBLE);
            }
        }
        else if(v.getId() == R.id.comment_edit){
            mCommentList.setSelection(mWallCommentsListAdapter.getCount() - 1);
        }
    }


    /**
     * Api call to close the wall
     *
     * @param wallId id of the wall to be closed
     */
    private void closeWall(String wallId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.CLOSE_WALL);
        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.WALL_ID, wallId);
        retroCallback.setExtras(args);
        retroCallbackList.add(retroCallback);

        CloseWallRequestModel closeWallRequestModel = new CloseWallRequestModel();
        closeWallRequestModel.setIs_solved("1");
        getActivity().finish();
        mYeloApi.closeWall(wallId, closeWallRequestModel, retroCallback);


    }


    /**
     * Loads the user profile
     *
     * @param userId id of the user whose profile we want to see
     * @param name   name of the user
     */
    private void loadProfile(String userId, String name) {
        final Intent userProfileIntent = new Intent(getActivity(), UserProfileActivity.class);

        userProfileIntent.putExtra(AppConstants.Keys.USER_ID, userId);
        userProfileIntent.putExtra(AppConstants.Keys.USER_NAME, name);
        userProfileIntent.putExtra(AppConstants.Keys.SERVICE_SCREEN_TYPE, AppConstants.ServiceScreenType.PROFILE);
        startActivity(userProfileIntent);
    }


    /**
     * Loads the actual chat screen. This is used in the case where the user taps on an item in the
     * list of chats
     *
     * @param userId The user Id of the chat to load
     * @param wallId The ID of the wallId
     */
    private void loadChat(String userId, String wallId, String chatName, String tags, String image, String tagName) {

        final String chatId = Utils.generateChatId(userId, AppConstants.UserInfo.INSTANCE.getId());
        final Intent chatScreenActivity = new Intent(getActivity(), ChatScreenActivity.class);
        chatScreenActivity.putExtra(AppConstants.Keys.USER_ID, userId);
        chatScreenActivity.putExtra(AppConstants.Keys.CHAT_ID, chatId);
        chatScreenActivity.putExtra(AppConstants.Keys.WALL_ID, wallId);
        chatScreenActivity.putExtra(AppConstants.Keys.CHAT_TITLE, chatName);
        chatScreenActivity.putExtra(AppConstants.Keys.PROFILE_IMAGE, image);
        chatScreenActivity.putExtra(AppConstants.Keys.FROM_WALL, true);
        chatScreenActivity.putExtra(AppConstants.Keys.TAG_NAME, tagName);


        chatScreenActivity.putExtra(AppConstants.Keys.MY_ID, AppConstants.UserInfo.INSTANCE.getId());
        startActivity(chatScreenActivity);
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }


    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

        if (taskId == AppConstants.QueryTokens.DELETE_WALL_POST) {
            getActivity().finish();
        }
    }


    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {
        if (taskId == AppConstants.QueryTokens.UPDATE_WALLCOMMENTS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLCOMMENT, getTaskTag(), null, TableWallComments.NAME, null, values, true, this);
            }
        } else if (taskId == AppConstants.QueryTokens.UPDATE_WALLPOST) {
            if (updateCount == 0) {


                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, getTaskTag(), null, TableWallPosts.NAME, null, values, true, this);
            }
        }
    }


    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

        if (taskId == AppConstants.QueryTokens.QUERY_WALL_DETAILS) {

            if (cursor.moveToFirst()) {
                shareWall(cursor.getString(cursor.getColumnIndex(DatabaseColumns.MESSAGE)));
            }
            cursor.close();

        }
    }


    /**
     * Show fragment for tagging users
     */
    public void loadTagFragment() {

        final Intent tagUserActivityIntent = new Intent(getActivity(), TagUserActivity.class);
        tagUserActivityIntent.putExtra(AppConstants.Keys.WALL_ID, mWallId);
        tagUserActivityIntent.putExtra(AppConstants.Keys.USER_ID, mUserId);
        tagUserActivityIntent.putExtra(AppConstants.Keys.TAG_USER_COUNT, Integer.parseInt(mConnection));
        tagUserActivityIntent.putExtra(AppConstants.Keys.FROM_WALL, true);
        startActivity(tagUserActivityIntent);

    }


    /**
     * Show fragment for tagging users
     */
    public void loadAskFriend() {

        final Intent askFriendActivity = new Intent(getActivity(), AskFriendActivity.class);
        askFriendActivity.putExtra(AppConstants.Keys.WALL_ID, mWallId);
        askFriendActivity.putExtra(AppConstants.Keys.FROM_WALL, true);
        startActivity(askFriendActivity);

    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        int menuResId = 0;
        if (mUserId.equals(AppConstants.UserInfo.INSTANCE.getId()) && mWallImageUrl.equals("")) {
            menuResId = R.menu.wall_owner_options;
        } else if (!mUserId.equals(AppConstants.UserInfo.INSTANCE.getId()) && mWallImageUrl.equals("")) {
            menuResId = R.menu.wall_other_options;
        } else if (mUserId.equals(AppConstants.UserInfo.INSTANCE.getId()) && !mWallImageUrl.equals("")) {
            menuResId = R.menu.wall_owner_options_white_options;
        } else if (!mUserId.equals(AppConstants.UserInfo.INSTANCE.getId()) && !mWallImageUrl.equals("")) {
            menuResId = R.menu.wall_other_options_white_share;
        }

        inflater.inflate(menuResId, menu);

    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                if (mFromNotifications) {
                    getActivity().finish();
                    loadHomeScreen();
                } else {
                    getActivity().finish();
                }

                return true;
            }


            case R.id.action_edit_post: {

                final Intent wallPostIntent = new Intent(getActivity(), EditWallPostActivity.class);
                wallPostIntent.putExtra(AppConstants.Keys.ID, mWallId);
                wallPostIntent.putExtra(AppConstants.Keys.EDIT_POST, true);
                startActivity(wallPostIntent);
                return true;

            }

            case R.id.action_delete: {

                if (!TextUtils.isEmpty(mWallId)) {
                    deletePost(mWallId);
                }
                return true;

            }

            case R.id.action_share: {

                mShareImage = mCommentList.getDrawingCache();
                Bitmap iconTop = BitmapFactory.decodeResource(getActivity().getResources(),
                        R.drawable.ic_logo_top);

                Bitmap iconLauncher = BitmapFactory.decodeResource(getActivity().getResources(),
                        R.drawable.ic_launcher);


                mShareImage = putOverlayRight(getActivity(), mShareImage, iconTop);

                if (TextUtils.isEmpty(mWallImageUrl)) {
                    mShareImage = putOverlayLeft(getActivity(), mShareImage, iconLauncher);
                }
                Utils.shareImageAsBitmap(mShareImage, getActivity(), "To connect on this post - get yelo android app \n" +
                        "- http://bit.ly/yelooo");
//                String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
//                DBInterface.queryAsync(AppConstants.QueryTokens.QUERY_WALL_DETAILS, getTaskTag(), null, true, TableWallPosts.NAME, null, selection, new String[]{mWallId}, null, null, null, null, this);
                return true;

            }

            case R.id.action_report_abuse: {
                if (!TextUtils.isEmpty(mWallId)) {
                    reportAbuse(mWallId);
                }
                return true;

            }


            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }


    /**
     * Loads the {@link red.yelo.fragments.HomeScreenFragment} into the fragment container
     */
    public void loadHomeScreen() {

        final Intent homeActivity = new Intent(getActivity(), HomeActivity.class);
        startActivity(homeActivity);


    }


    @Override
    public boolean onBackPressed() {
        if (mFromNotifications) {
            getActivity().finish();
            loadHomeScreen();
        } else {
            getActivity().finish();
        }
        return true;
    }


    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {

            /*
             *  parsing is done in RetroCallback Class
             */

            case HttpConstants.ApiResponseCodes.GET_PARTICULAR_WALL: {

                mProgressWheel.setVisibility(View.INVISIBLE);
                GetCreateWallResponseModel wallResponseModel = ((GetCreateWallResponseModel) model);

                if (wallResponseModel == null) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_wall_not_present), Toast.LENGTH_LONG).show();

                } else {
                    fetchWallComments(mWallId);
                }
                break;
            }

            case HttpConstants.ApiResponseCodes.GET_WALL_COMMENTS: {

                mProgressWheel.setVisibility(View.INVISIBLE);
                break;
            }

            case HttpConstants.ApiResponseCodes.REPORT_ABUSE: {
                Toast.makeText(getActivity(), getResources().getString(R.string.report), Toast.LENGTH_SHORT).show();
                break;
            }

            default:
                break;
        }
    }


    @Override
    public void failure(int requestId, int errorCode, String message) {

        showNetworkNotAvailableMessage(message);
        mProgressWheel.setVisibility(View.GONE);
    }


    @Override
    public void onPause() {
        super.onPause();
        mProgressWheel.setVisibility(View.GONE);
        cancelAllCallbacks(retroCallbackList);
    }


    @Override
    public void onShareClicked(View view, String userName, String category) {

        shareTag(userName, mSubCategoryNameText.getText().toString());
    }


    @Override
    public void onWallCommentClicked(View view, String user1Name, String user2Name, String user1Id, String user2Id, boolean isOwner, String number, String imageUrl, boolean isPresent, String type) {
        showCommentListOptions(user1Name, user2Name, user1Id, user2Id, isOwner, number, imageUrl, isPresent, type);
    }


    @Override
    public void onDismiss(PopupMenu popupMenu) {

    }


    /**
     * Method to handle click on comment
     */
    private void showCommentListOptions(final String user1Name, final String user2Name, final String user1Id, final String user2Id, final boolean isOwner, final String number, final String imageUrl, final boolean isPresent, final String type) {

        String[] options;

        options = selectOptionStringArray(user1Id, user2Id, user1Name, user2Name, isOwner, isPresent, type);

        new MaterialDialog.Builder(getActivity()).items(options).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                if (!isPresent && isOwner) {
                    if (which == 0) { // view user1
                        callNumber(number, user1Id, user1Name, "");
                    } else if (which == 1) { //view user2
                        loadProfile(user2Id, user2Name);
                    }
                } else if (!isPresent && !isOwner) {
                    if (which == 0) { //view user2
                        loadProfile(user2Id, user2Name);
                    }
                } else if (type.equals(AppConstants.CommentType.CHAT)) {
                    if (which == 0) { // view user1
                        loadProfile(user1Id, user1Name);
                    } else if (which == 1) { //view user2
                        loadProfile(user2Id, user2Name);
                    } else if (which == 2) { // message user1
                        loadChatFromComment(user1Id, user1Name, "", imageUrl, AppConstants.UserInfo.INSTANCE.getId(), AppConstants.UserInfo.INSTANCE.getFirstName(), "");
                    }
                } else {
                    if (which == 0) { // view user1
                        loadProfile(user1Id, user1Name);
                    } else if (which == 1) { //view user2
                        loadProfile(user2Id, user2Name);

                    } else if (which == 2) { // call user1
                        callNumber(number, user1Id, user1Name, "");
                    } else if (which == 3) { // message user1
                        loadChatFromComment(user1Id, user1Name, "", imageUrl, AppConstants.UserInfo.INSTANCE.getId(), AppConstants.UserInfo.INSTANCE.getFirstName(), "");
                    }
                }


            }
        }).show();

    }


    /**
     * Call the number of the user
     *
     * @param number
     * @param userId
     * @param userName
     * @param category
     */
    private void callNumber(String number, String userId, String userName, String category) {


        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
    }


    /**
     * Loads a chat directly. This is used in the case where the user directly taps on a chat button
     * on another user's profile page
     */

    private void loadChatFromComment(String taggedUserId, String chatName, String tags, String image, String userId, String userName, String category) {
        final String chatId = Utils.generateChatId(taggedUserId, AppConstants.UserInfo.INSTANCE.getId());

        loadChat(taggedUserId, chatId, chatName, tags, image);
    }


    /**
     * Loads the actual chat screen. This is used in the case where the user taps on an item in the
     * list of chats
     *
     * @param userId The user Id of the chat to load
     * @param chatId The ID of the chat
     */
    private void loadChat(String userId, String chatId, String chatName, String tags, String image) {

        final Intent chatScreenActivity = new Intent(getActivity(), ChatScreenActivity.class);
        chatScreenActivity.putExtra(AppConstants.Keys.USER_ID, userId);
        chatScreenActivity.putExtra(AppConstants.Keys.CHAT_ID, chatId);
        chatScreenActivity.putExtra(AppConstants.Keys.CHAT_TITLE, chatName);
        chatScreenActivity.putExtra(AppConstants.Keys.PROFILE_IMAGE, image);
        chatScreenActivity.putExtra(AppConstants.Keys.FROM_WALL, true);

        chatScreenActivity.putExtra(AppConstants.Keys.MY_ID, AppConstants.UserInfo.INSTANCE.getId());
        getActivity().startActivity(chatScreenActivity);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {


        if (mCommentList.computeVerticalScrollOffset() >= 30) {
            if (!mToolBarColorToggled) {
                colorizeView(getResources().getColor(R.color.primaryColor), mToolbarTransparent);
                mToolBarColorToggled = true;
            }

        } else {
            //colorizeView(getResources().getColor(R.color.transparent), mToolbar);

            if (mToolBarColorToggled) {
                if (Build.VERSION.SDK_INT >= 16) {
                    mToolbarTransparent.setBackground(getResources().getDrawable(R.drawable.transparent));
                } else {
                    mToolbarTransparent.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));

                }
                mToolBarColorToggled = false;
            }


        }

    }


    /**
     * Selects the list of options to be shown to the user in the dialog
     *
     * @param user1Id
     * @param user2Id
     * @param user1Name
     * @param user2Name
     * @param isOwner
     * @param isPresent
     * @param type
     * @return
     */
    private String[] selectOptionStringArray(String user1Id, String user2Id, String user1Name, String user2Name, boolean isOwner, boolean isPresent, String type) {

        String[] options;

        if (type.equals(AppConstants.CommentType.COMMENTS)) {
            options = new String[]{"View " + user1Name + "'s profile"};
        }
        else if (user1Id.equals(user2Id)) {
            if (isOwner) {
                options = new String[]{"View " + user1Name + "'s profile", "Call " + user1Name, "Message " + user1Name};
            } else {
                options = new String[]{"View " + user1Name + "'s profile"};

            }

            if (!isPresent && isOwner) {
                options = new String[]{"Call " + user1Name, "View " + user1Name + "'s profile"};
            } else if (!isPresent && !isOwner) {
                options = new String[]{"View " + user2Name + "'s profile"};
            }


            if (type.equals(AppConstants.CommentType.CHAT)) {
                options = new String[]{"View " + user1Name + "'s profile", "Message " + user1Name};
            }
        } else {
            if (isOwner) {
                options = new String[]{"View " + user1Name + "'s profile", "View " + user2Name + "'s profile", "Call " + user1Name, "Message " + user1Name};
            } else {
                options = new String[]{"View " + user1Name + "'s profile", "View " + user2Name + "'s profile"};

            }

            if (!isPresent && isOwner) {
                options = new String[]{"Call " + user1Name, "View " + user2Name + "'s profile"};
            } else if (!isPresent && !isOwner) {
                options = new String[]{"View " + user2Name + "'s profile"};
            }


            if (type.equals(AppConstants.CommentType.CHAT) && isOwner) {
                options = new String[]{"View " + user1Name + "'s profile", "View " + user2Name + "'s profile", "Message " + user1Name};
            } else if (type.equals(AppConstants.CommentType.CHAT) && !isOwner) {
                options = new String[]{"View " + user1Name + "'s profile", "View " + user2Name + "'s profile"};
            }
        }

        return options;
    }

    public Bitmap putOverlayRight(Activity activity, Bitmap bitmap, Bitmap overlay) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        float height = 0;
        float width = 0;

        if (TextUtils.isEmpty(mWallImageUrl)) {
            height = Utils.convertDpToPixel(10, getActivity());
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            int widthScreen = display.getWidth();
            width = widthScreen - Utils.convertDpToPixel(50, getActivity());

        } else {
            height = mWallImage.getHeight() - Utils.convertDpToPixel(40, getActivity());
            width = mWallImage.getWidth() - Utils.convertDpToPixel(50, getActivity());
        }
        canvas.drawBitmap(overlay, width, height, paint);

        return bitmap;
    }

    public Bitmap putOverlayLeft(Activity activity, Bitmap bitmap, Bitmap overlay) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        float height = 0;
        float width = 0;

        height = Utils.convertDpToPixel(10, getActivity());
        width = Utils.convertDpToPixel(16, getActivity());

        canvas.drawBitmap(overlay, width, height, paint);

        return bitmap;
    }


    private void addCommentLocally(String comment,String tempId) {

        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());
        final String sentAt = mFormatter.format(new Date());


        MixpanelAnalytics.getInstance().onContactReferred();
        ContentValues values = new ContentValues();
        values.put(DatabaseColumns.WALL_ID, mWallId);
        values.put(DatabaseColumns.TEMP_ID, tempId);
        values.put(DatabaseColumns.COMMENT, comment);
        values.put(DatabaseColumns.STATE, AppConstants.SyncStates.SYNCED);
        values.put(DatabaseColumns.IS_PRESENT, "true");
        values.put(DatabaseColumns.WALL_USER_ID, mUserId);
        values.put(DatabaseColumns.TYPE, AppConstants.CommentType.COMMENTS);
        values.put(DatabaseColumns.USER_ID, AppConstants.UserInfo.INSTANCE.getId());
        values.put(DatabaseColumns.TAGGED_USER_IDS, AppConstants.UserInfo.INSTANCE.getId());
        values.put(DatabaseColumns.TAGGED_NAMES, AppConstants.UserInfo.INSTANCE.getFirstName());
        values.put(DatabaseColumns.TAGGED_IMAGE_URLS, AppConstants.UserInfo.INSTANCE.getProfilePicture());

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

}
