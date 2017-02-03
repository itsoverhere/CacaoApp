package research.dlsu.cacaoapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewCacaosActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_cacaoupdate";
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
                Intent intent = new Intent(getBaseContext(), CameraActivity.class);
                finish();
                startActivity(intent);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        rvAlbums = (RecyclerView) findViewById(R.id.rv_albums);

        rvAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("asdas", "rv is clicked");
            }
        });

        albumAdapter = new AlbumAdapter(getBaseContext(), null);

        albumAdapter.setOnItemClickListener(new AlbumAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(long id) {
                if (!isFromChoosePhotoActivity) {
                    // normal viewing
                    Intent intent = new Intent();
                    intent.setClass(getBaseContext(), ViewCacaoUpdateActivity.class);
                    intent.putExtra(EXTRA_ID, id);
                    Log.i("ViewCacaosActivity", "extra_id is " + id);
                    startActivity(intent);
                } else {
                    throw new UnsupportedOperationException("Well whaddya know. It was needed.");
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestResults();
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
        rvAlbums.setLayoutManager(llm);
        rvAlbums.setAdapter(albumAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor cursor = getContentResolver().query(
                DatabaseContract.CacaoUpdate.CONTENT_URI, null, null, null, null);
        albumAdapter.changeCursor(cursor);
        albumAdapter.notifyDataSetChanged();
    }

    public void requestResults(){
        String phoneIdString = new DatabaseHelper(getBaseContext()).getCacaoUpdatesWithoutResults();
        if (phoneIdString != null) {
            new GetRequestResults().execute(phoneIdString);
        }else{
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(swipeRefreshLayout, "All cacaos already have results.", Snackbar.LENGTH_LONG).show();
        }
    }

    public class GetRequestResults extends AsyncTask<String, Void, String> {

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
            phoneIdString = params[0]; // get phoneIdString as fetched in requestResults()
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
            if(s!=null) {
                try {
                    cacaoResultsJson = new JSONArray(s);
                    for (int i = 0; cacaoResultsJson != null && i < cacaoResultsJson.length(); i++) {
                        CacaoUpdate c = new CacaoUpdate();
                        JSONObject jsonObject = cacaoResultsJson.getJSONObject(i);
                        c.setResult(jsonObject.getString(DatabaseContract.CacaoUpdate.COLUMN_RESULT));
                        c.setId(jsonObject.getInt(DatabaseContract.CacaoUpdate.PHONEIDCACAOUPDATE));
                        cacaoUpdateResults.add(c);
                        //cacaoResults.add(c);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Snackbar.make(rvAlbums, "Everything is up to date.", Snackbar.LENGTH_SHORT).show();
            }
            updateCacaoResults(cacaoUpdateResults);
        }
    }

    public void updateCacaoResults(ArrayList<CacaoUpdate> cacaoUpdateResults){
        new DatabaseHelper(getBaseContext()).updateCacaoUpdateResults(cacaoUpdateResults);
        swipeRefreshLayout.setRefreshing(false);

        // Update recyclerview here
        albumAdapter.notifyDataSetChanged();

    }

    public  String getIpAddress(){
        return getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE)
                .getString(SharedPreferencesFile.ATTRIBUTE_IPADDRESS, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View v = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialog_ipaddress, null);
        final EditText etIpAddress = (EditText) v.findViewById(R.id.et_ipaddress);
        etIpAddress.setText(RemoteServer.IPADDRESS);
        int menuId = item.getItemId();
        switch(menuId){
            case R.id.ip_address:
                AlertDialog alertDialog = new AlertDialog.Builder(ViewCacaosActivity.this)
                        .setView(v)
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE)
                                        .edit()
                                        .putString(SharedPreferencesFile.ATTRIBUTE_IPADDRESS, etIpAddress.getText().toString())
                                        .commit();
//                                RemoteServer.IPADDRESS = etIpAddress.getText().toString();
                            }
                        })
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                alertDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
