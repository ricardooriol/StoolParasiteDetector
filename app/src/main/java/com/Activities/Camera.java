package com.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Camera extends AppCompatActivity implements View.OnClickListener {

    private ImageView processedPicture;
    private TextView warningsText;
    private Bitmap bitmap;
    private String currentPhotoPath;
    private int flag = 0;
    private Object[] results = new Object[2];
    private Button goBackButton;
    private Button takePictureButton;
    private Button uploadPictureButton;
    private Button savePictureButton;
    private TextView microscopePicture;
    private ProgressBar progressBar;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(v -> goBack());
        takePictureButton = findViewById(R.id.takePictureButton);
        uploadPictureButton = findViewById(R.id.uploadPictureButton);
        savePictureButton = findViewById(R.id.savePictureButton);
        processedPicture = findViewById(R.id.processedPicture);
        microscopePicture = findViewById(R.id.microscopePicture);
        warningsText = findViewById(R.id.warningsText);
        progressBar = findViewById(R.id.progressBar);
        loadingText = findViewById(R.id.loadingText);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            TensorflowImageProcessor tensorflowImageProcessor = new TensorflowImageProcessor();
            if ((result.getResultCode() == RESULT_OK) && (result.getData() != null) && (flag == 1)) {
                bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                results[0] = bitmap;
                goBackButton.setVisibility(View.INVISIBLE);
                takePictureButton.setVisibility(View.INVISIBLE);
                uploadPictureButton.setVisibility(View.INVISIBLE);
                microscopePicture.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                loadingText.setVisibility(View.VISIBLE);
                new Thread(() -> {
                    results = tensorflowImageProcessor.processImage(results);
                    runOnUiThread(() -> displayBitmap(results));
                }).start();
            } else if ((result.getResultCode() == RESULT_OK) && (result.getData() != null) && (flag == 2)) {
                Intent data = result.getData();
                Uri imageUri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                results[0] = bitmap;
                goBackButton.setVisibility(View.INVISIBLE);
                takePictureButton.setVisibility(View.INVISIBLE);
                uploadPictureButton.setVisibility(View.INVISIBLE);
                microscopePicture.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                loadingText.setVisibility(View.VISIBLE);
                new Thread(() -> {
                    results = tensorflowImageProcessor.processImage(results);
                    runOnUiThread(() -> displayBitmap(results));
                }).start();
            }
        });

        takePictureButton.setOnClickListener(view -> {
            verifyPermissions();
            String fileName = "photo";
            File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            Intent intent = null;
            try {
                File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
                currentPhotoPath = imageFile.getAbsolutePath();
                Uri imageUri = FileProvider.getUriForFile(Camera.this, "com.Activities.FileProvider", imageFile);
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            flag = 1;
            activityResultLauncher.launch(intent);
        });

        uploadPictureButton.setOnClickListener(view -> {
            verifyPermissions();
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            flag = 2;
            activityResultLauncher.launch(intent);
        });

        savePictureButton.setOnClickListener(view -> {
            @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            String imageName = "processedImage_".concat(timeStamp).concat(".jpg");
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, imageName, "This image was processed By the Stoop Parasite Detector App.");
            savePictureButton.setVisibility(View.INVISIBLE);
            Snackbar snack = makeCustomSnackbar("The image was saved to your Camera Roll.");
            snack.show();
        });
    }

    @Override
    public void onClick(View view) {
    }

    private void goBack() {
        Intent intent = new Intent(this, com.Activities.MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    private void verifyPermissions() {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};
        if ((ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[1]) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[2]) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[3]) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[4]) == PackageManager.PERMISSION_GRANTED)) {

        } else {
            ActivityCompat.requestPermissions(Camera.this,
                    permissions,
                    1);
        }
    }

    private Snackbar makeCustomSnackbar(String text) {
        Snackbar snack = Snackbar.make(warningsText, text, Snackbar.LENGTH_INDEFINITE);
        View view = snack.getView();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params);
        snack.setAction("Dismiss", this);
        return snack;
    }


    private void displayBitmap(Object[] results) {
        bitmap = (Bitmap) results[0];
        int resultCode = (int) results[1];
        progressBar.setVisibility(View.INVISIBLE);
        loadingText.setVisibility(View.INVISIBLE);
        goBackButton.setVisibility(View.VISIBLE);
        if (resultCode == 1) {
            takePictureButton.setVisibility(View.INVISIBLE);
            uploadPictureButton.setVisibility((View.INVISIBLE));
            microscopePicture.setVisibility(View.INVISIBLE);
            processedPicture.setImageBitmap(bitmap);
            Snackbar snack = makeCustomSnackbar("Image successfully processed!");
            snack.show();
            savePictureButton.setVisibility(View.VISIBLE);

        } else if (resultCode == 2) {
            takePictureButton.setVisibility(View.INVISIBLE);
            uploadPictureButton.setVisibility((View.INVISIBLE));
            microscopePicture.setVisibility(View.INVISIBLE);
            processedPicture.setImageBitmap(bitmap);
            Snackbar snack = makeCustomSnackbar("No parasites were detected in your image.");
            snack.show();

        } else if (resultCode == 3) {
            Snackbar snack = makeCustomSnackbar("It seems you do not have a working internet connection, please try again later.");
            snack.show();

        } else {
            Snackbar snack = makeCustomSnackbar("There was an unexpected error, please try again later.");
            snack.show();
        }
        flag = 0;
    }
}