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
 */package red.yelo.utils.mugen;

import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;

import red.yelo.utils.mugen.attachers.AbsListViewAttacher;
import red.yelo.utils.mugen.attachers.RecyclerViewAttacher;

/**
 * Helper class to detect whenever an {@link android.widget.AbsListView} has to given a Load
 * More implementation.
 *
 * @author vinaysshenoy 31/10/14
 */
public class Mugen {

    private static final String TAG = "Mugen";

    private Mugen() {
        //Default constructor to prevent initialization
    }

    /**
     * Creates a Attacher for AbsListView implementations
     *
     * @param absListView The List for which load more functionality is needed
     * @param callbacks   The callbacks which will receive the Load more events
     */
    public static AbsListViewAttacher with(final AbsListView absListView, final MugenCallbacks callbacks) {
        return new AbsListViewAttacher(absListView, callbacks);
    }

    /**
     * Creates a Attacher for RecyclerView implementations
     *
     * @param recyclerView The List for which load more functionality is needed
     * @param callbacks    The callbacks which will receive the Load more events
     */
    public static RecyclerViewAttacher with(final RecyclerView recyclerView, final MugenCallbacks callbacks) {
        return new RecyclerViewAttacher(recyclerView, callbacks);
    }

}
