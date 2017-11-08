package org.opencv.samples.colorblobdetect;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;

public class P extends Activity {

    private static final String  TAG = "P::Activity";

    private static int RESULT_LOAD_IMAGE = 1;
    private Button botonAbrir;
    private Button botonConfirmar;

    // PP NLJS 13/08/2017 Creo variables necesarias para cargar la imagen
    private ImageView imageView;
    private ImageView imV;
    private Bitmap loadedImage;

    // PP NLJS 13/08/2017 Creo variables que recibirá de la actividad que lo mando a llamar
    private String               pp_imgAdd;

    private Scalar               CONTOUR_COLOR;
    private Mat                  mRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Scalar               mBlobColorRgba;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p);

        /*
        mBlobColorRgba = new Scalar(255);
        mDetector = new ColorBlobDetector();

        mBlobColorHsv = (Scalar)getIntent().getExtras().getSerializable("PP_EXTRA_SCALAR");
        mBlobColorRgba = (Scalar)getIntent().getExtras().getSerializable("PP_EXTRA_SCALAR2");
        mDetector = (ColorBlobDetector) getIntent().getExtras().getSerializable("PP_EXTRA_COLORBLOBDETECTOR");
        mSpectrum = (Mat) getIntent().getExtras().getSerializable("PP_EXTRA_MAT2");
        SPECTRUM_SIZE = (Size) getIntent().getExtras().getSerializable("PP_EXTRA_SIZE");
        CONTOUR_COLOR = (Scalar) getIntent().getExtras().getSerializable("PP_EXTRA_SCALAR3");

        */

        // PP NLJS 15/07/2017 Obtengo la información del intend que lo mando a llamar
        Intent intent = getIntent();
        pp_imgAdd = intent.getStringExtra("PP_EXTRA_STRING");

        imageView = (ImageView) findViewById(R.id.test_image);
        botonAbrir = (Button) findViewById(R.id.botonAbrirImagen);
        botonConfirmar = (Button) findViewById(R.id.botonConfirmar);

        botonConfirmar.setEnabled(false);

        // PP NLJS 13/08/2017 Abro la imagen en el ImageView
        chargeFile();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            ImageView imageView = (ImageView) findViewById(R.id.test_image);
            Button botonConfirmar = (Button) findViewById(R.id.botonConfirmar);
            Bitmap imagen = getImage(data);
            if ( imagen != null ){
                imageView.setImageBitmap(imagen);
                botonConfirmar.setEnabled(true);
                //postproceso();
            }else{
                Log.i("Hola", "La imagen no existe");
            }

        }
    }

    public void actionBotonConfirmar(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(P.this);
        alert.setTitle("Aceptar");
        alert.setMessage("Quieres usar esta imagen?");
        alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //ir al resultado
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Regresar a seleccionar imagen
            }
        });
        alert.show();
    }

    public void actionBotonAbrirImagen(View view){
        Intent loadIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(loadIntent, RESULT_LOAD_IMAGE);
    }

    public Bitmap getImage(Intent data){
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        File imgFile = new  File(picturePath);
        if(imgFile.exists()){
            //Convertir a bitmap desde direccion de la imagen
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            mRgba = new Mat(myBitmap.getHeight(), myBitmap.getWidth(), CvType.CV_8UC1);
            //postproceso();
            //Rotar Imagen
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap imagenRotada = Bitmap.createBitmap(myBitmap , 0, 0, myBitmap .getWidth(), myBitmap.getHeight(), matrix, true);
            return imagenRotada;
        }
        return null;

    }

    void chargeFile() {
        /**
         * PP NLJS 13/08/2017
         * Carga la imagen original en em imageView principal
         *
         */
        try {
            Log.d(TAG, "chargeFile: entrada: "+pp_imgAdd);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap pp_bitmap = BitmapFactory.decodeFile(pp_imgAdd, options);
            //Rotar Imagen
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap imagenRotada = Bitmap.createBitmap(pp_bitmap , 0, 0, pp_bitmap .getWidth(), pp_bitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(imagenRotada);

        }catch (Exception e){
            // PP NLJS 13/08/2017 Mando mensaje de error en caso de que la img no se pudiera cargar en el ImageView
            Log.d(TAG, "PP Función: (Activity: P) chargeImage. No se pudo cargar la imagen: "+ e.getMessage());
        }
    }

}
