package com.app_mo.animefaq;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app_mo.animefaq.device.DeviceInfo;
import com.app_mo.animefaq.model.MainQuestions;
import com.app_mo.animefaq.model.NavModel;
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

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {
    private LinearLayoutManager linearLayoutManager;
    private List<Question> questionList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private DrawerLayout drawer;
    private CoordinatorLayout contentLayout;
    private static int lastFirstVisiblePosition = 0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fab;
    private Gson gson;
    private ProgressBar progressBar, smallProgressBar;
    private MainQuestions mainQuestionsInfo;
    private int page = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        contentLayout = (CoordinatorLayout) findViewById(R.id.content);

        recyclerView = (RecyclerView) findViewById(R.id.main_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.requestLayout();
        linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        //linearLayoutManager.setStackFromEnd(true);
        recyclerView.invalidateItemDecorations();
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(getInfiniteScroll());
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.main_layout_swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.black),
                getResources().getColor(R.color.button1));

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        myAdapter = new MyAdapter(MainActivity.this, questionList, true);
        recyclerView.setAdapter(myAdapter);

        loadQuestions(page);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                stopRefreshing();
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(0);
            }
        });

//        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//
//                switch (newState) {
//                    case RecyclerView.SCROLL_STATE_IDLE:
//                        fab.show();
//
//                        //page++;
//                        //progressBar.setVisibility(View.VISIBLE);
//                        //progressBar.setProgress(100);
//                        //loadQuestions(page, firstVisibleItemPosition);
//
//                        if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
//                            fab.hide();
//                        }
//                        break;
//                    default:
//                        break;
//                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                swipeRefreshLayout.setEnabled(linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadDrawerData();

        ImageView userImg = (ImageView) findViewById(R.id.user_image_header);
        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Account.class));
            }
        });

        ((TextView) findViewById(R.id.name)).setText("Wael Alameen");
        ((TextView) findViewById(R.id.email)).setText("wael.almeen@gmail.com");
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
                    //recyclerView.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        fab.show();

                        if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                            fab.hide();
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                swipeRefreshLayout.setEnabled(linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        };
    }

    private void stopRefreshing() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    private void loadQuestions(int page) {
        String token = new SavePreferences(this).getValues("userWebToken", "webToken");
        gson = new Gson();
        mainQuestionsInfo = new MainQuestions(token, String.valueOf(page));
        final String jsonString = gson.toJson(mainQuestionsInfo);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                NetworkInfo.HOST_URL + "questions",
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("resultQ", response);
                progressBar.setVisibility(View.GONE);

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
                myAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastFirstVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("lastPos", String.valueOf(lastFirstVisiblePosition));
        //((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(lastFirstVisiblePosition, 0);
    }

    private void loadDrawerData() {
        RecyclerView rc = (RecyclerView) findViewById(R.id.drawer_recycler);
        rc.setHasFixedSize(true);
        rc.requestLayout();
        rc.setLayoutManager(new LinearLayoutManager(this));
        rc.setNestedScrollingEnabled(true);
        rc.invalidateItemDecorations();
        List<NavModel> navList = new ArrayList<>();
        navList.add(new NavModel("الاسئلة الاعلى تقييما", R.drawable.favi));
        navList.add(new NavModel("المفضلة", R.drawable.favi_list));
        navList.add(new NavModel("الضبط", R.drawable.settings));
        navList.add(new NavModel("مشاركة", R.drawable.share_list));
        navList.add(new NavModel("البحث", R.drawable.search));
        rc.setAdapter(new NavAdapter(this, navList, this));
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            DeviceInfo.exitFromApplication(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            startActivity(new Intent(this, PostQuestions.class));
        }

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        switch (i) {
            case 0:
                startActivity(new Intent(this, TopQuestions.class));
                break;
            case 1:
                startActivity(new Intent(this, FavoriteList.class));
                break;
            case 2:
                startActivity(new Intent(this, Settings.class));
                break;
            case 3:
                break;
            case 4:
                startActivity(new Intent(this, Search.class));
                break;
            default:
                break;
        }
    }
}
