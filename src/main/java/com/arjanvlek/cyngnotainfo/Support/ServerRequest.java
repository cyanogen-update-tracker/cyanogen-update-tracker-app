package com.arjanvlek.cyngnotainfo.Support;

import com.arjanvlek.cyngnotainfo.BuildConfig;

import java.net.MalformedURLException;
import java.net.URL;

public enum ServerRequest {

    DEVICES {
        @Override
        URL getURL(String... params) throws MalformedURLException {
            return new URL(getBaseUrl() + "devices");
        }
    },
    UPDATE_METHODS {
        @Override
        URL getURL(String... params) throws MalformedURLException {
            return new URL(getBaseUrl() + "updateMethods/" + params[0]);
        }
    },
    UPDATE_DATA {
        @Override
        URL getURL(String... params) throws MalformedURLException {
            return new URL(getBaseUrl() + "updateData/" + params[0] + "/" + params[1] + "/" + params[2]);
        }
    },
    DESCRIPTION {
        @Override
        URL getURL(String... params) throws MalformedURLException {
            return new URL(getBaseUrl() + "description/" + params[0] + "/" + params[1]);
        }
    },
    SERVER_STATUS {
        @Override
        URL getURL(String... params) throws MalformedURLException {
            return new URL(getBaseUrl() + "serverStatus");
        }
    },
    SERVER_MESSAGES {
        @Override
        URL getURL(String... params) throws MalformedURLException {
            return new URL(getBaseUrl() + "serverMessages");
        }
    };

    abstract URL getURL(String...params) throws MalformedURLException;

    private static String getBaseUrl() {
        return BuildConfig.USE_TEST_SERVER ? ServerConnector.TEST_SERVER_URL : ServerConnector.SERVER_URL;
    }
}
