package com.arjanvlek.cyngnotainfo;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GcmPackageReplacedReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null && (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) || intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED))) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                if (intent.getData().getSchemeSpecificPart().equals(context.getPackageName())) {
                    // invalidate the current GCM registration id, and re-register with GCM server using the GcmRegistrationIntentService
                    Intent i = new Intent(context, GcmRegistrationIntentService.class);
                    i.putExtra("package_upgrade", true);
                    startWakefulService(context, i);
                }
                else {
                    Intent i = new Intent(context, GcmRegistrationIntentService.class);
                    i.putExtra("package_upgrade", true);
                    startWakefulService(context, i);
                }
            }
        }
    }
}