package com.ahmadnawaz.i160020_150069;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayInputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.ahmadnawaz.i160020_150069.Chat.reciever_profile_url;
import static com.ahmadnawaz.i160020_150069.MainActivity.mAuth;


public class Message_Adapter extends RecyclerView.Adapter<Message_Adapter.MessageViewHolder> {
    List<MessageModel> ls;
    Context context;
    private Message_Adapter._ON_ClickListener onNotelistener;

    public Message_Adapter(List<MessageModel> ls,Context context,_ON_ClickListener onNotelistener) {
        this.ls=ls;
        this.context=context;
        this.onNotelistener=onNotelistener;
    }

    @NonNull
    public Message_Adapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row,parent,false);
        return new MessageViewHolder(row,onNotelistener);
    }

    @Override
    public void onBindViewHolder(@NonNull Message_Adapter.MessageViewHolder holder, int position) {


        if(ls.get(position).isSender(mAuth.getCurrentUser().getPhoneNumber())){ // if message is from sender
//            Toast.makeText(context, "Adapter in sender sender", Toast.LENGTH_SHORT).show();

            holder.receive_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Recieve message clicked", Toast.LENGTH_SHORT).show();
                }
            });

            holder.recieve_audio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Recieve audio clicked", Toast.LENGTH_SHORT).show();
                }
            });

            holder.receive_message.setVisibility(View.GONE);// hiding the receiving message
            holder.receive_image.setVisibility(View.GONE);// hiding the receiving message
            holder.send_image.setVisibility(View.GONE);// hiding the receiving message
            holder.send_message.setVisibility(View.GONE);// hiding the receiving message
            holder.recieve_audio.setVisibility(View.GONE);
            holder.send_audio.setVisibility(View.GONE);
            holder.reciever_profile.setVisibility(View.GONE);

            String message=ls.get(position).getMessage();
            if(ls.get(position).getImage_url()!=null){

//                    Toast.makeText(context, "Loading imgae"+ls.get(position).getImage_url(), Toast.LENGTH_SHORT).show();
                Glide.with(context).asBitmap().load(ls.get(position).getImage_url()).into(holder.send_image);
                holder.send_image.setVisibility(View.VISIBLE);// hiding the receiving message
            }
            if(message!=null) {
                holder.send_message.setVisibility(View.VISIBLE);// hiding the receiving message
                holder.send_message.setText(message);
            }

            if(ls.get(position).getVoiceurl()!=null){
//                Toast.makeText(context, "loading button", Toast.LENGTH_SHORT).show();
                holder.send_audio.setVisibility(View.VISIBLE);
            }

            holder.send_notifi.setVisibility(View.VISIBLE);
        }
        else {

            holder.receive_message.setVisibility(View.GONE);// hiding the receiving message
            holder.receive_image.setVisibility(View.GONE);// hiding the receiving message
            holder.send_image.setVisibility(View.GONE);// hiding the receiving message
            holder.send_message.setVisibility(View.GONE);// hiding the receiving message
            holder.recieve_audio.setVisibility(View.GONE);
            holder.send_audio.setVisibility(View.GONE);
            holder.send_notifi.setVisibility(View.GONE);

            if(reciever_profile_url!=null){ // to show the dp of receiver
                Glide.with(context).asBitmap().load(reciever_profile_url).into(holder.reciever_profile);
                holder.reciever_profile.setVisibility(View.VISIBLE);
            }

            String message = ls.get(position).getMessage();
            if (ls.get(position).getImage_url() != null) {
                Glide.with(context).asBitmap().load(ls.get(position).getImage_url()).into(holder.receive_image);
                holder.receive_image.setVisibility(View.VISIBLE);// hiding the receiving message
            }

            if (message != null) {
                holder.receive_message.setVisibility(View.VISIBLE);// hiding the receiving message
                holder.receive_message.setText(message);
            }
            if(ls.get(position).getVoiceurl()!=null){
                holder.recieve_audio.setVisibility(View.VISIBLE);// hiding the receiving message
            }

        }
    }

    @Override
    public int getItemCount() {
        return ls.size();
    }

    public interface _ON_ClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView send_message,receive_message;
        ImageView send_image,receive_image;
        CircleImageView reciever_profile,send_notifi,seen_notifi;
        Button send_audio,recieve_audio;
        _ON_ClickListener onNotelistener;
//        private SwipeRevealLayout swipelayout;

        public MessageViewHolder(@NonNull View itemView, _ON_ClickListener onNotelistener) {
            super(itemView);

            reciever_profile=itemView.findViewById(R.id.reciver_profile);


            send_message=itemView.findViewById(R.id.message_2);
            send_image=itemView.findViewById(R.id.iv2);
            receive_message=itemView.findViewById(R.id.message_1);
            receive_image=itemView.findViewById(R.id.iv1);
            send_notifi=itemView.findViewById(R.id.send_tick);
            seen_notifi=itemView.findViewById(R.id.seen_tick);


            send_audio=itemView.findViewById(R.id.audio_message_btn2);
            recieve_audio=itemView.findViewById(R.id.audio_message_btn1);

            send_audio.setOnClickListener(this);
            recieve_audio.setOnClickListener(this);
            receive_image.setOnClickListener(this);
            send_image.setOnClickListener(this);
//            recieve_audio.
            //itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            this.onNotelistener=onNotelistener;

        }

        @Override
        public void onClick(View v) {
            if(v==send_audio || v==recieve_audio){
                onNotelistener.onItemClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            onNotelistener.onItemLongClick(getAdapterPosition());
            return false;
        }
    }

}
