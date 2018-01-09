package com.app_mo.animefaq;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app_mo.animefaq.device.DateTimeHelper;
import com.app_mo.animefaq.model.AcceptedInfo;
import com.app_mo.animefaq.model.Answer;
import com.app_mo.animefaq.model.Reply;
import com.app_mo.animefaq.model.ReplyRequestInfo;
import com.app_mo.animefaq.model.SendReplyInfo;
import com.app_mo.animefaq.model.VoteInfo;
import com.app_mo.animefaq.network.MySingleton;
import com.app_mo.animefaq.network.NetworkInfo;
import com.app_mo.animefaq.storage.SavePreferences;
import com.facebook.FacebookSdk;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.github.pwittchen.infinitescroll.library.InfiniteScrollListener;
import com.google.gson.Gson;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.MyViewHolder> {
    private List<Answer> answerList;
    private Context context;
    private Activity activity;
    private String lastReply, userReply, timeReply;

    AnswersAdapter(Context context, List<Answer> answerList, Activity activity) {
        this.context = context;
        this.answerList = answerList;
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.answers_layout, parent, false);
        return new MyViewHolder(view, context, activity);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Answer a = answerList.get(position);
        holder.votesNumber.setText(a.getVotes());
        holder.expandableTextView.setText(a.getAnswerText());

        if (a.getImg() != null) {
            if (!a.getImg().equals("")) {
                Picasso.with(context).load(a.getImg())
                        .error(R.drawable.user)
                        .into(holder.userImage);
            }
        }

        holder.questionUser.setText(a.getUsername());

        if (!a.getCreate_time().equals("")) {
            holder.questionTime.setReferenceTime(DateTimeHelper.convertToMiliseconds(a.getCreate_time()));
        } else {
            holder.questionTime.setText("Just Now");
        }

        if (a.getAnswered() != null) {
            if (a.getAnswered().equalsIgnoreCase(a.getAnswerID())) {
                holder.correct.setImageResource(R.drawable.check_green);
            } else {
                holder.correct.setImageResource(R.drawable.correct);
            }
        }

        if (a.getReplay() != null) {
            if (!TextUtils.isEmpty(a.getReplay().getReplayText())) {
                lastReply = a.getReplay().getReplayText();
            } else {
                lastReply = "";
            }
        }

        if (a.getReplay() != null) {
            if (!TextUtils.isEmpty(a.getReplay().getReplayUsername())) {
                userReply = a.getReplay().getReplayUsername();
            } else {
                userReply = "";
            }
        }

        if (a.getReplay() != null) {
            if (!TextUtils.isEmpty(a.getReplay().getReplayCreate_Time())) {
                timeReply = a.getReplay().getReplayCreate_Time();
            } else {
                timeReply = "";
            }
        }

        if (!lastReply.equals("") && !userReply.equals("") && !timeReply.equals("")) {
            holder.lastReply.setText(lastReply + " - " +
                    Html.fromHtml("<font color=\"#0000ff\">" + userReply +"</font>"));
            holder.lastReplyTime.setReferenceTime(DateTimeHelper.convertToMiliseconds(timeReply));
        } else {
            holder.secondLayout.setVisibility(View.GONE);
            holder.noDataLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return (answerList != null ? answerList.size() : 0);
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        RelativeLayout secondLayout, holder;
        LinearLayout noDataLayout;
        ImageView upVote, correct;
        CircleImageView userImage;
        TextView votesNumber, questionUser, lastReply, loadMore, noData;
        RelativeTimeTextView questionTime, lastReplyTime;
        ImageButton postButton;
        ExpandableTextView expandableTextView;
        private Context context;
        private Gson gson;
        private VoteInfo voteInfo;
        SendReplyInfo sendReplyInfo;
        List<Reply> replyList = new ArrayList<>();
        ReplyRequestInfo requestInfo;
        private RecyclerView rc;
        private ReplyAdapter adapter;
        private AutoCompleteTextView com;
        private Activity activity;
        AcceptedInfo acceptedInfo;
        private boolean isPressed = false;
        private int page = -1;
        private LinearLayoutManager manager;
        private String answerID, answerText;
        private ProgressBar progressBar;

        MyViewHolder(View itemView, Context context, Activity activity) {
            super(itemView);

            this.context = context;
            this.activity = activity;

            FacebookSdk.sdkInitialize(context);

            secondLayout = (RelativeLayout) itemView.findViewById(R.id.second_layout);
            noDataLayout = (LinearLayout) itemView.findViewById(R.id.no_data_layout);
            noData = (TextView) itemView.findViewById(R.id.no_data);
            upVote = (ImageView) itemView.findViewById(R.id.up_vote);
            correct = (ImageView) itemView.findViewById(R.id.correct);
            userImage = (CircleImageView) itemView.findViewById(R.id.user_image);
            votesNumber = (TextView) itemView.findViewById(R.id.votes_number);
            expandableTextView = (ExpandableTextView) itemView.findViewById(R.id.expand_text_view);
            questionUser = (TextView) itemView.findViewById(R.id.question_user);
            questionTime = (RelativeTimeTextView) itemView.findViewById(R.id.question_time);
            lastReplyTime = (RelativeTimeTextView) itemView.findViewById(R.id.last_reply_time);
            lastReply = (TextView) itemView.findViewById(R.id.last_reply);
            loadMore = (TextView) itemView.findViewById(R.id.load_more);
            postButton = (ImageButton) itemView.findViewById(R.id.post);
            holder = (RelativeLayout) itemView.findViewById(R.id.relative_holder_layout);

            ChangeTypeface.setTypeface(context, questionUser);
            ChangeTypeface.setTypeface(context, loadMore);
            ChangeTypeface.setTypeface(context, noData);

            loadMore.setOnClickListener(this);
            upVote.setOnClickListener(this);
            correct.setOnClickListener(this);
            holder.setOnLongClickListener(this);
            noData.setOnClickListener(this);
         }

        @Override
        public void onClick(View view) {
            answerID = answerList.get(getLayoutPosition()).getAnswerID();
            answerText = answerList.get(getLayoutPosition()).getAnswerText();
            String user = answerList.get(getLayoutPosition()).getUsername();
            String time = answerList.get(getLayoutPosition()).getCreate_time();
            String votes = answerList.get(getLayoutPosition()).getVotes();
            String isAccepted = answerList.get(getLayoutPosition()).getAnswered();

            switch (view.getId()) {
                case R.id.load_more:
                    showWindow(context, answerID, answerText, user, time, votes, isAccepted);
                    break;
                case R.id.up_vote:;
                    setUpVote(answerID, view);
                    break;
                case R.id.correct:
                    if (isPressed) {
                        isPressed = false;
                        setNotCorrect(view, answerID);
                    } else {
                        isPressed = true;
                        setCorrect(view, answerID);
                    }
                    break;
                case R.id.no_data:
                    Intent in = new Intent(context, PostReply.class);
                    in.putExtra("answerID", answerID);
                    context.startActivity(in);
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean onLongClick(View view) {
            showMenuDialog(context, expandableTextView);
            return false;
        }

        private void showMenuDialog(Context context, ExpandableTextView expandableTextView) {
            PopupMenu popUpMenu = new PopupMenu(context, expandableTextView);
            popUpMenu.getMenuInflater().inflate(R.menu.user_menu, popUpMenu.getMenu());
            popUpMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getTitle().equals("ابلاغ")) {

                    }

                    return true;
                }
            });

            popUpMenu.show();
        }

        private void setCorrect(final View view, String answerID) {
            String token = new SavePreferences(context).getValues("userWebToken", "webToken");

            gson = new Gson();
            acceptedInfo = new AcceptedInfo(answerID, token);
            Log.d("answerID", String.valueOf(answerID));

            final String jsonString = gson.toJson(acceptedInfo);

            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    NetworkInfo.HOST_URL + "answers/mark",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d("result_answer_put", response);
                                acceptedInfo = gson.fromJson(response, AcceptedInfo.class);

                                if (acceptedInfo.getResult().equalsIgnoreCase("Success")) {
                                    correct.setImageResource(R.drawable.check_green);
                                    new SavePreferences(context).save("correct_answer", "isAnswered", "true");
                                    Snackbar.make(view, "لقد قمت بتحديد الاجابة المقبولة",
                                            Snackbar.LENGTH_SHORT).show();
                                } else {
                                    if (acceptedInfo.getMsg().equalsIgnoreCase("Only person who asked the question can mark as answered !")) {
                                        Snackbar.make(view, "فقط صاحب السؤال يمكنه تحديد الاجابة المقبولة", Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        Snackbar.make(view, "لا يمكن تحديد اجابة مقبولة اخرى", Snackbar.LENGTH_SHORT).show();
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
                        e.getMessage();
                        e.printStackTrace();
                    }
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("accepted_answer", jsonString);
                    return map;
                }
            };

            MySingleton.getInstance(context).addToRequestQueue(stringRequest);
        }

        private void setNotCorrect(final View view, String answerID) {
            String token = new SavePreferences(context).getValues("userWebToken", "webToken");

            gson = new Gson();
            acceptedInfo = new AcceptedInfo(answerID, token);
            Log.d("answerID", String.valueOf(answerID));

            final String jsonString = gson.toJson(acceptedInfo);

            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    NetworkInfo.HOST_URL + "answers/unmark",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d("result_answer_remove", response);
                                acceptedInfo = gson.fromJson(response, AcceptedInfo.class);

                                if (acceptedInfo.getResult().equalsIgnoreCase("Success")) {
                                    correct.setImageResource(R.drawable.correct);
                                    new SavePreferences(context).save("correct_answer", "isAnswered", "false");
                                    Snackbar.make(view, "لقد قمت بازالة تحديد الاجابة المقبولة", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    if (acceptedInfo.getMsg().equalsIgnoreCase("Only person who asked the question can mark as answered !")) {
                                        Snackbar.make(view, "فقط صاحب السؤال يمكنه تحديد الاجابة المقبولة", Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        Snackbar.make(view, "لا يمكن تحديد اجابة مقبولة اخرى", Snackbar.LENGTH_SHORT).show();
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
                        e.getMessage();
                        e.printStackTrace();
                    }
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("accepted_answer", jsonString);
                    return map;
                }
            };

            MySingleton.getInstance(context).addToRequestQueue(stringRequest);
        }

        private void setUpVote(String answerID, final View view) {
            String token = new SavePreferences(context).getValues("userWebToken", "webToken");

            gson = new Gson();
            voteInfo = new VoteInfo(answerID, token);
            final String jsonString = gson.toJson(voteInfo);

            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    NetworkInfo.HOST_URL + "answers/vote",
                    new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Log.d("result_vote", response);
                        voteInfo = gson.fromJson(response, VoteInfo.class);

                        if (voteInfo.getResult().equalsIgnoreCase("Success")) {
                            votesNumber.setText(voteInfo.getVotes());
                            Snackbar.make(view, "قمت بالتصويت للاجابة", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(view, "لا يمكن التصويت على الاجابة مرتين", Snackbar.LENGTH_SHORT).show();
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
                    map.put("upvote_info", jsonString);
                    return map;
                }
            };

            MySingleton.getInstance(context).addToRequestQueue(stringRequest);
        }

        private void showWindow(final Context context, final String answerID, String answerText,
                                final String user, String time, String votes, String isAccepted) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View view = inflater.inflate(R.layout.replies_layout, null);
            builder.setView(view);
            builder.setCancelable(true);
            builder.setRecycleOnMeasureEnabled(true);
            final Dialog dialog = builder.create();
            dialog.setContentView(view);

            ExpandableTextView expandableTextView = (ExpandableTextView) dialog
                    .findViewById(R.id.expand_text_view);
            expandableTextView.setText(answerText);

            TextView tvUser = (TextView) dialog.findViewById(R.id.question_user);
            tvUser.setText(user);

            RelativeTimeTextView tvTime = (RelativeTimeTextView) dialog.findViewById(R.id.question_time);
            tvTime.setReferenceTime(DateTimeHelper.convertToMiliseconds(time));

            TextView tvVotes = (TextView) dialog.findViewById(R.id.votes_number);
            tvVotes.setText(votes);

            ImageView acceptButton = (ImageView) dialog.findViewById(R.id.correct);

            if (isAccepted != null) {
                if (isAccepted.equalsIgnoreCase(answerID)) {
                    acceptButton.setImageResource(R.drawable.check_green);
                } else {
                    acceptButton.setImageResource(R.drawable.correct);
                }
            }

            ImageView shareButton = (ImageView) dialog.findViewById(R.id.share);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shareToSocialMedia(context);
                }
            });

            com = (AutoCompleteTextView) dialog.findViewById(R.id.comment);
            ChangeTypeface.setTypeface(context, com);

            progressBar = (ProgressBar) dialog.findViewById(R.id.progress_bar);

            rc = (RecyclerView) dialog.findViewById(R.id.reply_recycler);
            rc.setHasFixedSize(true);
            rc.requestLayout();
            manager = new LinearLayoutManager(context);
            rc.setLayoutManager(manager);
            rc.addOnScrollListener(getInfiniteScroll());
            rc.setItemViewCacheSize(20);
            rc.setDrawingCacheEnabled(true);
            rc.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

            loadReplies(page);

            ImageButton post = (ImageButton) dialog.findViewById(R.id.post);

            post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String reply = com.getText().toString().trim();

                    if (!TextUtils.isEmpty(reply)) {
                        sendReply(answerID, reply);
                    }
                }
            });

            Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            dialog.getWindow().setLayout(size.x, size.y - 300);
            dialog.show();

            dialog.setCanceledOnTouchOutside(true);

            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    dialogInterface.dismiss();
                }
            });
        }

        private void shareToSocialMedia(Context context) {
            List<Intent> targetShareIntents = new ArrayList<>();
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            PackageManager pm = activity.getPackageManager();
            List<ResolveInfo> resInfos = pm.queryIntentActivities(shareIntent, 0);
            if (!resInfos.isEmpty()) {
                System.out.println("Have package");
                for (ResolveInfo resInfo : resInfos) {
                    String packageName = resInfo.activityInfo.packageName;
                    Log.i("Package Name", packageName);

                    if (packageName.contains("com.twitter.android") ||
                            packageName.contains("com.facebook.katana")
                            || packageName.contains("com.whatsapp") ||
                            packageName.contains("com.google.android.apps.plus")
                            || packageName.contains("com.google.android.talk") ||
                            packageName.contains("com.slack")
                            || packageName.contains("com.google.android.gm") ||
                            packageName.contains("com.facebook.orca")
                            || packageName.contains("com.yahoo.mobile") ||
                            packageName.contains("com.skype.raider")
                            || packageName.contains("com.android.mms")||
                            packageName.contains("com.linkedin.android")
                            || packageName.contains("com.google.android.apps.messaging")) {
                        Intent intent = new Intent();

                        intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                        intent.putExtra("AppName", resInfo.loadLabel(pm).toString());
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, answerText);
                        //intent.putExtra(Intent.EXTRA_SUBJECT, answerText);
                        intent.setPackage(packageName);
                        targetShareIntents.add(intent);
                    }
                }
                if (!targetShareIntents.isEmpty()) {
                    Collections.sort(targetShareIntents, new Comparator<Intent>() {
                        @Override
                        public int compare(Intent o1, Intent o2) {
                            return o1.getStringExtra("AppName").compareTo(o2.getStringExtra("AppName"));
                        }
                    });

                    Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0),
                            "اختر تطبيق للمشاركة");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                            targetShareIntents.toArray(new Parcelable[]{}));
                    this.context.startActivity(chooserIntent);
                } else {
                    Toast.makeText(activity, "لا توجد تطبيقات للمشاركة", Toast.LENGTH_LONG).show();
                }
            }
        }

        private InfiniteScrollListener getInfiniteScroll() {
            return new InfiniteScrollListener(4, manager) {
                @Override
                public void onScrolledToEnd(int firstVisibleItemPosition) {
                    int visibleItemCount = manager.getChildCount();
                    int totalItemCount = manager.getItemCount();
                    int pastVisibleItems = manager.findFirstVisibleItemPosition();

                    if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                        page++;
                        loadReplies(page);
                    }
                }
            };
        }

        private void sendReply(String answerId, String reply) {
            String token = new SavePreferences(context).getValues("userWebToken", "webToken");
            gson = new Gson();
            sendReplyInfo = new SendReplyInfo(answerId, reply, token);
            final String jsonString = gson.toJson(sendReplyInfo);

            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    NetworkInfo.HOST_URL + "replays/add", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("rp_result", response);
                    com.setText("");

                    try {
                        gson = new Gson();
                        replyList.addAll(Arrays.asList(gson.fromJson(response, Reply[].class)));

                        for (Reply reply : replyList) {
                            if (reply.getResult().equalsIgnoreCase("Success")) {
                                ReplyAdapter adapter = new ReplyAdapter(context, replyList, activity);
                                rc.setAdapter(adapter);
                                adapter.notifyItemRangeChanged(adapter.getItemCount(), replyList.size() - 1);
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

            MySingleton.getInstance(context).addToRequestQueue(stringRequest);
        }

        private void loadReplies(int page) {
            gson = new Gson();
            String token = new SavePreferences(context).getValues("userWebToken", "webToken");
            requestInfo = new ReplyRequestInfo(answerID, String.valueOf(page), token);
            final String jsonString = gson.toJson(requestInfo);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(10);
            progressBar.setIndeterminate(true);

            if (replyList.size() < 4) {
                replyList.clear();
            }

            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    NetworkInfo.HOST_URL + "replays", new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d("replies_response", response);
                    try {
                        gson = new Gson();
                        replyList.addAll(Arrays.asList(gson.fromJson(response, Reply[].class)));
                        adapter = new ReplyAdapter(context, replyList, activity);
                        rc.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
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

            MySingleton.getInstance(context).addToRequestQueue(stringRequest);
        }
    }
}
