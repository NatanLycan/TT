package org.opencv.samples.colorblobdetect;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.opencv.core.Mat;

public class P extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p);
        Intent intent = getIntent();

        String cadena = intent.getStringExtra(ColorBlobDetectionActivity.EXTRA_FLOTANTE);
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(cadena);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_P);
        layout.addView(textView);

    }
}
