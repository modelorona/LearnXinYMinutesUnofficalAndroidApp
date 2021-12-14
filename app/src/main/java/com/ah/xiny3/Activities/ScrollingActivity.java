package com.ah.xiny3.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;

import com.ah.xiny3.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class ScrollingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        final Intent intent = getIntent(); // key is XINY_LANG_NAME
        final String languageToGet = intent.getStringExtra("XINY_LANG_NAME");
        WebView contentView;

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("X=" + languageToGet);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contentView = findViewById(R.id.contentView);
        contentView.setInitialScale(200);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();
        db.setFirestoreSettings(settings);

        CollectionReference lang_ref = db.collection("languages");

        lang_ref.whereEqualTo("language", languageToGet).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // should always only be one that matches the language. if not, we have a problem
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    contentView.loadData(doc.get("html").toString(), "text/html", null);
                    findViewById(R.id.progressBar_cyclic).setVisibility(View.GONE);
                }
            }
        });
    }

}
