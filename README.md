# Cyanogen Update Tracker (app)
Cyanogen Update Tracker app for Android devices

This is the end-user application for Cyanogen Update Tracker.
This open source version does not contain ads, and you are free to work on it.

##Contributing
If you'd like to contribute to this project, create a pull request with your changes. Once they're approved, they will be merged to the appropriate branch.
If you have a really great feature / improvement and like to have it in the official version of the app, which is released on Google Play, please [contact the main developer] (mailto:arjan.vlek.dev@gmail.com) to get it merged to the private play store repository.

There are a few rules / limitations for Play Store commits:
- The code must be tested on a real Cyanogen OS (or Lineage OS, if you add support for that) device
- The code may not interfere with AdMob and / or Google Cloud Messaging.
- The code must support non-rooted devices, or offer a manual installation guide for them.
- Changes to the back-end should be pushed to the [back-end repository] (https://github.com/arjanvlek/cyanogen-update-tracker-api) first, possibly in a new API version directory if it breaks things for older app versions.
- Changes to the database should also be pushed to the [back-end] (https://github.com/arjanvlek/cyanogen-update-tracker-api) repository, in a new SQL file. Database changes may never cause data to get lost. All data should always be migrated.

##How to develop?

###Prerequisites:
The app in general requires:
- Web server or VPS running the LAMP or the WAMP stack (Linux / Windows, Apache, MySQL, PHP)
- Domain name or static IP address
- [Cyanogen Update Tracker API] (https://github.com/arjanvlek/cyanogen-update-tracker-api)
- Cyanogen OS Version data (either from a third party API or added manually to the Web server)

This Android Studio project requires the following to be installed (from Android Studio or Android SDK Manager):
- Android Studio (latest version)
- Android 7.1 SDK, with Build-tools 25.0.1 and Tools 25.0.1 or higher
- Android support library and repository
- Google Repository
- Google Play Services

###Obtaining the code:
```
git clone https://github.com/arjanvlek/cyanogen-update-tracker-app.git
```
- In Android studio, browse to the cyanogen-update-tracker directory and open build.gradle (not the Build.gradle in the app directory!)


###Adding your API keys
This project uses Google Cloud messaging. If you'd like to enable push notifications, do the following:
- Create a new GCM project at https://developers.google.com/android/.
- Add the google-services.json file you get from Google to the /app directory.
- Add the Sender ID of GCM to /app/src/main/res/values-nl/google-services.xml (to prevent a release build error)

###Set up the app API Server
- Follow the guide from the [API server project] (https://github.com/arjanvlek/cyanogen-update-tracker-api).
- Add the base URL of the API to Build.gradle (this time in the /app folder). If you don't have a test version, add the same URL in the debug config

###Test it!
You can now run the app on an emulator or on your own phone, just by clicking the Run or Debug button in Android Studio.


###Notes:
- This is not an official Cyanogen application.
- Cyanogen and Cyanogen OS are registered trademarks of Cyanogen inc.
- I'm not responsible for bricked devices / failed updates.
- The branches are for each released version. Try to make a new branch when adding changes.
- The version-2.0.0 branch contains a work in progress on CyanogenMod support, which I never finished. It might be easy to migrate this to LineageOS and finish, but I don't have the time myself to do so.
If you do like to finish it, go ahead :)


