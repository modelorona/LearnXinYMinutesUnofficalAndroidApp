package com.ah.xiny3.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.ah.xiny3.R;
import com.github.florent37.materialtextfield.MaterialTextField;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.google.android.gms.ads.MobileAds;


import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    // ca-app-pub-3276395982865954~8864018841 is app id
    // ca-app-pub-3276395982865954/7523442048 is banner id
    // ca-app-pub-3940256099942544/6300978111 is test ad
    // ca-app-pub-3940256099942544~3347511713 is test sdk id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        ListView languageView = findViewById(R.id.languageList);
        ArrayList<String> languages = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, languages);

        EditText langSearch = findViewById(R.id.search_bar_inner);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).setPersistenceEnabled(true).build();
        db.setFirestoreSettings(settings);

        CollectionReference lang_ref = db.collection("languages");

        lang_ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc: task.getResult()){
                    if (doc.exists()) {
                        languages.add(doc.get("language").toString());
                    }
                }
                Collections.sort(languages);
                languageView.setAdapter(adapter); // set the languages in the listview
                findViewById(R.id.main_progress).setVisibility(View.GONE);
                languageView.setVisibility(View.VISIBLE);
            } else {
                Log.d("XINY TASK ERROR", task.getException().toString());
            }
        });

        languageView.setOnItemClickListener((parent, view, position, id) -> {
            // start the new activity, which will show the language
            Intent intent = new Intent(getBaseContext(), ScrollingActivity.class);
            Bundle extras = new Bundle();
            extras.putString("XINY_LANG_NAME", languageView.getItemAtPosition(position).toString());
            intent.putExtras(extras);
            startActivity(intent);
        });

        langSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                new LibsBuilder().withActivityTitle("About this app").withActivityTheme(R.style.AppTheme_NoActionBar).withActivityStyle(Libs.ActivityStyle.DARK).withFields(R.string.class.getFields()).start(MainActivity.this);
                break;
            case R.id.visit:
                Uri page = Uri.parse(getResources().getString(R.string.site_link));
                Intent visitIntent = new Intent(Intent.ACTION_VIEW, page);
                startActivity(visitIntent);
                break;
            case R.id.action_search:
                MaterialTextField f = findViewById(R.id.search_bar);
                // hide the bar if it is visible
                if (f.getVisibility() == View.VISIBLE) {
                    f.setVisibility(View.GONE);
                } else {
                    f.setVisibility(View.VISIBLE);
                    f.setHasFocus(true);
                }
                break;
        }
        return true;
    }

}
