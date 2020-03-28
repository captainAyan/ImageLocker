package com.codoprobe.imagelocker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codoprobe.imagelocker.utility.ChainRepository;
import com.codoprobe.imagelocker.utility.KeyStore;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.Key;

public class ChangeKeyActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText old_password_et, new_password_et, renew_password_et;
    private Button submit_btn;

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_key);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Change Key");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        old_password_et = (EditText) findViewById(R.id.old_password);
        new_password_et = (EditText) findViewById(R.id.new_password);
        renew_password_et = (EditText) findViewById(R.id.new_password_again);
        submit_btn = (Button) findViewById(R.id.submit);

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new_password_et.getText().toString().equals(renew_password_et.getText().toString())) {
                    try {
                        KeyStore.changeEncryptionKey(ChangeKeyActivity.this.getApplicationContext(),
                                new_password_et.getText().toString(),
                                old_password_et.getText().toString());

                        Toast.makeText(ChangeKeyActivity.this, "Done", Toast.LENGTH_SHORT).show();

                        startAsyncTask();

                    } catch (Exception e) {
                        Toast.makeText(ChangeKeyActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(
                            ChangeKeyActivity.this,
                            "New Password and Re-enter New Password are not same",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("Working...");

    }

    public void startAsyncTask() throws IOException {

        KeyChangeFileUpdateHandler fileUpdateHandler = new KeyChangeFileUpdateHandler(this);
        fileUpdateHandler.execute();
        ChangeKeyActivity.this.finish();
    }


    private static class KeyChangeFileUpdateHandler extends AsyncTask<Void, Void, Void> {

        private WeakReference<ChangeKeyActivity> weakReference;
        private String errorMessage;

        public KeyChangeFileUpdateHandler(ChangeKeyActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ChangeKeyActivity activity = weakReference.get();

            if (activity == null || activity.isFinishing()) return;
            else activity.pd.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                for (int i=0; i<ChainRepository.getInstance(weakReference.get())
                        .getChains().size(); i++) {

                    ChainRepository
                            .getInstance(weakReference.get())
                            .getChains()
                            .get(i)
                            .changeEncryptionKeyAndSaveBlocks(KeyStore
                                    .getEncryptionKey(weakReference.get()));

                }

            } catch (Exception e) {
                this.errorMessage = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ChangeKeyActivity activity = weakReference.get();
            if (activity == null || activity.isFinishing()) return;
            else {
                activity.pd.dismiss();
                if (this.errorMessage != null) Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
                else Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();

        return super.onOptionsItemSelected(item);
    }

}
