package com.Activities;

import static android.graphics.Color.BLACK;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.URLSpan;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class ListOfParasites extends AppCompatActivity {

    private final MainActivity mainActivity = new MainActivity();
    Button goBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_parasites);
        goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(v -> goBack());
        TextView listOfParasites = findViewById(R.id.listOfParasites);
        listOfParasites.setText(getParasiteList());
        listOfParasites.setMovementMethod(new ScrollingMovementMethod());
        TextView CDCWebsite = findViewById(R.id.CDCWebsiteText);
        CDCWebsite.setMovementMethod(LinkMovementMethod.getInstance());
        CDCWebsite.setLinkTextColor(BLACK);
        stripUnderlines(CDCWebsite);
    }

    private void goBack() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private String getParasiteList() {
        StringBuilder parasitesFullList = new StringBuilder();
        String parasiteName;
        int id;
        try {
            String JSON = JSONFileHandler.readJSON(mainActivity.getContext());
            JSONObject object = new JSONObject(JSON);
            JSONArray jsonArray = object.getJSONArray("parasites");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject parasite = jsonArray.getJSONObject(i);
                id = (Integer) parasite.get("id");
                parasiteName = (String) parasite.get("display_name");
                parasitesFullList.append(id).append(".- ").append(parasiteName).append("\n\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parasitesFullList.toString();
    }

    private void stripUnderlines(TextView textView) {
        Spannable s = new SpannableString(textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    private static class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }

    }
}