package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.adapters.MessageAdapter;
import com.example.chatapp.model.Message;
import com.example.chatapp.model.PublicKey;
import com.example.chatapp.model.User;
import com.example.chatapp.utils.RSAUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private static String ENCRYPTED_MESSAGE_TYPE_S = "S";
    private static String ENCRYPTED_MESSAGE_TYPE_R = "R";

    CircleImageView profileImage;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    DatabaseReference keyDatabaseReference;
    String currentUserPublicKey;
    String currentUserN;
    String currentReceiverPublicKey;
    String currentReceiverN;

    ImageButton buttonSend;
    EditText textSend;

    MessageAdapter messageAdapter;
    List<Message> messages;

    RecyclerView recyclerView;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        buttonSend = findViewById(R.id.button_send);
        textSend = findViewById(R.id.textSend);

        intent = getIntent();
        final String userId = intent.getStringExtra("userId");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        keyDatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference("PublicKeys");
        keyDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PublicKey publicKey = snapshot.getValue(PublicKey.class);
                    if ((publicKey == null) || (firebaseUser == null) || (firebaseUser.getUid() == null) || (userId == null)) {
                        continue;
                    }
                    if ((publicKey.getUserId() != null) && publicKey.getUserId().equals(firebaseUser.getUid())) {
                        currentUserPublicKey = publicKey.getPublicKey();
                        currentUserN = publicKey.getN();
                    }
                    if ((publicKey.getUserId() != null) && publicKey.getUserId().equals(userId)) {
                        currentReceiverPublicKey = publicKey.getPublicKey();
                        currentReceiverN = publicKey.getN();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = textSend.getText().toString();
                if ((message != null) && !message.equals("")) {
                    sendMessage(firebaseUser.getUid(), userId, message, null, "Chats");
                    String currentUserEncryptedMessage = encryptMessage(message, currentUserPublicKey, currentUserN);
                    sendMessage(firebaseUser.getUid(), userId, currentUserEncryptedMessage, ENCRYPTED_MESSAGE_TYPE_S, "EncryptedChats");
                    String currentReceiverEncryptedMessage = encryptMessage(message, currentReceiverPublicKey, currentReceiverN);
                    sendMessage(firebaseUser.getUid(), userId, currentReceiverEncryptedMessage, ENCRYPTED_MESSAGE_TYPE_R, "EncryptedChats");
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                textSend.setText("");
            }
        });

        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("Users")
                .child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(MessageActivity.this).load(user.getImageURL()).into(profileImage);
                }

                readMessages(firebaseUser.getUid(), userId, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String message, String type, String database) {
        DatabaseReference databaseReference = FirebaseDatabase
                                                .getInstance()
                                                .getReference();

        if (database == null) {
            return;
        }
        HashMap<String, Object> hashMap = new HashMap<>();
        if (database.equals("Chats")) {
            hashMap.put("sender", sender);
            hashMap.put("receiver", receiver);
            hashMap.put("message", message);
        } else {
            hashMap.put("sender", sender);
            hashMap.put("receiver", receiver);
            hashMap.put("encryptedMessage", message);
            hashMap.put("encryptionType", type);
        }

        databaseReference.child(database).push().setValue(hashMap);
    }

    private String encryptMessage(String message, String key, String n) {
        return RSAUtils.encrypt(message.getBytes() , new BigInteger(key), new BigInteger(n)).toString();
    }

    private void readMessages(final String myId, final String userId, final String imageUrl) {
        messages = new ArrayList<>();

        databaseReference = FirebaseDatabase
                                .getInstance()
                                .getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if ((message.getReceiver().equals(myId) && message.getSender().equals(userId)) ||
                            (message.getReceiver().equals(userId) && message.getSender().equals(myId))) {
                        messages.add(message);
                    }
                }
                messageAdapter = new MessageAdapter(MessageActivity.this, messages, imageUrl);
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
