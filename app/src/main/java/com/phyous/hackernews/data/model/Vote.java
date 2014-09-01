package com.phyous.hackernews.data.model;

import android.database.sqlite.SQLiteDatabase;
import com.airlocksoftware.database.SqlObject;

public class Vote extends SqlObject {
    public long itemId;
    public String username;
    public String auth;
    public String whence;

    public boolean success;

    public Vote() {
    }

    public boolean create(SQLiteDatabase db) {
        return super.createAndGenerateId(db);
    }

}
