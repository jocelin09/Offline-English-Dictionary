package offline.english.dictionary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

public class SettingsActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    Switch aSwitch;

    //FirebaseAnalytics
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        TextView clear_history = (TextView) findViewById(R.id.clear_history);
        TextView shareapp = (TextView) findViewById(R.id.shareapp);


        clear_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dbHelper = new DBHelper(SettingsActivity.this);
                    try {
                        dbHelper.openDB();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    showAlert();
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        });


        shareapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "English Dictionary - Offline");
                    String shareMessage = "Let me recommend you this free and offline Dictionary application\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Choose one"));

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }

            }
        });


        mFirebaseAnalytics.setCurrentScreen(this,"Settings Activity",null);

        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);

        //Sets the duration of inactivity that terminates the current session. The default value is 900000 (15 minutes).
        mFirebaseAnalytics.setSessionTimeoutDuration(900000);



    }


    private void showAlert() {

        try {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyDialogAlert);
            alertDialogBuilder.setTitle("Are you sure?")
                    .setMessage("All history data will be deleted")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dbHelper.deleteHistory();
                            Toast.makeText(SettingsActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();

                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) //press back icon
        {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


}
