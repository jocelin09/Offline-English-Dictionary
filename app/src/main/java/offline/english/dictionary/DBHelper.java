package offline.english.dictionary;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;

import com.crashlytics.android.Crashlytics;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.fabric.sdk.android.services.common.Crash;

/**
 * Created by english on 14/05/material.
 */

public class DBHelper extends SQLiteOpenHelper {


private static final String DB_Name = "eng_dictionary.db";
private Context context;
private SQLiteDatabase sqLiteDatabase;
private static String DB_Path = null;

/*public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    super(context, name, factory, version);
}*/

public DBHelper(Context context) {
    super(context, DB_Name, null, 1);
    this.context = context;
    this.DB_Path = "/data/data/" + context.getPackageName() + "/" + "databases/";
   // this.DB_Path = context.getApplicationInfo().dataDir + "/" + "databases/";
   
    
}


//method to call checkDatabse
public void createDB() throws IOException {
   // try {
        boolean dbExists = checkDatabase();
        if (!dbExists) // db doesn't exists
        {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBtoPath();
            } catch (IOException e) {
                throw new Error("Error whilecopying DB");
            }
        }
   /* } catch (Error error) {
        error.printStackTrace();
       // System.out.println("createDB dbhelper 65" + error);
       // Crashlytics.logException(error);
    }*/
    
}

//check if DB already exists or not
public boolean checkDatabase() {
    SQLiteDatabase checkDB = null;
   // DB_Path = context.getApplicationInfo().dataDir+"/databases/";
    try {
        String myPath = DB_Path + DB_Name;
        System.out.println("myPath = " + myPath);
        checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("checkDatabse dbhelper 85::" + e);
        //Crashlytics.logException(e);
    }
    if (checkDB != null) {
        checkDB.close();
    }
    
    return checkDB != null ? true : false;
    
}

/*@Override
public void onOpen(SQLiteDatabase db) {
    super.onOpen(db);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        db.disableWriteAheadLogging();
    }
}*/

//check if DB already exists or not
public boolean checkDatabase1() {
    DBHelper helper = new DBHelper(context);
    SQLiteDatabase checkDB = helper.getReadableDatabase();
   // SQLiteDatabase checkDB = null;
    String myPath;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
    {
       // myPath = checkDB.getPath();
        myPath = context.getDatabasePath(DB_Name).getPath();
        System.out.println("myPath pie = " + myPath);
    }
    else
    {
        myPath = DB_Path + DB_Name;
        System.out.println("myPath = " + myPath);
    }
    
    checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    System.out.println("checkDB = " + checkDB);
    if (checkDB != null) {
        checkDB.close();
    }
    
    return checkDB != null ? true : false;
    
}

private void copyDBtoPath() throws IOException {
    try {
        InputStream inputStream = context.getAssets().open(DB_Name);
        String outputFilename = DB_Path + DB_Name;
        System.out.println("outputFilename = " + outputFilename);
        OutputStream outputStream = new FileOutputStream(outputFilename);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        //Log.i("copyDB", "Database copied");
        System.out.println("DB copied....");
    } catch (IOException e) {
        e.printStackTrace();
      //  System.out.println("e = " + e);
       // Crashlytics.logException(e);
    }
    
}

//method to open DB
public void openDB() throws SQLException {
    try {
        String myPath = DB_Path + DB_Name;
        sqLiteDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    } catch (Exception e) {
        e.printStackTrace();
        //System.out.println("e:opendb = " + e);
    }
    
}

//ovveride close method
public synchronized void close() {
    if (sqLiteDatabase != null) {
        sqLiteDatabase.close();
    }
    super.close();
}


public Cursor getMeaning(String text) {
    Cursor cursor = sqLiteDatabase.rawQuery("SELECT en_definition,example,synonyms,antonyms FROM words WHERE en_word == UPPER('" + text + "')", null);
    return cursor;
}

public Cursor getSuggestions(String text) {
    Cursor cursor = sqLiteDatabase.rawQuery("SELECT _id,en_word FROM words WHERE en_word LIKE '" + text + "%' LIMIT 30", null);
    return cursor;
}

public void insertHistory(String text) {
    sqLiteDatabase.execSQL("INSERT INTO history(word) VALUES (UPPER('" + text + "'))");
}

public Cursor getHistory() {
    Cursor cursor = sqLiteDatabase.rawQuery("SELECT DISTINCT word,en_definition FROM history h JOIN words w ON h.word == w.en_word ORDER BY h._id DESC", null);
    return cursor;
}

public void deleteHistory() {
    sqLiteDatabase.execSQL("DELETE FROM history");
    
}

@Override
public void onCreate(SQLiteDatabase db) {

}

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    
    
    //delete the existing db and copy the new db
    try {
        this.getReadableDatabase();
        // this.close();
        System.out.println("DB_Name = " + DB_Name);
        context.deleteDatabase(DB_Name);
        copyDBtoPath();
    } catch (IOException e) {
        e.printStackTrace();
    }
    
}
}
