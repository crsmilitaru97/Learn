package com.example.learn;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference UsersRef;
    private CircleImageView profileImage;
    private String currentUserID;
    private String downloadUrl;
    private StorageReference UserProfileImageRef;
    private Uri resultUri;
    private HashMap userMap;
    private ImageButton btnEye;
    private EditText passText;
    private boolean hasImage = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        Button btnNext = findViewById(R.id.btnNext);
        profileImage = findViewById(R.id.imgAvatar);
        loadingBar = new ProgressDialog(this);
        btnEye = findViewById(R.id.eyeButton);
        passText = findViewById(R.id.txbPassword);

        btnNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = ((EditText) findViewById(R.id.txbEmail)).getText().toString();
                String pass = passText.getText().toString();
                if (validateEmail(email) && validatePass(pass)) {
                    createNewAccount(email, pass);

                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, 1);
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
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // some conditions for the picture
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();
            // crop the image
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        // Get the cropped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {       // store the cropped image into result
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                hasImage = true;
                resultUri = result.getUri();
                Picasso.get().load(resultUri).placeholder(R.drawable.avatar).into(profileImage);
            } else {
                hasImage = false;
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void createNewAccount(String email, String pass) {
        loadingBar.setTitle("Creating new account");
        //loadingBar.setMessage();
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(false);

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    saveUserData();
                } else
                    Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData() {
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        String name = ((EditText) findViewById(R.id.txbName)).getText().toString();
        String country = ((EditText) findViewById(R.id.txbCountry)).getText().toString();
        String profession = ((EditText) findViewById(R.id.txbProfession)).getText().toString();
        Switch switchPrivate = findViewById(R.id.switchPrivate);
        String email = ((EditText) findViewById(R.id.txbEmail)).getText().toString();

        userMap = new HashMap();
        userMap.put("uid", currentUserID);
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("country", country);
        userMap.put("profession", profession);
        userMap.put("hasImage", hasImage);
        userMap.put("isPrivate", switchPrivate.isChecked());


        final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

        if (hasImage) {
            filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();

                            UsersRef.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                loadingBar.dismiss();

                                                if (task.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();


                                                    sendUserToInterests();
                                                } else
                                                    Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else
                                        Toast.makeText(RegisterActivity.this, "Error:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    });

                }
            });
        } else {
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    loadingBar.dismiss();                       
                    sendUserToInterests();
                    Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private boolean validateEmail(String emailInput) {
        if (!emailInput.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            return true;
        } else {
            Toast.makeText(RegisterActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean validatePass(String passInput) {
        if (!passInput.isEmpty()) {
            return true;
        } else {
            Toast.makeText(RegisterActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();

            return false;
        }
    }

    private void sendUserToInterests() {
        Intent intent = new Intent(RegisterActivity.this, InterestsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
