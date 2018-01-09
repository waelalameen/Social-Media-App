package com.app_mo.animefaq;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app_mo.animefaq.databinding.ActivityPostQuestionsBinding;
import com.app_mo.animefaq.model.PostInfo;
import com.app_mo.animefaq.network.MySingleton;
import com.app_mo.animefaq.network.NetworkInfo;
import com.app_mo.animefaq.storage.SavePreferences;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class PostQuestions extends AppCompatActivity {
    ActivityPostQuestionsBinding binding;
    private Gson gson;
    private PostInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_questions);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");
        setFonts();

        binding.next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewPost();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, MainActivity.class));
                break;
            default:
                break;
        }
        return true;
    }

    private void createNewPost() {
        String title = binding.title.getText().toString().trim();
        String question = binding.question.getText().toString().trim();
        String savedToken = new SavePreferences(this).getValues("userWebToken", "webToken");

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(question) || (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(question))) {
            gson = new Gson();
            info = new PostInfo(title, question, savedToken);

            final String jsonString = gson.toJson(info);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, NetworkInfo.HOST_URL + "questions/add",
                    new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("post_result", response);
                    info = gson.fromJson(response, PostInfo.class);

                    if (info.getResult().equalsIgnoreCase("Success")) {
                        startActivity(new Intent(PostQuestions.this, MainActivity.class));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        Log.i("VolleyError", error.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        e.printStackTrace();
                    }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
    }

    private void setFonts() {
        ChangeTypeface.setTypeface(this, binding.title);
        ChangeTypeface.setTypeface(this, binding.question);
        ChangeTypeface.setTypeface(this, binding.toolbarTitle);
    }
}
