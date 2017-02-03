package research.dlsu.cacaoapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewAlbumsActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_cacao";
    private RecyclerView rvAlbums;
    private AlbumAdapter albumAdapter;

    SwipeRefreshLayout swipeRefreshLayout;

    private boolean isFromChoosePhotoActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_albums);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), AddNewCacaoActivity.class);
                startActivity(intent);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        rvAlbums = (RecyclerView) findViewById(R.id.rv_albums);
        albumAdapter = new AlbumAdapter(getBaseContext(), null);

        if (getIntent().hasExtra(ChoosePhotoActivity.EXTRA_FROM_CHOOSE_PHOTO)){
            isFromChoosePhotoActivity = true;
        }

        albumAdapter.setmOnLoadDataListener(new AlbumAdapter.OnLoadDataListener() {
            @Override
            public int onLoadDataNumUpdates(long id) {
//                return new DatabaseHelper(getBaseContext()).getNumberOfUpdatesOfCacao(id);
                return -1;
            }

            @Override
            public Date onLoadDataLastUpdate(long id) {
                return new Date(-1);
//                return new DatabaseHelper(getBaseContext()).getLastCacaoUpdateOfCacao(id);
            }
        });

        albumAdapter.setOnItemClickListener(new AlbumAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(long id) {
                if (!isFromChoosePhotoActivity) {
                    // normal viewing
                    Intent intent = new Intent();
                    intent.setClass(getBaseContext(), ViewCacaoActivity.class);
                    intent.putExtra(EXTRA_ID, id);
                    startActivity(intent);
                } else {
                    // from choose photo activity, return chosen cacao to ChoosePhotoActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(EXTRA_ID, id);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestResults();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        rvAlbums.setLayoutManager(linearLayoutManager);
        rvAlbums.setAdapter(albumAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor cursor = getContentResolver().query(
                DatabaseContract.Cacao.CONTENT_URI, null, null, null, null);
        albumAdapter.swapCursor(cursor);
        albumAdapter.notifyDataSetChanged();
    }

    public void requestResults(){
        String phoneIdString = new DatabaseHelper(getBaseContext()).getCacaoUpdatesWithoutResults();
        if (phoneIdString != null) {
            new GetRequestResults().execute();
        }else{
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public class GetRequestResults extends AsyncTask<String, Void, String>{

        String serialNumber = "";
        String phoneIdString = "";
        boolean hasResultToFetch = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferences sp = getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE);
            serialNumber = sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, "");
            phoneIdString = new DatabaseHelper(getBaseContext()).getCacaoUpdatesWithoutResults();
        }

        @Override
        protected String doInBackground(String... params) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .build();

            RequestBody requestBody = new FormBody.Builder()
                    .add(DatabaseContract.OnSiteUser.SERIALNUMBER, serialNumber)
                    .add(DatabaseContract.Cacao.EXTRA_PHONEIDCACAOSTRING, phoneIdString)
                    .build();

            Request request = new Request.Builder()
                    .url(RemoteServer.buildGetResultsUri(getIpAddress()))
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
            ArrayList<CacaoUpdate> cacaoUpdateResults = new ArrayList<>();
            JSONArray cacaoResultsJson = null;
            try {
                cacaoResultsJson = new JSONArray(s);
                for(int i=0; i<cacaoResultsJson.length(); i++){
                    CacaoUpdate c = new CacaoUpdate();
                    JSONObject jsonObject = cacaoResultsJson.getJSONObject(i);
                    c.setResult(jsonObject.getString(DatabaseContract.CacaoUpdate.COLUMN_RESULT));
                    c.setId(jsonObject.getInt(DatabaseContract.CacaoUpdate.EXTRA_ID));
                    cacaoUpdateResults.add(c);
                    // cacaoUpdateResults.add(c);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            updateCacaoResults(cacaoUpdateResults);
        }
    }

    public  String getIpAddress(){
        return getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE)
                .getString(SharedPreferencesFile.ATTRIBUTE_IPADDRESS, null);
    }

    public void updateCacaoResults(ArrayList<CacaoUpdate> cacaoUpdateResults){
        new DatabaseHelper(getBaseContext()).updateCacaoUpdateResults(cacaoUpdateResults);
        swipeRefreshLayout.setRefreshing(false);
    }

}
