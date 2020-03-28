package com.codoprobe.imagelocker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.codoprobe.imagelocker.utility.ChainRepository;

public class SplashScreen extends AppCompatActivity {

    private static final int READ_STORAGE_PERMISSION_CODE = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermission();

    }

    public void checkPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("Image Locker needs to access your storage to retrieve locked files. 😊😊")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(SplashScreen.this, "Oops!! Cannot proceed without the permission", Toast.LENGTH_SHORT).show();
                                SplashScreen.this.finish();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(SplashScreen.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_CODE);
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_STORAGE_PERMISSION_CODE);
            }
        } else { openMainActivity(); }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case READ_STORAGE_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openMainActivity();
                }
                else {
                    Toast.makeText(SplashScreen.this, "Oops!! Cannot proceed without the permission", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            }
        }
    }


    private void openMainActivity(){
        ChainRepository.getInstance(this.getApplicationContext());
        startActivity(new Intent(SplashScreen.this, MainActivity.class));
        finish();
    }

}
