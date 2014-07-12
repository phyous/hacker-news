package com.phyous.hackernews.util;

public class StringHelper {
    public static CharSequence trim(CharSequence s) {
        if (s == null || s.length() == 0) {
            return s;
        }

        int start = 0;
        int end = s.length() - 1;

        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }
}
