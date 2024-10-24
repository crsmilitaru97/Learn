package com.example.learn;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailText;
    private EditText passText;
    private ProgressDialog loadingBar;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            sendUserToMain();
    }

    private void sendUserToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    void LoginUser() {
        String email = emailText.getText().toString();
        String pass = passText.getText().toString();


        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is empty", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Password is empty", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Logging in");
            //loadingBar.setMessage();
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);

            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        loadingBar.dismiss();
                        sendUserToMain();
                    } else {
                        loadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, "Login error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailText = findViewById(R.id.txbEmailLogin);
        passText = findViewById(R.id.txbPasswordLogin);
        loadingBar = new ProgressDialog(this);

        final Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        ImageButton btnEye = findViewById(R.id.eyeButton);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }

        });

        btnEye.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        passText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        passText.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;
                }
                return true;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LoginUser();
            }
        });
    }
}