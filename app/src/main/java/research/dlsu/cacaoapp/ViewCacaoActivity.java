package research.dlsu.cacaoapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewCacaoActivity extends AppCompatActivity {

    public final static String EXTRA_ID_CACAO = "id_cacao";

    RecyclerView rvUpdates;
    TextView tvFarmCity, tvLocation, tvStatus, tvId, tvWeight; // tvEntries
    ImageButton ibSync;
    UpdatesAdapter updatesAdapter;

    ProgressDialog progressDialog;

    private long id_cacao;
    private long serverIdCacao = -1;
    Cacao cacao;
    CacaoUpdate cacaoUpdate = null;

    boolean isSyncCacao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cacao);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvFarmCity = (TextView) findViewById(R.id.tv_farm_city);
        tvId = (TextView) findViewById(R.id.tv_id);
//        tvCity = (TextView) findViewById(R.id.tv_city);
//        tvEntries = (TextView) findViewById(R.id.tv_entries);
        tvWeight = (TextView) findViewById(R.id.tv_weight);
        tvLocation = (TextView) findViewById(R.id.tv_location);
        tvStatus = (TextView) findViewById(R.id.tv_status);

        ibSync = (ImageButton) findViewById(R.id.button_sync);

        rvUpdates = (RecyclerView) findViewById(R.id.rv_updates);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                // Add new update here
                Intent intent = new Intent(getBaseContext(), ChoosePhotoActivity.class);
                intent.putExtra(EXTRA_ID_CACAO, id_cacao);
                startActivity(intent);
            }
        });

        updatesAdapter = new UpdatesAdapter(getBaseContext(), null);

        id_cacao = getIntent().getExtras().getLong(ViewAlbumsActivity.EXTRA_ID);

        updateCacaoDetailsDisplay();

        updatesAdapter.setOnSyncListener(new UpdatesAdapter.OnSyncListener() {
            @Override
            public void onSync(CacaoUpdate cacaoUpdate) {

                showProgressDialog();

                ViewCacaoActivity.this.cacaoUpdate = cacaoUpdate;

                isSyncCacao = false;

//                int serveridcacao = new DatabaseHelper(getBaseContext())
//                        .getServerIdCacaoWithIdCacao(cacaoUpdate.getIdCacao());
//
//                if (serveridcacao < 0) {
//                    // if not submit cacao first,
//                    submitCacaoToServer(cacao);
//
//                    // make sure to submit cacaoupdate afterwards
////                    syncCacaoUpdate(cacaoUpdate);
//                } else {
//                    // cacao already exists submit cacao update
//                    syncCacaoUpdate(cacaoUpdate);
//                }
                syncCacaoUpdate(cacaoUpdate);
            }
        });

        ibSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSyncCacao = true;

                boolean canSync = Boolean.parseBoolean(v.getTag(R.id.sync_enabled).toString());
                if (canSync) {
                    submitCacaoToServer(cacao);
                }
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        rvUpdates.setLayoutManager(linearLayoutManager);
        rvUpdates.setAdapter(updatesAdapter);
    }

    public void syncCacaoUpdate(CacaoUpdate cacaoUpdate){
        // check if idcacao already exists in server
//        int serveridcacao = new DatabaseHelper(getBaseContext())
//                .getServerIdCacaoWithIdCacao(cacaoUpdate.getIdCacao());

//        cacaoUpdate.setServerIdCacao(serveridcacao);

//        if(serveridcacao > 0) {
//            submitCacaoUpdateToServer(cacaoUpdate);
//        }

        submitCacaoUpdateToServer(cacaoUpdate);
    }


    public void submitCacaoUpdateToServer(CacaoUpdate cacaoUpdate){
        new SubmitCacaoUpdateToServerTask().execute(cacaoUpdate);
    }

    public class SubmitCacaoUpdateToServerTask extends AsyncTask<CacaoUpdate, Void, String>{

        CacaoUpdate cacaoUpdate;

        @Override
        protected String doInBackground(CacaoUpdate... params) {
            cacaoUpdate = params[0];

            MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");

            OkHttpClient okHttpClient
                    = new OkHttpClient.Builder()
                        .connectTimeout(100, TimeUnit.SECONDS)
                        .build();

            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeFile(cacaoUpdate.getPath());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            RequestBody requestBody =
                new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(DatabaseContract.CacaoUpdate.EXTRA_ID, String.valueOf(cacaoUpdate.getId()))
//                        .addFormDataPart(DatabaseContract.CacaoUpdate.EXTRA_SERVERIDCACAO, String.valueOf(cacaoUpdate.getServerIdCacao()))
//                        .addFormDataPart(DatabaseContract.CacaoUpdate.COLUMN_IDCACAO, String.valueOf(id_cacao))
                        .addFormDataPart(DatabaseContract.CacaoUpdate.COLUMN_DATE, String.valueOf(cacaoUpdate.getDate().getTime()))
                        .addFormDataPart(DatabaseContract.CacaoUpdate.EXTRA_IMAGE, cacaoUpdate.getPath(),
                                RequestBody.create(MEDIA_TYPE_JPEG, byteArray))
                        .build();

            Request request = new Request.Builder()
                    .url(RemoteServer.buildInsertCacaoUpdateUri(getIpAddress()))
                    .post(requestBody)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                Long serverIdCacaoUpdate = Long.parseLong(s);
                if(new DatabaseHelper(getBaseContext()).updateServerIdCacaoUpdate(cacaoUpdate.getId(), serverIdCacaoUpdate)){
                    Snackbar.make(tvFarmCity, "Success! Update " + cacaoUpdate.getId() + " has been synced.", Snackbar.LENGTH_SHORT);

                    updatesAdapter.notifyItemChanged(cacaoUpdate.getAdapterPosition());
                    progressDialog.dismiss();

                }else{
                    Snackbar.make(tvFarmCity, "Something went wrong during local database update, please try again.", Snackbar.LENGTH_SHORT);
                }
            }catch(NumberFormatException ex){
                Snackbar.make(tvFarmCity, "Something went wrong. Please try again.", Snackbar.LENGTH_SHORT);
            }
        }
    }

    public class SubmitCacaoToServerTask extends AsyncTask<Cacao, Void, String>{

        Cacao c = null;

        @Override
        protected String doInBackground(Cacao... params) {
            OkHttpClient okHttpClient
                    = new OkHttpClient.Builder()
                        .connectTimeout(100, TimeUnit.SECONDS)
                        .build();

            c = params[0];

            RequestBody requestBody
                    = new FormBody.Builder()
                    .add(DatabaseContract.Cacao.COLUMN_CITY, c.getCity())
                    .add(DatabaseContract.Cacao.COLUMN_FARM, c.getFarm())
                    .add(DatabaseContract.Cacao.COLUMN_LATITUDE, String.valueOf(c.getLatitude()))
                    .add(DatabaseContract.Cacao.COLUMN_LONGITUDE, String.valueOf(c.getLongitude()))
                    .add(DatabaseContract.Cacao.COLUMN_WEIGHT, String.valueOf(c.getWeight()))
                    .add(DatabaseContract.Cacao.EXTRA_ID, String.valueOf(c.getId()))
                    .add(DatabaseContract.OnSiteUser.SERIALNUMBER, c.getSerialNumber())
                    .build();

            Request request
                    = new Request.Builder()
                        .url(RemoteServer.buildInsertCacaoUri(getIpAddress()))
                        .post(requestBody)
                        .build();

            Response response = null;
            try {
                response = okHttpClient.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            long serverIdCacao = -1;
//            try{
//                serverIdCacao = Long.parseLong(s);
//                new DatabaseHelper(getBaseContext()).updateServerIdCacao(c.getId(), serverIdCacao);
//                ViewCacaoActivity.this.serverIdCacao = serverIdCacao;
//                updateCacaoDetailsDisplay();
//                if(!isSyncCacao) {
//                    syncCacaoUpdate(cacaoUpdate);
//                }
//            }catch(NumberFormatException e){
//                e.printStackTrace();
//                Snackbar.make(tvFarmCity, "Error in adding cacao to remote database.", Snackbar.LENGTH_SHORT).show();
//            }
        }
    }

    public  String getIpAddress(){
        return getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE)
                .getString(SharedPreferencesFile.ATTRIBUTE_IPADDRESS, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor cursor = getContentResolver().query(
                DatabaseContract.CacaoUpdate.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(id_cacao)).build(),
                null, null, null, null
        );

        updatesAdapter.changeCursor(cursor);
        updatesAdapter.notifyDataSetChanged();
    }

    public void updateCacaoDetailsDisplay(){
        Cursor cursor = getContentResolver().query(
                DatabaseContract.Cacao.buildCacaoItemUri(id_cacao),
                null, null, null, null
        );

        cacao = cursorToCacao(cursor);

        tvFarmCity.setText(cacao.getFarm() + ", " + cacao.getCity());
        // tvCity.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.Cacao.COLUMN_CITY)));

        tvLocation.setText(cacao.getLatitude() + ", " + cacao.getLongitude());
        tvWeight.setText("" + cacao.getWeight());
        tvStatus.setText(cacao.getStatus());
        tvId.setText("" + cacao.getId());

        Log.i("tag", cacao.getServerIdCacao() + "");

        if(cacao.getServerIdCacao() > 0){
            ibSync.setImageResource(R.drawable.sync_64_gray);
            ibSync.setClickable(false);
            ibSync.setTag(R.id.sync_enabled, false);
        }else{
            ibSync.setImageResource(R.drawable.sync_64_green);
            ibSync.setClickable(true);
            ibSync.setTag(R.id.sync_enabled, true);
        }

//        tvEntries.setText(String.valueOf(new DatabaseHelper(getBaseContext()).getNumberOfUpdatesOfCacao(id_cacao)));
    }

    public Cacao cursorToCacao(Cursor cursor){
        Cacao cacao = new Cacao();

        SharedPreferences sp = getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE);
        String serialNumber = sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, null);
        Log.i("SERIAL NUMBER 2 ITO UN","Serial number is " + sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, "noneasdasd"));

        long id = cursor.getLong(cursor.getColumnIndex(DatabaseContract.Cacao._ID));
        cacao.setId(id);
