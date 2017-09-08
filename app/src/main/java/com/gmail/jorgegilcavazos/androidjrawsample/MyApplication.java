package com.gmail.jorgegilcavazos.androidjrawsample;

import android.app.Application;

import net.dean.jraw.http.oauth.Credentials;

import java.util.UUID;

public class MyApplication extends Application {
    private static final String CLIENT_ID = "Your client id";
    private static final String REDIRECT_URL = "http://localhost/authorize_callback";

    private Credentials installedAppCredentials;
    private Credentials userlessAppCredentials;

    public Credentials getUserlessAppCredentials() {
        if (userlessAppCredentials == null) {
            userlessAppCredentials = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
        }
        return userlessAppCredentials;
    }

    public Credentials getInstalledAppCredentials() {
        if (installedAppCredentials == null) {
            installedAppCredentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
        }
        return installedAppCredentials;
    }
}
