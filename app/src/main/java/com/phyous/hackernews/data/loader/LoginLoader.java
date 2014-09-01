package com.phyous.hackernews.data.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.phyous.hackernews.data.DbHelperSingleton;
import com.phyous.hackernews.data.model.Result;
import com.phyous.hackernews.data.parser.LoginParser;
import com.phyous.hackernews.data.parser.LoginParser.LoginResponse;
import com.phyous.hackernews.data.UserPrefs;

public class LoginLoader extends AsyncTaskLoader<LoginResponse> {
    String mUsername;
    String mPassword;

    public LoginLoader(Context context, String username, String password) {
        super(context);
        mUsername = username;
        mPassword = password;
    }

    @Override
    public LoginResponse loadInBackground() {
        if (mUsername == null || mPassword == null) return new LoginResponse(Result.EMPTY);

        String newCookie = LoginParser.login(mUsername, mPassword);
        boolean isSuccess = newCookie != null;
        if (isSuccess) {
            // saves new user cookie and updates the timestamp
            UserPrefs prefs = new UserPrefs(getContext());
            prefs.saveUserCookie(newCookie);
            prefs.saveUsername(mUsername);
            prefs.savePassword(mPassword);

            // delete all caches after logging in
            SQLiteDatabase db = DbHelperSingleton.getInstance(getContext()).getWritableDatabase();
            DbHelperSingleton.clearCaches(db);
            db.close();
        }

        LoginResponse loginResponse;
        if(isSuccess)
            loginResponse = new LoginResponse(Result.SUCCESS);
        else
            loginResponse = new LoginResponse(Result.FAILURE);

        return loginResponse;
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        forceLoad();
    }

}
