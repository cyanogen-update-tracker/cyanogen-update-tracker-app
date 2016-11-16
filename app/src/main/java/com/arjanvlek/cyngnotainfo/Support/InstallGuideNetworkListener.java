package com.arjanvlek.cyngnotainfo.Support;


import android.graphics.Bitmap;
import android.media.Image;

import com.arjanvlek.cyngnotainfo.Model.InstallGuideData;

public interface InstallGuideNetworkListener {

    void onInstallGuideContentsReceived(InstallGuideData contents);
    void onInstallGuideImageReceived (Bitmap image);
}
