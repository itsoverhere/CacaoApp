package research.dlsu.cacaoapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DatabaseProvider extends ContentProvider {

    public static final int URIMATCHER_CODE_CACAO = 100;
    public static final int URIMATCHER_CODE_CACAO_ITEM = 101;
    public static final int URIMATCHER_CODE_CACAOUPDATE_DIR = 200;
    public static final int URIMATCHER_CODE_CACAO_CACAOUPDATE_ITEM = 201;
    public static final int URIMATCHER_CODE_CACAOUPDATE = 300;

    private static final UriMatcher mUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sqliteQueryBuilder;

    static{
        sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(DatabaseContract.CacaoUpdate.TABLE_NAME
//                + " " + DatabaseContract.CacaoUpdate.TABLE_ALIAS
//                        + " INNER JOIN "
//                        + DatabaseContract.Cacao.TABLE_NAME  + " " + DatabaseContract.Cacao.TABLE_ALIAS
//                        + " ON "
//                        + DatabaseContract.Cacao.TABLE_ALIAS + "." + DatabaseContract.Cacao._ID
//                        + " = "
//                        + DatabaseContract.CacaoUpdate.TABLE_ALIAS + "." + DatabaseContract.CacaoUpdate.COLUMN_IDCACAO
        );
    }

    DatabaseHelper databaseHelper;

    public DatabaseProvider() {
//        databaseHelper = new DatabaseHelper(getContext());
    }

    static UriMatcher buildUriMatcher(){
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, DatabaseContract.PATH_CACAO, URIMATCHER_CODE_CACAO);
        uriMatcher.addURI(authority, DatabaseContract.PATH_CACAO + "/#", URIMATCHER_CODE_CACAO_ITEM);
        uriMatcher.addURI(authority, DatabaseContract.PATH_CACAOUPDATE, URIMATCHER_CODE_CACAOUPDATE);
        uriMatcher.addURI(authority, DatabaseContract.PATH_CACAOUPDATE + "/#", URIMATCHER_CODE_CACAOUPDATE_DIR);
        uriMatcher.addURI(authority, DatabaseContract.PATH_CACAOUPDATE + "/#/#", URIMATCHER_CODE_CACAO_CACAOUPDATE_ITEM);

        return uriMatcher;
    }

    public String getType(Uri uri){
        final int match = buildUriMatcher().match(uri);

        switch(match){
            case URIMATCHER_CODE_CACAO:
                return DatabaseContract.Cacao.CONTENT_DIR_TYPE;
            case URIMATCHER_CODE_CACAO_ITEM:
                return DatabaseContract.Cacao.CONTENT_ITEM_TYPE;
            case URIMATCHER_CODE_CACAOUPDATE_DIR:
                return DatabaseContract.CacaoUpdate.CONTENT_DIR_TYPE;
            case URIMATCHER_CODE_CACAO_CACAOUPDATE_ITEM:
                return DatabaseContract.CacaoUpdate.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);
        }

    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch(mUriMatcher.match(uri)){
            case URIMATCHER_CODE_CACAO:
                retCursor = databaseHelper.getReadableDatabase().query(
                        DatabaseContract.Cacao.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder
                );
                break;
            case URIMATCHER_CODE_CACAO_ITEM:
                retCursor = queryCacaoItemDetails(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case URIMATCHER_CODE_CACAOUPDATE_DIR:
                retCursor = queryCacaoUpdateItem(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case URIMATCHER_CODE_CACAO_CACAOUPDATE_ITEM:
                retCursor = queryCacaoUpdateItem(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case URIMATCHER_CODE_CACAOUPDATE:
                retCursor = databaseHelper.getReadableDatabase().query(
                        DatabaseContract.CacaoUpdate.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("No query() operation found for URI : " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;

    }

    public Cursor queryCacaoUpdateItem(Uri uri, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder){
        long id_cacaoupdate = Long.parseLong(uri.getPathSegments().get(1)); // id of cacaoupdate
        // long id = Long.parseLong(uri.getPathSegments().get(2));
        Log.i("databaseprovider", "idcacaoupdate is " + id_cacaoupdate);
        return sqliteQueryBuilder.query(
                databaseHelper.getReadableDatabase(),
                null,
                //DatabaseContract.CacaoUpdate.TABLE_NAME + "." +
                DatabaseContract.CacaoUpdate._ID + " =? ",
                new String[]{String.valueOf(id_cacaoupdate)},
                null,
                null,
                sortOrder
        );
    }

    public Cursor queryCacaoItemDetails(Uri uri, String[] projection, String selection,
                                       String[] selectionArgs, String sortOrder){
        long id = Long.parseLong(uri.getPathSegments().get(1));
        Cursor cursor = databaseHelper.getReadableDatabase().query(
                DatabaseContract.Cacao.TABLE_NAME,
                projection,
                DatabaseContract.Cacao._ID + " = ? ",
                new String[]{String.valueOf(id)},
                null,
                null,
                sortOrder
        );

        cursor.moveToFirst();

        return cursor;
    }

    /*
    public static final String[] queryCacaoItemDetailsWithCacaoUpdatesProjection = new String[]{
            DatabaseContract.Cacao.TABLE_NAME + "." + DatabaseContract.Cacao._ID,
            DatabaseContract.Cacao.TABLE_NAME + "." + DatabaseContract.Cacao.COLUMN_TAG,
            DatabaseContract.Cacao.TABLE_NAME + "." + DatabaseContract.Cacao.COLUMN_STATUS,
            DatabaseContract.Cacao.TABLE_NAME + "." + DatabaseContract.Cacao.COLUMN_CITY,
            DatabaseContract.Cacao.TABLE_NAME + "." + DatabaseContract.Cacao.COLUMN_FARM,

            DatabaseContract.CacaoUpdate.TABLE_NAME + "." + DatabaseContract.CacaoUpdate._ID

    };

    public Cursor queryCacaoItemDetailsWithCacaoUpdates(Uri uri, String[] projection, String selection,
                                       String[] selectionArgs, String sortOrder){
        long id = Long.parseLong(uri.getPathSegments().get(1));
        return sqliteQueryBuilder.query(
                databaseHelper.getReadableDatabase(),

        )

        return databaseHelper.getReadableDatabase().query(
                sq,
                projection,
                DatabaseContract.Cacao._ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                sortOrder
        );
    }
    */


    public Cursor queryUpdatesOfCacao(Uri uri, String[] projection, String selection,
                                     String[] selectionArgs, String sortOrder){
        long id = Long.parseLong(uri.getPathSegments().get(1)); // id of cacao
//        return sqliteQueryBuilder.query(
//                databaseHelper.getReadableDatabase(),
//                projection,
//                DatabaseContract.Cacao.TABLE_ALIAS + "." + DatabaseContract.Cacao._ID + " =? ",
//                new String[]{String.valueOf(id)},
//                null,
//                null,
//                sortOrder
//        );

        String TABLES = DatabaseContract.CacaoUpdate.TABLE_NAME;
//                + " " + DatabaseContract.CacaoUpdate.TABLE_ALIAS
//                + " INNER JOIN "
//                + DatabaseContract.Cacao.TABLE_NAME  + " " + DatabaseContract.Cacao.TABLE_ALIAS
//                + " ON "
//                + DatabaseContract.Cacao.TABLE_ALIAS + "." + DatabaseContract.Cacao._ID
//                + " = "
//                + DatabaseContract.CacaoUpdate.TABLE_ALIAS + "." + DatabaseContract.CacaoUpdate.COLUMN_IDCACAO;

//        return databaseHelper.getReadableDatabase().query(
//                TABLES,
//                projection,
//                DatabaseContract.Cacao.TABLE_ALIAS + "." + DatabaseContract.Cacao._ID + " =? ",
//                new String[]{String.valueOf(id)},
//                null, null, null
//        );
        // id, date, path, idcacao, serveridcacaoupdate

        String selectionString =
                DatabaseContract.CacaoUpdate.TABLE_ALIAS + "." + DatabaseContract.CacaoUpdate._ID
                    + " AS " + DatabaseContract.CacaoUpdate._ID + ", " +
                DatabaseContract.CacaoUpdate.TABLE_ALIAS + "." + DatabaseContract.CacaoUpdate.COLUMN_DATE
                    + " AS " + DatabaseContract.CacaoUpdate.COLUMN_DATE + ", " +
                DatabaseContract.CacaoUpdate.TABLE_ALIAS + "." + DatabaseContract.CacaoUpdate.COLUMN_PATH
                    + " AS " + DatabaseContract.CacaoUpdate.COLUMN_PATH + ", " +
//                DatabaseContract.CacaoUpdate.TABLE_ALIAS + "." + DatabaseContract.CacaoUpdate.COLUMN_IDCACAO
//                    + " AS " + DatabaseContract.CacaoUpdate.COLUMN_IDCACAO + ", " +
                DatabaseContract.CacaoUpdate.TABLE_ALIAS + "." + DatabaseContract.CacaoUpdate.COLUMN_SERVERIDCACAOUPDATE
                    + " AS " + DatabaseContract.CacaoUpdate.COLUMN_SERVERIDCACAOUPDATE
                ;

        String sql = "SELECT "
                + selectionString
                + " FROM "
                + TABLES
                + " WHERE " + DatabaseContract.Cacao.TABLE_ALIAS + "." + DatabaseContract.Cacao._ID + " =?";

        Log.i("TAG", "QUERY : " + sql);
        Log.i("TAG", "QUERY id : " + id);

        return databaseHelper.getReadableDatabase().rawQuery(
            sql, new String[]{String.valueOf(id)}
        );
    }

//    public Cursor queryCacaoUpdateItem(Uri uri, String[] projection, String selection,
//                                      String[] selectionArgs, String sortOrder){
//        long ic_cacao = Long.parseLong(uri.getPathSegments().get(1)); // id of cacao not used
//        long id = Long.parseLong(uri.getPathSegments().get(2)); // id of cacaoupdate
//        return sqliteQueryBuilder.query(
//                databaseHelper.getReadableDatabase(),
//                projection,
//                DatabaseContract.CacaoUpdate.TABLE_NAME + "." + DatabaseContract.CacaoUpdate._ID + " =? ",
//                new String[]{String.valueOf(id)},
//                null,
//                null,
//                sortOrder
//        );
//    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        long id;
        int affectedRows;
        switch(mUriMatcher.match(uri)){
            case URIMATCHER_CODE_CACAO_ITEM:
                id = DatabaseContract.Cacao.getCacaoIdAsSegment(uri, 1);
                affectedRows = databaseHelper.getWritableDatabase().delete(
                        DatabaseContract.Cacao.TABLE_NAME,
                        DatabaseContract.Cacao._ID + " =? ",
                        new String[]{String.valueOf(id)}
                );
                break;
            case URIMATCHER_CODE_CACAO_CACAOUPDATE_ITEM:
                id = DatabaseContract.CacaoUpdate.getCacaoUpdateIdAsSegment(uri, 2);
                affectedRows = databaseHelper.getWritableDatabase().delete(
                        DatabaseContract.CacaoUpdate.TABLE_NAME,
                        DatabaseContract.CacaoUpdate._ID + " =? ",
                        new String[]{String.valueOf(id)}
                );
                break;
            default:
                throw new UnsupportedOperationException("No delete() operation found for URI : "+ uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return affectedRows;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id;
        Uri returnUri;
        switch(mUriMatcher.match(uri)){
            case URIMATCHER_CODE_CACAO:
                id = databaseHelper.getWritableDatabase().insert(
                        DatabaseContract.Cacao.TABLE_NAME,
                        null,
                        values
                );
                if(id > 0){
                    returnUri = DatabaseContract.Cacao.buildCacaoItemUri(id);
                }else{
                    throw new android.database.SQLException("Item wasn't inserted into the database properly");
                }
                break;
            case URIMATCHER_CODE_CACAOUPDATE:
                id = databaseHelper.getWritableDatabase().insert(
                        DatabaseContract.CacaoUpdate.TABLE_NAME,
                        null,
                        values
                );
                if(id > 0){
                    returnUri = DatabaseContract.CacaoUpdate.buildCacaoUpdateItemUri(id);
                }else{
                    throw new android.database.SQLException("Item wasn't inserted into the database properly");
                }
                break;
            default:
                throw new UnsupportedOperationException("No insert() operation for URI : " + uri);
        }

        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
