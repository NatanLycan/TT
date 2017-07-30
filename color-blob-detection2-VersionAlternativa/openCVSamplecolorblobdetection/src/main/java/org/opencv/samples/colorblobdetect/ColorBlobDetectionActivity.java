package org.opencv.samples.colorblobdetect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;

import static java.lang.System.exit;

/**
 * Compilado utilizando Android Studio Canary 3.0.0-alpha7
 */

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {

    // PP NLJS 16/07/2017 Creo EXTRA para enviar el rgb de la foto del objeto principal
    /*public final static String EXTRA_RED = "rojo";
    public final static String EXTRA_GREEN = "verde";
    public final static String EXTRA_BLUE = "azul";
    */
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(0,0,255,255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {

        /**
         * PP NCH 30/07/2017
         * Se obtendran tres eventos
         * 1- EL primer touch debera ser el del Objeto a medir y cambiara el color de contorno a verde
         * 2- El segundo toque sera al Objeto de referencia
         * 3- El tercer toque continuara a la siguiente etapa
         */
        if(pp_num==2){SaveImage();}
        if(pp_num==1){CONTOUR_COLOR = new Scalar(0,255,0,255);pp_num++;}
        if(pp_num==0){pp_num++; }


        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

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

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        // PP NCH 16/07/2017 llamada a la funcion cuando usuario da click en algun punto del mOpenCVcameraView
        //en este punto ya se obtuvieron los colores RGBA correspondientes, pero no se genero la imagen con contornos
        //Mat temp= mRgba;
        //SaveImage();
        //mRgba=temp;



        return false; // don't need subsequent touch events
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        /**
         * PP NCH  23/07/2017
         * La funcion se modifico para que al haber seleccionado el color con onTouch()
         * no actualize el frame que se muestra en el OpenCvCameraView
         * Despues de seleccionar el color la imagen se mantiene igual, posteriormente se imprimen los contornos
         * seleccionados  y como no cambia la imagen no se agregan mas contornos
         */
        if(pp_num==2){}
        else {
            if (mIsColorSelected) {
                Log.d(TAG, "onCameraFrame(miscolorselected): Done 1");
                if (mRgba.empty()) {
                    Log.d(TAG, "onCameraFrame: Crash");
                    exit(0);
                }
                /**
                 * TODO Checa esto richi, hay que revisar por que la variable mRgba se vacia
                 * DONE
                 * NLJS evento onCameraViewStoppped limpia la variable mRgba por eso al llegar a estoya no tenia nada
                 */
                mDetector.process(mRgba);
                List<MatOfPoint> contours = mDetector.getContours();
                Log.e(TAG, "Contours count: " + contours.size());

                Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

                Mat colorLabel = mRgba.submat(4, 68, 4, 68);
                colorLabel.setTo(mBlobColorRgba);

                Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
                mSpectrum.copyTo(spectrumLabel);
                //SaveImage();

            } else {
                mRgba = inputFrame.rgba();
            }
        }
        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }


    public void Act(View view) {

        /**
         * PP NCH 23/07/2017
         * Genera un intend para abrir la actividad P
         */

        Intent intent =new Intent(this,P.class);

        // PP NLJS 22/07/2017 Envio extra Scalar con los valores hsv
        intent.putExtra("PP_EXTRA_SCALAR",mBlobColorHsv);

        // PP NLJS 16/07/2017 Inicializo la actividad
        startActivity(intent);
    }

    int pp_num=0;//PP NCH 23/07/2017 Identifica contador para Imagen Objeto de referencia y objeto a medir

    public void SaveImage(){
        /**
         * PP NCH 23/07/2017
         * Convierte el objeto Mat mRgba en un bitmap para asi poder guardarlo en un archivo PNG
         * en el directorio /proportion
         * y un numero que identifica que imagen es
         *
         *  1 - Imagen Original
         *  2 - Imagen Objeto a medir
         *  3 - Imagen Objeto de referencia
         *
         *  PP NCH 30/07/2017
         *  Solo guarda la imagen final con la seleccion de los dos objetos  si requiere repetir se hara el de los tres procesos
         */
        Bitmap pp_bmp = null;//pp NCH 23/07/2017 Temporal para poder guardar Mat mRgba en un archivo
        pp_num++;
        try {
            pp_bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRgba, pp_bmp);
        } catch (CvException e) {
            Log.d(TAG, e.getMessage());
        }

        mRgba.release();


        FileOutputStream out = null;

        String filename = "Blob_intento_"+pp_num+".png";


        File sd = new File(Environment.getExternalStorageDirectory() + "/proportion");
        boolean success = true;
        if (!sd.exists()) {
            success = sd.mkdir();
        }
        if (success) {
            File dest = new File(sd, filename);

            try {
                out = new FileOutputStream(dest);
                pp_bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // pp_bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        Log.d(TAG, "OK!!");

                        // PP NCH 00/00/0000 Comentario

                            //Act(mOpenCvCameraView);
                        if (mOpenCvCameraView != null) {
                            mOpenCvCameraView.disableView();
                            Act(mOpenCvCameraView);
                        }else Act(mOpenCvCameraView);


                    }
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage() + "Error");
                    e.printStackTrace();
                }
            }
        }

    }

}
