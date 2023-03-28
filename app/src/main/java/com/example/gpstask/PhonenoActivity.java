package com.example.gpstask;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PhonenoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String savedPhoneNumber=sharedPreferences.getString("phone_number","");
        if (!savedPhoneNumber.isEmpty()) {
            // Launch the second activity if a phone number is saved
            Intent intent = new Intent(PhonenoActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finish the first activity to prevent user from going back to it
            return; // Return early to prevent the rest of the code from executing
        }
        setContentView(R.layout.activity_phoneno);
        Button submit=findViewById(R.id.submit);
        EditText phoneno=findViewById(R.id.phoneno);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phone = phoneno.getText().toString();

                if (phone.isEmpty() || phone.length() < 10) {
                    phoneno.setError("Please enter a valid phone number.");
                    return;
                }
                if (phone.isEmpty() || phone.length() > 10) {
                    phoneno.setError("Please enter a valid phone number.");
                    return;
                }

                editor.putString("phone_number",phone);
                editor.apply();
                // If phone number is valid, pass it to the next activity
                Intent intent = new Intent(PhonenoActivity.this, MainActivity.class);
                intent.putExtra("phone_number", phone);
                startActivity(intent);
                finish();
            }
        });
    }
}