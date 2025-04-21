package com.mirza.simplysocial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextLoginUsername;
    private EditText editTextLoginPassword;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextLoginUsername=findViewById(R.id.editTextLoginUsername);
        editTextLoginPassword=findViewById(R.id.editTextLoginPassword);
        buttonLogin=findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = editTextLoginUsername.getText().toString();
                final String password = editTextLoginPassword.getText().toString();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonInputString = new JSONObject();
                            jsonInputString.put("username", username);
                            jsonInputString.put("password", password);

                            URL url = new URL("http://ideapad.tail50fddd.ts.net:8080/api/auth/login");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setRequestProperty("Content-Type", "application/json; utf-8");
                            connection.setRequestProperty("Accept", "application/json");
                            connection.setDoOutput(true);

                            try (OutputStream os = connection.getOutputStream()) {
                                byte[] input = jsonInputString.toString().getBytes(StandardCharsets.UTF_8);
                                os.write(input, 0, input.length);
                            }
                            StringBuilder response = new StringBuilder();
                            try (BufferedReader br = new BufferedReader(
                                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                                String responseLine;
                                while ((responseLine = br.readLine()) != null) {
                                    response.append(responseLine.trim());
                                }
                            } catch (IOException e) {
                                Log.e("LoginActivity", "Error reading response: " + e.getMessage());
                                final String errorMessage = "Error reading response: " + e.getMessage();
                                runOnUiThread(() -> Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show());
                                return;
                            }finally {
                                connection.disconnect();
                            }
                            final String responseData = response.toString();
                            runOnUiThread(()->{
                                try{
                                    JSONObject jsonResponse = new JSONObject(responseData);
                                    boolean success = jsonResponse.getBoolean("success");
                                    String message = jsonResponse.getString("message");
                                    Toast.makeText(LoginActivity.this,message,Toast.LENGTH_LONG).show();
                                    if(success){
                                        String sessionToken=jsonResponse.getString("sessionToken");
                                        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs",MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("session_token",sessionToken);
                                        editor.apply();
                                        Toast.makeText(LoginActivity.this, "Login Successful! Token: "+sessionToken, Toast.LENGTH_LONG).show();
                                        //TODO: Navigate to main feed activity
                                    }
                                } catch (Exception e) {
                                    Log.e("LoginActivity", "Error parsing JSON response",e);
                                    Toast.makeText(LoginActivity.this,"Error parsing response"+e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });
                        }catch (Exception e){
                            Log.e("LoginActivity","Error making Login request: "+e.getMessage(),e);
                            final String errorMessage = "Error making login request: "+e.getMessage();
                            runOnUiThread(()-> Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show());
                        }
                    }
                }).start();
            }
        });

        //TODO: Improve navigation to registration activity (Button -> Link)
        Button goToRegisterButton = findViewById(R.id.goToRegisterButton);
        if(goToRegisterButton!=null){
            goToRegisterButton.setOnClickListener(v->{
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            });
        }
        Button goToPostButton = findViewById(R.id.goToPostButton);
        if(goToPostButton!=null){
            goToPostButton.setOnClickListener(v ->{
                startActivity(new Intent(LoginActivity.this,PostActivity.class));
            });
        }
    }
}