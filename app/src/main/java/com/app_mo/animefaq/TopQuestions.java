package com.app_mo.animefaq;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app_mo.animefaq.databinding.ActivityTopQuestionsBinding;
import com.app_mo.animefaq.model.MainQuestions;
import com.app_mo.animefaq.model.Question;
import com.app_mo.animefaq.network.MySingleton;
import com.app_mo.animefaq.network.NetworkInfo;
import com.app_mo.animefaq.storage.SavePreferences;
import com.github.pwittchen.infinitescroll.library.InfiniteScrollListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopQuestions extends AppCompatActivity {
    ActivityTopQuestionsBinding binding;
    private LinearLayoutManager linearLayoutManager;
    private int page = -1;
    private Gson gson;
    private MyAdapter adapter;
    private List<Question> questionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_top_questions);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");

        ChangeTypeface.setTypeface(this, binding.toolbarTitle);
        binding.toolbarTitle.setText("الاسئلة الاعلى تقييما");

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.requestLayout();
        binding.recyclerView.invalidateItemDecorations();
        linearLayoutManager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.addOnScrollListener(getInfiniteScroll());
        adapter = new MyAdapter(TopQuestions.this, questionList, true);
        binding.recyclerView.setAdapter(adapter);

        loadQuestions(page);

        binding.progressbarLayout.progressBar.setVisibility(View.VISIBLE);
        binding.progressbarLayout.progressBar.setProgress(10);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private InfiniteScrollListener getInfiniteScroll() {
        return new InfiniteScrollListener(4, linearLayoutManager) {
            @Override
            public void onScrolledToEnd(int firstVisibleItemPosition) {
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    page++;
                    loadQuestions(page);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        binding.fab.show();

                        if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                            binding.fab.hide();
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                binding.swipeRefreshLayout.setEnabled(linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        };
    }

    private void loadQuestions(int page) {
        String token = new SavePreferences(this).getValues("userWebToken", "webToken");
        gson = new Gson();
        MainQuestions info = new MainQuestions(token, String.valueOf(page));
        final String jsonString = gson.toJson(info);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                NetworkInfo.HOST_URL + "questions",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("resultQ", response);
                        binding.progressbarLayout.progressBar.setVisibility(View.GONE);

                        try {
                            gson = new Gson();
                            questionList.addAll(Arrays.asList(gson.fromJson(response, Question[].class)));

                            for (Question ques : questionList) {
                                if (ques.getResult().equalsIgnoreCase("Success")) {
                                    updateRecyclerView();
                                }
                            }

                        } catch (Exception e) {
                            Log.e("err", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Log.i("VolleyError", error.getMessage());
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("info", jsonString);
                Log.d("question_info", jsonString);
                return map;
            }
        };

        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void updateRecyclerView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            startActivity(new Intent(this, MainActivity.class));
        return true;
    }
}
