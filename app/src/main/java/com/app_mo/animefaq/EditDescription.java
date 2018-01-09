package com.app_mo.animefaq;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
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
import com.app_mo.animefaq.databinding.ActivityEditDescriptionBinding;
import com.app_mo.animefaq.model.DescriptionInfo;
import com.app_mo.animefaq.network.MySingleton;
import com.app_mo.animefaq.network.NetworkInfo;
import com.app_mo.animefaq.storage.SavePreferences;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class EditDescription extends AppCompatActivity implements View.OnClickListener {
    ActivityEditDescriptionBinding binding;
    Gson gson;
    DescriptionInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_description);
        setFonts();

        binding.next.setOnClickListener(this);
        binding.back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next:
                sendData(binding.description.getText().toString().trim());
                break;
            case R.id.back:
                startActivity(new Intent(this, Account.class));
                break;
            default:
                break;
        }
    }

    private void sendData(String text) {
        if (!TextUtils.isEmpty(text)) {
            String id = new SavePreferences(this).getValues("userId", "id");

            gson = new Gson();
            info = new DescriptionInfo(id, text);
            final String jsonString = gson.toJson(info);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, NetworkInfo.HOST_URL + "user/login",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("result", response);
                            info = gson.fromJson(response, DescriptionInfo.class);

                            if (info.getResult().equalsIgnoreCase("Success")) {
                                startActivity(new Intent(EditDescription.this, Account.class));
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.getMessage();
                    Dialogs.makeDialog(EditDescription.this, error.networkResponse.toString(), error.getMessage());
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

    private void setFonts() {
        ChangeTypeface.setTypeface(this, binding.description);
        ChangeTypeface.setTypeface(this, binding.titleText);
        ChangeTypeface.setTypeface(this, binding.bodyText);
    }
}
