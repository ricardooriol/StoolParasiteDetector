package com.Activities;

import android.Manifest;
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

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Button savePictureButton;
    private ImageView takenPicture;
    private TextView cameraIntroductionText;
    private TextView warningsText;
    private Bitmap bitmap;
    private String currentPhotoPath;
    private int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Button goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(v -> goBack());
        Button takePictureButton = findViewById(R.id.takePictureButton);
        Button uploadPictureButton = findViewById(R.id.uploadPictureButton);
        savePictureButton = findViewById(R.id.savePictureButton);
        takenPicture = findViewById(R.id.takenPicture);
        cameraIntroductionText = findViewById((R.id.cameraIntroductionText));
        warningsText = findViewById(R.id.warningsText);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Object[] results;
            APIConnection APIConnection = new APIConnection();

            if ((result.getResultCode() == RESULT_OK) && (result.getData() != null) && flag == 1) {
                bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                results = APIConnection.sendAndReceive(bitmap);
                bitmap = (Bitmap) results[0];
                int resultCode = (int) results[1];
                if (resultCode == 1) {
                    cameraIntroductionText.setVisibility(View.INVISIBLE);
                    takenPicture.setImageBitmap(bitmap);
                    Snackbar snack = makeCustomSnackbar("Image successfully processed!");
                    snack.show();
                    savePictureButton.setVisibility(View.VISIBLE);

                } else if (resultCode == 2) {
                    Snackbar snack = makeCustomSnackbar("The image does not meet the required specifications, please try a different one.");
                    snack.show();

                } else if (resultCode == 3) {
                    Snackbar snack = makeCustomSnackbar("It seems you do not have a working internet connection, please try again.");
                    snack.show();

                } else {
                    Snackbar snack = makeCustomSnackbar("There was an unexpected error, please try again.");
                    snack.show();
                }
                flag = 0;

            } else if ((result.getResultCode() == RESULT_OK) && (result.getData() != null) && flag == 2) {
                Intent data = result.getData();
                Uri imageUri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                results = APIConnection.sendAndReceive(bitmap);
                bitmap = (Bitmap) results[0];
                int resultCode = (int) results[1];
                if (resultCode == 1) {
                    cameraIntroductionText.setVisibility(View.INVISIBLE);
                    takenPicture.setImageBitmap(bitmap);
                    Snackbar snack = makeCustomSnackbar("Image successfully processed!");
                    snack.show();
                    savePictureButton.setVisibility(View.VISIBLE);

                } else if (resultCode == 2) {
                    Snackbar snack = makeCustomSnackbar("The image does not meet the required specifications, please try a different one.");
                    snack.show();

                } else if (resultCode == 3) {
                    Snackbar snack = makeCustomSnackbar("It seems you do not have a working internet connection, please try again.");
                    snack.show();

                } else if (resultCode == 0) {
                    Snackbar snack = makeCustomSnackbar("There was an unexpected error, please try again.");
                    snack.show();
                }
                flag = 0;
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
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            String imageName = "processedImage_".concat(timeStamp).concat(".jpg");
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, imageName, "This image was processed By the Stoop Parasite Detector App.");
            savePictureButton.setVisibility(View.INVISIBLE);
            Snackbar snack = makeCustomSnackbar("The image was saved to your Camera Roll.");
            snack.show();
        });
    }

    private void goBack() {
        Intent intent = new Intent(this, com.Activities.MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

    @Override
    public void onClick(View view) {
    }

}