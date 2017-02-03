package research.dlsu.cacaoapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

// NEW VERSION
public class MatchActivity extends Activity {

    private final String TAG = "MatchActivity";
    private final String DISPLAY_TAG = "MatchActivity";

    // templateMatrix refers to the image matrix of the template cacao image
    private Matrix templateMatrix = new Matrix();
    private Matrix cacaoMatrix = new Matrix();
//    private Matrix templateSavedMatrix = new Matrix();

    // variables for touch modes
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    //
    float lastAngle = 0f;

    //
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;
    private String cacaoImagePath = "";
    private String podname="", version="", mCurrentPhotoPath;

    private ImageView ivTemplate;
    private ImageView ivCacaoImage;
    private TextView tvDetails;
//    private Button buttonCalculate;
    private Button buttonAddWidth;
    private Button buttonSubtractWidth;
//    private Button buttonDone;
    private Button buttonStick;
    private SeekBar rotationSeekBar;
    Bitmap bitmapCacaoImage;
    Bitmap bitmapTemplate;
    Bitmap bitmapResult;

    boolean stickyTemplate = false;
    double aggregatedScale = 0.5;

    ScaleGestureDetector scaleGestureDetector;
    RotationGestureDetector rotationGestureDetector;
    GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_template);

        // initialize views
        ivTemplate = (ImageView) findViewById(R.id.ivtemplate);
        ivCacaoImage = (ImageView) findViewById(R.id.ivimage);
//        buttonCalculate = (Button) findViewById(R.id.calculate);
//        buttonDone = (Button) findViewById(R.id.done);
        buttonStick = (Button) findViewById(R.id.stick);
        tvDetails = (TextView) findViewById(R.id.tv);
        rotationSeekBar = (SeekBar) findViewById(R.id.seekBar);

        rotationSeekBar.setFadingEdgeLength(50);
        rotationSeekBar.setMax(360);
        rotationSeekBar.setProgress(180);
        rotationSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        // set image to what user took/chose
//        cacaoImagePath = getIntent().getStringExtra(ChoosePhotoActivity.EXTRA_PATH);
//        bitmapCacaoImage = BitmapFactory.decodeFile(cacaoImagePath);

        buttonStick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stickyTemplate = !stickyTemplate;
            }
        });


        // reorient image if needed
//        if(orientationValue == ExifInterface.ORIENTATION_UNDEFINED){
            Matrix tempMatrix = new Matrix();
            tempMatrix.postRotate(90);
//            bitmapCacaoImage = Bitmap.createBitmap(bitmapCacaoImage, 0, 0,
//                    bitmapCacaoImage.getWidth(), bitmapCacaoImage.getHeight(), tempMatrix, true);
//        }
//        ivCacaoImage.setImageBitmap(bitmapCacaoImage);
        bitmapCacaoImage = ((BitmapDrawable)ivCacaoImage.getDrawable()).getBitmap();

