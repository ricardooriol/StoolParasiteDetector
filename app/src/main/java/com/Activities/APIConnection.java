package com.Activities;

import android.graphics.Bitmap;

public class APIConnection {

    public Object[] sendAndReceive(Bitmap bitmap) {

        Object[] results = new Object[2];
        MainActivity mainActivity = new MainActivity();
        TensorflowImageProcessor tensorflowImageProcessor = new TensorflowImageProcessor();
        int resultCode = 0;
        results[0] = bitmap;
        results[1] = true;

        if (mainActivity.internetConnection()) {
            results = tensorflowImageProcessor.processImage(results);
            if (results[1].equals(true)) {
                resultCode = 1;
            }

        } else if (results[1].equals(false)) {
            //IMAGE DOES NOT MEET REQUIRED SPECIFICATIONS
            resultCode = 2;

        } else if (!mainActivity.internetConnection()) {
            //NO INTERNET CONNECTION
            resultCode = 3;
        }

        results[1] = resultCode;
        return results;
    }

}