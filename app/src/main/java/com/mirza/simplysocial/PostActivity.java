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

public class PostActivity extends AppCompatActivity {

    private EditText editTextFilePath;
    private Button buttonCreatePost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        editTextFilePath = findViewById(R.id.editTextFilePath);
        buttonCreatePost = findViewById(R.id.buttonCreatePost);

        buttonCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String filePath = editTextFilePath.getText().toString();

                        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        String sessionToken = sharedPreferences.getString("session_token", null);

                        if (sessionToken == null) {
                            runOnUiThread(() -> Toast.makeText(PostActivity.this, "Not logged in!", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        try {
                            JSONObject jsonInputString = new JSONObject();
                            jsonInputString.put("mediaFile", filePath);

                            URL url = new URL("http://YOUR_BACKEND_IP:8080/api/posts/create");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setRequestProperty("Content-Type", "application/json; utf-8");
                            connection.setRequestProperty("Accept", "application/json");
                            connection.setRequestProperty("Authorization", sessionToken); // Add the header
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
                                Log.e("PostActivity", "Error reading response", e);
                                final String errorMessage = "Error reading response: " + e.getMessage();
                                runOnUiThread(() -> Toast.makeText(PostActivity.this, errorMessage, Toast.LENGTH_LONG).show());
                                return;
                            } finally {
                                connection.disconnect();
                            }

                            final String responseData = response.toString();
                            runOnUiThread(() -> {
                                try {
                                    JSONObject jsonResponse = new JSONObject(responseData);
                                    boolean success = jsonResponse.getBoolean("success");
                                    String message = jsonResponse.getString("message");
                                    Toast.makeText(PostActivity.this, message, Toast.LENGTH_LONG).show();
                                    if (success) {
                                        Toast.makeText(PostActivity.this, "Post created!", Toast.LENGTH_SHORT).show();
                                        // Optionally navigate back or clear the input
                                    } else {
                                        Toast.makeText(PostActivity.this, "Failed to create post: " + message, Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                    Log.e("PostActivity", "Error parsing JSON response", e);
                                    Toast.makeText(PostActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                        } catch (Exception e) {
                            Log.e("PostActivity", "Error making post request", e);
                            final String errorMessage = "Error making post request: " + e.getMessage();
                            runOnUiThread(() -> Toast.makeText(PostActivity.this, errorMessage, Toast.LENGTH_LONG).show());
                        }
                    }
                }).start();
            }
        });
    }
}
