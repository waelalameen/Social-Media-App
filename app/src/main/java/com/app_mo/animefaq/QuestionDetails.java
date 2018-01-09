package com.app_mo.animefaq;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app_mo.animefaq.databinding.ActivityQuestionDetailsBinding;
import com.app_mo.animefaq.device.DateTimeHelper;
import com.app_mo.animefaq.model.AddAnswerInfo;
import com.app_mo.animefaq.model.Answer;
import com.app_mo.animefaq.model.AnswersRequestInfo;
import com.app_mo.animefaq.model.EditQuestionInfo;
import com.app_mo.animefaq.model.QuestionData;
import com.app_mo.animefaq.model.QuestionDetailsInfo;
import com.app_mo.animefaq.model.QuestionInfo;
import com.app_mo.animefaq.network.MySingleton;
import com.app_mo.animefaq.network.NetworkInfo;
import com.app_mo.animefaq.storage.SavePreferences;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

public class QuestionDetails extends AppCompatActivity {
    ActivityQuestionDetailsBinding binding;
    private List<Answer> answerList = new ArrayList<>();
    private EditQuestionInfo info;
    private AnswersAdapter adapter;
    private static boolean isPressed = false;
    private static boolean isFavoritePressed = false;
    private Menu menu;
    private Gson gson;
    private String questionId = null, questionBody, questionTitle, questionUser, questionAnswered, questionViews, questionAnswers, questionCreatTime;
    private int start = 0;
    private int end = 0;
    private int fromPostReply = 0;
    private LinearLayoutManager manager;
    private View view1;
    private int numberOfAns = 0;
    private Realm realm;
    private SavePreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_question_details);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        questionId = getIntent().getExtras().getString("questionId");
        questionAnswered = getIntent().getExtras().getString("questionAnswered");
        fromPostReply = getIntent().getExtras().getInt("from_post_reply");

        preferences = new SavePreferences(this);
        preferences.save("pref_questionId", "questionId", questionId);
        preferences.save("pref_questionAnswered", "questionAnswered", questionAnswered);

        if (questionId != null) {
            Log.d("question_id", questionId);
        }

        String token = preferences.getValues("userWebToken", "webToken");

        getQuestionInfo(token);
        getData();

        start = manager.findFirstVisibleItemPosition();
        end = manager.findLastVisibleItemPosition() + 4;

        loadAnswers(start, end);

        binding.post.setEnabled(false);

        binding.post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = binding.comment.getText().toString().trim();
                view1 = view;

                if (!TextUtils.isEmpty(comment)) {
                    postAnswer(comment, view1);
                }
            }
        });

        binding.comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.post.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    binding.post.setEnabled(false);
                }
            }
        });

        ChangeTypeface.setTypeface(this, binding.questionUser);
        ChangeTypeface.setTypeface(this, binding.answersNumber);
        ChangeTypeface.setTypeface(this, binding.comment);
        ChangeTypeface.setTypeface(this, binding.noData);
    }

    private void postAnswer(String comment, final View view) {
        String token = new SavePreferences(this).getValues("userWebToken", "webToken");
        gson = new Gson();
        AddAnswerInfo info = new AddAnswerInfo(questionId, token, comment);
        final String jsonString = gson.toJson(info);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                NetworkInfo.HOST_URL + "answers/add",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String res = null;

                        try {
                            Log.d("post_answer", response);
                            gson = new Gson();
                            answerList.addAll(Arrays.asList(gson.fromJson(response, Answer[].class)));
                            adapter = new AnswersAdapter(QuestionDetails.this, answerList,
                                    QuestionDetails.this);

                            for (Answer ans : answerList) {
                                res = ans.getResult();

                                if (ans.getResult().equalsIgnoreCase("Success")) {
                                    binding.comment.setText("");
                                    binding.noDataLayout.setVisibility(View.GONE);
                                    binding.answersRecycler.setVisibility(View.VISIBLE);
                                    binding.answersRecycler.setAdapter(adapter);
                                    adapter.notifyItemRangeChanged(adapter.getItemCount(), answerList.size() - 1);
                                    //adapter.notifyDataSetChanged();
                                } else {
                                    Snackbar.make(view, "لا يمكنك الاجابة مرة اخرى", Snackbar.LENGTH_SHORT).show();
                                }
                            }

                            if (res.equalsIgnoreCase("Success")) {
                                binding.answersNumber.setText(String.format("%s اجابات ", String.valueOf(numberOfAns + 1)));
                                answerList.remove(answerList.size() - 2);
                                adapter.notifyItemRangeChanged(adapter.getItemCount(), answerList.size() - 1);
                            } else {
                                answerList.remove(answerList.size() - 1);
                                adapter.notifyItemRangeChanged(adapter.getItemCount(), answerList.size() - 1);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
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
                Map<String, String> map = new HashMap<>();
                map.put("answer_post", jsonString);
                return map;
            }
        };

        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void getQuestionInfo(String token) {
        gson = new Gson();
        QuestionDetailsInfo info = new QuestionDetailsInfo(questionId, token);
        final String jsonString = gson.toJson(info);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                NetworkInfo.HOST_URL + "questions/load",
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("result", response);

                try {
                    gson = new Gson();
                    QuestionInfo info = gson.fromJson(response, QuestionInfo.class);

                    binding.views.setText(String.format("%s مشاهدة ", info.getViews()));
                    binding.title.setText(info.getTitle());
                    binding.questionBody.setText(info.getText());

                    questionTitle = info.getTitle();
                    questionBody = info.getText();
                    questionUser = info.getUser();
                    questionViews = info.getViews();
                    questionAnswers = info.getAnswers();
                    questionCreatTime = info.getCreate_time();

                    if (info.getImg() != null) {
                        if (!TextUtils.isEmpty(info.getImg())) {
                            Picasso.with(QuestionDetails.this).load(info.getImg())
                                    .error(R.drawable.user).into(binding.userImage);
                            Picasso.with(QuestionDetails.this).load(info.getImg())
                                    .error(R.drawable.user).into(binding.userImagePost);
                        }
                    }

                    binding.questionUser.setText(info.getUser());
                    binding.questionTime.setReferenceTime(DateTimeHelper
                            .convertToMiliseconds(info.getCreate_time()));
                    binding.answersNumber.setText(String.format("%s اجابات ", info.getAnswers()));
                    numberOfAns = Integer.parseInt(info.getAnswers());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Log.i("VolleyError", error.getMessage());
                } catch (Exception err) {
                    try {
                        throw new Exception();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

    private void getData() {
        binding.answersRecycler.setHasFixedSize(true);
        manager = new LinearLayoutManager(this);
        binding.answersRecycler.setLayoutManager(manager);
        binding.answersRecycler.requestLayout();
        binding.answersRecycler.setNestedScrollingEnabled(true);

        binding.answersRecycler.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        if (manager.findLastCompletelyVisibleItemPosition() == end) {
                            start = end;
                            end = end + 4;
                            loadAnswers(start, end);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void loadAnswers(int start, int end) {
        String token = new SavePreferences(this).getValues("userWebToken", "webToken");

        gson = new Gson();
        AnswersRequestInfo info = new AnswersRequestInfo(questionId, String.valueOf(start), String.valueOf(end), token);
        final String jsonString = gson.toJson(info);
        binding.layout.progressBar.setVisibility(View.VISIBLE);
        binding.layout.progressBar.setProgress(100);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                NetworkInfo.HOST_URL + "answers",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("answers_result", response);

                            gson = new Gson();
                            answerList.addAll(Arrays.asList(gson.fromJson(response, Answer[].class)));

                            for (Answer ans : answerList) {
                                if (ans.getResult().equalsIgnoreCase("Success")) {
                                    binding.layout.progressBar.setVisibility(View.GONE);
                                    adapter = new AnswersAdapter(QuestionDetails.this, answerList, QuestionDetails.this);
                                    binding.answersRecycler.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    binding.answersRecycler.setVisibility(View.GONE);
                                    binding.noDataLayout.setVisibility(View.VISIBLE);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.questions_menu, menu);
        this.menu = menu;

        RealmResults<QuestionData> res = realm.where(QuestionData.class).equalTo("id", Integer.parseInt(questionId)).findAll();
        res.load();

        if (res.size() > 0) {
            this.menu.findItem(R.id.action_favorite).setIcon(R.drawable.favi_list_red);
        } else {
            this.menu.findItem(R.id.action_favorite).setIcon(R.drawable.favi_list);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (fromPostReply == 1) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                onBackPressed();
            }
        }

        if (item.getItemId() == R.id.action_top) {
            if (isPressed) {
                this.menu.findItem(R.id.action_top).setIcon(R.drawable.favi);
                isPressed = false;
            } else {
                this.menu.findItem(R.id.action_top).setIcon(R.drawable.favi_gold);
                isPressed = true;
            }
        }

        if (item.getItemId() == R.id.action_favorite) {
            RealmResults<QuestionData> res = realm.where(QuestionData.class).equalTo("id", Integer.parseInt(questionId)).findAll();
            res.load();

            if (res.size() > 0) {
                menu.findItem(R.id.action_favorite).setIcon(R.drawable.favi_list);
                removePost();
            } else {
                menu.findItem(R.id.action_favorite).setIcon(R.drawable.favi_list_red);
                addPost();
            }
        }

        if (item.getItemId() == R.id.action_edit) {
            showEditDialog();
        }

        return true;
    }

    private void addPost() {
        RealmResults<QuestionData> results = realm.where(QuestionData.class).equalTo("id", Integer.parseInt(questionId)).findAll();
        results.load();

        if (results.size() == 0) {
            realm.beginTransaction();

            QuestionData obj = realm.createObject(QuestionData.class, Integer.parseInt(questionId));
            //obj.setId(Integer.parseInt(questionId));
            obj.setQuestion(questionTitle);
            obj.setAnswered(questionAnswered);
            obj.setAnswers(questionAnswers);
            obj.setUser(questionUser);
            obj.setViews(questionViews);
            obj.setCreate_time(questionCreatTime);

            realm.commitTransaction();
            Toast.makeText(this, "تمت اضافة السؤال الى المفضلة", Toast.LENGTH_SHORT).show();
        } else {
            removePost();
        }
    }

    private void removePost() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<QuestionData> res = realm.where(QuestionData.class).equalTo("id", Integer.parseInt(questionId)).findAll();
                res.deleteAllFromRealm();
                Toast.makeText(QuestionDetails.this, "تم حذف السؤال من المفضلة", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_question_layout, null);
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.setContentView(view);

        final EditText tvTitle = (EditText) dialog.findViewById(R.id.title);
        final EditText tvQuestion = (EditText) dialog.findViewById(R.id.question);
        ImageButton next = (ImageButton) dialog.findViewById(R.id.next);

        ChangeTypeface.setTypeface(this, tvTitle);
        ChangeTypeface.setTypeface(this, tvQuestion);

        tvTitle.setText(questionTitle);
        tvQuestion.setText(questionBody);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = tvTitle.getText().toString().trim();
                String question = tvQuestion.getText().toString().trim();

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(question) || (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(question))) {
                    postUpdate(title, question);
                }
            }
        });

        dialog.show();
    }

    private void postUpdate(String title, String question) {
        String savedToken = new SavePreferences(this).getValues("userWebToken", "webToken");
        gson = new Gson();
        info = new EditQuestionInfo(questionId, title, question, savedToken);

        final String jsonString = gson.toJson(info);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, NetworkInfo.HOST_URL + "questions/edit",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("edit_result", response);
                        info = gson.fromJson(response, EditQuestionInfo.class);

                        if (info.getResult().equalsIgnoreCase("Success")) {
                            Intent intent = new Intent(QuestionDetails.this, QuestionDetails.class);
                            intent.putExtra("questionId", questionId);
                            startActivity(intent);
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

    @Override
    public void onBackPressed() {
        if (fromPostReply == 1) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            super.onBackPressed();
        }
    }
}
