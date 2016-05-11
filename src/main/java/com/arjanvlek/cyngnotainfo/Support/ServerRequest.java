package com.arjanvlek.cyngnotainfo.Support;

import java.net.MalformedURLException;
import java.net.URL;

public enum ServerRequest {

    DEVICES {
        @Override
        URL getURL(String... params) throws MalformedURLException {
            String baseUrl = !ServerConnector.testing ? ServerConnector.SERVER_URL : ServerConnector.TEST_SERVER_URL;
            return new URL(baseUrl + "devices");
        }
    },
    UPDATE_METHODS {
        @Override
        URL getURL(String... params) throws MalformedURLException {
            String baseUrl = !ServerConnector.testing ? ServerConnector.SERVER_URL : ServerConnector.TEST_SERVER_URL;
            return new URL(baseUrl + "updateMethods/" + params[0]);
        }
    },
    UPDATE_DATA {
        @Override
        URL getURL(String... params) throws MalformedURLException {
            String baseUrl = !ServerConnector.testing ? ServerConnector.SERVER_URL : ServerConnector.TEST_SERVER_URL;
            return new URL(baseUrl + "updateData/" + params[0] + "/" + params[1] + "/" + params[2]);
        }
    },
    SERVER_STATUS {
        @Override
        URL getURL(String... params) throws MalformedURLException {
            String baseUrl = !ServerConnector.testing ? ServerConnector.SERVER_URL : ServerConnector.TEST_SERVER_URL;
            return new URL(baseUrl + "serverStatus");
        }
    },
    SERVER_MESSAGES {
        @Override
        URL getURL(String... params) throws MalformedURLException {
            String baseUrl = !ServerConnector.testing ? ServerConnector.SERVER_URL : ServerConnector.TEST_SERVER_URL;
            return new URL(baseUrl + "serverMessages");
        }
    };

    abstract URL getURL(String...params) throws MalformedURLException;
}
