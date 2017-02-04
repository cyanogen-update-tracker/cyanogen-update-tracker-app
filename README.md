# Cyanogen Update Tracker (app)
Cyanogen Update Tracker app for Android devices

This is the end-user application for Cyanogen Update Tracker.
This open source version does not contain ads, and serves as a base for great ideas. If you have a really great feature and like to have it in the official version on Google Play, please [contact the main developer] (mailto:arjan.vlek.dev@gmail.com)

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
git clone https://github.com/arjanvlek/android-cyanogen-update-tracker-app.git
```
- In Android studio, browse to the android-cyanogen-update-tracker directory and open build.gradle (not the Build.gradle in the app directory!)


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
This is not an official Cyanogen application.
Cyanogen and Cyanogen OS are registred trademarks of Cyanogen inc.
I'm not responsible for bricked devices / failed updates.


