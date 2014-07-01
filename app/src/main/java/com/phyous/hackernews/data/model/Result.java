package com.phyous.hackernews.data.model;

public enum Result {
    SUCCESS, MORE, NO_SUCH_USER, FAILURE,
    /** empty indicates null data was passed to the loader**/
    EMPTY,
    FNID_EXPIRED, Result;
}