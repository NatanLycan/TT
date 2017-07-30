package org.opencv.samples.colorblobdetect;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class P extends Activity {
    private static final String  TAG = "P::Activity";

    private static int RESULT_LOAD_IMAGE = 1;
    Button botonAbrir;
    Button botonConfirmar;
    ImageView imageView;
    Mat pp_mat1;
    private Scalar CONTOUR_COLOR;
    private ColorBlobDetector    mDetector;
    private Scalar               mBlobColorRgba;
    private Scalar               pp_mBlobColorHsv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p);

        // PP NLJS 15/07/2017 Obtengo la información del intend que lo mando a llamar
        Intent intent = getIntent();

        mBlobColorRgba = new Scalar(255);
        mDetector = new ColorBlobDetector();


        // PP NLJS 15/07/2017 Obtengo la información del intend que lo mando a llamar
        pp_mBlobColorHsv = (Scalar)getIntent().getExtras().getSerializable("PP_EXTRA_SCALAR");
        Log.d(TAG, "Extra HSV0: " + pp_mBlobColorHsv.val[0]);
        Log.d(TAG, "Extra HSV1: " + pp_mBlobColorHsv.val[1]);
        Log.d(TAG, "Extra HSV2: " + pp_mBlobColorHsv.val[2]);


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
            pp_mat1 = new Mat (myBitmap.getHeight(), myBitmap.getWidth(), CvType.CV_8UC1);
            postproceso();
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

        mDetector.setHsvColor(pp_mBlobColorHsv);
        Log.d(TAG, "postproceso: Still working 1");
        mDetector.process(pp_mat1);
        Log.d(TAG, "postproceso: Still working 2");
        List<MatOfPoint> contours = mDetector.getContours();
        Log.e(TAG, "Contours count: " + contours.size());

        Imgproc.drawContours(pp_mat1, contours, -1, CONTOUR_COLOR);

        Mat colorLabel = pp_mat1.submat(4, 68, 4, 68);
        colorLabel.setTo(mBlobColorRgba);


        Bitmap img = Bitmap.createBitmap(pp_mat1.cols(), pp_mat1.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(pp_mat1, img);
        ImageView vf = (ImageView) findViewById(R.id.view_final);
        if( vf != null )
            vf.setImageBitmap(img);

        /*
        mBlobColorHsv= converScalarRgba2Hsv(mBlobColorRgba);

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        Mat spectrumLabel = pp_mat1.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
        mSpectrum.copyTo(spectrumLabel);*/


    }




}
