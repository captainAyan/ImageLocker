package com.codoprobe.imagelocker;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.codoprobe.imagelocker.utility.BlockChain;
import com.codoprobe.imagelocker.utility.ChainRepository;
import com.codoprobe.imagelocker.utility.KeyStore;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ImageLockerActivity extends AppCompatActivity {

    private static final int WRITE_STORAGE_PERMISSION_CODE = 2;
    private static final int PICKFILE_REQUEST_CODE = 1;
    final String TAG = "IMAGE_LOCKER_ACTIVITY";
    private Toolbar toolbar;
    private Button choose_file_button;

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_locker);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Image Locker");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        choose_file_button = (Button) findViewById(R.id.choose_file_btn);
        choose_file_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, PICKFILE_REQUEST_CODE);
            }
        });

        pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("Working...");

        checkPermission();

    }


    public void checkPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("Image Locker needs to access your storage to save locked files. 😊😊")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(ImageLockerActivity.this, "Oops!! Cannot proceed without the permission", Toast.LENGTH_SHORT).show();
                                ImageLockerActivity.this.finish();
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(ImageLockerActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_CODE);
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_STORAGE_PERMISSION_CODE);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WRITE_STORAGE_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ImageLockerActivity.this, "Oops!! Cannot proceed without the permission", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            startAsyncTask(data);
        }
    }

    public void startAsyncTask(Intent data) {
        FileLockerHandlerAsyncTask task = new FileLockerHandlerAsyncTask(this);
        task.execute(data);
    }

    private static class FileLockerHandlerAsyncTask extends AsyncTask<Intent, Integer, String> {

        private WeakReference<ImageLockerActivity> weakReference;
        private String errorMessage;

        public FileLockerHandlerAsyncTask(ImageLockerActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ImageLockerActivity activity = weakReference.get();

            if (activity == null || activity.isFinishing()) return;
            else activity.pd.show();
        }

        @Override
        protected String doInBackground(Intent... intents) {
            try {
                weakReference.get().handleFileLocker(intents[0]);
            } catch (Exception e) {
                this.errorMessage = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            ImageLockerActivity activity = weakReference.get();
            if (activity == null || activity.isFinishing()) return;
            else {
                activity.pd.dismiss();
                if (this.errorMessage != null) Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
                else Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void handleFileLocker(Intent data) throws IOException {

        ArrayList<String> imagePathList = new ArrayList<String>();

        if (data.getData() != null) {
            Uri uri = data.getData();
            imagePathList.add(getRealPathFromURI(getApplicationContext(), uri));
        }
        // if multiple data (getting file uri)
        else if (data.getClipData() != null) {
            ClipData mClipData = data.getClipData();

            for (int i = 0; i < mClipData.getItemCount(); i++) {
                ClipData.Item item = mClipData.getItemAt(i);
                Uri uri = item.getUri();
                imagePathList.add(getRealPathFromURI(getApplicationContext(), uri));
            }
        }


        // creating chain
        for (String uri : imagePathList) {
            BlockChain.Chain.Builder chainBuilder = new BlockChain.Chain
                    .Builder(uri, KeyStore.getEncryptionKey(this.getApplicationContext()));

            ChainRepository
                    .getInstance(this.getApplicationContext())
                    .addChain(chainBuilder.build());
        }
    }

    private static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null
                , MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();

        return super.onOptionsItemSelected(item);
    }

}