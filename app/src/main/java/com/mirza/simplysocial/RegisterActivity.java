package com.mirza.simplysocial;

import android.os.Bundle;
import android.util.Log;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRealName;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextUsername=findViewById(R.id.editTextUsername);
        editTextPassword=findViewById(R.id.editTextPassword);
        editTextRealName=findViewById(R.id.editTextRealName);

        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(v -> {
            String realName = editTextRealName.getText().toString();
            String username = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            new Thread(() -> {
                try {
                    JSONObject jsonInputString = new JSONObject();
                    jsonInputString.put("realName",realName);
                    jsonInputString.put("username",username);
                    jsonInputString.put("password",password);

                    URL url = new URL("http://ideapad.tail50fddd.ts.net:8080/api/auth/register");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setRequestProperty("Accept","application/json");
                    connection.setDoOutput(true);

                    try(OutputStream os = connection.getOutputStream()){
                        byte[] input = jsonInputString.toString().getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    //Read the response from the server
                    try(BufferedReader br = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8))){
                                StringBuilder response = new StringBuilder();
                                String responseLine;
                                while((responseLine = br.readLine())!=null){
                                    response.append(responseLine.trim());
                        }
                                //Process the response on the main UI thread
                        final String responseData=response.toString();
                                runOnUiThread(() -> {
                                    try{
                                        JSONObject jsonResponse = new JSONObject(responseData);
                                        boolean success = jsonResponse.getBoolean("success");
                                        String message = jsonResponse.getString("message");
                                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                                        if(success){
                                            //Navigate to login
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(RegisterActivity.this,"Error Parsing Response:"+e.getMessage(),Toast.LENGTH_LONG).show();
                                        Log.e("RegisterActivity","Error parsing JSON response",e);
                                    }
                                });
                    } catch(IOException e){
                        final String errorMessage = "Error reading response: " + e.getMessage();
                        Log.e("RegisterActivity","Error Reading Response",e);
                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this,errorMessage,Toast.LENGTH_LONG).show());
                    }finally{
                        connection.disconnect();
                    }
                }catch (Exception e){
                    final String errorMessage = "Error making request: "+e.getMessage();
                    Log.e("RegisterActivity","Error making request",e);
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, errorMessage , Toast.LENGTH_LONG).show());
                }
            }).start();

            Toast.makeText(RegisterActivity.this,"Attempting Registration.....", Toast.LENGTH_SHORT).show();
            String data = String.join("\n", realName, username, password);
            Toast.makeText(RegisterActivity.this,data,Toast.LENGTH_LONG).show();
        });
    }
}
