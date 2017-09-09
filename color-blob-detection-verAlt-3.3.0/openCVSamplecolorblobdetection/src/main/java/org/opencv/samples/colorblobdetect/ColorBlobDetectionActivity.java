package org.opencv.samples.colorblobdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
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
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;

import static java.lang.System.exit;

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Mat                  pp_mRgba_original;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    private  String              pp_imgAdd = "Dirección por defecto";
    private  String              pp_imgAdd2 = "Dirección por defecto 2";

    private boolean nath = false;

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
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
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
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
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
        CONTOUR_COLOR = new Scalar(255,0,0,255);
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
        if(pp_num==1){CONTOUR_COLOR = new Scalar(0,255,0,255);pp_num++;nath=true;}
        if(pp_num==0){pp_num++; pp_mRgba_original=mRgba.clone(); nath=true;}


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
        else if (mIsColorSelected) {
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

            Imgproc.drawContours(mRgba, contours, 0, CONTOUR_COLOR);//tercer parametro solo imprime el primer contorno
            //este es el mas proximo

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            /*Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
*/

        } else {
            mRgba = inputFrame.rgba();
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

        // PP NLJS 22/07/2017 Creo intent para una nueva Actividad
        Intent intent =new Intent(this,P.class);

        // PP NLJS 22/07/2017 Envio los siguientes datos como EXTRAS
        // private Mat                  mRgba;
        // private Scalar               mBlobColorHsv;
        // private Scalar               mBlobColorRgba;
        // private ColorBlobDetector    mDetector;
        // private Mat                  mSpectrum;
        // private Size                 SPECTRUM_SIZE;
        // private Scalar               CONTOUR_COLOR;
        // private String               pp_imgAdd;

        intent.putExtra("PP_EXTRA_MAT",mRgba);
        Log.d(TAG, "chargeFile2: width: "+mRgba.cols());
        Log.d(TAG, "chargeFile2: rowa: "+mRgba.rows());
        intent.putExtra("PP_EXTRA_SCALAR",mBlobColorHsv);
        intent.putExtra("PP_EXTRA_SCALAR2",mBlobColorRgba);
        intent.putExtra("PP_EXTRA_COLORBLOBDETECTOR",mDetector);
        intent.putExtra("PP_EXTRA_MAT2",mSpectrum);
        intent.putExtra("PP_EXTRA_SIZE",SPECTRUM_SIZE);
        intent.putExtra("PP_EXTRA_SCALAR3",CONTOUR_COLOR);
        intent.putExtra("PP_EXTRA_STRING",pp_imgAdd);

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

        //PP NCH 23/07/2017 Creo bitmap temporal para poder guardar Mat mRgba en un archivo
        Bitmap pp_bmp = null;
        Bitmap pp_bmp_ori = null;


        //PP NLJS 06/08/2017 Intento inicializar el Bitmap
        try {
            //PP NLJS 06/08/2017 Establezco el tamaño del Bitmap
            pp_bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            pp_bmp_ori = Bitmap.createBitmap(pp_mRgba_original.cols(), pp_mRgba_original.rows(), Bitmap.Config.ARGB_8888);
            //PP NLJS 06/08/2017 Realizo la conversión
            Utils.matToBitmap(mRgba, pp_bmp);
            Utils.matToBitmap(pp_mRgba_original, pp_bmp_ori);

        } catch (CvException e) {
            Log.d(TAG, e.getMessage());
            Log.d(TAG, "PP Función: SaveImage. No fué posible transformar mRgba a Bitmap.");
            exit(0);
        }
        //PP NLJS 06/08/2017 Libero el espacio ocupado por mRgba
        //mRgba.release();

        //PP NLJS 06/08/2017 Reviso si el almacenamiento externo esta disponible para guardar la imagen
        boolean pp_flag = isExternalStorageWritable();
        Log.d(TAG, "PP Función: SaveImage. Validación de almacenamiento externo: " + pp_flag);

        //PP NLJS 06/08/2017 Creo el File donde guardar la imagen y el directorio
        File pp_img = getAlbumStorageDir("Proportion");

        //PP NLJS 06/08/2017 Válido que pp_img no sea nulo
        if(pp_img.exists()){
            //PP NLJS 13/08/2017 Cuento cuantas fotos hay en la carpeta de Proportion para asignarle un número a esta nueva img
            String [] pp_Files = pp_img.list();
            int pp_totFiles = pp_Files.length;
            pp_totFiles=pp_totFiles/2;
            String filename = "Proportion_" + pp_totFiles + ".png";
            String filename2 = "Proportion_" + pp_totFiles + "_ori.png";
            boolean pp_flag_comp = false;
            boolean pp_flag_comp2 = false;

            //PP NLJS 06/08/2017 Creo el FOS y File destino para guardar la img en la memoria del teléfono
            FileOutputStream pp_out = null;
            FileOutputStream pp_out2 = null;
            File pp_img2 = new File(pp_img,filename);
            File pp_img3 = new File(pp_img,filename2);

            //PP NLJS 06/08/2017 Intento inicializar el FOS
            try {
                pp_out = new FileOutputStream(pp_img2);
                pp_out2 = new FileOutputStream(pp_img3);
                pp_flag_comp = pp_bmp.compress(Bitmap.CompressFormat.PNG, 100, pp_out); // pp_bmp is your Bitmap instance
                pp_flag_comp2 = pp_bmp_ori.compress(Bitmap.CompressFormat.PNG, 100, pp_out2); // pp_bmp is your Bitmap instance
                pp_out.close();
                pp_out.close();

                // PP NLJS 13/08/2017 Guardo la dirección de la foto que se acaba de guardar
                pp_imgAdd = pp_img2.getAbsolutePath();
                pp_imgAdd2 = pp_img3.getAbsolutePath();
                Log.d(TAG, "PP Función: SaveImage. La imagen se guardó exitosamente. pp_imgAdd : " + pp_imgAdd);

                if (mOpenCvCameraView != null) {
                    Mat lol= mRgba.clone();
                    mOpenCvCameraView.disableView();
                    mRgba=lol.clone();
                    Act(mOpenCvCameraView);

                }else {
                    Act(mOpenCvCameraView);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "PP Función: SaveImage. Error al crear el FOS. Compress : " + pp_flag_comp);
            }
        }
    }
    public boolean isExternalStorageWritable() {
        /**
         * PP NLJS 06/08/2017
         * Revisa si el almacenamiento externo esta disponible para leer y escribir
         *
         */

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    public File getAlbumStorageDir(String albumName) {
        /**
         * PP NLJS 06/08/2017
         * Crea el directorio donde se van a guardar la fotos (Este será público para otras aplicaciones)
         *
         */

        // PP NLJS 06/08/2017 Creo el File que se va a utilizar y la ruta de almacenamiento
        // PP NLJS 06/08/2017 DIRECTORI_PICTURE Es un parametro necesario para indicar que el archivo a guardar será una imagen y la ruta se genere bien
        File pp_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);

        // PP NLJS 06/08/2017 Creo el directorio y mando un msg de error en caso de que ya exista o no se haya podido crear
        if (!pp_file.mkdirs()) {
            Log.e(TAG, "PP Función: getAlbumStorageDir. El directorio ya existía o no se pudo crear. " + pp_file.getAbsolutePath());
        }
        return pp_file;
    }
}