//        buttonCalculate.setOnClickListener(calculateListener);
        // ivTemplate.setOnTouchListener();

        // bitmapCacaoImage = ((BitmapDrawable) ivCacaoImage.getDrawable()).getBitmap();
        bitmapTemplate = ((BitmapDrawable) ivTemplate.getDrawable()).getBitmap();

        DisplayMetrics display = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(display);
        Log.i("Touch", "bitmapCacaoImage width and height " + bitmapCacaoImage.getWidth() + ", "+ bitmapCacaoImage.getHeight());

        float screenwidthheightratio = display.widthPixels/display.heightPixels;
        float templateWidthheightratio = bitmapCacaoImage.getWidth()/ bitmapCacaoImage.getHeight();

        // templateratio > screenratio, follow width
        // templateratio < screenratio, follow height
        float ratio = 1f;
        float newHeight = 1f, newWidth = 1f;
        if(templateWidthheightratio > screenwidthheightratio){
            ratio = (float) display.widthPixels / (float) bitmapCacaoImage.getWidth();
            newHeight = bitmapCacaoImage.getHeight() * ratio;
            newWidth = display.widthPixels;
            Log.i("Touch", "Follow width Ratio is " + ratio);
        }else{
            ratio = (float) display.heightPixels / (float) bitmapCacaoImage.getHeight();
            newWidth = bitmapCacaoImage.getWidth() * ratio;
            newHeight = display.heightPixels;
            Log.i("Touch", "Follow height Ratio is " + ratio);
        }

        Log.i("Touch", "tvDetails widthxheight " + display.widthPixels + ", " + display.heightPixels);
        Log.i("Touch", "new widthxheight " + newWidth + ", " + newHeight);
        bitmapCacaoImage = Bitmap.createScaledBitmap(bitmapCacaoImage, Math.round(newWidth), Math.round(newHeight), false);
        bitmapTemplate = Bitmap.createScaledBitmap(bitmapTemplate, Math.round(newWidth), Math.round(newHeight),false);

        // to delete
        templateMatrix.postScale(0.5f, 0.5f);
        ivTemplate.setImageMatrix(tempMatrix);

        ivCacaoImage.setScaleType(ImageView.ScaleType.MATRIX);
        ivTemplate.setScaleType(ImageView.ScaleType.MATRIX);
        ivTemplate.setAlpha(0.5f);

        ivCacaoImage.setImageBitmap(bitmapCacaoImage);
        ivTemplate.setImageBitmap(bitmapTemplate);

        buttonAddWidth = (Button) findViewById(R.id.addwidth);
        buttonSubtractWidth = (Button) findViewById(R.id.subtractwidth);
        buttonAddWidth.setOnClickListener(addTemplateWidth);
        buttonSubtractWidth.setOnClickListener(subtractTemplateWidth);
//        buttonDone.setOnClickListener(submitPhotoBack);

        scaleGestureDetector = new ScaleGestureDetector(getBaseContext(), new ScaleListener());
//        rotationGestureDetector = new RotationGestureDetector(onRotationGestureListener);
        gestureDetector = new GestureDetector(getBaseContext(), onGestureListener);

    }

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            int angle = i - 180;
            Log.i(TAG, ">>>>> angle is " + angle);

            float[] templateValues = new float[9];
            templateMatrix.getValues(templateValues); // places the matrix of the template to templateValues
            float globalX = templateValues[Matrix.MTRANS_X];
            float globalY = templateValues[Matrix.MTRANS_Y];
            float height = templateValues[Matrix.MSCALE_Y] * ((ImageView) ivTemplate).getDrawable().getIntrinsicHeight();
            float width = templateValues[Matrix.MSCALE_X] * ((ImageView) ivTemplate).getDrawable().getIntrinsicWidth();


