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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import red.yelo.R;


/**
 * Created by anshul1235 on 15/07/14.
 */
public class ImageCropFragment extends AbstractYeloFragment  {

    private static final String TAG = "ImageCropFragment";


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(false);
        final View view = inflater
                .inflate(R.layout.fragment_image_crop, container, false);


        return view;
    }



    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


}
