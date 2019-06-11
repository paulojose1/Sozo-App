package com.example.sozoapp.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sozoapp.R;
import com.example.sozoapp.models.CreateUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Register extends AppCompatActivity {

    private static final int REQUESTCODE =10323 ;
    private  static final int PReqCode = 23512;
    ImageView userPhoto;
    EditText userEmail, userPassword, userPassword2, userName;
    private ProgressBar loadingbar;
    private Button registerButton;
    String userId, email, name, password;
    Uri pickedImageUri;

   FirebaseAuth auth;
   FirebaseUser currentUser;
   FirebaseFirestore firestoreDB;
   DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userEmail = findViewById(R.id.email_register);
        userPassword = findViewById(R.id.password_register);
        userPassword2 = findViewById(R.id.password_register2);
        userName = findViewById(R.id.name_register);
        loadingbar = findViewById(R.id.progressBar);
        registerButton = findViewById(R.id.registerButton);
        userPhoto = findViewById(R.id.reg_user_photo);
        Intent myIntent = getIntent();



        auth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        currentUser = auth.getCurrentUser();


        loadingbar.setVisibility(View.INVISIBLE);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerButton.setVisibility(View.INVISIBLE);
                loadingbar.setVisibility(View.VISIBLE);
                final String email = userEmail.getText().toString();
                final String password = userPassword.getText().toString();
                final String password2 = userPassword2.getText().toString();
                final String name = userName.getText().toString();



                if (email.isEmpty() || name.isEmpty() || password.isEmpty() || !password.equals(password2))
                {
                    showMessage("Please fill in all fields!");
                    registerButton.setVisibility(View.VISIBLE);
                    loadingbar.setVisibility(View.INVISIBLE);
                } else
                {
                    CreateUserAccount(email, name, password);
                }
            }
        });


        userPhoto = findViewById(R.id.reg_user_photo);



    }

    private void CreateUserAccount(final String email, final String name, final String password )
    {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showMessage("Account Created");
                            updateUserInfo(name, pickedImageUri, auth.getCurrentUser());


                        } else {
                            showMessage("Account creation failed");
                            registerButton.setVisibility(View.VISIBLE);
                            loadingbar.setVisibility(View.INVISIBLE);
                        }
                    }

                });


    }

    //update user photo and name
    private void updateUserInfo(final String name, Uri pickedImageUri, final FirebaseUser currentUser)
    {
        //upload user photo to firebase and get URL back
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("user_photos");
        final StorageReference imageFilePath = mStorage.child(pickedImageUri.getLastPathSegment());
        imageFilePath.putFile(pickedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {

                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();



                        currentUser.updateProfile(profileChangeRequest)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {

                                        if (task.isSuccessful())
                                        {

                                            CreateUser createUser = new CreateUser(userName.getText().toString(), userEmail.getText().toString(),
                                                    userPassword.getText().toString(),currentUser.getPhotoUrl().toString(), currentUser.getUid());


                                            addUser(createUser);

                                        }

                                    }
                                });
                    }
                });

            }
        });


    }

    private void addUser(CreateUser createUser)
    {

        DocumentReference newUserRef = firestoreDB.collection("Users")
                .document(auth.getUid());


        newUserRef.set(createUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    showMessage("User Created");
                    updateUI();
                } else
                {
                    showMessage("Failed. Check Log");
                }
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users").push();


        myRef.setValue(createUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid)
            {
                showMessage("Register Complete");
                updateUI();
            }
        });
    }


    private void updateUI()
    {
        Intent home = new Intent(getApplicationContext(), Home.class);
        startActivity(home);
        finish();




    }

    //method to show toast message
    private void showMessage(String message)
    {
        Toast.makeText(getApplicationContext(), message,Toast.LENGTH_LONG).show();
    }

    public void registerImage(View v)
    {
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (Build.VERSION.SDK_INT >= 22)
                {
                    checkAndRequestPermision();
                } else
                {
                    openGallery();
                }
            }
        });
    }
    private void openGallery()
    {
        //todo : open gallery and wait for user to choose an image.
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESTCODE);
    }
    private void checkAndRequestPermision()
    {

        if (ContextCompat.checkSelfPermission(Register.this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Register.this, Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                Toast.makeText(Register.this, "Please accept required permissons", Toast.LENGTH_SHORT).show();
            } else
            {
                ActivityCompat.requestPermissions(Register.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }
        } else
        {
            openGallery();
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null)
        {
            //user has succefully picked image - save reference to Uri variable
            pickedImageUri = data.getData();
            userPhoto.setImageURI(pickedImageUri);


        }
    }




}
