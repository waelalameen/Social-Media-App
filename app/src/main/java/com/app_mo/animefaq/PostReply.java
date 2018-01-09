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
import com.app_mo.animefaq.databinding.ActivityPostReplyBinding;
import com.app_mo.animefaq.model.Reply;
import com.app_mo.animefaq.model.SendReplyInfo;
import com.app_mo.animefaq.network.MySingleton;
import com.app_mo.animefaq.network.NetworkInfo;
import com.app_mo.animefaq.storage.SavePreferences;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostReply extends AppCompatActivity {
    ActivityPostReplyBinding binding;
    private Gson gson;
    SendReplyInfo sendReplyInfo;
    private List<Reply> replyList = new ArrayList<>();
    private SavePreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_reply);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");

        preferences = new SavePreferences(this);

        ChangeTypeface.setTypeface(this, binding.toolbarTitle);
        ChangeTypeface.setTypeface(this, binding.comment);

        final String answerID = getIntent().getExtras().getString("answerID");

        binding.post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(binding.comment.getText().toString().trim())) {
                    sendReply(answerID, binding.comment.getText().toString().trim());
                }
            }
        });
    }

    private void sendReply(String answerId, String reply) {
        String token = new SavePreferences(this).getValues("userWebToken", "webToken");
        gson = new Gson();
        sendReplyInfo = new SendReplyInfo(answerId, reply, token);
        final String jsonString = gson.toJson(sendReplyInfo);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                NetworkInfo.HOST_URL + "replays/add", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("rpp_result", response);
                binding.comment.setText("");

                try {
                    gson = new Gson();
                    replyList.addAll(Arrays.asList(gson.fromJson(response, Reply[].class)));

                    for (Reply reply : replyList) {
                        if (reply.getResult().equalsIgnoreCase("Success")) {
                            String qID = preferences.getValues("pref_questionId", "questionId");
                            String qAnswered = preferences.getValues("pref_questionAnswered", "questionAnswered");
                            Intent in = new Intent(PostReply.this, QuestionDetails.class);
                            in.putExtra("questionId", qID);
                            in.putExtra("questionAnswered", qAnswered);
                            in.putExtra("from_post_reply", 1);
                            startActivity(in);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Log.i("VolleyError", error.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("info", jsonString);
                return map;
            }
        };

        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }
}
