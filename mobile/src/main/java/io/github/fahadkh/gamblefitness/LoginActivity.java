package io.github.fahadkh.gamblefitness;
/**
 * Created by rovik on 2/15/2017.
 * Code adapted from AndroidHive
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

    // Email, password edittext
    EditText txtUsername, txtPassword;

    // login button
    Button btnLogin;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    // Session Manager Class
    SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Session Manager
        session = new SessionManager(getApplicationContext());

        // Email, Password input text
        txtUsername = (EditText) findViewById(R.id.txtUsername);

        Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();


        // Login button
        btnLogin = (Button) findViewById(R.id.btnLogin);


        // Login button click event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Get username, password from EditText
                String username = txtUsername.getText().toString();

                // Check if username is filled
                if(username.trim().length() > 0){
                        session.createLoginSession(username);

                        // Staring MainActivity
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        finish();

                    }
                else{
                    // user didn't entered username
                    // Show alert asking him to enter the details
                    alert.showAlertDialog(LoginActivity.this, "Login failed..", "Please enter a valid username", false);
                }

            }
        });
    }
}

