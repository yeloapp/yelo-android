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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.vinaysshenoy.okulus.OkulusImageView;

import java.util.ArrayList;
import java.util.List;

import red.yelo.R;
import red.yelo.activities.ChatScreenActivity;
import red.yelo.activities.CreateServiceCardActivity;
import red.yelo.activities.UserProfileActivity;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.TableServices;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.Logger;
import red.yelo.utils.Utils;

/**
 * Created by anshul1235 on 24/10/14.
 */
public class ExpandedServiceCardFragment extends AbstractYeloFragment implements
        DBInterface.AsyncDbQueryCallback, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
        , RetroCallback.RetroResponseListener ,View.OnClickListener{
    public static final String TAG = "ExpandedServiceCardFragment";

    public static final int EDIT_SERVICE_CARD = 1;
    public static final int REQUEST_SHARE_ACTION = 1;



    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private ImageView mServiceImage;

    private OkulusImageView mUserImage;

    private Button mBookButton;

    private TextView mTitleText,mDescription,mGroupName,mSubcategoryName,mServiceCost,mDuration,mDeliverables,
    mBookMessage,mRatingCount;

    private  RatingBar mRatingBar;

    private String mUserId,mContactNumber,mCardId,mTitle,mTagName,mUserName,mPrice,mUserImageUrl,
    mDurationInfo,mDeliverableInfo,mServiceImageUrl,mDescriptionString,mGroupNameString,mGroupId,mSubCategoryId,
    mBookCount,mViewCount;


    private Toolbar mToolbar;

    private View mSendDialogView,mLayoutExpandedCard;

    private Bitmap mShareImage;

    private String localAbsoluteFilePath;



    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        final View contentView = inflater
                .inflate(R.layout.fragment_service, container, false);


        initialiseViews(contentView,savedInstanceState);
        setValues(getArguments());
        setListeners();






        return contentView;

    }


    private void initialiseViews(View contentView,Bundle savedInstanceState){

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
        mServiceImage = (ImageView) contentView.findViewById(R.id.service_card_image);
        mUserImage = (OkulusImageView) contentView.findViewById(R.id.image_user);
        mBookButton = (Button) contentView.findViewById(R.id.book_button);
        mTitleText = (TextView) contentView.findViewById(R.id.service_card_title);
        mDescription = (TextView) contentView.findViewById(R.id.description_text);
        mGroupName = (TextView) contentView.findViewById(R.id.group_name);
        mSubcategoryName = (TextView) contentView.findViewById(R.id.subcategory_name);
        mServiceCost = (TextView) contentView.findViewById(R.id.cost_value);
        mDuration = (TextView) contentView.findViewById(R.id.duration_value);
        mDeliverables = (TextView) contentView.findViewById(R.id.deliverable_values);
        mBookMessage = (TextView) contentView.findViewById(R.id.book_message);
        mLayoutExpandedCard = contentView.findViewById(R.id.layout_expanded_card);
        mRatingCount = (TextView) contentView.findViewById(R.id.rating_count);
        mRatingBar = (RatingBar) contentView.findViewById(R.id.ratingBar2);

        mLayoutExpandedCard.setDrawingCacheEnabled(true);




    }

    private void setValues(Bundle extras){

        if(extras!=null){

            mServiceImageUrl = extras.getString(AppConstants.Keys.SERVICE_IMAGE);
            mUserImageUrl = extras.getString(AppConstants.Keys.USER_IMAGE);

            mCardId = extras.getString(AppConstants.Keys.SERVICE_ID);
            mTitle = extras.getString(AppConstants.Keys.TITLE);
            mDescriptionString = extras.getString(AppConstants.Keys.DESCRIPTION);
            mPrice = extras.getString(AppConstants.Keys.PRICE);
            mGroupNameString = extras.getString(AppConstants.Keys.GROUP_NAME);
            mTagName = extras.getString(AppConstants.Keys.SUBCATEGORY_NAME);
            mUserId = extras.getString(AppConstants.Keys.USER_ID);
            mUserName = extras.getString(AppConstants.Keys.USER_NAME);
            mContactNumber = extras.getString(AppConstants.Keys.CONTACT_NUMBER);
            mDeliverableInfo = extras.getString(AppConstants.Keys.DELIVERABLE);
            mDurationInfo = extras.getString(AppConstants.Keys.DURATION);
            mGroupId = extras.getString(AppConstants.Keys.GROUP_ID);
            mSubCategoryId = extras.getString(AppConstants.Keys.SUBCATEGORY_ID);

            String rating,ratingCount;
            rating = extras.getString(AppConstants.Keys.RATING);
            ratingCount = extras.getString(AppConstants.Keys.RATING_COUNT);

            mRatingCount.setText("(" + ratingCount + ")");
            if(!TextUtils.isEmpty(rating))
                mRatingBar.setRating(Float.parseFloat(rating));



            mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
            setToolbar(mToolbar, mUserName, true);


            //set service image
            Glide.with(getActivity())
                    .load(mServiceImageUrl)
                    .asBitmap()
                    .centerCrop()
                    .animate(R.anim.fade_in)
                    .placeholder(R.color.snow_light)
                    .into(mServiceImage);

            //set the image with default drawable
            ColorGenerator generator = ColorGenerator.DEFAULT;

            int color = generator.getColor((mUserName.charAt(0) + "").toUpperCase());
            Resources r = getActivity().getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r.getDisplayMetrics());

            TextDrawable drawable = TextDrawable.builder().buildRoundRect((mUserName.charAt(0) + "").
                    toUpperCase(), color, Math.round(px));

            Utils.loadCircularImage(getActivity(), mUserImage, mUserImageUrl, AvatarBitmapTransformation.AvatarSize.BIG, drawable);

            //set values
            mTitleText.setText(mTitle);
            mDescription.setText(mDescriptionString);

            if(mUserId
                    .equals(AppConstants.UserInfo.INSTANCE.getId())){
                mBookButton.setVisibility(View.GONE);
                mBookMessage.setVisibility(View.GONE);
                setHasOptionsMenu(true);

            }
            else {
                mBookButton.setVisibility(View.VISIBLE);
                mBookButton.setText("Book");

            }
            mGroupName.setText(mGroupNameString.toUpperCase());
            mSubcategoryName.setText(" > "+mTagName.toUpperCase());

            mServiceCost.setText("\u20B9"+mPrice);
            mDeliverables.setText(mDeliverableInfo);
            mDuration.setText(mDurationInfo);




        }

        //

    }


    private void setListeners(){
        mBookButton.setOnClickListener(this);
        mUserImage.setOnClickListener(this);

        mYeloApi.viewedServicePing(mCardId,this);
    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static ExpandedServiceCardFragment newInstance(Bundle args) {
        ExpandedServiceCardFragment f = new ExpandedServiceCardFragment();
        f.setArguments(args);
        return f;
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {
    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {
    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
            return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_COLLECTIONS: {



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
        cancelAllCallbacks(retroCallbackList);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.book_button :

                bookRequest();

                break;

            case R.id.image_user :

                loadProfile(mUserId,mUserName);
                break;

        }
    }

    /**
     * Loads the actual chat screen. This is used in the case where the user taps on an item in the
     * list of chats
     */
    private void loadChat(String userId, String chatName, String image, String tagName, String contactNumber,
                          String price, String title,String message,String date) {

        final String chatId = Utils.generateChatId(userId, AppConstants.UserInfo.INSTANCE.getId());

        if (getActivity() != null) {

            final Intent chatScreenActivity = new Intent(getActivity(), ChatScreenActivity.class);
            chatScreenActivity.putExtra(AppConstants.Keys.USER_ID, userId);
            chatScreenActivity.putExtra(AppConstants.Keys.CHAT_ID, chatId);
            chatScreenActivity.putExtra(AppConstants.Keys.CHAT_TITLE, chatName);
            chatScreenActivity.putExtra(AppConstants.Keys.PROFILE_IMAGE, image);
            chatScreenActivity.putExtra(AppConstants.Keys.SERVICE_ID, mCardId);
            chatScreenActivity.putExtra(AppConstants.Keys.FROM_WALL, true);
            chatScreenActivity.putExtra(AppConstants.Keys.MESSAGE, message);
            chatScreenActivity.putExtra(AppConstants.Keys.DATE_TIME, date);
            chatScreenActivity.putExtra(AppConstants.Keys.TAG_NAME, tagName);
            chatScreenActivity.putExtra(AppConstants.Keys.CONTACT_NUMBER, contactNumber);
            chatScreenActivity.putExtra(AppConstants.Keys.SERVICE_PRICE, price);
            chatScreenActivity.putExtra(AppConstants.Keys.TITLE, title);
            chatScreenActivity.putExtra(AppConstants.Keys.MY_ID, AppConstants.UserInfo.INSTANCE.getId());

            startActivity(chatScreenActivity);
        }
    }


    private void bookRequest() {


        boolean wrapInScrollView = true;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mSendDialogView = inflater
                .inflate(R.layout.dialog_sendmessage_template, null);
        final String months[] = {"January", "February", "March", "April",
                "May", "June", "July", "August", "September",
                "October", "November", "December"};
        final EditText queryMessage = (EditText) mSendDialogView.findViewById(R.id.query_message);
        final EditText timeMessage = (EditText) mSendDialogView.findViewById(R.id.date);

        final DatePicker datePicker = (DatePicker) mSendDialogView.findViewById(R.id.datePicker);
        new MaterialDialog.Builder(getActivity())
                .title("Book request with date")
                .customView(mSendDialogView, wrapInScrollView)
                .positiveText("Send")
                .positiveColor(getResources().getColor(R.color.blue_link))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);


                        mYeloApi.bookService(mCardId,ExpandedServiceCardFragment.this);
                        loadChat(mUserId, mUserName, mUserImageUrl, mTagName, mContactNumber, mPrice, mTitle,
                                queryMessage.getText().toString().trim(), timeMessage.getText().toString());



                    }


                })
                .build()
                .show();


    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if(AppConstants.UserInfo.INSTANCE.getId().equals(mUserId)) {
            inflater.inflate(R.menu.service_owner_options, menu);
        }
        else {
            inflater.inflate(R.menu.share_option, menu);
        }

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                getActivity().finish();

                return true;
            }

            case R.id.action_share: {


                mShareImage =  mLayoutExpandedCard.getDrawingCache();

                //if the layout is large, i.e scrollable then we need to shrink the screenshot
                //to the screen size , below is the implementation for this
                //- Anshul
                if(mShareImage == null){

                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                    int width = display.getWidth();
                    int height = display.getHeight();

                    mLayoutExpandedCard.layout(0,0,width,
                            height);
                    mShareImage =  mLayoutExpandedCard.getDrawingCache();

                }

                Bitmap iconTop= BitmapFactory.decodeResource(getActivity().getResources(),
                        R.drawable.ic_logo_top);

                mShareImage = putOverlay(getActivity(),mShareImage,iconTop);
                Utils.shareImageAsBitmap(mShareImage, getActivity(), getResources().getString(R.string.share_service_card));


                return true;
            }


            case R.id.action_edit_post: {

                final Intent createService = new Intent(getActivity(), CreateServiceCardActivity.class);
                createService.putExtra(AppConstants.Keys.SERVICE_ID, mCardId);
                createService.putExtra(AppConstants.Keys.EDIT_SERVICE, true);
                createService.putExtra(AppConstants.Keys.SERVICE_IMAGE, mServiceImageUrl);
                createService.putExtra(AppConstants.Keys.USER_IMAGE, mUserImageUrl);
                createService.putExtra(AppConstants.Keys.TITLE, mTitle);
                createService.putExtra(AppConstants.Keys.DESCRIPTION, mDescriptionString);
                createService.putExtra(AppConstants.Keys.PRICE, mPrice);
                createService.putExtra(AppConstants.Keys.GROUP_NAME, mGroupNameString);
                createService.putExtra(AppConstants.Keys.GROUP_ID, mGroupId);
                createService.putExtra(AppConstants.Keys.SUBCATEGORY_ID, mSubCategoryId);
                createService.putExtra(AppConstants.Keys.SUBCATEGORY_NAME, mTagName);
                createService.putExtra(AppConstants.Keys.USER_ID, mUserId);
                createService.putExtra(AppConstants.Keys.USER_NAME, mUserName);
                createService.putExtra(AppConstants.Keys.CONTACT_NUMBER, mContactNumber);
                createService.putExtra(AppConstants.Keys.DELIVERABLE, mDeliverableInfo);
                createService.putExtra(AppConstants.Keys.DURATION, mDurationInfo);

                startActivityForResult(createService,EDIT_SERVICE_CARD);
                return true;

            }

            case R.id.action_delete: {

                if (!TextUtils.isEmpty(mCardId)) {
                    deletePost(mCardId);
                }
                return true;

            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
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
        alertDialogBuilder.setMessage(getResources().getString(R.string.delete_service_alert_message))
                .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {

                callDeleteApi(mCardId);
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ExpandedServiceCardFragment.EDIT_SERVICE_CARD) {
            getActivity().finish();
        }

    }

    /**
     * Api to call for delete particular wall
     *
     * @param serviceId id of the wall to be deleted
     */
    private void callDeleteApi(String serviceId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.DELETE_SERVICE);
        retroCallbackList.add(retroCallback);

        mYeloApi.deleteService(serviceId, retroCallback);
        String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

        getActivity().setProgressBarIndeterminate(true);
        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_SERVICE_CARD, getTaskTag(), null,
                TableServices.NAME, selection, new String[]{serviceId}, true, this);


    }

    /**
     * This loads the profile of the user
     *
     * @param userId user id of the user u want to open profile of
     * @param name   name of the user
     */
    private void loadProfile(String userId, String name) {
        final Intent userProfileIntent = new Intent(getActivity(), UserProfileActivity.class);

        userProfileIntent.putExtra(AppConstants.Keys.USER_ID, userId);
        userProfileIntent.putExtra(AppConstants.Keys.USER_NAME, name);
        userProfileIntent.putExtra(AppConstants.Keys.SERVICE_SCREEN_TYPE, AppConstants.ServiceScreenType.PROFILE);
        startActivity(userProfileIntent);
    }

    public  Bitmap putOverlay(Activity activity,Bitmap bitmap, Bitmap overlay) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        float height = mServiceImage.getHeight() - Utils.convertDpToPixel(40, getActivity());
        float width = mServiceImage.getWidth() - Utils.convertDpToPixel(50,getActivity());
        Logger.d(TAG,mUserImage.getWidth()+"");
        canvas.drawBitmap(overlay, width, height, paint);

        return bitmap;
    }


}
