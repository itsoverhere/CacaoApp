package research.dlsu.cacaoapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.sql.Date;
import java.util.Calendar;

/**
 * Created by courtneyngo on 4/22/16.
 */
public class AlbumAdapter extends CursorRecyclerViewAdapter<AlbumAdapter.AlbumViewHolder>{

    private OnItemClickListener mOnItemClickListener;
    private OnLoadDataListener mOnLoadDataListener;

    int viewWidth = -1;

    public AlbumAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder viewHolder, Cursor cursor, int position) {
        CacaoUpdate cacaoupdate = cursorToCacaoUpdate(cursor);
        viewHolder.tvId.setText(String.valueOf(cacaoupdate.getId()));
        viewHolder.tvPath.setText(cacaoupdate.getPath());

        if(!cacaoupdate.getResult().isEmpty()) {
            viewHolder.tvResult.setText(cacaoupdate.getResult());
        }else{
            viewHolder.tvResult.setText("No results yet");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(cacaoupdate.getDate());
        viewHolder.tvDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + " "
                                  + calendar.get(Calendar.MONTH)+1 + " "
                                  + calendar.get(Calendar.YEAR) + " || "
                                  + calendar.get(Calendar.HOUR_OF_DAY)+ ":"
                                  + calendar.get(Calendar.MINUTE));

//        viewHolder.tvNumUpdates.setText(String.valueOf(cacao.getNumOfUpdates()));
//
//        if(cacao.getNumOfUpdates()!=0){
//            viewHolder.tvLastUpdate.setText(cacao.getLastUpdate().toString());
//        }else{
//            viewHolder.tvLastUpdate.setText("No entries yet.");
//        }
        boolean fileExists = false;

//        if(viewWidth > 0) {
//            //bitmap = decodeSampledBitmapFromPath(cacaoupdate.getPath(), viewWidth, -1);
//            bitmap = BitmapFactory.decodeFile(cacaoupdate.getPath());
////            viewHolder.ivImage.setImageBitmap(bitmap);
//        }
        if(new File(cacaoupdate.getPath()).exists()){
            fileExists = true;
        }

        // button sync
        if(cacaoupdate.getServerIdCacaoUpdate() < 1 && fileExists){
            viewHolder.buttonSync.setImageResource(R.drawable.sync_64_green);
        }else if(fileExists){
            viewHolder.buttonSync.setImageResource(R.drawable.sync_64_gray);
        }else if(!fileExists){
            viewHolder.buttonSync.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }

        viewHolder.container.setTag(cacaoupdate.getId());

        if(position % 2 == 0){
            viewHolder.container.setBackgroundColor(Color.parseColor("#ffffff"));
        }else{
            viewHolder.container.setBackgroundColor(Color.parseColor("#efefef"));
        }

        viewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id = Integer.parseInt(v.getTag().toString());
                mOnItemClickListener.onItemClick(id);
            }
        });
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_album, parent, false);

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_update_item, parent, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    viewWidth = view.getWidth();

                    notifyDataSetChanged();
                }
            });
        }

        return new AlbumViewHolder(view);
    }

    public CacaoUpdate cursorToCacaoUpdate(Cursor cursor){
        CacaoUpdate cacaoupdate = new CacaoUpdate();
        long id = cursor.getLong(cursor.getColumnIndex(DatabaseContract.CacaoUpdate._ID));
        cacaoupdate.setId(id);
        cacaoupdate.setPath(cursor.getString(cursor.getColumnIndex(DatabaseContract.CacaoUpdate.COLUMN_PATH)));
        cacaoupdate.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DatabaseContract.CacaoUpdate.COLUMN_DATE))));

        String type = cursor.getString(cursor.getColumnIndex(DatabaseContract.CacaoUpdate.COLUMN_CACAOTYPE));
        cacaoupdate.setCacaoType(CacaoUpdate.CacaoType.valueOf(type));

        cacaoupdate.setServerIdCacaoUpdate(cursor.getInt(cursor.getColumnIndex(DatabaseContract.CacaoUpdate.COLUMN_SERVERIDCACAOUPDATE)));
        cacaoupdate.setResult(cursor.getString(cursor.getColumnIndex(DatabaseContract.CacaoUpdate.COLUMN_RESULT)));

//        cacao.setTag(cursor.getString(cursor.getColumnIndex(DatabaseContract.Cacao.COLUMN_TAG)));
//        cacao.setStatus(cursor.getString(cursor.getColumnIndex(DatabaseContract.Cacao.COLUMN_STATUS)));

//        cacao.setNumOfUpdates(mOnLoadDataListener.onLoadDataNumUpdates(id));
//        cacao.setLastUpdate(mOnLoadDataListener.onLoadDataLastUpdate(id));

        return cacaoupdate;
    }

    @Override
    public int getItemCount() {
        return getCursor().getCount();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder{

        TextView tvId;
        TextView tvDate;
        TextView tvPath;
        TextView tvResult;
        ImageButton buttonSync;
        ImageView ivImage;
//        TextView tvStatus;
//        TextView tvLastUpdate;
//        TextView tvNumUpdates;
        View container;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            tvId = (TextView) itemView.findViewById(R.id.tv_id);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvPath = (TextView) itemView.findViewById(R.id.tv_path);
            tvResult = (TextView) itemView.findViewById(R.id.tv_result);
            buttonSync = (ImageButton) itemView.findViewById(R.id.button_sync);
            ivImage = (ImageView) itemView.findViewById(R.id.iv_image);

//            tvLastUpdate = (TextView) itemView.findViewById(R.id.tv_last_update);
//            tvNumUpdates = (TextView) itemView.findViewById(R.id.tv_entries);
            container = itemView.findViewById(R.id.container);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setmOnLoadDataListener(OnLoadDataListener onLoadDataListener){
        this.mOnLoadDataListener = onLoadDataListener;
    }

    public interface OnItemClickListener{
        public void onItemClick(long id);
    }

    public interface OnLoadDataListener{
        public int onLoadDataNumUpdates(long id);
        public Date onLoadDataLastUpdate(long id);
    }

    // Clean all elements of the recycler

    public void clear() {
        // items.clear();
        getCursor().close();
        notifyDataSetChanged();
    }

    public void addAll(Cursor cursor) {
        // items.addAll(list);
        changeCursor(cursor);
        notifyDataSetChanged();

    }

    // Bitmap related methods
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

//        Log.i("calculate sample size", "inSampleSize is " + inSampleSize + " and with is " + width);

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

//        if (width > reqWidth) {
////            final int halfHeight = height / 2;
//            final int halfWidth = width / 2;
//            Log.i("calculate sample size", "inSampleSize is " + inSampleSize);
//
//            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
//            // height and width larger than the requested height and width.
//            while ((halfWidth / inSampleSize) > reqWidth) {
//
//                Log.i("calculate sample size", "inSampleSize is " + inSampleSize);
//                inSampleSize *= 2;
//            }
//        }

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

}


