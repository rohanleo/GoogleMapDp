package com.example.map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    DatabaseReference databaseMessages;
    Button sendMsg;
    EditText editMsg;
    ListView listView;
    List<ChatMessage>chatMessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.marker_chat_room);
        sendMsg = findViewById(R.id.sendbtn);
        editMsg = findViewById(R.id.chatmsg);
        listView = findViewById(R.id.listView);
        chatMessageList = new ArrayList<>();
        final String id = (String) getIntent().getSerializableExtra("markerId");
        databaseMessages = FirebaseDatabase.getInstance().getReference("messages").child(id);

        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editMsg.getText().toString();
                if(!message.equals(""))
                {
                    ChatMessage chatMessage = new ChatMessage(LoginActivity.userName,message);
                    chatMessageList.add(chatMessage);
                    editMsg.setText("");
                    String code = databaseMessages.push().getKey();
                    databaseMessages.child(code).setValue(chatMessage);
                    //System.out.println("Data Stored");
                    Toast.makeText(ChatActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(ChatActivity.this, "Type Something", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatMessageList.clear();
                for (DataSnapshot chat : dataSnapshot.getChildren()){
                    ChatMessage getchatMessage = chat.getValue(ChatMessage.class);
                    chatMessageList.add(getchatMessage);
                }
                ChatMessageAdapter chatMessageAdapter = new ChatMessageAdapter(ChatActivity.this,chatMessageList);
                listView.setAdapter(chatMessageAdapter);
                listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                listView.setStackFromBottom(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });

    }
}