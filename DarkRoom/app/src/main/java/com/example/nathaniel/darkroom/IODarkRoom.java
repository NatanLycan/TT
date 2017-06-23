package com.example.nathaniel.darkroom;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;

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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();
}
