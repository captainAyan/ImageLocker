package com.codoprobe.imagelocker;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.codoprobe.imagelocker.utility.KeyStore;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CardView image_locker, view_image, change_key, app_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Image Locker");

        image_locker = (CardView) findViewById(R.id.image_locker_button);
        view_image = (CardView) findViewById(R.id.view_image_button);
        change_key = (CardView) findViewById(R.id.change_key_button);
        app_info = (CardView) findViewById(R.id.app_info_button);

        image_locker.setOnClickListener(onClickListener);
        view_image.setOnClickListener(onClickListener);
        change_key.setOnClickListener(onClickListener);
        app_info.setOnClickListener(onClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchEnableAllButton(true);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.image_locker_button : {
                    startActivity(new Intent(MainActivity.this, ImageLockerActivity.class));
                    switchEnableAllButton(false);
                    break;
                }

                case R.id.view_image_button : {
                    startViewImageButton();
                    break;
                }

                case R.id.change_key_button : {
                    startActivity(new Intent(MainActivity.this, ChangeKeyActivity.class));
                    switchEnableAllButton(false);
                    break;
                }

                case R.id.app_info_button : {
                    startActivity(new Intent(MainActivity.this, InfoActivity.class));
                    switchEnableAllButton(false);
                    break;
                }

                default:
                    Toast.makeText(MainActivity.this, "None", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void startViewImageButton() {
        final EditText keyInputEditText = new EditText(this);
        keyInputEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        keyInputEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        keyInputEditText.setHint("Enter password here");

        TextInputLayout editTextLayout = new TextInputLayout(this);
        editTextLayout.addView(keyInputEditText);

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = this.getResources().getDimensionPixelSize(R.dimen.dialog_margin_side);
        params.rightMargin = this.getResources().getDimensionPixelSize(R.dimen.dialog_margin_side);

        editTextLayout.setLayoutParams(params);
        container.addView(editTextLayout);

        new AlertDialog.Builder(this)
                .setTitle("Enter Password")
                .setView(container)
                .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(KeyStore.authenticate(MainActivity.this,
                                        keyInputEditText.getText().toString())) {

                                    startActivity(new Intent(
                                            MainActivity.this,
                                            ViewImagesActivity.class));

                                    switchEnableAllButton(false);
                                }
                                else {
                                    Toast.makeText(
                                            MainActivity.this,
                                            "Password is not correct. 😥",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel, null).show();
    }

    private void switchEnableAllButton(boolean d) {
        image_locker.setEnabled(d);
        view_image.setEnabled(d);
        change_key.setEnabled(d);
        app_info.setEnabled(d);
    }


}
