package com.ahmadnawaz.i160020_150069;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

class RVAdapter extends RecyclerView.Adapter<RVAdapter.RVViewHolder> {
    List<ContactModel> ls;
    Context context;
    private _ON_ClickListener onNotelistener;
    private SparseBooleanArray selectedItems;
    private int Group_status;


    public RVAdapter(List<ContactModel> ls,Context context,_ON_ClickListener onNotelistener) {
        this.ls=ls;
        this.context=context;
        this.onNotelistener=onNotelistener;
        this.selectedItems=new SparseBooleanArray();
        this.Group_status=0;

    }

    @NonNull
    public RVAdapter.RVViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row= LayoutInflater.from(parent.getContext()).inflate(R.layout.row,parent,false);
        return new RVViewHolder(row,onNotelistener);
    }

    @Override
    public void onBindViewHolder(@NonNull RVAdapter.RVViewHolder holder, int position) {

        holder.myBackground.setSelected(selectedItems.get(position, false));

        holder.name.setText(ls.get(position).getName());
        holder.phno.setText(ls.get(position).getPhno());
        holder.address.setText(ls.get(position).getAddress());
        Glide.with(context).asBitmap().load(ls.get(position).getImage_url()).into(holder.ci);

        if(ls.get(position).getActivestatus()!=null && ls.get(position).getActivestatus().equals("true")){
            holder.notify_online.setVisibility(View.VISIBLE);
        }
        else{
            holder.notify_online.setVisibility(View.GONE);
        }


    }

    public void Active_group_status(){  // activating the background color to be selected for group from contact menu
        Group_status=1;
    }



    @Override
    public int getItemCount() {
        return ls.size();
    }

    public interface _ON_ClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }


    public class RVViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView name,phno,address;
        CircleImageView ci,notify_online;
        ImageView notify_select;
        _ON_ClickListener onNotelistener;
        int click_time;
        RelativeLayout myBackground;


        public RVViewHolder(@NonNull View itemView, _ON_ClickListener onNotelistener) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            phno=itemView.findViewById(R.id.phno);
            address=itemView.findViewById(R.id.address);
            ci=itemView.findViewById(R.id.ci);
            notify_select=itemView.findViewById(R.id.selected_row);
            notify_online=itemView.findViewById(R.id.notify_online_ci);
            myBackground=itemView.findViewById(R.id.row_layout_);
            click_time=0;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            this.onNotelistener=onNotelistener;

        }

        @Override
        public void onClick(View v) {
            if(Group_status==1) {
                if (selectedItems.get(getAdapterPosition(), false)) {
                    selectedItems.delete(getAdapterPosition());
                    myBackground.setSelected(false);
                } else {
                    selectedItems.put(getAdapterPosition(), true);
                    myBackground.setSelected(true);
                }
            }
            onNotelistener.onItemClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            onNotelistener.onItemLongClick(getAdapterPosition());
            return false;
        }
    }

    public void updatelist(List<ContactModel> newlist){
        ls=new ArrayList<>();
        ls.addAll(newlist);
        notifyDataSetChanged();
    }


}
