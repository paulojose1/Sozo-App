package com.example.sozoapp.ui;

/**
 * The Login class implements methods to
 * to get user email and passwords input
 * and check with with database for validation.
 * Also has a method go to register screen
 * **/
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sozoapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    //Variables
    private FirebaseAuth auth;
    private Intent HomeActivity;

    //Widgets
    private EditText userEmail, userPassword;
    private Button btnLogin;
    private ProgressBar loginProgress;
    private TextView registerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize widgets
        userEmail = findViewById(R.id.email_register);
        userPassword = findViewById(R.id.password_field);
        btnLogin = findViewById(R.id.login_btn);
        loginProgress = findViewById(R.id.login_progress_bar);
        auth = FirebaseAuth.getInstance();
        HomeActivity = new Intent(this, Home.class);
        registerView = findViewById(R.id.register_view);


        registerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent registerActivity = new Intent(getApplicationContext(), Register.class);
                startActivity(registerActivity);
                finish();
            }
        });

        loginProgress.setVisibility(View.INVISIBLE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProgress.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.INVISIBLE);

                final String mail = userEmail.getText().toString();
                final String password = userPassword.getText().toString();

                if (mail.isEmpty() || password.isEmpty())
                {
                    showMessage("You forgot your Password or E-mail");
                    btnLogin.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);
                } else 
                {
                    signIn(mail, password);
                }
            }
        });



    }

    //Sign in method gets user input trough edit text and passes
    // to the database to be validated.
    private void signIn(String mail, String password)
    {

          auth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task)
              {
                  if (task.isSuccessful())
                  {
                      loginProgress.setVisibility(View.INVISIBLE);
                      btnLogin.setVisibility(View.VISIBLE);
                      updateUI();
                  } else
                  {
                      showMessage(task.getException().getMessage());
                      btnLogin.setVisibility(View.VISIBLE);
                      loginProgress.setVisibility(View.INVISIBLE);
                  }
              }
          });

    }

    //Update Ui method is an Intent calling method
    //if user is validated from the database this method is called
    //and send user to main class
    private void updateUI()
    {

        startActivity(HomeActivity);
        finish();

    }


    private void showMessage(String text)
    {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
        
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();

        if(user != null)
        {

            updateUI();

        }
    }


}
