package com.app_mo.animefaq;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app_mo.animefaq.model.LoginInfo;
import com.app_mo.animefaq.network.MySingleton;
import com.app_mo.animefaq.network.NetworkInfo;
import com.app_mo.animefaq.storage.SavePreferences;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    private Gson gson;
    private LoginInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        //requestUserWebToken();
    }

    private void requestUserWebToken() {
        String strId = new SavePreferences(this).getValues("userId", "id");
        int id = 0;

        if (strId != null && !strId.equals(""))
            id = Integer.parseInt(strId);

        String mac = NetworkInfo.getDeviceMACAddress();

        gson = new Gson();
        info = new LoginInfo(id, mac);
        final String jsonString = gson.toJson(info);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, NetworkInfo.HOST_URL + "user/login/auto",
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("splash_result", response);
                info = gson.fromJson(response, LoginInfo.class);

                String savedToken = new SavePreferences(SplashActivity.this).getValues("userWebToken", "webToken");

                if (info.getResult().equalsIgnoreCase("Success")) {
                    Log.d("savedToken", savedToken);
                    Log.d("newToken", info.getToken());

                    if (info.getToken().equals(savedToken)) {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, Login.class));
                    }
                } else {
                    startActivity(new Intent(SplashActivity.this, Login.class));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("VolleyError", error.getMessage());
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
    }
}
