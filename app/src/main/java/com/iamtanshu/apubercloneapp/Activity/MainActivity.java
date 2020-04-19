package com.iamtanshu.apubercloneapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.iamtanshu.apubercloneapp.R;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText edtUserName, edtPassword, edtTypePerson;
    Button btn_SignUp, btn_OTL;
    RadioButton rb_Driver, rb_Passenger;
    RadioGroup rg_Type;


    enum State {
        SIGNUP, LOGIN
    }

    State state = State.SIGNUP;

    private static MainActivity instance = null;

    public static MainActivity getInstance() {
        instance = instance == null ? new MainActivity() : instance;
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ParseUser.getCurrentUser() != null) {
            ParseUser.logOut();
        }
        edtUserName = findViewById(R.id.edt_UserName);
        edtPassword = findViewById(R.id.edt_Password);
        edtTypePerson = findViewById(R.id.edt_PersonType);

        rg_Type = findViewById(R.id.rg_TypePerson);
        rb_Driver = findViewById(R.id.rb_Driver);
        rb_Passenger = findViewById(R.id.rb_Passenger);

        btn_SignUp = findViewById(R.id.btn_signUp);
        btn_OTL = findViewById(R.id.btn_OTL);
        btn_SignUp.setOnClickListener(this);
        btn_OTL.setOnClickListener(this);
        if (ParseUser.getCurrentUser() != null) {
            tranisitionToMain();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signup_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_up:
                state = state == State.SIGNUP ? State.LOGIN : State.SIGNUP;
                switch (state) {
                    case SIGNUP:
                        item.setTitle(getResources().getString(R.string.login));
                        btn_SignUp.setText(getResources().getString(R.string.sign_up));
                        break;
                    case LOGIN:
                        item.setTitle(getResources().getString(R.string.sign_up));
                        btn_SignUp.setText(getResources().getString(R.string.login));
                        break;
                }
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_signUp:
                final String username = edtUserName.getText().toString().trim();
                String password = edtUserName.getText().toString().trim();
                if (username.equals("") || password.equals("")) {
                    Toast.makeText(MainActivity.this, "Username and Password are required..", Toast.LENGTH_SHORT).show();
                    return;
                }


                switch (state) {
                    case SIGNUP:
                        if (rg_Type.getCheckedRadioButtonId() == -1) {
                            Toast.makeText(MainActivity.this, "Are you a driver or passenger?", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String type = rb_Driver.isChecked() ? "Driver" : "Passenger";
                        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                        dialog.setMessage("Signing up the user");
                        dialog.setCancelable(false);
                        dialog.show();

                        ParseUser user = new ParseUser();
                        user.setUsername(username);
                        user.setPassword(password);
                        user.put("as", type.trim());
                        user.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                dialog.dismiss();
                                if (e != null) {
                                    Toast.makeText(MainActivity.this, "Signing up user fail.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Toast.makeText(MainActivity.this, "User is sign up.", Toast.LENGTH_SHORT).show();
                                tranisitionToMain();
                            }
                        });
                        break;
                    case LOGIN:
                        final ProgressDialog loginDialog = new ProgressDialog(MainActivity.this);
                        loginDialog.setMessage("Logging-in..");
                        loginDialog.setCancelable(false);
                        loginDialog.show();
                        ParseUser.logInInBackground(username, password, new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                loginDialog.dismiss();
                                if (user == null || e != null) {
                                    Toast.makeText(MainActivity.this, "Invalid username and password", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Toast.makeText(MainActivity.this, user.getUsername() + " logged-in successfully.", Toast.LENGTH_SHORT).show();
                                tranisitionToMain();
                            }
                        });
                        break;
                }
                break;
            case R.id.btn_OTL:
                final String type = edtTypePerson.getText().toString().trim();
                if (type.equals("Driver") || type.equals("Passenger")) {
                    if (ParseUser.getCurrentUser() == null) {
                        ParseAnonymousUtils.logIn(new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (user != null && e == null) {
                                    Toast.makeText(MainActivity.this, "We have an anonymous user", Toast.LENGTH_SHORT).show();
                                    user.put("as", type);
                                    user.saveInBackground();
                                }

                            }
                        });
                    }
                }
                break;
        }
    }

    private void tranisitionToMain() {
        Intent intent = new Intent(MainActivity.this, TransitionActivity.class);
        startActivity(intent);
        finish();
    }

}
