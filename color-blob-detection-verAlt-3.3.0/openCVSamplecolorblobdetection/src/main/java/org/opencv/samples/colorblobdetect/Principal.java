package org.opencv.samples.colorblobdetect;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Principal extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Button btnCamara = (Button)findViewById(R.id.boton_camara);
        btnCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(Principal.this, ColorBlobDetectionActivity.class);
                Principal.this.startActivity(intent);
            }
        });
    }
}
