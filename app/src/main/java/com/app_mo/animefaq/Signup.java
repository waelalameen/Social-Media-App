package com.app_mo.animefaq;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app_mo.animefaq.behavior.Dialogs;
import com.app_mo.animefaq.databinding.ActivitySignupBinding;
import com.app_mo.animefaq.device.DeviceInfo;
import com.app_mo.animefaq.model.Users;
import com.app_mo.animefaq.network.MySingleton;
import com.app_mo.animefaq.network.NetworkInfo;
import com.app_mo.animefaq.network.Upload;
import com.app_mo.animefaq.storage.SavePreferences;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {
    ActivitySignupBinding signupBinding;
    private Users user;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signupBinding = DataBindingUtil.setContentView(this, R.layout.activity_signup);

        setTypefaceChanged();

        signupBinding.signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUsers();
            }
        });
    }

    private void addUsers() {
        String userName = signupBinding.username.getText().toString().trim();
        String fullName = signupBinding.fullname.getText().toString().trim();
        String email = signupBinding.email.getText().toString().trim();
        String password = signupBinding.password.getText().toString().trim();
        String retype = signupBinding.retypePassword.getText().toString().trim();

        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(fullName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(retype)) {
            new SavePreferences(this).save("values", "username", userName);
            uploadDataToServer(userName, fullName, email, password);
        } else {
            Toast.makeText(this, "املىْ الحقول", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadDataToServer(String userName, String fullName, String email, String password) {
        gson = new Gson();
        user = new Users(userName, email, fullName, password, NetworkInfo.getDeviceMACAddress(), DeviceInfo.getDeviceName(), "dsbfbf4547765");
        final String jsonString = gson.toJson(user);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, NetworkInfo.HOST_URL + "user/register", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("result", response);
                user = gson.fromJson(response, Users.class);
                setResponse(user);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.getMessage();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("users", jsonString);
                Log.d("users", jsonString);
                return params;
            }
        };

        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void setResponse(Users user) {
        if (user.getResult().equalsIgnoreCase("Success")) {
            Intent intent = new Intent(this, Upload.class);
            intent.putExtra("id", user.getId());
            startActivity(intent);
            Log.d("id", String.valueOf(user.getId()));
        } else {
            Dialogs.makeDialog(this, user.getResult(), user.getMsg());
        }
    }

    private void setTypefaceChanged() {
        ChangeTypeface.setTypeface(this, signupBinding.usernameLabel);
        ChangeTypeface.setTypeface(this, signupBinding.username);
        ChangeTypeface.setTypeface(this, signupBinding.fullnameLabel);
        ChangeTypeface.setTypeface(this, signupBinding.fullname);
        ChangeTypeface.setTypeface(this, signupBinding.emailLabel);
        ChangeTypeface.setTypeface(this, signupBinding.email);
        ChangeTypeface.setTypeface(this, signupBinding.passwordLabel);
        ChangeTypeface.setTypeface(this, signupBinding.password);
        ChangeTypeface.setTypeface(this, signupBinding.retypePasswordLabel);
        ChangeTypeface.setTypeface(this, signupBinding.retypePassword);
        ChangeTypeface.setTypeface(this, signupBinding.termsText);
        ChangeTypeface.setTypeface(this, signupBinding.signup);
    }
}
