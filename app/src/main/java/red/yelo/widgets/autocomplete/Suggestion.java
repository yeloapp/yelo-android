

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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class representing a suggestion
 * 
 */
public class Suggestion implements Parcelable {

    /**
     * The suggestion id. Used for selecting the suggestion when an item from
     * the drop down is tapped
     */
    public String id;

    /**
     * The name of the suggestion. Used for displaying the title label
     */
    public String name;

    /**
     * Any tag_id, if present
     */
    public String tag_id;

    public String word_id;

    public Suggestion() {
    }

    public Suggestion(Parcel source) {
        id = source.readString();
        name = source.readString();
        tag_id = source.readString();
        word_id = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(tag_id);
        dest.writeString(word_id);

    }

    @Override
    public String toString() {
        return String.format("Suggestion %s, %s, %s, %s", id, name, tag_id, word_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* REQUIRED FOR PARCELLABLE. DO NOT MODIFY IN ANY WAY */
    public static final Creator<Suggestion> CREATOR = new Creator<Suggestion>() {

                                                        @Override
                                                        public Suggestion createFromParcel(
                                                                        Parcel source) {
                                                            return new Suggestion(source);
                                                        }

                                                        @Override
                                                        public Suggestion[] newArray(
                                                                        int size) {
                                                            return new Suggestion[size];
                                                        }
                                                    };

}
