
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
 */

package red.yelo.widgets.autocomplete;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import red.yelo.retromodels.response.KeywordSuggestionsResponseModel;

/**
 * Custom AutoCompleteTextView to provide suggestions
 * when typing from internet
 */
public class NetworkedAutoCompleteTextView extends
        MaterialAutoCompleteTextView implements OnItemClickListener {

    private static final String TAG = "NetworkedAutoCompleteTextView";

    /**
     * The threshold of the search query length at which the network search
     * should be performed
     */
    private int mSuggestCountThreshold;

    /**
     * The amount of time(in milliseconds) to wait after the user has typed to
     * actually trigger the network search
     */
    private int mSuggestWaitThreshold;

    /**
     * The current suggestions used for displaying the dropdowns
     */
    private List<KeywordSuggestionsResponseModel.Keywords> mSuggestions;

    /**
     * Handler for posting callbacks for perfoming the search request
     */
    private Handler mHandler;

    /**
     * Runnable for perfoming search requests
     */
    private Runnable mPerformSearchRunnable;

    /**
     * Callbacks for perfomiong search requests
     */
    private INetworkSuggestCallbacks mNetworkSuggestCallbacks;

    /**
     * TextWatcher reference for performing network requests
     */
    private SuggestNetworkTextWatcher mSuggestNetworkTextWatcher;

    /**
     * Whether the network suggestions are enabled or not
     */
    private boolean mSuggestionsEnabled;

    /**
     * Suggestions adapter for providing suggestions
     */
    private SuggestionsAdapter mSuggestionsAdapter;

    /**
     * @param context
     */
    public NetworkedAutoCompleteTextView(final Context context) {
        super(context);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public NetworkedAutoCompleteTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mSuggestNetworkTextWatcher = new SuggestNetworkTextWatcher();
        addTextChangedListener(mSuggestNetworkTextWatcher);
        setOnItemClickListener(this);
        mHandler = new Handler();
        mSuggestionsAdapter = new SuggestionsAdapter(getContext(), null);
        setAdapter(mSuggestionsAdapter);
        mSuggestionsEnabled = true;
    }

    /**
     * Enable/Disable the network suggestions
     *
     * @param enabled <code>true</code> to enable network suggestions,
     *                <code>false</code> to disable them
     */
    public void setNetworkSuggestionsEnabled(final boolean enabled) {

        mSuggestionsEnabled = enabled;
    }

    /**
     * Set the text, while disabling any suggestions
     */
    public void setTextWithFilter(CharSequence text, boolean filter) {

        /*
         * If filter is disabled, temporarily disable suggestions, set the text,
         * and reenable suggestions. This will need to be set to true later on if the text changes.
         */
        if (!filter) {

            mSuggestionsAdapter.setDisplaySuggestionsEnabled(false);
            setNetworkSuggestionsEnabled(false);
            //setAdapter(null);
            setText(text);
            setAdapter(mSuggestionsAdapter);
            setNetworkSuggestionsEnabled(true);
        }
    }

    /**
     * Set the text, while disabling any suggestions
     */
    public void setTextWithFilter(int resId, boolean filter) {

        /*
         * If filter is disabled, temporarily disable suggestions, set the text,
         * and reenable suggestions. This will need to be set to true later on if the text changes.
         */
        if (!filter) {

            mSuggestionsAdapter.setDisplaySuggestionsEnabled(false);
            setNetworkSuggestionsEnabled(false);
            //setAdapter(null);
            setText(resId);
            setAdapter(mSuggestionsAdapter);
            setNetworkSuggestionsEnabled(true);
        }
    }

    public int getSuggestCountThreshold() {
        return mSuggestCountThreshold;
    }

    public void setSuggestCountThreshold(final int suggestCountThreshold) {
        mSuggestCountThreshold = suggestCountThreshold;
    }

    public int getSuggestWaitThreshold() {
        return mSuggestWaitThreshold;
    }

    public void setSuggestWaitThreshold(final int suggestWaitThreshold) {
        mSuggestWaitThreshold = suggestWaitThreshold;
    }

    public INetworkSuggestCallbacks getNetworkSuggestCallbacks() {
        return mNetworkSuggestCallbacks;
    }

    public void setNetworkSuggestCallbacks(
            final INetworkSuggestCallbacks callbacks) {
        mNetworkSuggestCallbacks = callbacks;
    }

    /**
     * Add a new set of suggestions to this TextView
     *
     * @param query       The query for which the siggestions are fetched
     * @param suggestions The list of suggestions to use
     * @param append      <code>false</code> to add the new suggestions to the
     *                    TextView, <code>true</code> to replace the suggestion
     */
    public void onSuggestionsFetched(final String query,
                                     final List<KeywordSuggestionsResponseModel.Keywords> suggestions, final boolean replace) {

        if (mSuggestions == null) {
            mSuggestions = new ArrayList<KeywordSuggestionsResponseModel.Keywords>();
        }

        if (replace) {
            mSuggestions.clear();
        }

        mSuggestions.addAll(suggestions);
        mSuggestionsAdapter.setSuggestionsMaster(mSuggestions);
        performFiltering(query, 0);
    }

    /**
     * {@link android.text.TextWatcher} implementation for perfoming Network calls
     */
    private class SuggestNetworkTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(final CharSequence s, final int start,
                                      final int count, final int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, final int start,
                                  final int before, final int count) {


            if (mSuggestionsEnabled) {

                 String newSearchSequence = s.toString();
                if(!TextUtils.isEmpty(newSearchSequence)) {
                    String[] split = newSearchSequence.split(",");

                    newSearchSequence = split[split.length - 1].substring(1);
                    removeAnyCallbacks();
                    if (newSearchSequence.length() >= mSuggestCountThreshold) {

                        mPerformSearchRunnable = makeSearchRunnable(newSearchSequence);
                        mHandler.postDelayed(mPerformSearchRunnable, mSuggestWaitThreshold);
                    }
                }

            }

            if (s.toString().length() > 0) {
                if (String.valueOf(s.charAt(s.length() - 1)).equals(",")) {
                    setNetworkSuggestionsEnabled(true);
                }
            }

        }

        @Override
        public void afterTextChanged(final Editable s) {

        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAnyCallbacks();
    }

    /**
     * Removes any pending callbacks(if any) from the handler
     */
    private void removeAnyCallbacks() {
        if (mPerformSearchRunnable != null) {
            mHandler.removeCallbacks(mPerformSearchRunnable);
        }
    }

    /**
     * Creates a runnable for perfoming a search query
     *
     * @param query The query to search for
     * @return a {@link Runnable} for performing a search request
     */
    private Runnable makeSearchRunnable(final String query) {
        return new Runnable() {

            @Override
            public void run() {
                if (mNetworkSuggestCallbacks != null) {
                    mNetworkSuggestCallbacks
                            .performNetworkQuery(NetworkedAutoCompleteTextView.this, query);
                }
            }
        };
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
                            final int position, final long id) {
        final KeywordSuggestionsResponseModel.Keywords suggestion = (KeywordSuggestionsResponseModel.Keywords) mSuggestionsAdapter
                .getItem(position);

        if (mNetworkSuggestCallbacks != null) {
            mNetworkSuggestCallbacks.onSuggestionClicked(this, suggestion);
        }

    }
}
