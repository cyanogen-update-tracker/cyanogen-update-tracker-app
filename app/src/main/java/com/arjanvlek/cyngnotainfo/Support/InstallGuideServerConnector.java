package com.arjanvlek.cyngnotainfo.Support;

import com.arjanvlek.cyngnotainfo.Model.InstallGuideData;

import static com.arjanvlek.cyngnotainfo.Support.ServerRequest.INSTALL_GUIDE;

/**
 * Created by arjan on 16-10-16.
 */

public class InstallGuideServerConnector extends ServerConnector {

    private InstallGuideNetworkListener listener;

    public InstallGuideServerConnector addNetworkListener(InstallGuideNetworkListener listener) {
        this.listener = listener;
        return this;
    }

    public void fetchInstallGuidePageFromServer(Integer pageNumber, Long deviceId, Long updateMethodId) {
        InstallGuideData data = findOneFromServerResponse(fetchDataFromServer(INSTALL_GUIDE, deviceId.toString(), updateMethodId.toString(), pageNumber.toString()), InstallGuideData.class);
        
    }
}
