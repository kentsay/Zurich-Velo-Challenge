package ch.ethz.gis.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.gis.databean.VeloRoute;

public class VeloDbHelper extends SQLiteOpenHelper {

    //Log cat TAG setting
    private static String TAG = "DataBaseHelper";

    //Database Info
    private static String DATABASE_PATH       = "";
    private static final String DATABASE_NAME = "routes.db";
    private static final int DATABASE_VERSION = 1;

    //Table Names
    private static final String TABLE_ROUTES = "routes";
    private static final String TABLE_FAVOURITE_ROUTES = "favourite";

    //Table Columns
    private static final String KEY_ROUTES_ID           = "id";
    private static final String KEY_ROUTES_DISTANCE     = "distance";
    private static final String KEY_ROUTES_SNAPSHOT_URL = "snapshot_url";
    private static final String KEY_ROUTES_ELEVATION    = "elevation";
    private static final String KEY_ROUTES_NAME         = "name";
    private static final String KEY_ROUTES_KML_URL      = "kml_url";

    private static VeloDbHelper sInstance;
    private final Context mContext;
    private SQLiteDatabase db;

    public static synchronized VeloDbHelper getInstance(Context context) {
        //create singleton instance only
        if (sInstance == null) {
            sInstance = new VeloDbHelper(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        }
        return sInstance;
    }

    public VeloDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        //check SDK version to modify DB path
        if(android.os.Build.VERSION.SDK_INT >= 17) {
            DATABASE_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DATABASE_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
        try {
            //If database not exists copy it from the assets
            boolean dbExist = checkDataBase();
            if (!dbExist) {
                copyDataBase();
                Log.e(TAG, "DataBase copy success");
            }

        } catch (IOException mIOException) {
            throw new Error("Error Copying DataBase");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVOURITE_ROUTE_TABLE = "CREATE TABLE " + TABLE_FAVOURITE_ROUTES +
                "(" +
                KEY_ROUTES_ID + " INTEGER PRIMARY KEY" + // Define a primary key
                ")";

        db.execSQL(CREATE_FAVOURITE_ROUTE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //Check that the database exists here: /data/data/your package/databases/Da Name
    private boolean checkDataBase()
    {
        File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
        Log.v(TAG, dbFile + "   " + dbFile.exists());
        return dbFile.exists();
    }

    //Copy the database from assets
    private void copyDataBase() throws IOException
    {
        //check if DATABASE_PATH exists (e.g: SamSung Nexus)
        File folder = new File(DATABASE_PATH);
        if (!folder.exists()) {
            folder.mkdir();
            Log.v(TAG, "database folder missing, created folder");
        }
        InputStream mInput = mContext.getAssets().open(DATABASE_NAME);
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    // Get Velo Routes from DB
    public List<VeloRoute> getVeloRoutes() {
        List<VeloRoute> routesList = new ArrayList<>();
        String ROUTES_SELECT_QUERY = String.format("SELECT * FROM %s", TABLE_ROUTES);

        db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ROUTES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    VeloRoute route = new VeloRoute();
                    route.setId(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_ID)));
                    route.setRoute_name(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_NAME)));
                    route.setElevation(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_ELEVATION)));
                    route.setRoute_distance(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_DISTANCE)));
                    route.setSnapshot_url(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_SNAPSHOT_URL)));
                    route.setKml_url(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_KML_URL)));
                    routesList.add(route);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("SQL", "Error while trying to get routes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return routesList;
    }

    // Get 30 random routes from DB
    public List<VeloRoute> getRandomVeloRoutes() {
        List<VeloRoute> routesList = new ArrayList<>();
        String ROUTES_SELECT_QUERY = String.format("SELECT * FROM %s order by random() limit 30", TABLE_ROUTES);
        Log.d("SQL", ROUTES_SELECT_QUERY);
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ROUTES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    VeloRoute route = new VeloRoute();
                    route.setId(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_ID)));
                    route.setRoute_name(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_NAME)));
                    route.setElevation(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_ELEVATION)));
                    route.setRoute_distance(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_DISTANCE)));
                    route.setSnapshot_url(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_SNAPSHOT_URL)));
                    route.setKml_url(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_KML_URL)));
                    routesList.add(route);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("SQL", "Error while trying to get routes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return routesList;
    }

    // Get Velo Routes from DB
    public List<VeloRoute> getVeloRoutesByArgument(int distMin, int elevMin, int distMax, int elevMax) {
        List<VeloRoute> routesList = new ArrayList<>();
        String ROUTES_SELECT_QUERY = String.format("SELECT * FROM %s WHERE distance >= %d and distance < %d and elevation >= %d and elevation < %d",
                TABLE_ROUTES, distMin, distMax, elevMin, elevMax);
        Log.d("SQL", ROUTES_SELECT_QUERY);

        db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ROUTES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    VeloRoute route = new VeloRoute();
                    route.setId(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_ID)));
                    route.setRoute_name(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_NAME)));
                    route.setElevation(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_ELEVATION)));
                    route.setRoute_distance(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_DISTANCE)));
                    route.setSnapshot_url(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_SNAPSHOT_URL)));
                    route.setKml_url(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_KML_URL)));
                    routesList.add(route);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("SQL", "Error while trying to get routes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return routesList;
    }


    // Insert a route path into the DB user favourite table
    public void addFavouriteRoute(VeloRoute route) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            String routeId= route.getId();
            ContentValues values = new ContentValues();
            values.put(KEY_ROUTES_ID, routeId);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_FAVOURITE_ROUTES, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }

    // Checked if the route has been favourite or not
    public boolean checkFavouriteRouteExists(String id) {
        boolean exists = false;
        String ROUTES_SELECT_QUERY = String.format("SELECT * FROM %s WHERE id=?", TABLE_FAVOURITE_ROUTES);

        db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ROUTES_SELECT_QUERY, new String[] {id});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Log.v("DB", cursor.getString(cursor.getColumnIndex(KEY_ROUTES_ID)));
                    exists = true;
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("SQL", "Error while trying to get routes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return exists;
    }

    // Get user favourite routes from db
    public List<VeloRoute> getAllFavouriteRoutes() {
        List<VeloRoute> routesList = new ArrayList<>();
        String ROUTES_SELECT_QUERY = String.format(
                "SELECT fav.id, r.name, r.distance, r.snapshot_url, r.elevation, r.kml_url " +
                        "FROM %s as fav JOIN %s as r ON fav.id=r.id;",
                TABLE_FAVOURITE_ROUTES, TABLE_ROUTES);

        db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ROUTES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Log.v("DB", cursor.getString(cursor.getColumnIndex(KEY_ROUTES_ID)));
                    VeloRoute route = new VeloRoute();
                    route.setId(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_ID)));
                    route.setRoute_name(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_NAME)));
                    route.setElevation(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_ELEVATION)));
                    route.setRoute_distance(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_DISTANCE)));
                    route.setSnapshot_url(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_SNAPSHOT_URL)));
                    route.setKml_url(cursor.getString(cursor.getColumnIndex(KEY_ROUTES_KML_URL)));
                    routesList.add(route);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("SQL", "Error while trying to get routes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return routesList;
    }

    // Delete route from favourite
    public void deleteFromFavourite(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_FAVOURITE_ROUTES, "id=?", new String[] {id});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all posts and users");
        } finally {
            db.endTransaction();
        }
    }
}