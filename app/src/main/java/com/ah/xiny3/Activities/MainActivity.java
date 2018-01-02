package com.ah.xiny3.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ah.xiny3.Dialogs.SimpleAlert;
import com.ah.xiny3.R;
import com.ah.xiny3.Util.Cache;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase db;
    private ListView languageView;
    private ArrayList<String> languages;
    private ArrayAdapter<String> adapter;
    private Cache cache;
    private final String languageList = "languageList"; // where the list of languages gets stored locally

    // todo: look into this in regards to cleaning up firebase code: https://stackoverflow.com/questions/39930671/android-how-to-access-the-same-object-through-different-activities
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        languageView = findViewById(R.id.languageList);
        cache = new Cache(getApplicationContext());


        // check to see if list of languages is stored locally. if not, read in from database
        List<File> files = Arrays.asList(getCacheDir().listFiles());
        if (files.contains(new File(getCacheDir(), languageList))){
            List<String> cachedLanguageList = Arrays.asList(cache.readFromCache(languageList).split(System.lineSeparator()));
            adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_expandable_list_item_1, cachedLanguageList);
            languageView.setAdapter(adapter);
        }
        else {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInAnonymously().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // todo: need to get the offline list of languages lolz
                    db = FirebaseDatabase.getInstance();
                    DatabaseReference dbref = db.getReference();
                    languages = new ArrayList<>();
                    dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // time to get the list of all languages
                            for (DataSnapshot child : dataSnapshot.child("languages").getChildren()) {
                                String name = child.getKey();
                                languages.add(name);
                            }
                            adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, languages);
                            languageView.setAdapter(adapter); // set the languages in the listview
                            // save the languages to the cache
                            Runnable cacheLangaugeList = () -> {
                                //noinspection StringBufferMayBeStringBuilder
                                StringBuffer buffer = new StringBuffer();
                                for (String l: languages){
                                    buffer.append(l).append(System.lineSeparator());
                                }
                                cache.writeToCache(languageList, buffer.toString().getBytes());
                            };
                            AsyncTask.execute(cacheLangaugeList);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            SimpleAlert.displayWithOK(MainActivity.this, "An error has occured with the database read.", "Error Occurred");
                        }
                    });

                    dbref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // whenever timestamp changes, update it in preferences
                            // get last edit timestamp and store it in preferences via background task
                            SharedPreferences settings = getSharedPreferences("com.ah.xiny.preferences", 0);
                            Runnable saveToPreference = () -> {
                                @SuppressWarnings("ConstantConditions") long timestamp = Long.parseLong(dataSnapshot.child("last_edit_timestamp").getValue().toString());
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putLong("com.ah.xiny.last_edit_timestamp", timestamp);
                                editor.apply();
                            };
                            AsyncTask.execute(saveToPreference);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // what to do
                        }
                    });

                } else {
                    SimpleAlert.displayWithOK(MainActivity.this, "An error has occured with the database sign in.", "Error Occurred");
                }
            });
        }

        languageView.setOnItemClickListener((parent, view, position, id) -> {
            // start the new activity, which will show the language
            Intent intent = new Intent(getBaseContext(), ScrollingActivity.class);
            Bundle extras = new Bundle();
            extras.putString("XINY_LANG_NAME", languageView.getItemAtPosition(position).toString());
            intent.putExtras(extras);
            startActivity(intent);
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
    public void onDestroy(){
        FirebaseAuth.getInstance().signOut();
        deleteFile(languageList); // clear the language cache on destroy. this allows for updating on start todo: see how to implement this as a weekly job
        FirebaseDatabase.getInstance().goOffline();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                new LibsBuilder().withActivityTitle("About this app").withActivityTheme(R.style.AppTheme_NoActionBar).withActivityStyle(Libs.ActivityStyle.DARK).withFields(R.string.class.getFields()).start(getApplicationContext());
                break;
            case R.id.visit:
                Uri page = Uri.parse(getResources().getString(R.string.site_link));
                Intent visitIntent = new Intent(Intent.ACTION_VIEW, page);
                startActivity(visitIntent);
                break;
        }
        return true;
    }

}
