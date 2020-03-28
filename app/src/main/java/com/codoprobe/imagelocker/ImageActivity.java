package com.codoprobe.imagelocker;

import android.app.ProgressDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.codoprobe.imagelocker.utility.ChainRepository;

public class ImageActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView imageView;
    private ProgressDialog pd;

    private int image_index_in_repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = (ImageView) findViewById(R.id.image_view);

        image_index_in_repo = getIntent().getIntExtra("index", 0);

        imageView.setImageBitmap(
                ChainRepository
                        .getInstance(this.getApplicationContext())
                        .getChains()
                        .get(image_index_in_repo)
                        .getBitmap()
        );

        pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);

    }

    // adding menu items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.image_save:
                // save image
                ChainRepository
                        .getInstance(this.getApplicationContext())
                        .getChains()
                        .get(image_index_in_repo)
                        .saveFile();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
