package com.codoprobe.imagelocker;

import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.codoprobe.imagelocker.adapter.ImageAdapter;
import com.codoprobe.imagelocker.utility.ChainRepository;

public class ViewImagesActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private GridLayoutManager manager;
    private ImageAdapter adapter;

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private RelativeLayout emptyView;

    private ChainRepository chainRepository;
    private ChainRepository.Listener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_images);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Images");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ViewImagesActivity.this, ImageLockerActivity.class));
                finish();
            }
        });

        emptyView = (RelativeLayout) findViewById(R.id.empty_view);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        chainRepository = ChainRepository.getInstance(this.getApplicationContext());
        setupEmptyView();

        adapter = new ImageAdapter(this, chainRepository);
        manager = new GridLayoutManager(this, 2);

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        listener = new ChainRepository.Listener() {
            @Override
            public void onChange(String type, int index) {
                if(type.equals("ADD")) {
                    adapter.notifyItemInserted(index);
                }
                else if(type.equals("REMOVE")) {
                    adapter.notifyItemRemoved(index);
                    adapter.notifyItemRangeChanged(index, chainRepository.getChains().size());
                }
                setupEmptyView();
            }
        };
        chainRepository.addListener(listener);
    }

    public void setupEmptyView() {
        if (chainRepository.getChains().size() == 0) emptyView.setVisibility(View.VISIBLE);
        else emptyView.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
