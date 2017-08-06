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
    Button botonAbrir;
    Button botonConfirmar;
    ImageView imageView;
    private Scalar CONTOUR_COLOR;
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
        // private Scalar               CONTOUR_COLOR;*/
        mRgba = (Mat)getIntent().getExtras().getSerializable("PP_EXTRA_MAT");
        mBlobColorHsv = (Scalar)getIntent().getExtras().getSerializable("PP_EXTRA_SCALAR");
        mBlobColorRgba = (Scalar)getIntent().getExtras().getSerializable("PP_EXTRA_SCALAR2");
        mDetector = (ColorBlobDetector) getIntent().getExtras().getSerializable("PP_EXTRA_COLORBLOBDETECTOR");
        mSpectrum = (Mat) getIntent().getExtras().getSerializable("PP_EXTRA_MAT2");
        SPECTRUM_SIZE = (Size) getIntent().getExtras().getSerializable("PP_EXTRA_SIZE");
        CONTOUR_COLOR = (Scalar) getIntent().getExtras().getSerializable("PP_EXTRA_SCALAR3");

//        if(mRgba!=null)Log.d(TAG, "P: mRgba no es nulo");
//        if(mBlobColorHsv!=null) Log.d(TAG, "P: mBlobColorHsv no es nulo");
//        if(mBlobColorRgba!=null) Log.d(TAG, "P: mBlobColorRgba no es nulo");
//        if(mDetector!=null) Log.d(TAG, "P: mDetector no es nulo");
//        if(mSpectrum!=null) Log.d(TAG, "P: mSpectrum no es nulo");
//        if(SPECTRUM_SIZE!=null) Log.d(TAG, "P: SPECTRUM_SIZE no es nulo");
//        if(CONTOUR_COLOR!=null) Log.d(TAG, "P: CONTOUR_COLOR no es nulo");


        imageView = (ImageView) findViewById(R.id.test_image);
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
       /* int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (imageView.getWidth() - cols) / 2;
        int yOffset = (imageView.getHeight() - rows) / 2;

        x-=xOffset;
        y-=yOffset;

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        Mat temp= mRgba;
        mRgba=temp;

        Bitmap img = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba, img);
        ImageView vf = (ImageView) findViewById(R.id.view_final);

        if( vf != null )
            vf.setImageBitmap(img);
*/
    }

}
