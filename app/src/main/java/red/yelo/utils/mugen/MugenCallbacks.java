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

/**
 * Callbacks for load more methods
 *
 * @author Vinay S Shenoy
 */
public interface MugenCallbacks {

    /**
     * Callback for when the next set of items should be loaded
     */
    public void onLoadMore();

    /**
     * Callback for whether a load operation is currently ongoing
     *
     * @return <code>true</code> if a load operation is happening,
     * <code>false</code> otherwise. If <code>true</code>, load more
     * event won't be triggered
     */
    public boolean isLoading();

    /**
     * Callback for whether all items have been loaded
     *
     * @return <code>true</code> if all items have been loaded,
     * <code>false</code> otherwise. If <code>true</code>, load more
     * event won't be triggered
     */
    public boolean hasLoadedAllItems();
}