package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.chatapp.utils.RSAUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

public class RegisterActivty extends AppCompatActivity {

    MaterialEditText username, email, password;
    Button register_button;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    DatabaseReference rsaKeysDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register_button = findViewById(R.id.button_register);

        firebaseAuth = FirebaseAuth.getInstance();

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameText = username.getText().toString();
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();

                if (TextUtils.isEmpty(usernameText) || TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)) {
                    Toast.makeText(RegisterActivty.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (passwordText.length() < 6) {
                    Toast.makeText(RegisterActivty.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    register(usernameText, emailText, passwordText);
                }


            }
        });

    }

    private void register(final String username, String email, String password) {

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            String userId = firebaseUser.getUid();

                            databaseReference = FirebaseDatabase
                                    .getInstance()
                                    .getReference("Users")
                                    .child(userId);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userId);
                            hashMap.put("username", username);
                            hashMap.put("imageUrl", "default");

                            generateRsaPublicAndPrivateKey(userId);

                            databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(RegisterActivty.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivty.this, "You can't register with this email or password", Toast.LENGTH_LONG).show();
                        }
                    }
                });



    }

    private void generateRsaPublicAndPrivateKey(String userId) {
        rsaKeysDatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        List<BigInteger> userKeys = RSAUtils.generateKeys();
        if ((userKeys == null) || (userKeys.isEmpty())) {
            return;
        }
        if (userId != null) {
            hashMap.put("userId", userId);
        }
        if (userKeys.get(0) != null) {
            hashMap.put("n", userKeys.get(0).toString());
        }
        if (userKeys.get(1) != null) {
            hashMap.put("publicKey", userKeys.get(1).toString());
        }
        rsaKeysDatabaseReference.child("PublicKeys").push().setValue(hashMap);

        hashMap.clear();
        if (userId != null) {
            hashMap.put("userId", userId);
        }
        if (userKeys.get(2) != null) {
            hashMap.put("privateKey", userKeys.get(2).toString());
        }
        rsaKeysDatabaseReference.child("PrivateKeys").push().setValue(hashMap);
    }
}
