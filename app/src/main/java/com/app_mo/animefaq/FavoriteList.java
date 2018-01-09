package com.app_mo.animefaq;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app_mo.animefaq.databinding.ActivityFavoriteListBinding;
import com.app_mo.animefaq.device.DateTimeHelper;
import com.app_mo.animefaq.model.QuestionData;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class FavoriteList extends AppCompatActivity {
    private ActivityFavoriteListBinding binding;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_favorite_list);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");

        ChangeTypeface.setTypeface(this, binding.toolbarTitle);
        ChangeTypeface.setTypeface(this, binding.noData);
        binding.toolbarTitle.setText("الاسئلة المفضلة");

        binding.progressbarLayout.progressBar.setVisibility(View.VISIBLE);
        binding.progressbarLayout.progressBar.setProgress(10);

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.requestLayout();
        binding.recyclerView.invalidateItemDecorations();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(linearLayoutManager);

        loadSavedQuestions();

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                stopRefreshing();
            }
        });
    }

    private void loadSavedQuestions() {
        RealmResults<QuestionData> results = realm.where(QuestionData.class).findAllAsync();
        results.load();

        if (results.size() > 0) {
            binding.progressbarLayout.progressBar.setVisibility(View.GONE);
            binding.recyclerView.setAdapter(new Adapter(this, results, true));
        } else {
            binding.noDataLayout.setVisibility(View.VISIBLE);
            binding.progressbarLayout.progressBar.setVisibility(View.GONE);
        }
    }

    private void stopRefreshing() {
//        questionList.clear();
//        start = linearLayoutManager.findFirstVisibleItemPosition();
//        end = linearLayoutManager.findLastVisibleItemPosition() + 4;
//        loadQuestions(start, end);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
        private Context context;
        private List<QuestionData> questionList = new ArrayList<>();
        private boolean isClicked;

        Adapter(Context context, List<QuestionData> questionList, boolean isClicked) {
            this.context = context;
            this.questionList = questionList;
            this.isClicked = isClicked;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_questions_layout, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            QuestionData q = questionList.get(position);

            if (q.getQuestion() != null) {
                if (q.getQuestion().length() >= 64) {
                    holder.text.setText(String.format("%s ...", q.getQuestion().substring(0, 64)));
                } else {
                    holder.text.setText(q.getQuestion());
                }
            }

            holder.answersNumber.setText(q.getAnswers());
            holder.quesTime.setReferenceTime(DateTimeHelper.convertToMiliseconds(q.getCreate_time()));
            holder.quesUser.setText(q.getUser());
            holder.views.setText(String.format("%s مشاهدات ", q.getViews()));

            if (q.getAnswered() != null) {
                if (!q.getAnswered().equals("0")) {
                    holder.acceptedAnswer.setImageResource(R.drawable.check_green);
                } else {
                    holder.acceptedAnswer.setImageResource(R.drawable.correct);
                }
            }
        }

        @Override
        public int getItemCount() {
            return (questionList != null ? questionList.size() : 0);
        }

        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView acceptedAnswer;
            TextView text, answersNumber, quesUser, views, expandableText;
            RelativeTimeTextView quesTime;
            //RelativeLayout questionLayout = (RelativeLayout) itemView.findViewById(R.id.question_layout);

            MyViewHolder(View itemView) {
                super(itemView);

                acceptedAnswer = (ImageView) itemView.findViewById(R.id.accepted_answer);
                text = (TextView) itemView.findViewById(R.id.text);
                expandableText = (TextView) itemView.findViewById(R.id.expandable_text);
                answersNumber = (TextView) itemView.findViewById(R.id.answers_number);
                quesTime = (RelativeTimeTextView) itemView.findViewById(R.id.question_time);
                quesUser = (TextView) itemView.findViewById(R.id.question_user);
                views = (TextView) itemView.findViewById(R.id.views);

                //itemView.setOnClickListener(this);

                if (isClicked) {
                    text.setOnClickListener(this);
                }
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.text:
                        Intent intent = new Intent(context, QuestionDetails.class);
                        intent.putExtra("questionId", String.valueOf(questionList.get(getLayoutPosition()).getId()));
                        context.startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            startActivity(new Intent(this, MainActivity.class));
        return true;
    }
}
