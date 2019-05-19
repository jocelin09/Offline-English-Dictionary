package example.jocelinthomas.dictionary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    SearchView searchView;
    static DBHelper dbHelper;
    static boolean databaseopened = false;

    SimpleCursorAdapter simpleCursorAdapter;


    ArrayList<History> historyArrayList;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    RelativeLayout emptyHistory;
    Cursor cursorHistory;

    boolean doubleTapToExit = false;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
      //  MobileAds.initialize(this, getString(R.string.ADMOB_ADD_ID));

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();

        mAdView.loadAd(adRequest);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchView = (SearchView) findViewById(R.id.searchView);

        //onclick because you want the whole searchview clickable instead of only the search icon
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
                //startActivity(new Intent(MainActivity.this, WordMeaningActivity.class));
            }
        });
        dbHelper = new DBHelper(this);

        if (dbHelper.checkDatabse()) {
            openDatabase();
        } else {
            //DB doesn't exists
            LoadDataAsync loadDataAsync = new LoadDataAsync(MainActivity.this);
            loadDataAsync.execute();
        }

        final String[] from = new String[]{"en_word"}; //same name as column name
        final int[] to = new int[]{R.id.suggestion_text};

        simpleCursorAdapter = new SimpleCursorAdapter(MainActivity.this
                , R.layout.suggestion_row, null, from, to, 0) {
            @Override
            public void changeCursor(Cursor cursor) {
                super.swapCursor(cursor);
            }
        };

        searchView.setSuggestionsAdapter(simpleCursorAdapter);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                //Add clicked text to searchbox
                CursorAdapter cursorAdapter = searchView.getSuggestionsAdapter();
                Cursor cursor = cursorAdapter.getCursor();
                cursor.moveToPosition(position);
                String clicked_word = cursor.getString(cursor.getColumnIndex("en_word"));
                searchView.setQuery(clicked_word, false);

                searchView.clearFocus();
                searchView.setFocusable(false);
                searchView.setQuery("", false);

                Intent intent = new Intent(MainActivity.this, WordMeaningActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("en_word", clicked_word);
                intent.putExtras(bundle);
                startActivity(intent);

                return true;
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //When user hits enter or search icon this method will be called
                String text = searchView.getQuery().toString(); // get text entered by the user
                Pattern pattern = Pattern.compile("[A-Za-z \\-.]{1,25}");
                Matcher matcher = pattern.matcher(text);

                if (matcher.matches()) {

                    Cursor cursor = dbHelper.getMeaning(text);
                    //DB might or might not have the word entered by the user

                    if (cursor.getCount() == 0) //DB doesn't have the word entered by the user
                    {
                        showAlertDialog();
                    } else {
                        searchView.setQuery("", false);
                        searchView.clearFocus();
                        searchView.setFocusable(false);

                        Intent intent = new Intent(MainActivity.this, WordMeaningActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("en_word", text);
                        intent.putExtras(bundle);
                        startActivity(intent);

                    }
                } else {
                    showAlertDialog();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {

                searchView.setIconifiedByDefault(false);

                Pattern pattern = Pattern.compile("[A-Za-z \\-.]{1,25}");
                Matcher matcher = pattern.matcher(newText);

                if (matcher.matches()) {
                    Cursor c = dbHelper.getSuggestions(newText);
                    simpleCursorAdapter.changeCursor(c); //shows suggestions whenever user enters new words
                }

                return false;
            }
        });

        emptyHistory = (RelativeLayout) findViewById(R.id.empty_history);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_history);

        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);

       fetchHistory();


    }

    private void showAlertDialog() {
        searchView.setQuery("", false);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogAlert);
        alertDialogBuilder.setTitle("Word not found.")
                .setMessage("Please search again")

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        searchView.clearFocus();
                    }
                }).show();
    }

    private void fetchHistory() {

        historyArrayList = new ArrayList<>();
        adapter = new HistoryAdapter(this, historyArrayList);
        recyclerView.setAdapter(adapter);

        History history;
        if (databaseopened) {
            cursorHistory = dbHelper.getHistory();
            if (cursorHistory.moveToFirst()) {
                do {
                    history = new History(cursorHistory.getString(cursorHistory.getColumnIndex("word"))
                            , cursorHistory.getString(cursorHistory.getColumnIndex("en_definition")));
                    historyArrayList.add(history);

                }
                while (cursorHistory.moveToNext());
            }
            adapter.notifyDataSetChanged();

            if (adapter.getItemCount() == 0) {
                emptyHistory.setVisibility(View.VISIBLE);
            } else {
                emptyHistory.setVisibility(View.GONE);
            }

        }

    }

    //when user comes back from wordmeaning activity it should show the newly searched words in history list
    @Override
    protected void onResume() {
        super.onResume();
        try {
            fetchHistory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openDatabase() {
        try {
            dbHelper.openDB();
            databaseopened = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;

            case R.id.exit:
                System.exit(0);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (doubleTapToExit) {
            super.onBackPressed();
        }
        this.doubleTapToExit = true;
        Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleTapToExit = false;
            }
        }, 2000);

    }
}
