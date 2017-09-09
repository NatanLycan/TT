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

        // PP NLJS 15/07/2017 Obtengo la información del intend que lo mando a llamar
        Intent intent = getIntent();

        mBlobColorRgba = new Scalar(255);
        mDetector = new ColorBlobDetector();

        // PP NLJS 15/07/2017 Obtengo los siguientes EXTRAS del intend que lo mando a llamar
        // private Mat                  mRgba;
        // private Scalar               mBlobColorHsv;
        // private Scalar               mBlobColorRgba;
        // private ColorBlobDetector    mDetector;
        // private Mat                  mSpectrum;
        // private Size                 SPECTRUM_SIZE;
        // private Scalar               CONTOUR_COLOR;
        // private String               pp_imgAdd;

        mRgba = (Mat)getIntent().getExtras().getSerializable("PP_EXTRA_MAT");
        Log.d(TAG, "chargeFile2: width1: "+mRgba.cols());
        mBlobColorHsv = (Scalar)getIntent().getExtras().getSerializable("PP_EXTRA_SCALAR");
        mBlobColorRgba = (Scalar)getIntent().getExtras().getSerializable("PP_EXTRA_SCALAR2");
        mDetector = (ColorBlobDetector) getIntent().getExtras().getSerializable("PP_EXTRA_COLORBLOBDETECTOR");
        mSpectrum = (Mat) getIntent().getExtras().getSerializable("PP_EXTRA_MAT2");
        SPECTRUM_SIZE = (Size) getIntent().getExtras().getSerializable("PP_EXTRA_SIZE");
        CONTOUR_COLOR = (Scalar) getIntent().getExtras().getSerializable("PP_EXTRA_SCALAR3");

        pp_imgAdd = intent.getStringExtra("PP_EXTRA_STRING");
        Log.d(TAG, "PP Función: (Activity: P) onCreate. pp_imgAdd : " + pp_imgAdd);
        // PP NLJS 13/08/2017
        imageView = (ImageView) findViewById(R.id.test_image);
        Log.d(TAG, "chargeFile2: width1_1: "+mRgba.cols());
        // PP NLJS 13/08/2017 Abro la imagen en el ImageView
        chargeFile(pp_imgAdd);
        Log.d(TAG, "chargeFile2: width1_2: "+mRgba.cols());
        //chargeFile2();
        Log.d(TAG, "chargeFile2: width1_3: "+mRgba.cols());

        // PP NLJS 15/07/2017 Obtengo los valores RGB
        /*mBlobColorRgba.val[0] = intent.getDoubleExtra(ColorBlobDetectionActivity.EXTRA_RED,0.0);
        mBlobColorRgba.val[1] = intent.getDoubleExtra(ColorBlobDetectionActivity.EXTRA_GREEN,0.0);
        mBlobColorRgba.val[2] = intent.getDoubleExtra(ColorBlobDetectionActivity.EXTRA_BLUE,0.0);
        */

        // PP NLJS 15/07/2017 Imprimo valores RGB, solo como prueba en consola
        /*Log.d(TAG, "Extra rojo: " + mBlobColorRgba.val[0]);
        Log.d(TAG, "Extra verde: " + mBlobColorRgba.val[1]);
        Log.d(TAG, "Extra azul: " + mBlobColorRgba.val[2]);


        mBlobColorRgba.val[3]= (double) 255;
        */
        

        imageView = (ImageView) findViewById(R.id.test_image);
        botonAbrir = (Button) findViewById(R.id.botonAbrirImagen);
        botonConfirmar = (Button) findViewById(R.id.botonConfirmar);

        botonConfirmar.setEnabled(false);

        CONTOUR_COLOR = new Scalar(255,0,0,255);

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

    public void postproceso(){

        /**
         * TODO: Arreglar el error que crashea la app
         */

        mDetector.setHsvColor(mBlobColorHsv);
        Log.d(TAG, "postproceso: Still working 1");

        mDetector.process(mRgba);//aqui mueres
        Log.d(TAG, "postproceso: Still working 2");
        List<MatOfPoint> contours = mDetector.getContours();
        Log.e(TAG, "Contours count: " + contours.size());

        Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

        Mat colorLabel = mRgba.submat(4, 68, 4, 68);
        colorLabel.setTo(mBlobColorRgba);


        Bitmap img = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(),Bitmap.Config.ARGB_8888);

        /*
        mBlobColorHsv= converScalarRgba2Hsv(mBlobColorRgba);

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
        mSpectrum.copyTo(spectrumLabel);*/


    }




    void chargeFile(String imgAdd) {
        /**
         * PP NLJS 13/08/2017
         * Carga la imagen original en em imageView principal
         *
         */
        Log.d(TAG, "chargeFile2: width4: "+mRgba.cols());
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Log.d(TAG, "chargeFile2: width4_1: "+mRgba.cols());
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Log.d(TAG, "chargeFile2: width4_2: "+mRgba.cols());
            //Mat temp=mRgba.clone();
            Log.d(TAG, "chargeFile2: width4_2: "+imgAdd);
            Bitmap pp_bitmap = BitmapFactory.decodeFile(imgAdd, options);
            //mRgba=temp.clone();
            Log.d(TAG, "chargeFile2: width4_3: "+mRgba.cols()+"    Height: "+mRgba.rows());


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

    //PP NCH 13/08/2017
    void chargeFile2(){
        Log.d(TAG, "chargeFile2: width2: "+mRgba.cols());
        try{
            Log.d(TAG, "chargeFile2: width3: "+mRgba.cols());
            Bitmap pp_bitmap2 = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRgba,pp_bitmap2);
            imV.setImageBitmap(pp_bitmap2);
        }catch (Exception e){
            Log.d(TAG, "chargeFile2: Error: "+e.getMessage());
        }


    }


}
