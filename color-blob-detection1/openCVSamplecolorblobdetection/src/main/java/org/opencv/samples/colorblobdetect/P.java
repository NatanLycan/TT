package org.opencv.samples.colorblobdetect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.File;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static java.lang.System.exit;

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

        // PP NLJS 15/07/2017 Obtengo la información del intent que lo mando a llamar
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
        imV = (ImageView) findViewById(R.id.view_final);
        Log.d(TAG, "chargeFile2: width1_1: "+mRgba.cols());
        // PP NLJS 13/08/2017 Abro la imagen en el ImageView
        chargeFile(pp_imgAdd);
        Log.d(TAG, "chargeFile2: width1_2: "+mRgba.cols());
        chargeFile2();
        Log.d(TAG, "chargeFile2: width1_3: "+mRgba.cols());

        // PP QBR 13/08/2017
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                int x = (int)event.getX();
                int y = (int)event.getY();
                int pixel = bitmap.getPixel(x,y);
                int redValue = Color.red(pixel);
                int blueValue = Color.blue(pixel);
                int greenValue = Color.green(pixel);

                postproceso( redValue, blueValue, greenValue );
                return true;
            }
        });

        /* PP NLJS 13/08/2017

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                 Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                int x = (int)event.getX();
                int y = (int)event.getY();
                int pixel = bitmap.getPixel(x,y);
                int redValue = Color.red(pixel);
                int blueValue = Color.blue(pixel);
                int greenValue = Color.green(pixel);

                postproceso( redValue, blueValue, greenValue );
                return true;
            }
        });*/
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
        Intent loadIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
            mRgba = new Mat (myBitmap.getHeight(), myBitmap.getWidth(), CvType.CV_8UC4);
            //Rotar Imagen
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap imagenRotada = Bitmap.createBitmap(myBitmap , 0, 0, myBitmap .getWidth(), myBitmap.getHeight(), matrix, true);
            return imagenRotada;
        }
        return null;

    }

    /**
     * Esta Funcion es de ColorBLOB
     * @param hsvColor
     * @return
     */
    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }


    /**
     * Esta funcion es similar a
     */
    public void postproceso(int R, int G, int B){
        /**
         * TODO: Hacer el postproceso y con los valores obtenidos hacer lo que hacia el activity dle color blob
         */
        try{
            Log.d(TAG, "postproceso: Error: width "+mRgba.cols());
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            if(mRgba.empty()) Log.d(TAG, "postproceso: mRgba esta vacio");
            if(contours.equals("")) Log.d(TAG, "postproceso: contours esta vacio");
        }catch (Exception e){
            Log.d(TAG, "postproceso: Error: "+e.getMessage());
        }
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
            Mat temp=mRgba.clone();
            Bitmap pp_bitmap = BitmapFactory.decodeFile(imgAdd, options);
            mRgba=temp.clone();
            Log.d(TAG, "chargeFile2: width4_3: "+mRgba.cols()+"    Height: "+mRgba.rows());
            imageView.setImageBitmap(pp_bitmap);


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