//            templateMatrix.postRotate(lastAngle - angle, (float)center.getX(), (float)center.getY());
            templateMatrix.postRotate(lastAngle-angle, globalX, globalY);
            Log.i(TAG, "matrix is " + templateMatrix);
            ivTemplate.setImageMatrix(templateMatrix);
            lastAngle = angle;

            // tl 857, 515
            // tr 1398, 1111
            // bl 22, 1314
            // br 628, 1908
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    View.OnClickListener addTemplateWidth = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            templateMatrix.postScale(1.10f, 1f, mid.x, mid.y);
            ivTemplate.setImageMatrix(templateMatrix);
        }
    };

    View.OnClickListener subtractTemplateWidth = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            templateMatrix.postScale(0.95f, 1f, mid.x, mid.y);
            ivTemplate.setImageMatrix(templateMatrix);
        }
    };

    View.OnClickListener calculateListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            cropPhoto();
            saveAndFinish();
        }
    };

    public void cropPhoto(){
        float[] templateValues = new float[9];
        templateMatrix.getValues(templateValues); // places the matrix of the template to templateValues
        float globalX = templateValues[Matrix.MTRANS_X];
        float globalY = templateValues[Matrix.MTRANS_Y];
//            float height = templateValues[Matrix.MSCALE_Y] * ((ImageView) ivTemplate).getDrawable().getIntrinsicHeight();
//            float width = templateValues[Matrix.MSCALE_X] * ((ImageView) ivTemplate).getDrawable().getIntrinsicWidth();

        double height = aggregatedScale * ((ImageView) ivTemplate).getDrawable().getIntrinsicHeight();
        double width = aggregatedScale * ((ImageView) ivTemplate).getDrawable().getIntrinsicWidth();

        float rAngle = Math.round(Math.atan2(templateValues[Matrix.MSKEW_X], templateValues[Matrix.MSCALE_X]) * (180 / Math.PI));

        Matrix rerotateMatrix = new Matrix(); // bg is a blank matrix, no changes whatsoever
        float tx = templateValues[Matrix.MTRANS_X];
        float ty = templateValues[Matrix.MTRANS_Y];
        float sx = templateValues[Matrix.MSCALE_X];

        float[] bgValues = new float[9];
        rerotateMatrix.getValues(bgValues); // transfers values of rerotateMatrix to bgValues

        float bgtx = bgValues[Matrix.MTRANS_X];
        float bgty = bgValues[Matrix.MTRANS_Y];
        float bgsx = bgValues[Matrix.MSCALE_X];

        rerotateMatrix.setRotate(rAngle, ((ImageView) ivTemplate).getDrawable().getIntrinsicWidth() / 2, ((ImageView) ivTemplate).getDrawable().getIntrinsicHeight() / 2); // rotate it by the template's global x and y, or translate photo to move 0, 0 to top left

        float excessX = (float) (bitmapCacaoImage.getHeight() * Math.sin(Math.toRadians(rAngle)));
        // rerotateMatrix.postTranslate(-excessX, 0);
        // rotates original picture
        Log.i(TAG, "RaNGLE IS " + rAngle);
//            bitmapCacaoImage = Bitmap.createBitmap(bitmapCacaoImage, 0, 0, bitmapCacaoImage.getWidth(), bitmapCacaoImage.getHeight(), rerotateMatrix, false);
//            ivCacaoImage.setImageBitmap(bitmapCacaoImage); // display for checking

        Log.i(TAG, "globalX " + globalX + ", globalY " + globalY);
        // bitmapCacaoImage.recycle();


        Bitmap newnewbitmap = bitmapCacaoImage;

        // Get the coordinates of the template with rotation considered
        Coordinate topRight = new Coordinate(globalX + (width * Math.cos(Math.toRadians(-rAngle))), globalY + (width * Math.sin(Math.toRadians(-rAngle))));
        Coordinate bottomLeft = new Coordinate(globalX - (height * Math.cos(Math.toRadians(90-(-rAngle)))), globalY + (height * Math.sin(Math.toRadians(90-(-rAngle)))));
//            Coordinate bottomRight = new Coordinate(topRight.getX() + (height * Math.cos(Math.toRadians((-rAngle) + 90))), topRight.getY() + (height * Math.sin(Math.toRadians((-rAngle) + 90))));
        Coordinate bottomRight = new Coordinate(bottomLeft.getX() + (width * Math.cos(Math.toRadians(-rAngle))), bottomLeft.getY() + (width * Math.sin(Math.toRadians(-rAngle))));

        Log.i(TAG, "top left x = " + globalX + " y = " + globalY);
        Log.i(TAG, "top right " + topRight.toString());
        Log.i(TAG, "bottom right " + bottomRight.toString());
        Log.i(TAG, "bottom left " + bottomLeft.toString());

        // tr 707, 725
        //tl 1210 1052
        // bl 275, 1376
        // br 767 1717

        // Create a blank picture on a canvas that has the same height and width as the original picture
        Bitmap bitmap3 = Bitmap.createBitmap(newnewbitmap.getWidth(),
                newnewbitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap3);
//            canvas.rotate(rAngle);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, newnewbitmap.getWidth(),
                newnewbitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        // Make a path that follows the outline of the template
        Path path=new Path();
        path.lineTo(globalX, globalY);
        path.lineTo((float)topRight.getX(), (float)topRight.getY());
        path.lineTo((float)bottomRight.getX(), (float)bottomRight.getY());
        path.lineTo((float)bottomLeft.getX(), (float)bottomLeft.getY());
        path.lineTo(globalX, globalY);
        canvas.drawPath(path, paint);

        // Cut the path on the canvas
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // Draw the original cacao photo on the cut out
        canvas.drawBitmap(newnewbitmap, rect, rect, paint);


        // translate the origin point to globalX and globalY
        Matrix reAngleMatchMatrix = new Matrix();
        reAngleMatchMatrix.setTranslate(globalX, globalY);
        // then rotate around that pivot
        reAngleMatchMatrix.postRotate(rAngle, globalX, globalY);
        // postRotate crops, then rotates
//            Bitmap.createBitmap()
//            bitmapResult = Bitmap.createBitmap(bitmap3, (int)globalX, (int)globalY, (int) width, (int) height, reAngleMatchMatrix, true);


        // Now, the picture follows the template, which could be rotated
        // The pictures needs to be rerotated back to a orthogonal position
        // Calculate for the new position of x and y
            /*
            *   z = sqrt( (original x)^2 + (original y)^2 )
                Angle y = arscin(original y / z)
                Angle x = angle a - angle y
                w = cos(x) * z
            * */
        Log.i(DISPLAY_TAG, "+==========================================================================+ posttranslate");
        Log.i(DISPLAY_TAG, "rAngle is " + rAngle + " globalX is " + globalX + " globalY is " + globalY);
        Log.i(DISPLAY_TAG, "bitmap width is " + bitmap3.getWidth() + " bitmap height is " + bitmap3.getHeight());
        Log.i(DISPLAY_TAG, "+==========================================================================+");

        double newx = -1, newy = -1;

        // because the calculation differs if cw or ccw:
        if (rAngle < 0){
            // rotated clockwise
            double sidez = Math.sqrt(Math.pow(globalX,2) + Math.pow(globalY,2));
            double angley = Math.toDegrees(Math.asin(globalY/sidez));
            double anglex = -rAngle - angley;
            newx = Math.cos(Math.toRadians(anglex)) * sidez;

            double sideb = bitmap3.getWidth() - globalX;
            double sidec = Math.sqrt(Math.pow(globalY, 2) + Math.pow(sideb, 2));
            double angled = Math.toDegrees(Math.asin(globalY/sidec));
            double anglee = angled + Math.abs(rAngle);
            newy = sidec * Math.sin(Math.toRadians(anglee));
        }else{
            // rotated counter clockwise
            // for y
            double sidei = Math.sqrt(Math.pow(globalX,2) + Math.pow(globalY,2));
            double anglej = Math.toDegrees(Math.asin(globalY/sidei));
            double anglek = 90 - (rAngle + anglej);
            newy = Math.cos(Math.toRadians(anglek)) * sidei;

            // for x
            double sidem = bitmap3.getHeight() - globalY;
            double sidep = Math.sqrt(Math.pow(sidem, 2) + Math.pow(globalX, 2));
            double anglen = Math.toDegrees(Math.asin(sidem/sidep)); // or Math.acos(Math.toRadians(globalX/sidep))
            double angleo = anglen - rAngle;
            newx = Math.cos(Math.toRadians(angleo)) * sidep;
        }

        // This will reorient that picture back to an orthogonal angle
        bitmapResult = Bitmap.createBitmap(bitmap3, 0, 0, bitmap3.getWidth(), bitmap3.getHeight(), reAngleMatchMatrix, true);
//            bitmapResult = Bitmap.createBitmap(bitmap3, (int)globalX, (int)globalY, (int)width, (int)height, reAngleMatchMatrix, true);
        Log.i(DISPLAY_TAG, "new x is " + newx + " newy is " + newy);
        float[] pts = new float[2];
        pts[0] = globalX;
        pts[1] = globalY;
        Log.i(DISPLAY_TAG, "MAP old x is " + pts[0] + " old y is " + pts[1]);
        reAngleMatchMatrix.mapPoints(pts);
        Log.i(DISPLAY_TAG, "MAP new x is " + pts[0] + " newy is " + pts[1]);
        // This will cut the picture based on the new x and new y
        bitmapResult = Bitmap.createBitmap(bitmapResult, (int)newx, (int)newy,
                (int) width, (int) height,
                new Matrix(), true);
//

        ivCacaoImage.setImageBitmap(bitmapResult);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        File folder = new File(Environment.getExternalStorageDirectory(), "Cacao");
        boolean folderExists = true;
        if(!folder.exists()){
            if(!folder.mkdir()){
                Log.i(TAG, "Error in making Cacao directory");
                folderExists = false;
            }
        }
//
//        if(folderExists) {
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            String imageFileName = timeStamp + "_CACAO_" + podname + "_" + version;
////            File storageDir = Environment.getExternalStoragePublicDirectory(
////                    Environment.DIRECTORY_PICTURES);
//            File image = File.createTempFile(
//                    imageFileName,  /* prefix */
//                    ".jpg",         /* suffix */
//                    folder      /* directory */
//            );
//
//            // Save a file: path for use with ACTION_VIEW intents
//            mCurrentPhotoPath = image.getAbsolutePath();
//            return image;
//        }

        if(folderExists) {
            FileOutputStream out = null;

            //        File folder = new File(Environment.getExternalStorageDirectory(), "Cacao");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File imageFile = new File(folder.getAbsolutePath(), timeStamp + "_CACAO_" + podname + "_" + version+".PNG");
            mCurrentPhotoPath = imageFile.getAbsolutePath();
            Log.i("TRY", "imageFile is " + imageFile);

            try {
                out = new FileOutputStream(imageFile);
                bitmapResult.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    View.OnClickListener submitPhotoBack = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveAndFinish();
        }
    };

    public void saveAndFinish(){
        try {
            createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent i = new Intent();
        i.putExtra(ChoosePhotoActivity.EXTRA_PATH, "extra");
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        rotationGestureDetector.onTouchEvent(event);
        if(!stickyTemplate){
            if(event.getPointerCount() == 2) {
                scaleGestureDetector.onTouchEvent(event);
            }else if(event.getPointerCount() == 1 ) {
                gestureDetector.onTouchEvent(event);
            }
        }

        printCoordinates();

//        Log.i(TAG, "currently pointing at : " + event.getX() +", " + event.getY() );
        return super.onTouchEvent(event);
    }

    GestureDetector.SimpleOnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            return super.onScroll(e1, e2, distanceX, distanceY);

            templateMatrix.postTranslate(-distanceX, -distanceY);
            ivTemplate.setImageMatrix(templateMatrix);
            return false;
        }
    };

    public void printCoordinates(){
        float[] templateValues = new float[9];
        templateMatrix.getValues(templateValues); // places the matrix of the template to templateValues
        float globalX = templateValues[Matrix.MTRANS_X];
        float globalY = templateValues[Matrix.MTRANS_Y];
//        float height = templateValues[Matrix.MSCALE_Y] * ((ImageView) ivTemplate).getDrawable().getIntrinsicHeight();
//        float width = templateValues[Matrix.MSCALE_X] * ((ImageView) ivTemplate).getDrawable().getIntrinsicWidth();

        double height = aggregatedScale * ((ImageView) ivTemplate).getDrawable().getIntrinsicHeight();
        double width = aggregatedScale * ((ImageView) ivTemplate).getDrawable().getIntrinsicWidth();
        float rAngle = Math.round(Math.atan2(templateValues[Matrix.MSKEW_X], templateValues[Matrix.MSCALE_X]) * (180 / Math.PI));

        Matrix rerotateMatrix = new Matrix(); // bg is a blank matrix, no changes whatsoever
        float tx = templateValues[Matrix.MTRANS_X];
        float ty = templateValues[Matrix.MTRANS_Y];
        float sx = templateValues[Matrix.MSCALE_X];

        float[] bgValues = new float[9];
        rerotateMatrix.getValues(bgValues); // transfers values of rerotateMatrix to bgValues

        float bgtx = bgValues[Matrix.MTRANS_X];
        float bgty = bgValues[Matrix.MTRANS_Y];
        float bgsx = bgValues[Matrix.MSCALE_X];

        rerotateMatrix.setRotate(rAngle, ((ImageView) ivTemplate).getDrawable().getIntrinsicWidth() / 2, ((ImageView) ivTemplate).getDrawable().getIntrinsicHeight() / 2); // rotate it by the template's global x and y, or translate photo to move 0, 0 to top left

        float excessX = (float) (bitmapCacaoImage.getHeight() * Math.sin(Math.toRadians(rAngle)));
        // rerotateMatrix.postTranslate(-excessX, 0);
        // rotates original picture
        Log.i(TAG, "RaNGLE IS " + rAngle);
//            bitmapCacaoImage = Bitmap.createBitmap(bitmapCacaoImage, 0, 0, bitmapCacaoImage.getWidth(), bitmapCacaoImage.getHeight(), rerotateMatrix, false);
//            ivCacaoImage.setImageBitmap(bitmapCacaoImage); // display for checking

        Log.i(TAG, "globalX " + globalX + ", globalY " + globalY);
        // bitmapCacaoImage.recycle();


        Bitmap newnewbitmap = bitmapCacaoImage;

        Coordinate topRight = new Coordinate(globalX + (width * Math.cos(Math.toRadians(-rAngle))), globalY + (width * Math.sin(Math.toRadians(-rAngle))));
        Coordinate bottomLeft = new Coordinate(globalX - (height * Math.cos(Math.toRadians(90-(-rAngle)))), globalY + (height * Math.sin(Math.toRadians(90-(-rAngle)))));
//            Coordinate bottomRight = new Coordinate(topRight.getX() + (height * Math.cos(Math.toRadians((-rAngle) + 90))), topRight.getY() + (height * Math.sin(Math.toRadians((-rAngle) + 90))));
        Coordinate bottomRight = new Coordinate(bottomLeft.getX() + (width * Math.cos(Math.toRadians(-rAngle))), bottomLeft.getY() + (width * Math.sin(Math.toRadians(-rAngle))));

        Log.i(DISPLAY_TAG, "WIDTH " + width);
        Log.i(DISPLAY_TAG, "HEIGHT " + height);
        Log.i(DISPLAY_TAG, "top left x = " + globalX + " y = " + globalY);
        Log.i(DISPLAY_TAG, "top right " + topRight.toString());
        Log.i(DISPLAY_TAG, "bottom right " + bottomRight.toString());
        Log.i(DISPLAY_TAG, "bottom left " + bottomLeft.toString());
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        // transformationMatrix of the current "scale" session;
        // scale sessions start when the user places two fingers down on the screen
        // and ends when the two fingers leave the screen
        Matrix currentSessionTransformationMatrix = new Matrix();
        float lastScale = 1;
        float lastFocusX = 0, lastFocusY = 0;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            // currentSessionTransformationMatrix = templateMatrix;
            // currentSessionTransformationMatrix.postTranslate(lastFocusX - (ivTemplate.getWidth() / 2), lastFocusY - (ivTemplate.getHeight() / 2));
            // currentSessionTransformationMatrix.postScale(lastScale, lastScale);
            // ivTemplate.setImageMatrix(currentSessionTransformationMatrix);

            lastFocusX = detector.getFocusX();
            lastFocusY = detector.getFocusY();

//            Log.i(TAG, " begin lastScale " + lastScale + " x : " + lastFocusX + " y : " + lastFocusY);
            lastScale = 1;
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            // Log.i(TAG, "Scale is " + Float.toString(scale));

//            currentSessionTransformationMatrix = new Matrix();
            currentSessionTransformationMatrix = templateMatrix;
//            float[] values = new float[9];
//            templateMatrix.getValues(values);

            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
//            currentSessionTransformationMatrix.postTranslate(focusX - (ivTemplate.getWidth() / 2), focusY - (ivTemplate.getHeight() / 2));
//            currentSessionTransformationMatrix.postScale(detector.getScaleFactor() * lastScale, detector.getScaleFactor() * lastScale);
//            ivTemplate.setImageMatrix(currentSessionTransformationMatrix);

//            templateMatrix.postTranslate(focusX - (ivTemplate.getWidth() / 2), focusY - (ivTemplate.getHeight() / 2));
            // templateMatrix.postScale(detector.getScaleFactor() * lastScale, detector.getScaleFactor() * lastScale);
            templateMatrix.postScale(detector.getScaleFactor() / lastScale, detector.getScaleFactor() / lastScale);

            Log.i(DISPLAY_TAG, "Last Scale " + lastScale );
            Log.i(DISPLAY_TAG, "SCALE factor " + detector.getScaleFactor() );
            Log.i(DISPLAY_TAG, "SCALE /last scale " + (detector.getScaleFactor()/lastScale) );
//            float width = templateValues[Matrix.MSCALE_X] * ((ImageView) ivTemplate).getDrawable().getIntrinsicWidth();
//            float height = templateValues[Matrix.MSCALE_Y] * ((ImageView) ivTemplate).getDrawable().getIntrinsicHeight();

//            Log.i(TAG, "POST SCALE lastScale " + lastScale + "   scaleFactor " + detector.getScaleFactor() + " mult " + (detector.getScaleFactor() * lastScale));
            ivTemplate.setImageMatrix(templateMatrix);
            lastScale = detector.getScaleFactor();

            return super.onScale(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            lastScale = detector.getScaleFactor() * lastScale;
            lastFocusX = detector.getFocusX();
            lastFocusY = detector.getFocusY();
            // templateMatrix = currentSessionTransformationMatrix;
//            templateMatrix.postTranslate(lastFocusX - (ivTemplate.getWidth() / 2), lastFocusY - (ivTemplate.getHeight() / 2));
//            templateMatrix.postScale(lastScale, lastScale);
            ivTemplate.setImageMatrix(templateMatrix);
            aggregatedScale *= detector.getScaleFactor();

            Log.i(DISPLAY_TAG, "last scale " + detector.getScaleFactor());
            Log.i(DISPLAY_TAG, "aggregatedScale " + aggregatedScale);
//            Log.i(TAG, "end lastScale " + lastScale + " x : " + lastFocusX + " y : " + lastFocusY + " sf: " + detector.getScaleFactor());
        }
    }

    RotationGestureDetector.OnRotationGestureListener onRotationGestureListener = new RotationGestureDetector.OnRotationGestureListener() {
        @Override
        public void OnRotation(RotationGestureDetector detector) {
            float angle = detector.getAngle();
//            Log.i(TAG, "Angle is " + Float.toString(angle));
//            Matrix templateMatrix = new Matrix();
//            float[] values = new float[9];
//            templateMatrix.getValues(values);
//            float focusX = detector.getFocusY();
//            float focusY = detector.getFocusY();
//            transformationMatrix.postTranslate(focusX-(ivTemplate.getWidth()/2), focusY-(ivTemplate.getHeight()/2));
            templateMatrix.postRotate(-angle, (ivTemplate.getWidth() / 2), (ivTemplate.getHeight() / 2));
            ivTemplate.setImageMatrix(templateMatrix);
        }
    };

    //private class RotateListener extends RotateListene

    // calculate space between first two fingers
    public float calculateSpacing(MotionEvent event){
        // distance of two fingers : 0 and 1 are indices of two different fingers
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    // calculate midpoint of the first two fingers
    public void calculateMidPoint(PointF point, MotionEvent event){
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x/2, y/2);
    }

    // calculate the degrees rotated
    public float calculateRotation(MotionEvent event){
        double delta_x = event.getX(0) - event.getX(1);
        double delta_y = event.getY(0) - event.getY(1);
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch(menuId){
            case R.id.crop_done:
                    cropPhoto();
                    saveAndFinish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


}
