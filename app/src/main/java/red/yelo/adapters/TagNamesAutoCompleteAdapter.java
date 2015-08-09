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
 */package red.yelo.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import red.yelo.http.HttpConstants;
import red.yelo.http.api.YeloApi;
import red.yelo.retromodels.response.GetNamesModel;

/**
 * Created by anshul1235 on 12/08/14.
 */
public class TagNamesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private YeloApi mYeloApi;

    private ArrayList<String> resultList;

    private ArrayList<String> autocomplete(String input) {

        return fillAddressDetails(input);
    }

    private ArrayList<String> fillAddressDetails(String input) {

        final Map<String, String> params = new HashMap<String, String>(6);

        params.put(HttpConstants.Q, input);

        GetNamesModel getTagNamesModel = mYeloApi.getTagNames(params);

        ArrayList<String> predictions = new ArrayList<String>();
        for (int i = 0; i < getTagNamesModel.names.size(); i++) {
            predictions.add(getTagNamesModel.names.get(i));
        }

        return predictions;


    }

    public TagNamesAutoCompleteAdapter(Context context, int textViewResourceId, YeloApi yeloApi) {
        super(context, textViewResourceId);
        mYeloApi = yeloApi;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }


}