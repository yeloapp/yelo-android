//package red.yelo.fragments;
//
//import android.app.Activity;
//import android.content.ActivityNotFoundException;
//import android.content.ContentValues;
//import android.content.Context;
//import android.content.Intent;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.support.v4.app.LoaderManager;
//import android.support.v4.content.Loader;
//import android.support.v7.widget.CardView;
//import android.support.v7.widget.Toolbar;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.afollestad.materialdialogs.MaterialDialog;
//import com.soundcloud.android.crop.Crop;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URL;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Random;
//
//import red.yelo.R;
//import red.yelo.activities.SearchLocationActivity;
//import red.yelo.data.DBInterface;
//import red.yelo.data.DatabaseColumns;
//import red.yelo.data.SQLConstants;
//import red.yelo.data.SQLiteLoader;
//import red.yelo.data.TableSubCategories;
//import red.yelo.data.TableTags;
//import red.yelo.data.TableWallPosts;
//import red.yelo.fragments.dialogs.SingleChoiceDialogFragment;
//import red.yelo.http.HttpConstants;
//import red.yelo.http.RetroCallback;
//import red.yelo.http.WallPostIntentService;
//import red.yelo.retromodels.request.PostWallMessageRequestModel;
//import red.yelo.retromodels.response.GetCollectionResponseModel;
//import red.yelo.retromodels.response.GoogleGeocodeResponse;
//import red.yelo.retromodels.response.Tags;
//import red.yelo.retromodels.response.TagsRecommendationResponseModel;
//import red.yelo.retromodels.response.TagsSuggestionsResponseModel;
//import red.yelo.utils.AppConstants;
//import red.yelo.utils.DateFormatter;
//import red.yelo.utils.Logger;
//import red.yelo.utils.PhotoUtils;
//import red.yelo.utils.SharedPreferenceHelper;
//import red.yelo.widgets.MultiTagLayout;
//import red.yelo.widgets.ProgressWheel;
//import red.yelo.widgets.autocomplete.TokenCompleteTextView;
//import retrofit.Callback;
//import retrofit.mime.TypedFile;
//
///**
// * Created by anshul1235 on 15/07/14.
// */
//
////Depeciated not to be used! please refer to PostOnWallActivity.java
//public class PostOnWallFragment extends AbstractYeloFragment implements View.OnClickListener
//        , Callback,
//        LoaderManager.LoaderCallbacks<Cursor>,
//        DBInterface.AsyncDbQueryCallback, AdapterView.OnItemClickListener, TextWatcher,
//        TokenCompleteTextView.TokenListener, MultiTagLayout.OnTagClickListener,
//        RetroCallback.RetroResponseListener {
//
//    public static final String TAG = "PostOnWallFragment";
//
//    private EditText mMessageEdit;
//
//    private TextView mPlaceName;
//
//    private ProgressWheel mProgressWheel;
//
//    private CardView mCardView;
//
//    /**
//     * GridView into which the all tag suggesstions will be placed
//     */
//   /* private GridView mTagMultiselectList;
//
//    private TagsArrayAdapter mTagAdapter;*/
//
//
//    private MultiTagLayout mMultiTagLayout;
//
//    /**
//     * cursor to load the categories so as to get ids of each in onclick
//     */
//    private Cursor mCursor;
//
//    private String mLatitude, mLongitude, mTagIdSelected,mTagNameSelected;
//
//    private String[] mAddress;
//
//    /**
//     * Reference to the Dialog Fragment for selecting the picture type
//     */
//    private SingleChoiceDialogFragment mChoosePictureDialogFragment;
//
//
//    private static final int PICK_FROM_CAMERA = 1;
//    private static final int CROP_FROM_CAMERA = 2;
//    private static final int PICK_FROM_FILE = 3;
//    private static final int PIC_CROP = 4;
//
//
//
//    private String mWallImageFileName = AppConstants.WALL_IMAGE_NAME;
//
//    private Uri mCameraImageCaptureUri;
//
//
//    private SimpleDateFormat mFormatter;
//
//    private DateFormatter mMessageDateFormatter;
//
//    private Bitmap mCompressedPhoto;
//
//    private ImageView mWallImage;
//
//    private ImageView mAddImagePlaceholder;
//
//    private boolean mWasWallImageUploaded;
//
//    private File mWallImageFile;
//
//    private static final String ACTION_POST = "red.yelo.http.action.POST";
//
//    private Tags[] mTags;
//
//    private String mTextTruncate = "";
//
//    private String mPlace;
//
//    private List<String> mTagsIds = new ArrayList<String>();
//
//
//    /**
//     * list of callbacks to keep a record for cancelling in onPause
//     */
//    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();
//
//    private String[] mHintText;
//
//    private Toolbar mToolbar;
//
//
//    @Override
//    public View onCreateView(final LayoutInflater inflater,
//                             final ViewGroup container, final Bundle savedInstanceState) {
//        init(container, savedInstanceState);
//        final View contentView = inflater
//                .inflate(R.layout.fragment_query, container, false);
//
//        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
//        setToolbar(mToolbar);
//
//        setHasOptionsMenu(true);
//
//        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
//                AppConstants.WALL_DATE_FORMAT);
//
//        mMultiTagLayout = (MultiTagLayout) contentView.findViewById(R.id.tag_container);
//        mMultiTagLayout.setOnTagClickListener(this);
//        mMultiTagLayout.setShouldSelectOnClick(true);
//        mMultiTagLayout.setSelectionMode(MultiTagLayout.SelectionMode.SINGLE);
//
//        mPlaceName = (TextView) contentView.findViewById(R.id.text_location);
//        mMessageEdit = (EditText) contentView.findViewById(R.id.message);
//        mWallImage = (ImageView) contentView.findViewById(R.id.wall_image);
//        mAddImagePlaceholder = (ImageView) contentView.findViewById(R.id.gallery_ic);
//        mProgressWheel = (ProgressWheel) contentView.findViewById(R.id.progress_wheel);
//        mProgressWheel.setBarColor(getResources().getColor(R.color.primaryColorDark));
//
//        mPlaceName.setOnClickListener(this);
//
//
//        mHintText = getActivity().getResources().getStringArray(R.array.post_start_sample);
//
//        int idx = new Random().nextInt(mHintText.length);
//        String random = (mHintText[idx]);
//        mMessageEdit.setHint(random);
//       // mMessageEdit.setSelection(mMessageEdit.getText().length());
//
//
//        mLatitude = SharedPreferenceHelper.getString(R.string.pref_latitude);
//        mLongitude = SharedPreferenceHelper.getString(R.string.pref_longitude);
//
//        mCameraImageCaptureUri = Uri.fromFile(new File(Environment
//                .getExternalStorageDirectory(),
//                mWallImageFileName
//        ));
//
//        if(savedInstanceState!=null){
//            mMessageEdit.setHint(savedInstanceState.getString(AppConstants.Keys.MESSAGE));
//
//            mTagNameSelected = savedInstanceState.getString(AppConstants.Keys.TAG_ID);
//            mTagIdSelected = savedInstanceState.getString(AppConstants.Keys.TAG_NAME);
//            mMultiTagLayout.setTagSelected(mTagIdSelected,true);
//            mAddress = savedInstanceState.getStringArray(AppConstants.Keys.ADDRESS);
//            mPlaceName.setText(TextUtils.join(",", mAddress));
//        }
//
//        setFileName();
//        fillAddressDetails();
//        fetchGocs();
//        loadGroupGocs();
//
//        return contentView;
//    }
//
//
//
//    private void loadGroupGocs() {
//
//        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS, null, this);
//
//    }
//
//    private void fetchGocs() {
//        RetroCallback retroCallback;
//        retroCallback = new RetroCallback(this);
//        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_GROUPS);
//        retroCallbackList.add(retroCallback);
//
//        mYeloApi.getGocs(retroCallback);
//    }
//
//    private void loadSubCategories(String groupId) {
//
//        Bundle args = new Bundle();
//        args.putString(AppConstants.Keys.TAG_ID, groupId);
//        fetchCategories(groupId);
//        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_CATEGORIES, args, this);
//
//    }
//
//
//    private void fetchCategories(String groupId) {
//        RetroCallback retroCallback;
//        retroCallback = new RetroCallback(this);
//        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SUB_CATEGORIES);
//
//        Bundle args = new Bundle();
//        args.putString(AppConstants.Keys.TAG_ID, groupId);
//        retroCallback.setExtras(args);
//        retroCallbackList.add(retroCallback);
//
//        mYeloApi.getSubCategories(groupId, retroCallback);
//    }
//
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//
//        outState.putString(AppConstants.Keys.MESSAGE,mMessageEdit.getText().toString());
//        outState.putString(AppConstants.Keys.TAG_ID,mTagIdSelected);
//        outState.putString(AppConstants.Keys.TAG_NAME,mTagNameSelected);
//        outState.putStringArray(AppConstants.Keys.ADDRESS, mAddress);
//        super.onSaveInstanceState(outState);
//
//    }
//
//    private void loadSuggesstions() {
//
//        if (isAttached()) {
//            getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS, null, this);
//        }
//
//    }
//
//    public static PostOnWallFragment newInstance(Bundle categoryDetails) {
//        PostOnWallFragment f = new PostOnWallFragment();
//        f.setArguments(categoryDetails);
//        return f;
//    }
//
//    @Override
//    protected Object getTaskTag() {
//        return hashCode();
//    }
//
//    private void postToWall(String tagName, String tagIdSelected, String latitude, String longitude, String[] address,
//                            String message) {
//        PostWallMessageRequestModel postWallMessageRequestModel = new PostWallMessageRequestModel();
//
//        postWallMessageRequestModel.wall.setMessage(message);
//        if (tagIdSelected == null) {
//            Toast.makeText(getActivity(), getResources().getString(R.string.toast_select_tags), Toast.LENGTH_SHORT).show();
//
//        } else {
//            postWallMessageRequestModel.wall.setTag_id(tagIdSelected);
//        }
//        postWallMessageRequestModel.wall.setLatitude(latitude);
//        postWallMessageRequestModel.wall.setLongitude(longitude);
//
//        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
//                AppConstants.WALL_DATE_FORMAT);
//
//        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());
//
//        final String sentAt = mFormatter.format(new Date());
//        String tempId = "";
//        try {
//            tempId = mMessageDateFormatter.getEpoch(sentAt) + "";
//        } catch (ParseException e) {
//            //should not happen
//        }
//        postWallMessageRequestModel.wall.setTmp_id(tempId);
//
//        Logger.d(TAG, latitude, longitude);
//
//            if (mAddress.length > 0) {
//
//
//                if(mAddress.length>3) {
//                    postWallMessageRequestModel.wall.setCity(address[address.length - 3].trim());
//                    postWallMessageRequestModel.wall.setCountry(address[address.length - 1].trim());
//                    postWallMessageRequestModel.wall.setAddress(address[address.length - 4].trim());
//                }
//                else if(mAddress.length == 1){
//                    postWallMessageRequestModel.wall.setCity(address[0].trim());
//                    postWallMessageRequestModel.wall.setCountry("India");
//
//
//                }
//                RetroCallback retroCallback;
//                retroCallback = new RetroCallback(this);
//                retroCallback.setRequestId(HttpConstants.ApiResponseCodes.CREATE_WALL);
//                Bundle args = new Bundle();
//                args.putString(AppConstants.Keys.TEMP_ID, tempId);
//                retroCallback.setExtras(args);
//                retroCallbackList.add(retroCallback);
//
//                addWallLocally(tempId, tagName, tagIdSelected, latitude, longitude, address, message);
//                mYeloApi.postWallMessage(postWallMessageRequestModel, retroCallback);
//
//                getActivity().finish();
//
//        }else {
//            Toast.makeText(getActivity(), getResources().getString(R.string.toast_select_location), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void addWallLocally(String tempId, String tagName, String tagIdSelected, String latitude, String longitude, String[] address,
//                                String message) {
//
//        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
//                AppConstants.WALL_DATE_FORMAT);
//
//        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());
//
//        final String sentAt = mFormatter.format(new Date());
//
//
//        ContentValues valuesWall = new ContentValues(6);
//
//        valuesWall.put(DatabaseColumns.TEMP_ID, tempId);
//        valuesWall.put(DatabaseColumns.MESSAGE, message);
//        valuesWall.put(DatabaseColumns.TAG_NAME, tagName);
//        valuesWall.put(DatabaseColumns.STATE, AppConstants.SyncStates.SYNCING + "");
//        valuesWall.put(DatabaseColumns.TAG_ID, tagIdSelected);
//        valuesWall.put(DatabaseColumns.USER_NAME, AppConstants.UserInfo.INSTANCE.getFirstName());
//        valuesWall.put(DatabaseColumns.USER_ID, AppConstants.UserInfo.INSTANCE.getId());
//        try {
//            valuesWall.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(sentAt));
//            valuesWall.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(sentAt));
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//            //should not happen
//        }
//        valuesWall.put(DatabaseColumns.USER_IMAGE, AppConstants.UserInfo.INSTANCE.getProfilePicture());
//
//
//        DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, null, null
//                , TableWallPosts.NAME, null, valuesWall, true, this);
//
//    }
//
//    private void postToWallWithImage(String tagName, String tagIdSelected, String latitude, String longitude, String[] address,
//                                     String message, String imagepath) {
//
//
////        final Map<String, String> params = new HashMap<String, String>(8);
////        params.put(HttpConstants.POST_MESSAGE, message);
////        params.put(HttpConstants.POST_LATITUDE,latitude);
////        params.put(HttpConstants.POST_LONGITUDE,longitude);
////        params.put(HttpConstants.POST_TAG_ID,tagIdSelected);
//        if (tagIdSelected == null) {
//            Toast.makeText(getActivity(), getResources().getString(R.string.toast_select_tags), Toast.LENGTH_SHORT).show();
//
//        } else {
//
//            if(mAddress!=null) {
//                if (mAddress.length > 2) {
//                    postOnWallWithImage(getActivity(), tagIdSelected, latitude, longitude, address, message, imagepath);
//
//                } else {
//                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_select_location), Toast.LENGTH_SHORT).show();
//                }
//            }
//            else {
//                Toast.makeText(getActivity(), getResources().getString(R.string.toast_select_location), Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    /**
//     * Starts this service to perform action Foo with the given parameters. If
//     * the service is already performing a task this action will be queued.
//     *
//     * @see android.app.IntentService
//     */
//    public void postOnWallWithImage(Context context, String tagIdSelected, String latitude,
//                                    String longitude, String[] address,
//                                    String message, String imagepath) {
//        Intent intent = new Intent(context, WallPostIntentService.class);
//
//        intent.setAction(ACTION_POST);
//        intent.putExtra(HttpConstants.POST_MESSAGE, message);
//        intent.putExtra(HttpConstants.POST_LATITUDE, latitude);
//        intent.putExtra(HttpConstants.POST_LONGITUDE, longitude);
//        intent.putExtra(HttpConstants.POST_TAG_ID, tagIdSelected);
//        intent.putExtra(HttpConstants.POST_CITY, address[address.length - 3].trim());
//        if(mAddress.length>3) {
//
//            intent.putExtra(HttpConstants.POST_ADDRESS,address[address.length - 4].trim() );
//
//        }
//        intent.putExtra(HttpConstants.POST_COUNTRY, address[address.length - 1].trim());
//        intent.putExtra(AppConstants.WALL_IMAGE_NAME, imagepath);
//
//        context.startService(intent);
//
//        getActivity().finish();
//    }
//
//
//    @Override
//    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {
//
//    }
//
//    @Override
//    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {
//
//    }
//
//    @Override
//    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {
//
//        if (taskId == AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS) {
//            if (updateCount == 0) {
//                Logger.d(TAG, "insert");
//
//                final ContentValues values = (ContentValues) cookie;
//                DBInterface
//                        .insertAsync(AppConstants.QueryTokens.INSERT_TAG_SUGGESSTIONS,
//                                getTaskTag(), null, TableTags.NAME, null, values, true, this);
//
//            }
//        }
//
//    }
//
//    @Override
//    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {
//
//    }
//
//    @Override
//    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
//        if (loaderId == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {
//
//            return new SQLiteLoader(getActivity(), false, TableTags.NAME, null,
//                    null, null, null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);
//        } else if (loaderId == AppConstants.Loaders.LOAD_CATEGORIES) {
//
//            String categoryId = bundle.getString(AppConstants.Keys.TAG_ID);
//
//            String selection = DatabaseColumns.CATEGORY_ID + SQLConstants.EQUALS_ARG;
//            return new SQLiteLoader(getActivity(), false, TableSubCategories.NAME, null,
//                    selection, new String[]{categoryId}, null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);
//
//        }  else {
//            return null;
//        }
//
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//
//        if (loader.getId() == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {
//
//            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
//
//            mTags = new Tags[cursor.getCount()];
//            cursor.moveToFirst();
//            for (int i = 0; i < cursor.getCount(); i++) {
//
//                mTags[i] = new Tags(cursor.getString(
//                        cursor.getColumnIndex(DatabaseColumns.NAME)),
//                        cursor.getString(
//                                cursor.getColumnIndex(DatabaseColumns.ID)), "image_url");
//                cursor.moveToNext();
//                mProgressWheel.setVisibility(View.GONE);
//                mCardView.setVisibility(View.VISIBLE);
//            }
//
//            ArrayList<MultiTagLayout.Tag> tagList = new ArrayList<MultiTagLayout.Tag>(mTags.length);
//            //tagList.addAll(Arrays.asList(mTags));
//                /*mTagAdapter = new TagsArrayAdapter(getActivity(), R.layout.layout_tag_grid, tagList, true);
//
//                mTagMultiselectList.setAdapter(mTagAdapter);*/
//
//            for (Tags eachTag : mTags) {
//                tagList.add(new MultiTagLayout.Tag(eachTag.getId(), eachTag.getName()));
//            }
//
//            mMultiTagLayout.setTags(tagList);
//
//            /*ArrayList<Tags> tagList = new ArrayList<Tags>();
//            tagList.addAll(Arrays.asList(mTags));
//            mTagAdapter = new TagsArrayAdapter(getActivity(), R.layout.layout_tag_grid, tagList, true);
//
//            mTagMultiselectList.setAdapter(mTagAdapter);*/
//
//        }
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//        if (loader.getId() == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {
//        }
//    }
//
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//        //performTagClickOperation(position, view);
//
//    }
//
//    /*private void performTagClickOperation(int position, View view) {
//
//        Tags tag = mTagAdapter.getItem(position);
//        tag.toggleChecked();
//        TagsArrayAdapter.TagsViewHolder viewHolder = (TagsArrayAdapter.TagsViewHolder)
//                view.getTag();
//
//        if (tag.isChecked()) {
//            viewHolder.getTextView().setTextColor(getResources().getColor(R.color.grass_primary));
//            viewHolder.getTextView().setBackgroundResource(R.drawable.tag_background_select);
//        } else {
//            viewHolder.getTextView().setTextColor(getResources().getColor(R.color.tag_text));
//            viewHolder.getTextView().setBackgroundResource(R.drawable.tag_background);
//        }
//
//        if (tag.isChecked()) {
//
//            if (!mTextTruncate.equals("")) {
//                mSelectTagsSuggesstions.getText().delete(mSelectTagsSuggesstions.getText().length() - mTextTruncate.length(),
//                        mSelectTagsSuggesstions.getText().length());
//                mTextTruncate = "";
//            }
//            mSelectTagsSuggesstions.addObject(tag);
//            if (!tag.getId().equals(AppConstants.NO_ID)) {
//                mTagsIds.add(getTagIdFromTagName(tag.toString()));
//            }
//        } else {
//            for (int i = 0; i < mTagsIds.size(); i++) {
//                if (tag.toString().equals(getTagNameFromTagId(mTagsIds.get(i)))) {
//                    mTagsIds.remove(i);
//                    mSelectTagsSuggesstions.removeObject(tag);
//
//                }
//            }
//        }
//    }*/
//
//    @Override
//    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//    }
//
//    @Override
//    public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//        String[] split = s.toString().split(",");
//
//        if (split.length > 0) {
//            mTextTruncate = split[split.length - 1].trim();
//            if (mTextTruncate.length() > 0) {
//                fetchSuggestions(split[split.length - 1].trim());
//                loadSuggesstions();
//            }
//
//        } else {
//            mTextTruncate = s.toString().trim();
//            fetchSuggestions(s.toString().trim());
//
//        }
//        //mLocationSuggesstionAdapter.getFilter().filter(s.toString());
//    }
//
//    @Override
//    public void afterTextChanged(Editable s) {
//
//        if (s == null || s.length() == 0) {
//            loadSuggesstions();
//        }
//    }
//
//    private void fetchSuggestions(String q) {
//
//        final Map<String, String> params = new HashMap<String, String>(1);
//        params.put(HttpConstants.Q, q);
//
//        RetroCallback retroCallback;
//        retroCallback = new RetroCallback(this);
//        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_TAG_AUTO_SUGGESTIONS);
//        retroCallbackList.add(retroCallback);
//
//        mYeloApi.getTagSuggestions(params, retroCallback);
//
//    }
//
//    @Override
//    public void onClick(View v) {
//
//        if (v.getId() == R.id.select_tags_edits) {
//            //mPostLayout.setVisibility(View.GONE);
//            //mTagMultiselectList.setVisibility(View.VISIBLE);
//
//        } else if (v.getId() == R.id.text_location) {
//            final Intent selectLocationActivity = new Intent(getActivity(),
//                    SearchLocationActivity.class);
//
//            //selectLocationActivity.putExtra(AppConstants.Keys.PLACE,mPlaceName.getText().toString());
//            selectLocationActivity.putExtra(AppConstants.Keys.FROM_WALL, true);
//
//            startActivityForResult(selectLocationActivity, AppConstants.RequestCodes.GET_PLACE);
//
//        } else if (v.getId() == R.id.post_wall_message) {
//
//        }
//    }
//
//    /**
//     * Method to handle click on profile image
//     */
//    private void showChoosePictureSourceDialog() {
//
//        new MaterialDialog.Builder(getActivity())
//                .items(getResources().getStringArray(R.array.take_photo_choices_wall))
//                .itemsCallback(new MaterialDialog.ListCallback() {
//                    @Override
//                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//                        if (which == 0) { // Pick from camera
//                            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageCaptureUri);
//
//                            try {
//                                startActivityForResult(
//                                        Intent.createChooser(intent, getString(R.string.complete_action_using)),
//                                        PICK_FROM_CAMERA);
//                            } catch (final ActivityNotFoundException e) {
//                                e.printStackTrace();
//                            }
//
//                        } else if (which == 1) { // pick from file
//                            Intent intent = new Intent(Intent.ACTION_PICK,
//                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                            intent.setType("image/*");
//                            intent.setAction(Intent.ACTION_GET_CONTENT);
//                            startActivityForResult(
//                                    Intent.createChooser(intent, getString(R.string.complete_action_using)),
//                                    PICK_FROM_FILE);
//                        }
//
//                    }
//                })
//                .show();
//
//    }
//
//
//    private void fillAddressDetails() {
//
//        final Map<String, String> params = new HashMap<String, String>(6);
//        params.put(HttpConstants.LATLNG, SharedPreferenceHelper.getString(R.string.pref_latitude)
//                + "," + SharedPreferenceHelper.getString(R.string.pref_longitude));
//        params.put(HttpConstants.KEY, getResources().getString(R.string.google_api_key));
//        params.put(HttpConstants.RESULT_TYPE, HttpConstants.STREET_ADDRESS);
//
//        RetroCallback retroCallback;
//        retroCallback = new RetroCallback(this);
//        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE);
//        retroCallbackList.add(retroCallback);
//
//        mGoogleApi.getMyAddress(params, retroCallback);
//
//    }
//
//
//    @Override
//    public void onActivityResult(final int requestCode, final int resultCode,
//                                 final Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == Activity.RESULT_OK) {
//
//            switch (requestCode) {
//                case PICK_FROM_CAMERA:
//                    // doCrop(PICK_FROM_CAMERA);
//                    if (resultCode == Activity.RESULT_OK)
//                        new Crop(mCameraImageCaptureUri).output(mCameraImageCaptureUri).withAspect(600, 300).start(getActivity());
//
//                    // performCrop(mCameraImageCaptureUri);
//                    break;
//
//                case Crop.REQUEST_CROP:
//
//                    setAndSaveImage(mCameraImageCaptureUri, PICK_FROM_CAMERA);
//                    break;
//
//                case PIC_CROP: {
//                    if (data != null) {
//                        // get the returned data
//                        Bundle extras = data.getExtras();
//                        // get the cropped bitmap
//                        setAndSaveImage(mCameraImageCaptureUri, PICK_FROM_CAMERA);
//                        //mProfilePic.setImageBitmap(selectedBitmap);
//                    }
//                }
//                break;
//
//                case AppConstants.RequestCodes.GET_PLACE: {
//                    if (resultCode == Activity.RESULT_OK) {
//                        String place = data.getStringExtra(AppConstants.Keys.PLACE);
//                        mAddress = place.split(",");
//                        mLatitude = SharedPreferenceHelper.getString(R.string.pref_latitude);
//                        mLongitude = SharedPreferenceHelper.getString(R.string.pref_longitude);
//                        mPlaceName.setText(place);
//
//                        Logger.d(TAG, "location manually saved");
//                        if (place.equals(AppConstants.MY_LOCATION)) {
//                            if (isAttached())
//                                fillAddressDetails();
//                            // TODO Update your TextView.
//                        }
//                        //
//                        break;
//                    }
//                }
//
//                case PICK_FROM_FILE:
//                    if (data != null) {
//                        Uri selectedImage = data.getData();
//                        final String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
//                        Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
//                        // some devices (OS versions return an URI of com.android instead of com.google.android
//                        if (selectedImage.toString().startsWith("content://com.android.gallery3d.provider")) {
//                            // use the com.google provider, not the com.android provider.
//                            selectedImage = Uri.parse(selectedImage.toString().replace("com.android.gallery3d", "com.google.android.gallery3d"));
//                        }
//                        if (cursor != null) {
//                            cursor.moveToFirst();
//                            int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
//                            // if it is a picasa image on newer devices with OS 3.0 and up
//                            //Logger.d(TAG,selectedImage.toString()+"SOMETHING");
//                            if (selectedImage.toString().startsWith("content://com.google.android.apps") ||
//                                    selectedImage.toString().startsWith("content://com.google.android.gallery3d")) {
////                            Logger.d(TAG,selectedImage.toString()+"SOMETHING");
//                                columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
//                                if (columnIndex != -1) {
//                                    final Uri uriurl = selectedImage;
////                                Logger.d(TAG,selectedImage.toString());
//                                    // Do this in a background thread, since we are fetching a large image from the web
//
//
//                                    new Thread(new Runnable() {
//                                        public void run() {
//
//                                            mCompressedPhoto = getBitmap(mWallImageFileName, uriurl);
//                                            getActivity().runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    mWasWallImageUploaded = true;
//                                                    mCompressedPhoto = PhotoUtils.compressManageAspect(500, 500, mCompressedPhoto);
//                                                    mWallImage.setImageBitmap(mCompressedPhoto);
//                                                    mAddImagePlaceholder.setVisibility(View.GONE);
//                                                    PhotoUtils.saveImage(PhotoUtils.compressManageAspect(500, 500, mCompressedPhoto), mWallImageFileName);
//                                                }
//                                            });
//                                        }
//                                    }).start();
//
//
//                                }
//                            } else { // it is a regular local image file
//                                Uri mGalleryImageCaptureUri = data.getData();
//                                //setAndSaveImage(mGalleryImageCaptureUri, PICK_FROM_FILE);
//                                //performCrop(mGalleryImageCaptureUri);
//                                new Crop(mGalleryImageCaptureUri).output(mCameraImageCaptureUri).withAspect(600, 300).start(getActivity());
//                                // doCrop(PICK_FROM_FILE);
//
//
//                            }
//                        }
//
//                        break;
//                    }
//            }
//        }
//    }
//
//    private Bitmap getBitmap(String tag, Uri url) {
//        File cacheDir;
//        // if the device has an SD card
//        cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), ".OCFL311");
//
//        if (!cacheDir.exists())
//            cacheDir.mkdirs();
//
//        File f = new File(cacheDir, tag);
//
//        try {
//            Bitmap bitmap = null;
//            InputStream is = null;
//            if (url.toString().startsWith("content://com.google.android.apps") ||
//                    url.toString().startsWith("content://com.google.android.gallery3d")) {
//                is = getActivity().getContentResolver().openInputStream(url);
//            } else {
//                is = new URL(url.toString()).openStream();
//            }
//            OutputStream os = new FileOutputStream(f);
//            byte[] buffer = new byte[512];
//            int len;
//            while ((len = is.read(buffer)) != -1) {
//                os.write(buffer, 0, len);
//            }
//            os.close();
//            return BitmapFactory.decodeFile(f.getAbsolutePath());
//        } catch (Exception ex) {
//            // something went wrong
//            ex.printStackTrace();
//            return null;
//        }
//    }
//
//
//    /**
//     * Set the Profile Image and Save it locally
//     *
//     * @param uri             URI of the image to be saved.
//     * @param source_of_image If the image was from Gallery or Camera
//     */
//
//    private void setAndSaveImage(final Uri uri, final int source_of_image) {
//        String source_string;
//        if (source_of_image == PICK_FROM_FILE) {
//            source_string = "Gallery";
//        } else {
//            source_string = "Camera";
//        }
//
//        Uri mPhotoUri = uri;
//
//            mCompressedPhoto = PhotoUtils
//                    .rotateBitmapIfNeededAndCompressIfTold(getActivity(), uri, source_string, false);
//
//
//            if (mCompressedPhoto != null) {
//
//                mWallImage.setImageBitmap(mCompressedPhoto);
//                mAddImagePlaceholder.setVisibility(View.GONE);
//                PhotoUtils.saveImage(PhotoUtils.compressManageAspect(500, 500, mCompressedPhoto), mWallImageFileName);
//
//            }
//            mWasWallImageUploaded = true;
//
//    }
//
//
//    private void setFileName() {
//
//        mWallImageFile = new File(Environment.getExternalStorageDirectory(), mWallImageFileName);
//    }
//
//    @Override
//    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
//        inflater.inflate(R.menu.post_wall_options, menu);
//
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(final MenuItem item) {
//        switch (item.getItemId()) {
//
//
//            case R.id.action_post: {
//
//
//                if (mMessageEdit.getText().toString().trim().equals("Put me in touch with")) {
//                    Toast.makeText(getActivity(), getResources().getString(R.string.please_write_your_query), Toast.LENGTH_SHORT).show();
//
//                }
////                else if(validatePostMessage(mMessageEdit.getText().toString()).equals(AppConstants.MessageType.PHONE_PRESENT)){
////                    Toast.makeText(getActivity(), getResources().getString(R.string.no_phone_number_message), Toast.LENGTH_SHORT).show();
////
////                }
//                else
//                {
//                    if (mTagNameSelected != null) {
//                        if (mAddress != null) {
//                            if (mWasWallImageUploaded) {
//                                TypedFile typedFile;
//                                File photo;
//                                photo = new File(mWallImageFile.getAbsolutePath());
//                                typedFile = new TypedFile("application/octet-stream", photo);
//
//                                postToWallWithImage(mTagNameSelected, mTagIdSelected, mLatitude, mLongitude, mAddress,
//                                        mMessageEdit.getText().toString(), mWallImageFile.getAbsolutePath());
//
//                            } else {
//                                postToWall(mTagNameSelected, mTagIdSelected, mLatitude, mLongitude, mAddress, mMessageEdit.getText().toString());
//                            }
//                        } else {
//                            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_location), Toast.LENGTH_SHORT).show();
//
//                        }
//                    } else {
//                        Toast.makeText(getActivity(), getResources().getString(R.string.please_select_tags_message), Toast.LENGTH_SHORT).show();
//                    }
//
//
//                    return true;
//                }
//            }
//
//            default: {
//                return super.onOptionsItemSelected(item);
//            }
//        }
//    }
//
//    @Override
//    public void onTokenAdded(Object token) {
//        MultiTagLayout.Tag tag = (MultiTagLayout.Tag) token;
//        mMultiTagLayout.setTagSelected(tag.id, true);
//        mTagsIds.add(tag.id);
//    }
//
//    @Override
//    public void onTokenRemoved(Object token) {
//
//        MultiTagLayout.Tag tag = (MultiTagLayout.Tag) token;
//        mMultiTagLayout.setTagSelected(tag.id, false);
//
//    }
//
//    private void fetchTagSuggestions() {
//
//        RetroCallback retroCallback;
//        retroCallback = new RetroCallback(this);
//        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_TAG_SUGGESTIONS);
//        retroCallbackList.add(retroCallback);
//
//        mYeloApi.getTagRecommendations(retroCallback);
//
//        mProgressWheel.setVisibility(View.VISIBLE);
//        mProgressWheel.spin();
//
//    }
//
//    @Override
//    public void onTagClicked(View view, MultiTagLayout.Tag tag) {
//
//        mTagIdSelected = tag.id;
//        mTagNameSelected = tag.name;
//    }
//
//    @Override
//    public void success(Object model, int requestId) {
//        switch (requestId) {
//
//            case HttpConstants.ApiResponseCodes.GET_GROUPS: {
//
//                GetCollectionResponseModel getCollectionResponseModel = ((GetCollectionResponseModel) model);
//
//                for (GetCollectionResponseModel.Collection eachEntry : getCollectionResponseModel.groups) {
//
//                    ContentValues values = new ContentValues();
//                    values.put(DatabaseColumns.ID, eachEntry.id);
//                    values.put(DatabaseColumns.NAME, eachEntry.name);
//
//                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
//
//                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS, getTaskTag(), values,
//                            TableTags.NAME, values, selection, new String[]{eachEntry.id}, true, this);
//
//                }
//
//                break;
//
//
//            }
//
//            case HttpConstants.ApiResponseCodes.GET_TAG_AUTO_SUGGESTIONS: {
//                getActivity().setProgressBarIndeterminateVisibility(false);
//
//
//                TagsSuggestionsResponseModel tagsSuggestionsResponseModel = ((TagsSuggestionsResponseModel) model);
//                mTags = new Tags[tagsSuggestionsResponseModel.tags.size()];
//
//                for (int i = 0; i < tagsSuggestionsResponseModel.tags.size(); i++) {
//
//                    mTags[i] = new Tags(tagsSuggestionsResponseModel.tags.get(i).name,
//                            tagsSuggestionsResponseModel.tags.get(i).id, "image_url");
//
//
//                    ContentValues values = new ContentValues();
//                    values.put(DatabaseColumns.ID, tagsSuggestionsResponseModel.tags.get(i).id);
//                    values.put(DatabaseColumns.NAME, tagsSuggestionsResponseModel.tags.get(i).name);
//
//
//                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
//
//                    Logger.d(TAG, "UPDATE");
//
//                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS, getTaskTag(), values,
//                            TableTags.NAME, values, selection, new String[]{tagsSuggestionsResponseModel.tags.get(i).id}, true, this);
//
//
//                }
//                /*ArrayList<MultiTagLayout.Tag> tagList = new ArrayList<MultiTagLayout.Tag>(mTags.length);
//                //tagList.addAll(Arrays.asList(mTags));
//                *//*mTagAdapter = new TagsArrayAdapter(getActivity(), R.layout.layout_tag_grid, tagList, true);
//
//                mTagMultiselectList.setAdapter(mTagAdapter);*//*
//
//                for(Tags eachTag : mTags) {
//                    tagList.add(new MultiTagLayout.Tag(eachTag.getId(), eachTag.getName()));
//                }*/
//
//                break;
//            }
//            case HttpConstants.ApiResponseCodes.GET_TAG_SUGGESTIONS: {
//                TagsRecommendationResponseModel tagsRecommendationResponseModel = ((TagsRecommendationResponseModel) model);
//
//
//                mProgressWheel.setVisibility(View.INVISIBLE);
//                for (int i = 0; i < tagsRecommendationResponseModel.tags.size(); i++) {
//
//                    ContentValues values = new ContentValues();
//                    values.put(DatabaseColumns.ID, tagsRecommendationResponseModel.tags.get(i).id);
//                    values.put(DatabaseColumns.NAME, tagsRecommendationResponseModel.tags.get(i).name);
//
//
//                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
//
//                    Logger.d(TAG, "UPDATE");
//
//                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS, getTaskTag(), values,
//                            TableTags.NAME, values, selection, new String[]{tagsRecommendationResponseModel.tags.get(i).id}, true, this);
//
//
//                }
//                for (int i = 0; i < tagsRecommendationResponseModel.user_tags.size(); i++) {
//                    ContentValues values = new ContentValues();
//                    values.put(DatabaseColumns.ID, tagsRecommendationResponseModel.user_tags.get(i).id);
//                    values.put(DatabaseColumns.NAME, tagsRecommendationResponseModel.user_tags.get(i).name);
//
//
//                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
//
//
//                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS, getTaskTag(), values,
//                            TableTags.NAME, values, selection, new String[]{tagsRecommendationResponseModel.user_tags.get(i).id}, true, this);
//
//
//                }
//
//                break;
//            }
//            case HttpConstants.ApiResponseCodes.CREATE_WALL: {
//
//                Toast.makeText(getActivity(), "Wall Posted", Toast.LENGTH_SHORT).show();
//
//                // all the db changes has been moved to RetroCallback
//                // as I am instantly closing this fragment as a response
//
//                break;
//            }
//            case HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE: {
//
//                GoogleGeocodeResponse googleGeocodeResponse = ((GoogleGeocodeResponse) model);
//                if (googleGeocodeResponse.results.size() != 0) {
//                    mAddress = googleGeocodeResponse.results.get(0).getAddress();
//                    mPlaceName.setText(TextUtils.join(",", mAddress));
//                }
//
//                break;
//            }
//            default:
//                break;
//        }
//    }
//
//    @Override
//    public void failure(int requestId, int errorCode, String message) {
//
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//
//
//        for (RetroCallback aRetroCallbackList : retroCallbackList) {
//            if (aRetroCallbackList.getRequestId() != HttpConstants.ApiResponseCodes.CREATE_WALL)
//                aRetroCallbackList.cancel();
//        }
//    }
//
//    private void performCrop(Uri picUri) {
////        try {
////            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(600,300,600, 300, Uri.fromFile(mWallImageFile));
////            cropImage.setOutputQuality(100);
////            cropImage.setSourceImage(picUri);
////            startActivityForResult(cropImage.getIntent(getActivity()), PIC_CROP);
////        }
////        // respond to users whose devices do not support the crop action
////        catch (ActivityNotFoundException anfe) {
////            // display an error message
////            String errorMessage = "Whoops - your device doesn't support the crop action!";
////            Toast toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
////            toast.show();
////        }
//    }
//
//    private String validatePostMessage(String message){
//
//
//        if(message.matches("[0-9]+")&&message.length()==10){
//            return AppConstants.MessageType.PHONE_PRESENT;
//        }
//        return AppConstants.MessageType.MESSAGE_OK;
//    }
//
//}
