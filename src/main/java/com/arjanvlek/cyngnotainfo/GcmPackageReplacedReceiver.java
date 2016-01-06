package com.arjanvlek.cyngnotainfo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.arjanvlek.cyngnotainfo.Support.NetworkConnectionManager;

public class GcmPackageReplacedReceiver extends WakefulBroadcastReceiver {

    private NetworkConnectionManager networkConnectionManager;
    @Override
    public void onReceive(Context context, Intent intent) {
        networkConnectionManager = new NetworkConnectionManager(context);
        if(networkConnectionManager.checkNetworkConnection()) {
            if (intent != null && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
                // invalidate the current GCM registration id, and re-register with GCM server using the GcmRegistrationIntentService
                Intent i = new Intent(context, GcmRegistrationIntentService.class);
                i.putExtra("package_upgrade", true);
                i.putExtra("test", true);
                startWakefulService(context, i);
            } else {
                Intent i = new Intent(context, GcmRegistrationIntentService.class);
                i.putExtra("package_upgrade", true);
                startWakefulService(context, i);
            }
        }
    }
}