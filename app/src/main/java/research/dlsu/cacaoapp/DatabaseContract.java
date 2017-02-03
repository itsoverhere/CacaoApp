package research.dlsu.cacaoapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by courtneyngo on 4/21/16.
 */
public class DatabaseContract {

    public static final String CONTENT_AUTHORITY = "research.dlsu.cacaoapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ONSITEUSER = "onsiteuser";
    public static final String PATH_CACAO = "cacao";
    public static final String PATH_CACAOUPDATE = "cacaoupdate";
    public static final String PATH_SCIENTIST = "scientist";

    public static class OnSiteUser implements BaseColumns {
        public static final String TABLE_NAME = "onsiteuser";

        public static final String SERIALNUMBER = "serialnumber";

//        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_FIRSTNAME = "firstname";
        public static final String COLUMN_LASTNAME = "lastname";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_FARM = "farm";
        public static final String COLUMN_CITY = "city";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FIRSTNAME + " TEXT NOT NULL, "
                + COLUMN_LASTNAME + " TEXT NOT NULL, "
                + COLUMN_USERNAME + " TEXT NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_FARM + " TEXT NOT NULL, "
                + COLUMN_CITY + " TEXT " + " ); ";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ONSITEUSER).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ONSITEUSER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ONSITEUSER;

        public static Uri buildOnSiteUserItemUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static class OffSiteUser implements BaseColumns {
        public static final String TABLE_NAME = "offsiteuser";
//        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_FIRSTNAME = "firstname";
        public static final String COLUMN_LASTNAME = "lastname";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FIRSTNAME + " TEXT NOT NULL, "
                + COLUMN_LASTNAME + " TEXT NOT NULL, "
                + COLUMN_USERNAME + " TEXT NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL" + ");";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SCIENTIST).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCIENTIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCIENTIST;

        public static Uri buildScientistItemUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static class Cacao implements BaseColumns {
        public static final String EXTRA_ID = "phoneidcacao"; // only to be used for asynctask

        public static final String EXTRA_PHONEIDCACAOSTRING = "idcacaostring";
        public static final String EXTRA_PHONEIDCACAO = "phoneidcacao";

        public static final String TABLE_NAME = "cacao";
        public static final String TABLE_ALIAS = "c";
//        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_IDSCIENTIST = "idoffsiteuser";
        public static final String COLUMN_IDONSITEUSER = "idonsiteuser";

        public static final String COLUMN_TAG = "tag"; // for various remarks?
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_FARM = "farm";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_WEIGHT = "weight";
        public static final String COLUMN_RESULT = "result";

        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";

        public static final String COLUMN_SERVERIDCACAO = "serveridcacao";

        // could be changed to be NOT SENT / PENDING / RESULT
        public static final String STATUS_ONGOING = "ongoing";
        public static final String STATUS_DONE = "done";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

                + COLUMN_IDONSITEUSER + " INTEGER, " // NOT NULL, "
                + COLUMN_IDSCIENTIST + " INTEGER, "

                + COLUMN_SERVERIDCACAO + " INTEGER DEFAULT -1, "

                + COLUMN_TAG + " TEXT, "
                + COLUMN_WEIGHT + " REAL DEFAULT 0.0, "// NOT NULL, "

                + COLUMN_STATUS + " TEXT NOT NULL DEFAULT '" + Cacao.STATUS_ONGOING + "', "
                + COLUMN_RESULT + " TEXT DEFAULT '',"

                + COLUMN_LATITUDE + " REAL DEFAULT 0, " // NOT NULL, "
                + COLUMN_LONGITUDE + " REAL DEFAULT 0," // NOT NULL, "

                + COLUMN_FARM + " TEXT NOT NULL, "
                + COLUMN_CITY + " TEXT NOT NULL" + ");";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CACAO).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CACAO;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CACAO;

        public static Uri buildCacaoItemUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getCacaoIdAsSegment(Uri uri, int segmentIndex){
            return Long.parseLong(uri.getPathSegments().get(segmentIndex));
        }
    }

    public static class CacaoUpdate implements BaseColumns {
        public static final String EXTRA_ID = "idcacaoupdate"; // only to be used for asynctask
        public static final String EXTRA_IMAGE = "image";
        public static final String EXTRA_SERVERIDCACAO = "serveridcacao";

        public static final String PHONEIDCACAOUPDATE = "phoneidcacaoupdate";

        public static final String TABLE_NAME = "cacaoupdate";
        public static final String TABLE_ALIAS = "cu";

//        public static final String COLUMN_ID = "_id";
//        public static final String COLUMN_IDCACAO = "idcacao";

        public static final String COLUMN_SERVERIDCACAOUPDATE = "serveridcacaoupdate";

        public static final String COLUMN_PATH = "path"; // image path

//        public static final String COLUMN_REMARKS = "remarks";
        public static final String COLUMN_RESULT = "result";
        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_CACAOTYPE = "cacaotype";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

//                + COLUMN_IDONSITEUSER + " INTEGER NOT NULL, "
//                + COLUMN_IDCACAO + " INTEGER NOT NULL, "
//                  + COLUMN_IDSCIENTIST + " INTEGER, "

                + COLUMN_SERVERIDCACAOUPDATE + " INTEGER DEFAULT -1, "

                + COLUMN_PATH + " TEXT NOT NULL, "

//                + COLUMN_REMARKS + " REAL, "
//                + COLUMN_RESULT + " REAL, "
                + COLUMN_DATE + " REAL, "

                + COLUMN_RESULT + " TEXT DEFAULT '',"
                + COLUMN_CACAOTYPE + " TEXT NOT NULL "

//                + "FOREIGN KEY (" + COLUMN_IDONSITEUSER + ") REFERENCES " + OnSiteUser.TABLE_NAME + " (" + OnSiteUser._ID + "), "
//                + "FOREIGN KEY (" + COLUMN_IDCACAO + ") REFERENCES " + Cacao.TABLE_NAME + " (" + Cacao._ID + ")"
//                + "FOREIGN KEY (" + COLUMN_IDSCIENTIST + ") REFERENCES " + Scientist.TABLE_NAME + " (" + Scientist._ID + ")"

                + ");";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CACAOUPDATE).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CACAOUPDATE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CACAOUPDATE;

        public static Uri buildCacaoUpdateItemUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getCacaoUpdateIdAsSegment(Uri uri, int segmentIndex){
            return Long.parseLong(uri.getPathSegments().get(segmentIndex));
        }
    }



}
