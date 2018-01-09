package com.app_mo.animefaq;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app_mo.animefaq.device.DateTimeHelper;
import com.app_mo.animefaq.model.Reply;
import com.app_mo.animefaq.model.ReplyInfo;
import com.app_mo.animefaq.network.MySingleton;
import com.app_mo.animefaq.network.NetworkInfo;
import com.app_mo.animefaq.storage.SavePreferences;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.gson.Gson;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.MyViewHolder> {
    private Context context;
    private List<Reply> replyList;
    private static boolean isPressed = false;
    private Activity activity;
    private static String likeTrigger, dislikeTrigger;

    ReplyAdapter(Context context, List<Reply> replyList, Activity activity) {
        this.context = context;
        this.replyList = replyList;
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_replys_layout, parent, false);
        return new MyViewHolder(view, context, activity);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Reply reply = replyList.get(position);

        if (reply.getImg() != null) {
            if (!reply.getImg().equals("")) {
                Picasso.with(context).load(reply.getImg()).error(R.drawable.user).into(holder.userImage);
            }
        }

        holder.question.setText(reply.getReplayText());
        holder.info.setText(String.format("%s - ", reply.getUsername()));
        holder.tvTime.setReferenceTime(DateTimeHelper.convertToMiliseconds(reply.getCreate_time()));
        holder.likes.setText(reply.getLikes());
        holder.dislikes.setText(reply.getDislikes());

        likeTrigger = reply.getLike_trigger();
        dislikeTrigger = reply.getDislike_trigger();

        if (reply.getLike_trigger().equals("1")) {
            holder.likeButton.setImageResource(R.drawable.like_green);
        } else {
            holder.likeButton.setImageResource(R.drawable.like);
        }

        if (reply.getDislike_trigger().equals("1")) {
            holder.disLikeButton.setImageResource(R.drawable.dislike_green);
        } else {
            holder.disLikeButton.setImageResource(R.drawable.dislike);
        }
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView userImage;
        TextView info, likes, dislikes;
        ExpandableTextView question;
        RelativeTimeTextView tvTime;
        ImageButton likeButton, disLikeButton, menu;
        private Gson gson;
        private ReplyInfo replyInfo;
        private Context context;
        private String replyId = "33";

        MyViewHolder(View itemView, Context context, Activity activity) {
            super(itemView);

            this.context = context;

            userImage = (CircleImageView) itemView.findViewById(R.id.user_image);
            question = (ExpandableTextView) itemView.findViewById(R.id.expand_text_view);
            info = (TextView) itemView.findViewById(R.id.question_info);
            likes = (TextView) itemView.findViewById(R.id.like_number);
            dislikes = (TextView) itemView.findViewById(R.id.dislike_number);
            likeButton = (ImageButton) itemView.findViewById(R.id.like_button);
            disLikeButton = (ImageButton) itemView.findViewById(R.id.dislike_button);
            tvTime = (RelativeTimeTextView) itemView.findViewById(R.id.reply_time);
            menu = (ImageButton) itemView.findViewById(R.id.menu);

            menu.setOnClickListener(this);
            likeButton.setOnClickListener(this);
            disLikeButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.like_button:
                    if (likeTrigger.equals("1")) {
                        likeButton.setImageResource(R.drawable.like);
                    } else {
                        setLike();
                    }
                    break;
                case R.id.dislike_button:
                    if (dislikeTrigger.equals("1")) {
                        disLikeButton.setImageResource(R.drawable.dislike);
                    } else {
                        setDislike();
                    }
                    break;
                case R.id.menu:
                    showMenuDialog(context, menu);
                    break;
                default:
                    break;
            }
        }

        private void showMenuDialog(final Context context, ImageButton menuButton) {
            PopupMenu popUpMenu = new PopupMenu(context, menuButton);
            popUpMenu.getMenuInflater().inflate(R.menu.reply_menu, popUpMenu.getMenu());
            popUpMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getTitle().equals("تعديل")) {

                    }

                    return true;
                }
            });

            popUpMenu.show();
        }

        private void setLike() {
            String token = new SavePreferences(context).getValues("userWebToken", "webToken");
            final String likesNumber = likes.getText().toString();

            gson = new Gson();
            replyInfo = new ReplyInfo(replyId, token);
            final String jsonString = gson.toJson(replyInfo);

            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    NetworkInfo.HOST_URL + "replays/like", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("result", response);
                    replyInfo = gson.fromJson(response, ReplyInfo.class);

                    if (replyInfo.getResult().equalsIgnoreCase("Success")) {
                        int number = Integer.parseInt(likesNumber) + 1;
                        likes.setText(String.valueOf(number));
                        likeButton.setImageResource(R.drawable.like_green);
                    } else {
                        Log.d("error_like", "error");
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

        private void setDislike() {
            String token = new SavePreferences(context).getValues("userWebToken", "webToken");
            final String dislikesNumber = dislikes.getText().toString();

            gson = new Gson();
            replyInfo = new ReplyInfo(replyId, token);
            final String jsonString = gson.toJson(replyInfo);

            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    NetworkInfo.HOST_URL + "replays/dislike", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("result", response);
                    replyInfo = gson.fromJson(response, ReplyInfo.class);

                    if (replyInfo.getResult().equalsIgnoreCase("Success")) {
                        int number = Integer.parseInt(dislikesNumber) + 1;
                        dislikes.setText(String.valueOf(number));
                        disLikeButton.setImageResource(R.drawable.dislike_green);
                    } else {
                        Log.d("error_dislike", "error");
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

        private void removeLike() {
            String token = new SavePreferences(context).getValues("userWebToken", "webToken");
            final String likesNumber = likes.getText().toString();

            gson = new Gson();
            replyInfo = new ReplyInfo(replyId, token);
            final String jsonString = gson.toJson(replyInfo);

            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    NetworkInfo.HOST_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("result", response);
                    replyInfo = gson.fromJson(response, ReplyInfo.class);

                    if (replyInfo.getResult().equalsIgnoreCase("Success")) {
                        int number = Integer.parseInt(likesNumber) - 1;
                        likes.setText(String.valueOf(number));
                        likeButton.setImageResource(R.drawable.like);
                    } else {
                        Log.d("error_rm_like", "error");
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
                    map.put("info", jsonString);
                    return map;
                }
            };

            MySingleton.getInstance(context).addToRequestQueue(stringRequest);
        }

        private void removeDislike() {
            String token = new SavePreferences(context).getValues("userWebToken", "webToken");
            final String dislikesNumber = dislikes.getText().toString();

            gson = new Gson();
            replyInfo = new ReplyInfo(replyId, token);
            final String jsonString = gson.toJson(replyInfo);

            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    NetworkInfo.HOST_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("result", response);
                    replyInfo = gson.fromJson(response, ReplyInfo.class);

                    if (replyInfo.getResult().equalsIgnoreCase("Success")) {
                        int number = Integer.parseInt(dislikesNumber) - 1;
                        dislikes.setText(String.valueOf(number));
                        disLikeButton.setImageResource(R.drawable.dislike);
                    } else {
                        Log.d("error_rm_dislike", "error");
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
                    map.put("info", jsonString);
                    return map;
                }
            };

            MySingleton.getInstance(context).addToRequestQueue(stringRequest);
        }
    }
}
