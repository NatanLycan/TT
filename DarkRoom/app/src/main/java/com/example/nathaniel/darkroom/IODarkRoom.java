package com.example.nathaniel.darkroom;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class IODarkRoom extends Activity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static final String TAG = "MainActivity";

    static{
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        }else{
            Log.d(TAG, "OpenCV loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iodark_room);

        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.iodark_room, menu);
        return true;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();
    //--->
    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_openGallary) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),
                    SELECT_PICTURE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //--->
    Mat sampledImage=new Mat();
    //--->
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                Log.i(TAG, "selectedImagePath: " + selectedImagePath);
                loadImage(selectedImagePath);
                displayImage(sampledImage);
            }
        }
    }
    //--->
    private String getPath(Uri uri) {
    // just some safety built in
        if(uri == null ) {
            return null;
        }
    // try to retrieve the image from the media store first
    // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null,
                null);
        if(cursor != null ){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }
    //--->
    private void loadImage(String path)
    {
        Mat originalImage = Imgcodecs.imread(path);
        Mat rgbImage=new Mat();
        Imgproc.cvtColor(originalImage, rgbImage, Imgproc.COLOR_BGR2RGB);
        //rgbImage= originalImage;
        Display display = getWindowManager().getDefaultDisplay();
//This is "android graphics Point" class
        Point size = new Point();
        display.getSize(size);
        int width = (int) size.x;
        int height = (int) size.y;

        double downSampleRatio= calculateSubSampleSize(rgbImage,width,height);
        Imgproc.resize(rgbImage, sampledImage, new Size(),downSampleRatio,downSampleRatio,Imgproc.INTER_AREA);
        try {
            ExifInterface exif = new ExifInterface(selectedImagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    1);
            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
    //get the mirrored image
                    sampledImage=sampledImage.t();
    //flip on the y-axis
                    Core.flip(sampledImage, sampledImage, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
    //get up side down image
                    sampledImage=sampledImage.t();
    //Flip on the x-axis
                    Core.flip(sampledImage, sampledImage, 0);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //--->
    private static double calculateSubSampleSize(Mat srcImage, int reqWidth,
                                                 int reqHeight) {
    // Raw height and width of image
        final int height = srcImage.height();
        final int width = srcImage.width();
        double inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
    // Calculate ratios of requested height and width to the raw
    //height and width
            final double heightRatio = (double) reqHeight / (double) height;
            final double widthRatio = (double) reqWidth / (double) width;
    // Choose the smallest ratio as inSampleSize value, this will
    //guarantee final image with both dimensions larger than or
    //equal to the requested height and width.
            inSampleSize = heightRatio<widthRatio ? heightRatio :widthRatio;
        }
        return inSampleSize;
    }
    //--->
    private void displayImage(Mat image)
    {
    // create a bitMap
        Bitmap bitMap = Bitmap.createBitmap(image.cols(),
                image.rows(),Bitmap.Config.RGB_565);
    // convert to bitmap:
        Utils.matToBitmap(image, bitMap);
    // find the imageview and draw it!
        ImageView iv = (ImageView) findViewById(R.id.IODarkRoomImageView);
        iv.setImageBitmap(bitMap);
    }
}
