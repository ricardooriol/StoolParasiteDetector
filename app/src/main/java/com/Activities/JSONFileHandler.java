package com.Activities;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class JSONFileHandler {
    private final Context context;

    public JSONFileHandler(Context context) {
        this.context = context;
    }

    public void loadJSON() {

        InputStream inputStream = null;
        try {
            inputStream = this.context.getAssets().open("json/parasites_label_map.pbtxt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        String rawMap = scanner.hasNext() ? scanner.next() : "";
        String processedMap = "{\n\t\"parasites\": [";

        processedMap += rawMap.replaceAll("item", "").
                replaceAll("(id:) ([0-9]+)", "\"id\": $2,").
                replaceAll("display_name:", "\"display_name\":").
                replaceAll("\\}\n\n \\{", "\\},\n\n \\{");
        processedMap += "\t ] \n\n }";

        File path = this.context.getFilesDir();
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(path, "parasites_label_map.json"));
            outputStream.write(processedMap.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readJSON(Context context) {
        String JSON = "";
        File path = context.getFilesDir();
        File readFrom = new File(path, "parasites_label_map.json");
        byte[] buffer = new byte[(int) readFrom.length()];
        try {
            FileInputStream is = new FileInputStream(readFrom);
            is.read(buffer);
            is.close();
            JSON = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSON;
    }
}