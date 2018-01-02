package com.ah.xiny3.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import com.ah.xiny3.Dialogs.SimpleAlert;
import com.ah.xiny3.R;
import com.ah.xiny3.Util.Cache;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.Arrays;
import java.util.List;


public class ScrollingActivity extends AppCompatActivity {

    private WebView contentView;
    private Cache cache;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        final Intent intent = getIntent(); // key is XINY_LANG_NAME
        final String languageToGet = intent.getStringExtra("XINY_LANG_NAME");
        cache = new Cache(getApplicationContext());
        final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
        settings = getSharedPreferences("com.ah.xiny.preferences", Context.MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("X=" + languageToGet);

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contentView = findViewById(R.id.contentView);
        contentView.setInitialScale(200);
        // todo: look into having a progress bar or loading bar while this shit does its job #gang
        List<File> files = Arrays.asList(getCacheDir().listFiles());
        if (files.contains(new File(getCacheDir(), languageToGet)) && settings.contains("com.ah.xiny." + languageToGet + "timestamp")) { // read from cache if file already exists
            long languageToGetTimestamp = settings.getLong("com.ah.xiny." + languageToGet + "timestamp", 0L); // should never use default value
            long firebaseTimestamp = settings.getLong("com.ah.xiny.last_edit_timestamp", 0L); // should never have a default, only put because it's not optional
            if (firebaseTimestamp > languageToGetTimestamp) { // if the timestamp on the database is larger than the one saved, then that means the data on device is outdated
                dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        updateWebview(dataSnapshot, languageToGet);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // nothing to do
                    }
                });
            } else { // there was no change, use local cache
                String html = cache.readFromCache(languageToGet);
                if (html.equals("fail")) // could not read language from cache
                    SimpleAlert.displayWithOK(getApplicationContext(), "The content for " + languageToGet + " cannot be read from the database.\n\nPlease check to see if your internet connection or mobile data are turned on.", "Error showing language content");
                else
                    contentView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
            }

        } else {
            dbref.addListenerForSingleValueEvent(new ValueEventListener() { // read from database and then write it to cache
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    updateWebview(dataSnapshot, languageToGet);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    SimpleAlert.displayWithOK(getApplicationContext(), "The content for " + languageToGet + " cannot be read from the database.\n\nPlease check to see if your internet connection or mobile data are turned on.", "Error showing language content");
                }
            });
        }

    }

    private void updateWebview(DataSnapshot dataSnapshot, String languageToGet) {
        DataSnapshot child = dataSnapshot.child("languages").child(languageToGet).child("html");
        //noinspection ConstantConditions
        contentView.loadDataWithBaseURL("", child.getValue().toString(), "text/html", "UTF-8", "");
        cache.writeToCache(languageToGet, child.getValue().toString().getBytes()); // cache the html content
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("com.ah.xiny." + languageToGet + "timestamp", System.currentTimeMillis() / 1000); // languagetimestamp save
        editor.apply();
    }

}
