package com.app_mo.animefaq;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app_mo.animefaq.device.DateTimeHelper;
import com.app_mo.animefaq.model.Question;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.util.ArrayList;
import java.util.List;

class MyAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<Question> questionList = new ArrayList<>();
    private boolean isClicked;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    MyAdapter(Context context, List<Question> questionList, boolean isClicked) {
        this.context = context;
        this.questionList = questionList;
        this.isClicked = isClicked;
    }

    @Override
    public int getItemViewType(int position) {
        return (questionList.get(position) != null ? VIEW_ITEM : VIEW_PROG);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;

        if (viewType == VIEW_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_questions_layout, parent, false);
            viewHolder = new MyViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.small_progressbar_layout, parent, false);
            viewHolder = new ProgressBarViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            Question q = questionList.get(position);

            if (q.getQuestion() != null) {
                if (q.getQuestion().length() >= 64) {
                    ((MyViewHolder) holder).text.setText(String.format("%s ...", q.getQuestion().substring(0, 64)));
                } else {
                    ((MyViewHolder) holder).text.setText(q.getQuestion());
                }
            }

            ((MyViewHolder) holder).answersNumber.setText(q.getAnswers()); //"2017-08-02 20:33:53"
            ((MyViewHolder) holder).quesTime.setReferenceTime(DateTimeHelper.convertToMiliseconds(q.getCreate_time()));
            ((MyViewHolder) holder).quesUser.setText(q.getUser());
            ((MyViewHolder) holder).views.setText(String.format("%s مشاهدات ", q.getViews()));

            if (q.getAnswered() != null) {
                if (!q.getAnswered().equals("0")) {
                    ((MyViewHolder) holder).acceptedAnswer.setImageResource(R.drawable.check_green);
                } else {
                    ((MyViewHolder) holder).acceptedAnswer.setImageResource(R.drawable.correct);
                }
            }
         } else {
            ((ProgressBarViewHolder) holder).progressBar.setProgress(10);
            ((ProgressBarViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return (questionList != null ? questionList.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
                    intent.putExtra("questionId", questionList.get(getLayoutPosition()).getqID());
                    intent.putExtra("questionAnswered", questionList.get(getLayoutPosition()).getAnswered());
                    intent.putExtra("from_post_reply", 0);
                    context.startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }

    private class ProgressBarViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        ProgressBarViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.small_progress_bar);
        }
    }
}
