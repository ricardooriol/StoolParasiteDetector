package com.Activities;

import static android.graphics.Color.BLACK;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ListOfParasites extends AppCompatActivity {

    Button goBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_parasites);
        goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(v -> goBack());

        TextView Trichuris = findViewById(R.id.Trichuris);
        Trichuris.setMovementMethod(LinkMovementMethod.getInstance());
        Trichuris.setLinkTextColor(BLACK);
        stripUnderlines(Trichuris);

        TextView Ascaris = findViewById(R.id.Ascaris);
        Ascaris.setMovementMethod(LinkMovementMethod.getInstance());
        Ascaris.setLinkTextColor(BLACK);
        stripUnderlines(Ascaris);

        TextView Uncinaria = findViewById(R.id.Uncinaria);
        Uncinaria.setMovementMethod(LinkMovementMethod.getInstance());
        Uncinaria.setLinkTextColor(BLACK);
        stripUnderlines(Uncinaria);

        TextView Diphyllobothrium = findViewById(R.id.Diphyllobothrium);
        Diphyllobothrium.setMovementMethod(LinkMovementMethod.getInstance());
        Diphyllobothrium.setLinkTextColor(BLACK);
        stripUnderlines(Diphyllobothrium);

        TextView Taenia = findViewById(R.id.Taenia);
        Taenia.setMovementMethod(LinkMovementMethod.getInstance());
        Taenia.setLinkTextColor(BLACK);
        stripUnderlines(Taenia);

        TextView Balantidium = findViewById(R.id.Balantidium);
        Balantidium.setMovementMethod(LinkMovementMethod.getInstance());
        Balantidium.setLinkTextColor(BLACK);
        stripUnderlines(Balantidium);

        TextView Hymenolepis = findViewById(R.id.Hymenolepis);
        Hymenolepis.setMovementMethod(LinkMovementMethod.getInstance());
        Hymenolepis.setLinkTextColor(BLACK);
        stripUnderlines(Hymenolepis);

        TextView Enterobius = findViewById(R.id.Enterobius);
        Enterobius.setMovementMethod(LinkMovementMethod.getInstance());
        Enterobius.setLinkTextColor(BLACK);
        stripUnderlines(Enterobius);

        TextView Amoeba = findViewById(R.id.Amoeba);
        Amoeba.setMovementMethod(LinkMovementMethod.getInstance());
        Amoeba.setLinkTextColor(BLACK);
        stripUnderlines(Amoeba);

        TextView Giardia = findViewById(R.id.Giardia);
        Giardia.setMovementMethod(LinkMovementMethod.getInstance());
        Giardia.setLinkTextColor(BLACK);
        stripUnderlines(Giardia);
    }

    private void goBack() {
        Intent intent = new Intent(this, com.Activities.MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

    private class URLSpanNoUnderline extends URLSpan {
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