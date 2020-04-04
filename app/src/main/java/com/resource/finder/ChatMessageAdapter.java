package com.resource.finder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {

    private Activity context;
    private List<ChatMessage>chat;

    public ChatMessageAdapter(Activity context,List<ChatMessage>chat){
        super(context,R.layout.message_layout,chat);
        this.context=context;
        this.chat=chat;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = context.getLayoutInflater();
        View listViewItem = layoutInflater.inflate(R.layout.message_layout,null,true);
        TextView sender = listViewItem.findViewById(R.id.sender);
        TextView message = listViewItem.findViewById(R.id.msg);

        ChatMessage chatMessage = chat.get(position);
        sender.setText(chatMessage.getSender());
        message.setText(chatMessage.getMessage());
        return listViewItem;
    }
}