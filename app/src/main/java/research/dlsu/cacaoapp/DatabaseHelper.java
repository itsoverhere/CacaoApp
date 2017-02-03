package research.dlsu.cacaoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by courtneyngo on 4/21/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public final static String SCHEMA = "cacao";
    public static final int VERSION = 3;

    SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();

    public DatabaseHelper(Context context) {
        super(context, SCHEMA, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(DatabaseContract.OnSiteUser.CREATE_TABLE);
//        db.execSQL(DatabaseContract.Scientist.CREATE_TABLE);
//        db.execSQL(DatabaseContract.Cacao.CREATE_TABLE);
        db.execSQL(DatabaseContract.CacaoUpdate.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL(DatabaseContract.OnSiteUser.DELETE_TABLE);
//        db.execSQL(DatabaseContract.Scientist.DELETE_TABLE);
        //db.execSQL(DatabaseContract.Cacao.DELETE_TABLE);
        db.execSQL(DatabaseContract.CacaoUpdate.DELETE_TABLE);

        onCreate(db);
    }

    // CUSTOM DB OPERATIONS

//    public int getNumberOfUpdatesOfCacao(long id){
//        Cursor cursor = getReadableDatabase().query(
//                DatabaseContract.CacaoUpdate.TABLE_NAME,
//                null,
//                DatabaseContract.CacaoUpdate.COLUMN_IDCACAO + "=?",
//                new String[]{String.valueOf(id)},
//                null, null, null
//        );
//
//        if(cursor.moveToFirst()){
//            return cursor.getCount();
//        }else{
//            return 0;
//        }
//    }

//    public Date getLastCacaoUpdateOfCacao(long id){
//        Cursor cursor = getReadableDatabase().query(
//                DatabaseContract.CacaoUpdate.TABLE_NAME,
//                null,
//                DatabaseContract.CacaoUpdate.COLUMN_IDCACAO + "=?",
//                new String[]{String.valueOf(id)},
//                null, null,
//                DatabaseContract.CacaoUpdate.COLUMN_DATE + " DESC"
//        );
//
//        if(cursor.moveToFirst()){
//            return new Date(cursor.getLong(cursor.getColumnIndex(DatabaseContract.CacaoUpdate.COLUMN_DATE)));
//        }else{
//            return new Date(0);
//        }
//    }

//    public int getServerIdCacaoWithIdCacaoUpdate(int idCacaoupdate){
//
//        int serverIdCacao = -1;
//
//        sqliteQueryBuilder.setTables(DatabaseContract.CacaoUpdate.TABLE_NAME
//            + " INNER JOIN "
//            + DatabaseContract.Cacao.TABLE_NAME
//            + " ON "
//            + DatabaseContract.Cacao.TABLE_NAME + "." + DatabaseContract.Cacao._ID
//            + " = "
//            + DatabaseContract.CacaoUpdate.TABLE_NAME + "." + DatabaseContract.CacaoUpdate.COLUMN_IDCACAO
//        );
//
//        String[] projection = new String[]{
//            DatabaseContract.Cacao.TABLE_NAME + "." + DatabaseContract.Cacao.COLUMN_SERVERIDCACAO
//        };
//
//        String selection =
//            DatabaseContract.CacaoUpdate.TABLE_NAME + "." + DatabaseContract.CacaoUpdate.COLUMN_IDCACAO
//            + "=?";
//
//        String[] selectionArgs = new String[]{
//            String.valueOf(idCacaoupdate)
//        };
//
//        Cursor cursor = sqliteQueryBuilder.query(
//                getReadableDatabase(),
//                projection,
//                selection,
//                selectionArgs,
//                null, null, null
//        );
//
//        if(cursor.moveToFirst()){
//            serverIdCacao = cursor.getInt(0); // column index is zero-based
//        }
//
//        return serverIdCacao;
//    }
//
//    public int getServerIdCacaoWithIdCacao(long idCacao){
//
//        int serverIdCacao = -1;
//
//        Cursor cursor = getReadableDatabase().query(
//                DatabaseContract.Cacao.TABLE_NAME,
//                new String[]{DatabaseContract.Cacao.COLUMN_SERVERIDCACAO},
//                DatabaseContract.Cacao._ID + "=? ",
//                new String[]{String.valueOf(idCacao)},
//                null, null, null
//        );
//
//        if(cursor.moveToFirst()){
//            serverIdCacao = cursor.getInt(cursor.getColumnIndex(DatabaseContract.Cacao.COLUMN_SERVERIDCACAO)); // column index is zero-based
//        }
//
//        return serverIdCacao;
//    }
//
//    public void updateServerIdCacao(long idCacao, long serverIdCacao){
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(DatabaseContract.Cacao.COLUMN_SERVERIDCACAO, serverIdCacao);
//
//        int i = getWritableDatabase().update(
//                DatabaseContract.Cacao.TABLE_NAME,
//                contentValues,
//                DatabaseContract.Cacao._ID + "=?",
//                new String[]{String.valueOf(idCacao)}
//        );
//
//        Log.i("TAG", "update server id idCacao is " + idCacao + " serveridcacao is " + serverIdCacao + " i is " + i);
//    }

    public boolean updateServerIdCacaoUpdate(long idCacaoUpdate, long serverIdCacaoUpdate){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.CacaoUpdate.COLUMN_SERVERIDCACAOUPDATE, serverIdCacaoUpdate);

        int i = getWritableDatabase().update(
                DatabaseContract.CacaoUpdate.TABLE_NAME,
                contentValues,
                DatabaseContract.CacaoUpdate._ID + "=?",
                new String[]{String.valueOf(idCacaoUpdate)}
        );

        Log.i("TAG", "update server idupdate idCacaoupdate is " + idCacaoUpdate + " serveridcacaoupdate is " + serverIdCacaoUpdate + " i is " + i);

        if(i != 1){
            return false;
        }else{
            return true;
        }
    }

    public String getCacaoUpdatesWithoutResults(){
        String idString = "(";

//        Cursor cursor = getReadableDatabase().query(
//                DatabaseContract.Cacao.TABLE_NAME,
//                null,
////                DatabaseContract.Cacao.COLUMN_RESULT + " = '' OR "
////                        + DatabaseContract.Cacao.COLUMN_RESULT + " = NULL",
//                null,
//                null,
//                null, null, null
//        );

        Cursor cursor = getReadableDatabase().query(
                DatabaseContract.CacaoUpdate.TABLE_NAME,
                null,
                DatabaseContract.CacaoUpdate.COLUMN_RESULT + " is ''", // OR "
//                        + DatabaseContract.Cacao.COLUMN_RESULT + " is NULL",
                null, null, null, null
        );

        if(cursor.moveToFirst()){
            idString += cursor.getString(cursor.getColumnIndex(DatabaseContract.Cacao._ID));
            while(cursor.moveToNext()){
                idString += ", " + cursor.getString(cursor.getColumnIndex(DatabaseContract.Cacao._ID));
            }
        }else{
            return null;
        }

        cursor.close();

        return idString +")";
    }

    public void updateCacaoUpdateResults(ArrayList<CacaoUpdate> cacaoUpdatesList){
        SQLiteDatabase db = getWritableDatabase();
        //db.beginTransaction();
        int returnCount = 0;
        for(int i = 0; i < cacaoUpdatesList.size(); i++ ){
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseContract.CacaoUpdate.COLUMN_RESULT, cacaoUpdatesList.get(i).getResult());
            int rows = db.update(
                    DatabaseContract.CacaoUpdate.TABLE_NAME,
                    contentValues,
                    DatabaseContract.CacaoUpdate._ID + "=?",
                    new String[]{String.valueOf(cacaoUpdatesList.get(i).getId())}
            );

            returnCount+=rows;
        }
        //db.endTransaction();

        db.close();

        Log.i("TAG", "affected rows : " + returnCount);
    }

    public ContentValues[] cacaoListToContentValues(ArrayList<Cacao> cacaosList){
        ContentValues[] contentValuesList = new ContentValues[cacaosList.size()];

        for(int i = 0; i < cacaosList.size(); i++ ){
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseContract.Cacao.COLUMN_RESULT, cacaosList.get(i).getResult());
            contentValuesList[i] = contentValues;
        }

        return contentValuesList;
    }

}
