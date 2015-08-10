# yelo Android App
* Clone the repo.
* git clone git@github.com:yeloapp/yelo-android.git
* Import to Android Studio.

Remember to update your SDK and build tools to the latest versions. We use the Renderscript Support Library, which always gets updated to the latest build tools.

You'll need to register a Google Maps Api Keys v2 and add it in a file called api_keys.xml in the res/values folder. Make sure this is not pushed to your repo and keep it local on your machine.


<resources>

    <!-- Google API Keys -->
    <string name="ga_tracking_id">GOOGLE_ANALYTICS_TRACKING_ID</string>

     <!-- mixpanel_api_key -->
    <string name="mixpanel_api_key">MIXPANEL_ID</string>

    <!-- GCM ID -->
    <string name="gcm_sender_id">SENDER_ID</string>

    <!--BUGSENSE ID -->
    <string name="bug_sense_api_key">BUGSENSE_ID</string>

    <!--FACEBOOK ID-->
    <string name="facebook_app_id">FACEBOOK_APP_ID</string>

</resources>



You are good to go!


ATTRIBUTIONS

- [Okulus](https://github.com/vinaysshenoy/okulus)
- [ShowCaseView](https://github.com/amlcurran/ShowcaseViews)
- [Glide Image Loading Library](https://github.com/bumptech/glide)
- [Retrofit](http://square.github.io/retrofit/)
- [Otto Bus](http://square.github.io/otto/)
- [Facebook Android SDK](https://github.com/facebook/facebook-android-sdk)
- [Mixpanel](https://mixpanel.com/)
- [Material Dialogs](https://github.com/afollestad/material-dialogs)
- [Splunk-mint](https://mint.splunk.com/)
- [Floating action button](https://github.com/makovkastar/FloatingActionButton)
- [TextDrawable](https://github.com/amulyakhare/TextDrawable)

#LICENSE

Copyright (C) 2014 yelo

Please note that this is only licensing for the code and not the look and feel, brand and communication of yelo and its apps. The intent of this license is to enable you to make new things and not to produce a copy version.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
