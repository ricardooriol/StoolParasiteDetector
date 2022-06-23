package com.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class License extends AppCompatActivity {

    Button goBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(v -> goBack());
        TextView licenseText = findViewById(R.id.licenseText);
        licenseText.setMovementMethod(new ScrollingMovementMethod());
    }

    private void goBack() {
        Intent intent = new Intent(this, com.Activities.MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}