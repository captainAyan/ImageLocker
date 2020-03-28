package com.codoprobe.imagelocker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        final String[] urls = {
                "https://undraw.co",
                "https://www.flaticon.com/free-icon/samurai_2009887",
                "https://www.flaticon.com/free-icon/information_1076337?term=information&page=1&position=67",
                "https://www.flaticon.com/free-icon/key_1680173",
                "https://www.flaticon.com/free-icon/safe_2489669?term=locker&page=1&position=36",
                "https://www.flaticon.com/free-icon/image_2648568?term=monitor%20images&page=1&position=45"};

        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.list, null);

        final ListView lv = (ListView) convertView.findViewById(R.id.lv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,urls);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls[position]));
                startActivity(browserIntent);
            }
        });


        Element e = new Element("Image Credits", R.drawable.image);
        e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(InfoActivity.this).setTitle("Image Credits")
                        .setView(lv)
                        .show();

            }
        });

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription("'Codoprobe Image Locker is image locking application. It stores image in blocks (Imitating a BlockChain)'")
                .setImage(R.drawable.icon)
                .addItem(new Element().setTitle("Version 1.1.0-beta"))
                .addGroup("Connect with us")
                .addEmail("dropostteam@gmail.com")
                .addWebsite("https://codoprobe.herokuapp.com")
                .addFacebook("codoprobe")
                .addTwitter("codoprobe")
                .addYoutube("UCNc8FRvQ-8mvqlu-O1Pq_YA")
                .addInstagram("codoprobe")
                .addGitHub("codoprobe")
                .addItem(e)
                .create();

        setContentView(aboutPage);
    }
}
