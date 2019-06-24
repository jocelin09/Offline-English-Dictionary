package offline.english.dictionary;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import offline.english.dictionary.fragments.AntonymsFragment;
import offline.english.dictionary.fragments.DefinitionFragment;
import offline.english.dictionary.fragments.ExamplesFragment;
import offline.english.dictionary.fragments.SynonymsFragment;

public class WordMeaningActivity extends AppCompatActivity{

    private ViewPager viewPager;
    String enword;
    DBHelper dbHelper;
    Cursor c = null;

    public String definition;
    public String synonyms;
    public String antonyms;
    public String example;

    TextToSpeech textToSpeech;
    FloatingActionButton fab;
    boolean startFromSharedText = false;


    //FirebaseAnalytics
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_meaning);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        Bundle bundle = getIntent().getExtras();
        enword = bundle.getString("en_word");

        fab = (FloatingActionButton) findViewById(R.id.fab);

        //textToSpeech  =  new TextToSpeech(this,this);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                startFromSharedText = true;

                if (sharedText != null) {
                    Pattern pattern = Pattern.compile("[A-Za-z \\-.]{1,25}");
                    Matcher matcher = pattern.matcher(sharedText);

                    if (matcher.matches()) {
                        enword = sharedText;
                    } else {
                        enword = "Not Available";
                    }

                }
            }
        }
        dbHelper = new DBHelper(this);

        try {
            dbHelper.openDB();
        } catch (SQLException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        try {
            c = dbHelper.getMeaning(enword);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            System.out.println("Exception occured");
        }
        if (c.moveToFirst()) {

            definition = c.getString(c.getColumnIndex("en_definition"));
            example = c.getString(c.getColumnIndex("example"));
            synonyms = c.getString(c.getColumnIndex("synonyms"));
            antonyms = c.getString(c.getColumnIndex("antonyms"));
            dbHelper.insertHistory(enword);
        } else {
            enword = "Not Available";
        }

        c.close();
       System.out.println("example" +example);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String definitions =  definition.substring(0,1).toUpperCase() + definition.substring(1).toLowerCase();
                    String shareBody = "Word: "+enword+"\nDefinition: "+definitions;
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, enword);
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }

            }
        });
        ImageButton btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    textToSpeech = new TextToSpeech(WordMeaningActivity.this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {

                            if (status == TextToSpeech.SUCCESS) {
                                int result = textToSpeech.setLanguage(Locale.getDefault());

                                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                                {
                                    Toast.makeText(WordMeaningActivity.this, "This language is not supported", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    {
                                        textToSpeech.speak(enword, TextToSpeech.QUEUE_FLUSH, null,null);
                                    }
                                    else
                                    {
                                        Toast.makeText(WordMeaningActivity.this, "Speech is not supported ", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }
                            else
                                {
                                Toast.makeText(WordMeaningActivity.this, "Initialization failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }


            }
        });



        Toolbar toolbar = (Toolbar) findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);

        String word = enword.toUpperCase();
        getSupportActionBar().setTitle(word);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        if (viewPager != null) {
            setViewPager(viewPager);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mFirebaseAnalytics.setCurrentScreen(this,"Meaning Activity",null);

        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);

        //Sets the duration of inactivity that terminates the current session. The default value is 900000 (15 minutes).
        mFirebaseAnalytics.setSessionTimeoutDuration(900000);



    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void setViewPager(ViewPager viewPager) {

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFrag(new DefinitionFragment(), "Definition");
        viewPagerAdapter.addFrag(new SynonymsFragment(), "Synonyms");
        viewPagerAdapter.addFrag(new AntonymsFragment(), "Antonyms");
        viewPagerAdapter.addFrag(new ExamplesFragment(), "Examples");
        viewPager.setAdapter(viewPagerAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) //press back icon
        {

            //try {
                if (startFromSharedText) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    onBackPressed();
                }
           /* } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }*/
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        // super.onBackPressed();
      //  try {
            if (startFromSharedText) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                super.onBackPressed();
            }
       /* } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }*/
    }
    public void onPause(){
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onPause();
    }


    @Override
    public void onDestroy() {
       // try {
            if (textToSpeech != null) {
                textToSpeech.stop();
                textToSpeech.shutdown();
            }
            super.onDestroy();
       /* } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }*/
    }



}
