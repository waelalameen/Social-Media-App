package com.app_mo.animefaq;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.app_mo.animefaq.model.NavModel;

import java.util.List;

/**
 * Created by hp on 8/7/2017.
 */

class NavAdapter extends RecyclerView.Adapter<NavAdapter.MyViewHolder> {
    private Context context;
    private List<NavModel> navModelList;
    private AdapterView.OnItemClickListener onItemClickListener;
    private int selectedPosition = 0;

    NavAdapter(Context context, List<NavModel> navModelList, AdapterView.OnItemClickListener onItemClickListener) {
        this.context = context;
        this.navModelList = navModelList;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_layout, parent, false);
        return new MyViewHolder(view, context, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        NavModel navModel = navModelList.get(position);
        holder.name.setText(navModel.getName());
        holder.icon.setImageResource(navModel.getIcon());

        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.itemView.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        return navModelList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        ImageView icon;
        AdapterView.OnItemClickListener onItemClickListener;

        MyViewHolder(View itemView, Context context, AdapterView.OnItemClickListener onItemClickListener) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.drawer_text);
            icon = (ImageView) itemView.findViewById(R.id.drawer_icon);
            this.onItemClickListener = onItemClickListener;

            ChangeTypeface.setTypeface(context, name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onItemClick(null, view, getLayoutPosition(), getItemId());
            notifyItemChanged(selectedPosition);
            selectedPosition = getLayoutPosition();
            itemView.setBackgroundColor(context.getResources().getColor(R.color.toolbarColor));
            notifyItemChanged(selectedPosition);
        }
    }
}
