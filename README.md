# Cyanogen Update Tracker (app)
Cyanogen Update Tracker app for Android devices

This is the end-user application for Cyanogen Update Tracker.
It is available on Google Play and uses Google Services.

##How to develop?

###Prerequisites:
The app in general requires:
- Web server (apache, php, mysql) or VPS (apache, php, mysql and automatic update fetching using CRON)
- Domain name
- Cyanogen OS Version data (either from Cyanogen or added manually to the Web server)

This Android Studio project requires the following to be installed (from Android Studio or Android SDK Manager):
- Android Studio (latest version)
- Android 6.0 SDK, with Build-tools 21.1.2 and Tools 24.4.1 or higher
- Android support library and repository
- Google Repository
- Google Play Services

###Obtaining the code:
- Make a directory called 'CyngnOtaInfo'.
- `cd` into that directory

####Clone the project:
```
git clone https://github.com/arjanvlek/android-cyanogen-update-tracker-app.git
```

- Rename the cloned directory to `app`. (this is important!)
- Copy the file `missing-git-files.zip` to the `CyngnOtaInfo` directory (dir above the app directory).
- Extract all the contents of this zip file in the `CyngnOtaInfo` directory.

It should have at least these contents:
```
Directories:
 .gradle (gradle dir)
 .idea (Android Studio dir)
 app (Main source code)
 build (Build folder for running)
 gradle (Gradle distribution)
 
Files:
 build.gradle (Top-level build.gradle file)
 CyngnOtaInfo.iml (Android Studio Project)
 gradle.properties (Gradle settings)
 gradlew (Mac / Linux version of command-line Gradle runner)
 gradlew.bat (Windows version of command-line Gradle runner) 
 local.properties (Local SDK settings - will prompt to correct SDK location on first run)
 settings.gradle (Gradle settings)
```

###Adding your API keys
This project uses Google Cloud messaging and Admob.
- You can configure these APIs at https://developers.google.com/android/.
- Please note that Admob is integrated in an old way, so you need to update it in `fragment_updateinformation.xml` and `fragment_deviceinformation.xml` to use the new Google-Services.json file format.

###Set up the app API Server
- Follow the guide from the [API server project] (https://github.com/arjanvlek/android-cyanogen-update-tracker-api).
- Add the base URL of the API to these files:

```
ServerConnector.java: lines 28 and 29
GCMRegistrationIntentService.java: lines 52 and 53
```

###Test it!
You can now run the app on an emulator or on your own phone, just by clicking the Run or Debug button in Android Studio.


###Notes:
This is not an official Cyanogen application.
Cyanogen and Cyanogen OS are registred trademarks of Cyanogen inc.
I'm not responsible for bricked devices / failed updates.


