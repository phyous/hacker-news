package com.phyous.hackernews.data.parser;

import com.phyous.hackernews.data.ConnectionManager;
import com.phyous.hackernews.data.model.Result;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

public class LoginParser {
    private static final String LOGIN_URL_EXTENSION = "/newslogin?whence=news";

    public static class LoginResponse {
        public LoginResponse() {
            // empty constructor
        }

        public LoginResponse(Result result) {
            this.result = result;
        }

        public Result result;
    }

    /**
     * Attempts to log the user into http://news.ycombinator.com. If successful, returns a user
     * authentication cookie. Else it returns null.
     **/
    public static String login(String username, String password) {
        try {
            Response loginResponse = ConnectionManager.anonConnect(LOGIN_URL_EXTENSION)
                    .method(Method.GET)
                    .execute();
            Document loginPage = loginResponse.parse();
            String fnid = loginPage.select("input[name=fnid]")
                    .attr("value");

            Response response = ConnectionManager.anonConnect("/y")
                    .data("fnid", fnid)
                    .data("u", username)
                    .data("p", password)
                    .header("Origin", ConnectionManager.BASE_URL)
                    .followRedirects(true)
                    .referrer(ConnectionManager.BASE_URL + LOGIN_URL_EXTENSION)
                    .method(Method.POST)
                    .execute();

            String cookie = response.cookie("user");
            if (StringUtils.isNotBlank(cookie)) {
                return cookie;
            }
        } catch (Exception e) {
            // connection error
        }
        return null;
    }
}