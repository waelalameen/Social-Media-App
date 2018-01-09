package com.app_mo.animefaq;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app_mo.animefaq.behavior.Dialogs;
import com.app_mo.animefaq.databinding.ActivityLoginBinding;
import com.app_mo.animefaq.model.LoginInfo;
import com.app_mo.animefaq.network.MySingleton;
import com.app_mo.animefaq.network.NetworkInfo;
import com.app_mo.animefaq.storage.SavePreferences;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    ActivityLoginBinding activityLoginBinding;
    private Gson gson;
    private LoginInfo info;
    private CallbackManager callbackManager;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 1234;
    private TwitterAuthClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // Initialize Twitter SDK
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger())
                .twitterAuthConfig(new TwitterAuthConfig("CONSUMER_KEY", "CONSUMER_SECRET"))
                .debug(true)
                .build();
        Twitter.initialize(config);

        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        setTypefaceChanged();

        client = new TwitterAuthClient();

        callbackManager = CallbackManager.Factory.create();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        activityLoginBinding.signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Signup.class));
            }
        });

        activityLoginBinding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNormalLogin();
            }
        });

        activityLoginBinding.facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFacebookLogin();
            }
        });

        activityLoginBinding.google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setGoogleLogin();
            }
        });

        activityLoginBinding.twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTwitterLogin();
            }
        });
    }

    private void setTypefaceChanged() {
        ChangeTypeface.setTypeface(this, activityLoginBinding.noteText);
        ChangeTypeface.setTypeface(this, activityLoginBinding.forgotText);
        ChangeTypeface.setTypeface(this, activityLoginBinding.email);
        ChangeTypeface.setTypeface(this, activityLoginBinding.password);
        ChangeTypeface.setTypeface(this, activityLoginBinding.orText);
        ChangeTypeface.setTypeface(this, activityLoginBinding.login);
        ChangeTypeface.setTypeface(this, activityLoginBinding.signup);
    }

    private void setNormalLogin() {
        String email = activityLoginBinding.email.getText().toString().trim();
        String password = activityLoginBinding.password.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) || (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password))) {
            gson = new Gson();
            info = new LoginInfo(email, password, "app", NetworkInfo.getDeviceMACAddress());
            final String jsonString = gson.toJson(info);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, NetworkInfo.HOST_URL + "user/login",
                    new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("result", response);
                    info = gson.fromJson(response, LoginInfo.class);
                    setResponse(info);
                    Dialogs.onHideProgress(Login.this, Login.this);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.getMessage();
                    Dialogs.makeDialog(Login.this, error.networkResponse.toString(), error.getMessage());
                    Dialogs.onHideProgress(Login.this, Login.this);
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("info", jsonString);
                    Log.d("info", jsonString);
                    return params;
                }
            };

            MySingleton.getInstance(this).addToRequestQueue(stringRequest);
            Dialogs.onMakeProgress(this, this, getResources().getString(R.string.progress_title), "جاري تسجيل الدخول");
        }
    }

    private void setResponse(LoginInfo info) {
        if (info.getResult().equalsIgnoreCase("Success")) {
            Log.d("id", String.valueOf(info.getId()));
            Log.d("token", String.valueOf(info.getToken()));
            Log.d("msg", String.valueOf(info.getMsg()));

            new SavePreferences(this).save("userId", "id", String.valueOf(info.getId()));
            new SavePreferences(this).save("userWebToken", "webToken", info.getToken());
            Log.d("saved id", String.valueOf(new SavePreferences(this).getValues("userId", "id")));
            Log.d("saved token", new SavePreferences(this).getValues("userWebToken", "webToken"));

            startActivity(new Intent(this, MainActivity.class));
        } else {
            Dialogs.makeDialog(this, info.getResult(), info.getMsg());
        }
    }

    private void setFacebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_photos", "email", "public_profile",
                "user_posts" , "AccessToken"));
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("uid", loginResult.getAccessToken().getUserId());
                Log.i("token", loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                Log.i("Login Status", "Login Cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FacebookException", error.getMessage());
            }
        });
    }

    private void setGoogleLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void setTwitterLogin() {
        client.authorize(this, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.i("UserId", String.valueOf(result.data.getUserId()));
                Log.i("UserName", result.data.getUserName());
            }

            @Override
            public void failure(TwitterException exception) {
                Log.e("TwitterException", exception.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // for Facebook callback manager
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // for Google sign in
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }

        // for Twitter sign in
        client.onActivityResult(requestCode, resultCode, data);
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d("handleSignInResult:", String.valueOf(result.isSuccess()));

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    private void updateUI(boolean b) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("ConnectionResult:", connectionResult.getErrorMessage());
    }
}