//        cacao.setTag(cursor.getString(cursor.getColumnIndex(DatabaseContract.Cacao.COLUMN_TAG)));
        cacao.setStatus(cursor.getString(cursor.getColumnIndex(DatabaseContract.Cacao.COLUMN_STATUS)));
        cacao.setSerialNumber(serialNumber);
        cacao.setCity(cursor.getString(cursor.getColumnIndex(DatabaseContract.Cacao.COLUMN_CITY)));
        cacao.setFarm(cursor.getString(cursor.getColumnIndex(DatabaseContract.Cacao.COLUMN_FARM)));
        cacao.setLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseContract.Cacao.COLUMN_LATITUDE)));
        cacao.setLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseContract.Cacao.COLUMN_LONGITUDE)));
        cacao.setWeight(cursor.getDouble(cursor.getColumnIndex(DatabaseContract.Cacao.COLUMN_WEIGHT)));
        cacao.setServerIdCacao(cursor.getInt(cursor.getColumnIndex(DatabaseContract.Cacao.COLUMN_SERVERIDCACAO)));

        return cacao;
    }

    public void submitCacaoToServer(Cacao cacao){
        new SubmitCacaoToServerTask().execute(cacao);
    }

    public void showProgressDialog(){
        progressDialog = ProgressDialog.show(ViewCacaoActivity.this, "Uploading",
                "Sending cacao to server. You can sync this later.", true, true);
    }

}
