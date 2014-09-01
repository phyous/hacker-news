package com.phyous.hackernews.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.phyous.hackernews.R;
import com.phyous.hackernews.data.loader.LoginLoader;
import com.phyous.hackernews.data.model.Result;
import com.phyous.hackernews.data.parser.LoginParser.LoginResponse;
import com.todddavies.components.progressbar.ProgressWheel;

public class LoginActivity extends Activity implements LoaderManager.LoaderCallbacks<LoginResponse> {
    public static final String TAG = LoginActivity.class.getSimpleName();
    public static final String USERNAME = TAG + ".username";
    public static final String PASSWORD = TAG + ".password";

    private String mUsername = null;
    private String mPassword = null;
    private EditText mEditUsername;
    private EditText mEditPassword;
    private Button mLoginButton;
    private TextView mTextLogo;
    private ProgressWheel mProgressLoggingIn;

    // Duration which listview fades in and out
    private static final long FADE_ANIMATION_DURATION = 500;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Find views associated with this activity & bind listeners
        findViews();
        bindListeners();

        // Set EditText/Logo/Spinners to initial UI state to prepare for user input
        initializeUiState();

        restoreSavedState(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    private void findViews() {
        mEditUsername = (EditText) findViewById(R.id.username);
        mEditPassword = (EditText) findViewById(R.id.password);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mTextLogo = (TextView) findViewById(R.id.logo_text);
        mProgressLoggingIn = (ProgressWheel) findViewById(R.id.spinner);
    }

    private void bindListeners() {
        mLoginButton.setOnClickListener(mSubmitBtnListener);
        mLoginButton.setOnTouchListener(mButtonTouchListener);
        mEditPassword.setOnEditorActionListener(mSubmitKeyboardListener);
    }

    private void restoreSavedState(Bundle savedState) {
        if (savedState != null) {
            mUsername = savedState.getString(USERNAME);
            mPassword = savedState.getString(PASSWORD);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mEditUsername.getText() != null) mUsername = mEditUsername.getText().toString();
        if (mEditPassword.getText() != null) mPassword = mEditPassword.getText().toString();
        if (mUsername != null) outState.putString(USERNAME, mUsername);
        if (mPassword != null) outState.putString(PASSWORD, mPassword);
        super.onSaveInstanceState(outState);
    }

    private View.OnClickListener mSubmitBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            performSubmit();
        }
    };

    private TextView.OnEditorActionListener mSubmitKeyboardListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            boolean consumeAction = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSubmit();

                // hide keyboard
                InputMethodManager inputManager =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (getCurrentFocus() != null) {
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }

                consumeAction = true;
            }
            return consumeAction;
        }
    };

    private View.OnTouchListener mButtonTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(getResources().getColor(R.color.grey_button_pressed));
                    break;
                case MotionEvent.ACTION_UP:
                    v.setBackgroundColor(getResources().getColor(R.color.grey_button));
                    break;
            }
            return false;
        }
    };

    // Loader callbacks
    @Override
    public Loader<LoginResponse> onCreateLoader(int id, Bundle args) {
        return new LoginLoader(this, mUsername, mPassword);
    }

    @Override
    public void onLoadFinished(Loader<LoginResponse> loader, LoginResponse response) {
        if (response.result == Result.EMPTY) {
            return; // this means the request was from initLoader()
        }

        if (response.result == Result.SUCCESS) {
            Toast t = Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT);
            t.show();
            doPostAction();
            finish();
        } else {
            showErrorAnimation();
            Toast t = Toast.makeText(this, getString(R.string.login_failure), Toast.LENGTH_LONG);
            t.show();
        }
    }

    @Override
    public void onLoaderReset(Loader<LoginResponse> loader) {
    }

    protected void performSubmit() {
        if (mEditUsername.getText() != null) mUsername = mEditUsername.getText().toString();
        if (mEditPassword.getText() != null) mPassword = mEditPassword.getText().toString();

        showLoadingAnimation();

        getLoaderManager().restartLoader(0, null, this);
    }

    private void doPostAction() {
        mProgressLoggingIn.stopSpinning();
    }

    private void initializeUiState() {
        if (mEditUsername != null) mEditUsername.setEnabled(true);
        if (mEditPassword != null) mEditPassword.setEnabled(true);
        mLoginButton.setBackgroundColor(getResources().getColor(R.color.grey_button));
        mTextLogo.setVisibility(View.VISIBLE);
        mProgressLoggingIn.setVisibility(View.GONE);
    }

    private void showLoadingAnimation() {
        if (mEditUsername != null) mEditUsername.setEnabled(false);
        if (mEditPassword != null) mEditPassword.setEnabled(false);
        mTextLogo.setVisibility(View.GONE);

        mProgressLoggingIn.setAlpha(0f);
        mProgressLoggingIn.setVisibility(View.VISIBLE);
        mProgressLoggingIn.animate().alpha(1f).setDuration(FADE_ANIMATION_DURATION).setListener(null);
        mProgressLoggingIn.spin();
    }

    private void showErrorAnimation() {
        if (mEditUsername != null) mEditUsername.setEnabled(true);
        if (mEditPassword != null) mEditPassword.setEnabled(true);
        mProgressLoggingIn.setVisibility(View.GONE);
        mProgressLoggingIn.stopSpinning();

        mTextLogo.setAlpha(0f);
        mTextLogo.setVisibility(View.VISIBLE);
        mTextLogo.animate().alpha(1f).setDuration(FADE_ANIMATION_DURATION).setListener(null);
    }
}
