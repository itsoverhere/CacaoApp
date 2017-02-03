package research.dlsu.cacaoapp;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewCacaoUpdateActivity extends AppCompatActivity {

    public static final int HAS_SYNCED = 0;
    public static final int NOT_SYNCED = 1;
    public static final int NULL_BITMAP = 2;

    private TextView tvId;
    private TextView tvDate;
    private TextView tvPath;
    private TextView tvType;
    private ImageButton buttonSync;
    private ImageView ivImage;
    ProgressDialog progressDialog;

    private CacaoUpdate cacaoupdate;
    int viewWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cacao_update);

        tvId = (TextView) findViewById(R.id.tv_id);
        tvDate = (TextView) findViewById(R.id.tv_date);
        tvPath = (TextView) findViewById(R.id.tv_path);
        tvType = (TextView) findViewById(R.id.tv_type);
        buttonSync = (ImageButton) findViewById(R.id.button_sync);
        ivImage = (ImageView) findViewById(R.id.iv_image);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        ViewTreeObserver viewTreeObserver = ivImage.getViewTreeObserver();
//        if (viewTreeObserver.isAlive()) {
//            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    ivImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    viewWidth = ivImage.getWidth();
//
//                    getCacaoUpdateId();
//                }
//            });
//        }
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        viewWidth = size.x;

        getCacaoUpdateId();
    }

    public void getCacaoUpdateId(){
        long idcacaoupdate = getIntent().getExtras().getLong(ViewCacaosActivity.EXTRA_ID, -1);
        if(idcacaoupdate==-1){
            finish();
        }else {
            populateData(idcacaoupdate);
        }
    }

    public void populateData(long idcacaoupdate){
        Cursor cursor = getContentResolver().query(
            DatabaseContract.CacaoUpdate.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(idcacaoupdate)).build(),
            null, null, null, null
        );


        if(cursor.moveToFirst()){
            cacaoupdate = cursorToCacaoUpdate(cursor);

            tvId.setText(String.valueOf(cacaoupdate.getId()));
            tvPath.setText(cacaoupdate.getPath());
            tvType.setText(cacaoupdate.getCacaoType().name());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(cacaoupdate.getDate());
            tvDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.get(Calendar.MONTH)+1 + " "
                    + calendar.get(Calendar.YEAR) + " || "
                    + calendar.get(Calendar.HOUR_OF_DAY)+ ":"
                    + calendar.get(Calendar.MINUTE));

            Log.i("tag", "IVIMAGE WIDTH IS  "+ ivImage.getWidth());

            Bitmap bitmap = null;
            if(viewWidth > 0) {
                bitmap = decodeSampledBitmapFromPath(cacaoupdate.getPath(), viewWidth, -1);
                ivImage.setImageBitmap(bitmap);
            }

            if(cacaoupdate.getServerIdCacaoUpdate() != -1){
                // has already synced
                setButtonSync(HAS_SYNCED);
            }else if(bitmap != null){
                // hasn't synced
                setButtonSync(NOT_SYNCED);
            }else if(bitmap == null){
                setButtonSync(NULL_BITMAP);
            }

        }else{
            finish();
        }

        cursor.close();

    }

    public CacaoUpdate cursorToCacaoUpdate(Cursor cursor){

        CacaoUpdate cacaoupdate = new CacaoUpdate();

        long id = cursor.getLong(cursor.getColumnIndex(DatabaseContract.CacaoUpdate._ID));
        cacaoupdate.setId(id);
        cacaoupdate.setPath(cursor.getString(cursor.getColumnIndex(DatabaseContract.CacaoUpdate.COLUMN_PATH)));
        cacaoupdate.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DatabaseContract.CacaoUpdate.COLUMN_DATE))));
        cacaoupdate.setServerIdCacaoUpdate(cursor.getInt(cursor.getColumnIndex(DatabaseContract.CacaoUpdate.COLUMN_SERVERIDCACAOUPDATE)));

        String type = cursor.getString(cursor.getColumnIndex(DatabaseContract.CacaoUpdate.COLUMN_CACAOTYPE));
        cacaoupdate.setCacaoType(CacaoUpdate.CacaoType.valueOf(type));

        return cacaoupdate;
    }

    // Bitmap related methods
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromPath(String path,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // BitmapFactory.decodeResource(res, resId, options);
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        // return BitmapFactory.decodeResource(res, resId, options);
        return BitmapFactory.decodeFile(path, options);
    }

    public class SubmitCacaoUpdateToServerTask extends AsyncTask<CacaoUpdate, Void, String>{

        CacaoUpdate cacaoUpdate;

        @Override
        protected String doInBackground(CacaoUpdate... params) {

            Log.i("display", "nowsending to server : " + RemoteServer.buildInsertCacaoUpdateUri(getIpAddress()));

            publishProgress();

            cacaoUpdate = params[0];

            SharedPreferences sp = getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE);
            String serialNumber = sp.getString(SharedPreferencesFile.ATTRIBUTE_SERIALNUMBER, null);

            MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");

            OkHttpClient okHttpClient
                    = new OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .build();

            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeFile(cacaoUpdate.getPath());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            if(bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                RequestBody requestBody =
                        new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart(DatabaseContract.CacaoUpdate.EXTRA_ID, String.valueOf(cacaoUpdate.getId()))
                                //                        .addFormDataPart(DatabaseContract.CacaoUpdate.EXTRA_SERVERIDCACAO, String.valueOf(cacaoUpdate.getServerIdCacao()))
                                //                        .addFormDataPart(DatabaseContract.CacaoUpdate.COLUMN_IDCACAO, String.valueOf(id_cacao))
                                .addFormDataPart(DatabaseContract.CacaoUpdate.COLUMN_DATE, String.valueOf(cacaoUpdate.getDate().getTime()))
                                .addFormDataPart(DatabaseContract.CacaoUpdate.COLUMN_CACAOTYPE, cacaoUpdate.getCacaoType().name())
                                .addFormDataPart(DatabaseContract.OnSiteUser.SERIALNUMBER, serialNumber)
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
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("cacaoupdateresult", "results are " + s);
            try {
                Long serverIdCacaoUpdate = Long.parseLong(s);
                if(new DatabaseHelper(getBaseContext()).updateServerIdCacaoUpdate(cacaoUpdate.getId(), serverIdCacaoUpdate)){
                    Snackbar.make(ivImage, "Success! Update " + cacaoUpdate.getId() + " has been synced.", Snackbar.LENGTH_SHORT).show();

                    setButtonSync(HAS_SYNCED);
                }else{
                    Snackbar.make(ivImage, "Something went wrong during local database update, please try again.", Snackbar.LENGTH_SHORT).show();
                }
            }catch(NumberFormatException ex){
                Snackbar.make(ivImage, "Something went wrong. Please try again.", Snackbar.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }
    }

    public void setButtonSync(int sync){
        if (sync == HAS_SYNCED || sync == NULL_BITMAP) {
            buttonSync.setEnabled(false);
            buttonSync.setTag(R.id.sync_enabled, false);
            buttonSync.setOnClickListener(null);

            if(sync == HAS_SYNCED){
                buttonSync.setImageResource(R.drawable.sync_64_gray);
            }else if(sync == NULL_BITMAP){
                buttonSync.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            }
        }else if(sync == NOT_SYNCED){
            buttonSync.setImageResource(R.drawable.sync_64_green);
            buttonSync.setEnabled(true);
            buttonSync.setTag(R.id.sync_enabled, true);
            buttonSync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SubmitCacaoUpdateToServerTask().execute(cacaoupdate);
                }
            });
        }
    }

    public  String getIpAddress(){
        return getSharedPreferences(SharedPreferencesFile.SP_NAME, MODE_PRIVATE)
                .getString(SharedPreferencesFile.ATTRIBUTE_IPADDRESS, null);
    }

    public void showProgressDialog(){
        progressDialog = ProgressDialog.show(ViewCacaoUpdateActivity.this, "Uploading",
                "Sending cacao update to server. You can sync this later. Tap anywhere to dismiss.", true, true);
    }
}
