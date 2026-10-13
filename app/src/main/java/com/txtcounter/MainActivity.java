package com.txtcounter;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private EditText editText;
    private LinearLayout lineContainer;
    private TextView charsTv, wordsTv, linesTv;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setTitle("Special thanks to Sir Nitin Kumar for making this amazing tool possible for us syed wajeeh ul hassan naqvi");

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);

        editText = new EditText(this);
        editText.setHint("Paste Large Text Here");
        editText.setSingleLine(false);
        editText.setMinLines(6);
        editText.setMaxLines(10);
        editText.setGravity(Gravity.TOP);
        editText.setVerticalScrollBarEnabled(true);
        editText.setHorizontallyScrolling(false);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        main.addView(editText);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.addView(btn("Paste", v -> paste()));
        bar.addView(btn("Clear", v -> editText.setText("")));
        bar.addView(btn("Line View", v -> lineView()));
        bar.addView(btn("Save TXT", v -> save()));
        main.addView(bar);

        ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1));
        lineContainer = new LinearLayout(this);
        lineContainer.setOrientation(LinearLayout.VERTICAL);
        sv.addView(lineContainer);
        main.addView(sv);

        charsTv = new TextView(this);
        wordsTv = new TextView(this);
        linesTv = new TextView(this);
        main.addView(charsTv);
        main.addView(wordsTv);
        main.addView(linesTv);
        setContentView(main);

        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int c, int d) {}
            public void onTextChanged(CharSequence s, int a, int c, int d) { updateCounters(); }
            public void afterTextChanged(Editable e) {}
        });
        updateCounters();
    }

    private Button btn(String t, View.OnClickListener l) {
        Button x = new Button(this);
        x.setText(t);
        x.setOnClickListener(l);
        return x;
    }

    private void paste() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = cm.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            CharSequence t = clip.getItemAt(0).coerceToText(this);
            if (t != null) editText.setText(t.toString());
        }
    }

    private void lineView() {
        lineContainer.removeAllViews();
        String text = editText.getText().toString().replace("\r\n", "\n");
        for (String line : text.split("\n", -1)) {
            TextView tv = new TextView(this);
            tv.setText(line);
            tv.setTextSize(18);
            tv.setPadding(10, 10, 10, 10);
            tv.setFocusable(true);
            tv.setClickable(true);
            lineContainer.addView(tv);
        }
    }

    private void updateCounters() {
        String text = editText.getText().toString();
        charsTv.setText("Characters: " + text.codePointCount(0, text.length()));
        String trimmed = text.trim();
        int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        wordsTv.setText("Words: " + words);
        linesTv.setText("Lines: " + text.replace("\r\n", "\n").split("\n", -1).length);
    }

    private void save() {
        String text = editText.getText().toString();
        if (text.isEmpty()) {
            Toast.makeText(this, "No text to save", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = "Text_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".txt";
        try {
            byte[] data = text.getBytes(StandardCharsets.UTF_8);
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                v.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                v.put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/TXT Counter");
                Uri u = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                try (OutputStream os = getContentResolver().openOutputStream(u)) { os.write(data); }
            } else {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return;
                }
                File dir = new File(Environment.getExternalStorageDirectory(), "TXT Counter");
                if (!dir.exists()) dir.mkdirs();
                try (FileOutputStream fos = new FileOutputStream(new File(dir, name))) { fos.write(data); }
            }
            Toast.makeText(this, "Large TXT Saved Successfully", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
