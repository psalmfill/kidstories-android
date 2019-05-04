package com.dragonlegend.kidstories;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.dragonlegend.kidstories.Api.ApiInterface;
import com.dragonlegend.kidstories.Api.Client;
import com.dragonlegend.kidstories.Api.Responses.LoginResponse;
import com.dragonlegend.kidstories.Database.Helper.BedTimeDbHelper;
import com.dragonlegend.kidstories.Model.User;
import com.pixplicity.easyprefs.library.Prefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    EditText mEmailField, mPasswordField;
    Button mLoginButton;
    String mEmail, mPassword;
    String lg = "lg";

    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;

    FrameLayout mHolder_ProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailField = findViewById(R.id.login_email);
        mPasswordField = findViewById(R.id.login_password);
        mHolder_ProgressBar = (FrameLayout) findViewById(R.id.progressHolder);
        mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
                mHolder_ProgressBar.setVisibility(View.VISIBLE);
                Toast.makeText(Login.this, "Login Clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    public void doLogin(){
        mEmail = mEmailField.getText().toString().trim();
        mPassword = mPasswordField.getText().toString().trim();

        String token = Prefs.getString("reg_token", "");
        if(validate()){

            Client.getInstance().create(ApiInterface.class).loginUser(token,mEmail,mPassword).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    Log.d("TAG", "onResponse: " + response.body());
                    if(response.isSuccessful()){

                        LoginResponse userResponse = response.body();
                        Log.d("TAG", "userResponse: -> " + userResponse.getData().getToken() );
                        Prefs.putString("token", "Bearer " +userResponse.getData().getToken());
                        Prefs.putBoolean("isLoggedIn", true);
                        Log.d("TAG", "tokenResponse: -> " + Prefs.getString("token", ""));
                        BedTimeDbHelper dbHelper = new BedTimeDbHelper(Login.this);
                        //dbHelper.addUser(userResponse.getData());


                        Toast.makeText(Login.this,"Login Successful",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this,Home.class);
                        intent.putExtra(Config.USER_ID,userResponse.getData().getId());
                        startActivity(intent);
                    }else{
                        Toast.makeText(Login.this,"Invalid Login details",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Log.d("TAG", "onFailure: " + t.getMessage());

                }
            });

        }


    }
    public boolean validate(){
        if(mEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()){
            mEmailField.setError(getString(R.string.invalid_email_address_error));
            return false;
        }
        if (mPassword.isEmpty()){
            mPasswordField.setError("Empty password");
            return false;
        }
        return true;

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, Home.class);
        startActivity(i);
        finish();
    }
}
