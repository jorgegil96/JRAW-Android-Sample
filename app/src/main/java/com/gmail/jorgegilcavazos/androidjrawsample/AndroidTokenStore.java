package com.gmail.jorgegilcavazos.androidjrawsample;

import android.content.SharedPreferences;

import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.auth.TokenStore;

public class AndroidTokenStore implements TokenStore {

    private SharedPreferences sharedPrefs;

    public AndroidTokenStore(SharedPreferences sharedPrefs) {
        this.sharedPrefs = sharedPrefs;
    }

    @Override
    public boolean isStored(String key) {
        return sharedPrefs.getString(key, null) != null;
    }

    @Override
    public String readToken(String key) throws NoSuchTokenException {
        String token = sharedPrefs.getString(key, null);
        if (token == null)
            throw new NoSuchTokenException("Missing token: " + token);

        return token;
    }

    @Override
    public void writeToken(String key, String token) {
        sharedPrefs.edit().putString(key, token).apply();
    }
}
