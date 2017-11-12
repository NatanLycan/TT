package org.opencv.samples.colorblobdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Button;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.lang.System.exit;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.LINE_8;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private boolean mIsColorSelected = false;
    private Mat mRgba;
    private Mat pp_mRgba_original;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private ColorBlobDetector mDetector;
    private Mat mSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar CONTOUR_COLOR;
    private String pp_imgAdd = "Dirección por defecto";
    private String pp_imgAdd2 = "Dirección por defecto 2";
    private String pp_imgAdd3 = "Dirección por defecto 2";
    private Button btntick;
    private Button btnadd;
    private Button btncross;
    private Mat mRgba_Buttons_backup;
    private boolean pp_espera = false;
    private boolean savim = false;
    private String MEDIDA = "";
    private boolean nath = false;
    ArrayList<List<MatOfPoint>> ListaContornosRojos = new ArrayList<List<MatOfPoint>>();
    ArrayList<List<MatOfPoint>> ListaContornosVerdes = new ArrayList<List<MatOfPoint>>();
    private Mat dibujada;
    // PP NLJS 21/10/2017 Creo objeto Mat para marcar los vértices y hacer corrección de angulos;
    private Mat vertices;
    private double x_r_Pix = 0, y_r_Pix = 0;//x & y en pixeles referencia (Tarjeta)
    private double x_g_Pix = 0, y_g_Pix = 0;//x & y en pixeles O. a medir
    private double x_r_cm = 8.6, y_r_cm = 5.4;//x & y en centimetros (Tarjeta)
    private double x_g_um = 0, y_g_um = 0;//dependiendo de la unidad seleccionada se transformara el resulatado medido
    // PP NLJS 11/11/2017 Arreglo para hacer búsquedas en la matriz
    ArrayList <ArrayList<double[]>> find;



    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public void mensaje(String m) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(m)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setMaxFrameSize(1920, 1080);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        btntick = (Button) findViewById(R.id.boton_tick);
        btncross = (Button) findViewById(R.id.boton_cross);
        btnadd = (Button) findViewById(R.id.boton_add);
        MEDIDA = getIntent().getStringExtra("MEDIDA");
        mensaje("Has selecionado la siguiente unidad de medida : " + MEDIDA.toLowerCase());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
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
        CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
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
        if (pp_espera) {
            return false;
        }
        if (pp_num == 1) {
            CONTOUR_COLOR = new Scalar(0, 255, 0, 255);
            pp_num++;
            nath = true;
            btntick.setVisibility(View.VISIBLE);
            btncross.setVisibility(View.VISIBLE);
            btnadd.setVisibility(View.VISIBLE);
            btntick.bringToFront();
            btncross.bringToFront();
            btnadd.bringToFront();
            pp_espera = true;
        }
        if (pp_num == 0) {
            if (savim == false) {
                pp_mRgba_original = mRgba.clone();
                savim = true;
            }
            pp_num++;
            pp_mRgba_original = mRgba.clone();
            nath = true;
            btntick.setVisibility(View.VISIBLE);
            btncross.setVisibility(View.VISIBLE);
            btnadd.setVisibility(View.VISIBLE);
            btntick.bringToFront();
            btncross.bringToFront();
            btnadd.bringToFront();
            pp_espera = true;
            //mOpenCvCameraView.setClickable(false);
            //mOpenCvCameraView.disableView();

        }


        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int) event.getX() - xOffset;
        int y = (int) event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;
        Rect touchedRect = new Rect();

        touchedRect.x = (x > 4) ? x - 4 : 0;
        touchedRect.y = (y > 4) ? y - 4 : 0;

        touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y + 4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;
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

        /**
         /* NCH 17/09/2017 Este codigo estaba en la funcion onCameraFrame en el oomentario codigo reirado
         *  Optimiza la actualizacion de frames no afecta a calculo del contornos
         */

        Point p = new Point(x, y);
        mDetector.process(mRgba);
        //mDetector.process2(mRgba,p);
        List<MatOfPoint> contours = mDetector.getContours();
        Log.e(TAG, "Contours count: " + contours.size());

        //NCH 23/09/2017 Busco contorno con centro mas cerca al click
        List<MatOfPoint> thecontour = TheContour(contours, p);
       /* if (pp_num == 1) {
            ListaContornosRojos.add(thecontour);
        } else {
            ListaContornosVerdes.add(thecontour);
        }*/


        //NCH 10/10/2017 Guardo la imagen inicial para por si elige no mantener la imagen

        mRgba_Buttons_backup = mRgba.clone();

        //Imgproc.drawContours(mRgba, contours, 0, CONTOUR_COLOR);//tercer parametro solo imprime el primer contorno
        Imgproc.drawContours(mRgba, thecontour, 0, CONTOUR_COLOR);//tercer parametro solo imprime el primer contorno

        //este es el mas proximo

        if (pp_num == 1) {
            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);
        }
        /**
         * NCH 24/09/2017
         * Se gaurdan los recuadros donde se muestra el color que se selecciono en cada pulsacion
         */
        if (pp_num == 2) {
            Mat colorLabel2 = mRgba.submat(4, 68, 72, 136);
            colorLabel2.setTo(mBlobColorRgba);

        }


        return false; // don't need subsequent touch events
    }

    /**
     * NCH 23/09/2017
     * Busca el contorno cuyo centro este mas cerca del lugar donde sucecio el click
     *
     * @param Contours
     * @param p
     * @return
     */
    public List<MatOfPoint> TheContour(List<MatOfPoint> Contours, Point p) {
        double xdis = 9999999;
        double ydis = 9999999;
        int theone = 0;
        double disMax = 9999999;
        Log.d(TAG, "TheContour: punto  x:" + p.x + "    y:" + p.y);
        List<Moments> mu = new ArrayList<Moments>(Contours.size());
        for (int i = 0; i < Contours.size(); i++) {
            mu.add(i, Imgproc.moments(Contours.get(i), false));
            Moments pu = mu.get(i);
            double x = (pu.get_m10() / pu.get_m00());
            double y = (pu.get_m01() / pu.get_m00());
            //sCore.circle(rgbaImage, new Point(x, y), 4, new Scalar(255,49,0,255));
            if (disMax > distancia(p.x, p.y, x, y)) {
                disMax = distancia(p.x, p.y, x, y);
                theone = i;
                Log.d(TAG, "TheContour: centro x:" + x + "     y:" + y);
            }
        }
        List<MatOfPoint> m = new ArrayList<MatOfPoint>();
        m.add(Contours.get(theone));
        return m;
    }

    public double distancia(double x1, double y1, double x2, double y2) {
        return sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        /**
         * PP NCH  23/07/2017
         * La funcion se modifico para que al haber seleccionado el color con onTouch()
         * no actualize el frame que se muestra en el OpenCvCameraView
         * Despues de seleccionar el color la imagen se mantiene igual, posteriormente se imprimen los contornos
         * seleccionados  y como no cambia la imagen no se agregan mas contornos
         */
        if (pp_num == 2) {
        } else if (mIsColorSelected) {
            Log.d(TAG, "onCameraFrame(miscolorselected): Done 1");
            if (mRgba.empty()) {
                Log.d(TAG, "onCameraFrame: Crash");
                exit(0);
            }
            /**
             * Checa esto richi, hay que revisar por que la variable mRgba se vacia
             * DONE
             * NLJS evento onCameraViewStoppped limpia la variable mRgba por eso al llegar a estoya no tenia nada
             */

            // codigo retirado


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
        cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    public void Act(View view) {

        /**
         * PP NCH 23/07/2017
         * Genera un intend para abrir la actividad P
         */

        // PP NLJS 22/07/2017 Creo intent para una nueva Actividad
        Intent intent = new Intent(this, P.class);

        // PP NLJS 22/07/2017 Envio los siguientes datos como EXTRAS
        // private Mat                  mRgba;
        // private Scalar               mBlobColorHsv;
        // private Scalar               mBlobColorRgba;
        // private ColorBlobDetector    mDetector;
        // private Mat                  mSpectrum;
        // private Size                 SPECTRUM_SIZE;
        // private Scalar               CONTOUR_COLOR;
        // private String               pp_imgAdd;

        intent.putExtra("PP_EXTRA_MAT", mRgba);
        Log.d(TAG, "chargeFile2: width: " + mRgba.cols());
        Log.d(TAG, "chargeFile2: rowa: " + mRgba.rows());
        intent.putExtra("PP_EXTRA_SCALAR", mBlobColorHsv);
        intent.putExtra("PP_EXTRA_SCALAR2", mBlobColorRgba);
        intent.putExtra("PP_EXTRA_COLORBLOBDETECTOR", mDetector);
        intent.putExtra("PP_EXTRA_MAT2", mSpectrum);
        intent.putExtra("PP_EXTRA_SIZE", SPECTRUM_SIZE);
        intent.putExtra("PP_EXTRA_SCALAR3", CONTOUR_COLOR);
        // PP NLJS 22/10/2017 Mando pp_imgAdd3 para desplegar la img con los resultados gráficos
        intent.putExtra("PP_EXTRA_STRING", pp_imgAdd3);

        // PP NLJS 16/07/2017 Inicializo la actividad
        startActivity(intent);
    }

    int pp_num = 0;//PP NCH 23/07/2017 Identifica contador para Imagen Objeto de referencia y objeto a medir


    public void SaveImage() {
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
        Bitmap pp_bmp_draw = null;


        //PP NLJS 06/08/2017 Intento inicializar el Bitmap
        try {
            //PP NLJS 06/08/2017 Establezco el tamaño del Bitmap
            pp_bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            pp_bmp_ori = Bitmap.createBitmap(pp_mRgba_original.cols(), pp_mRgba_original.rows(), Bitmap.Config.ARGB_8888);
            pp_bmp_draw = Bitmap.createBitmap(dibujada.cols(), dibujada.rows(), Bitmap.Config.ARGB_8888);

            //PP NLJS 06/08/2017 Realizo la conversión
            Utils.matToBitmap(mRgba, pp_bmp);
            Utils.matToBitmap(pp_mRgba_original, pp_bmp_ori);
            Utils.matToBitmap(dibujada, pp_bmp_draw);

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
        if (pp_img.exists()) {
            //PP NLJS 13/08/2017 Cuento cuantas fotos hay en la carpeta de Proportion para asignarle un número a esta nueva img
            String[] pp_Files = pp_img.list();
            int pp_totFiles = pp_Files.length;
            pp_totFiles = pp_totFiles / 3;
            String filename = "Proportion_" + pp_totFiles + ".png";
            String filename2 = "Proportion_" + pp_totFiles + "_ori.png";
            String filename3 = "Proportion_" + pp_totFiles + "_draw.png";
            boolean pp_flag_comp = false;
            boolean pp_flag_comp2 = false;
            boolean pp_flag_comp3 = false;

            //PP NLJS 06/08/2017 Creo el FOS y File destino para guardar la img en la memoria del teléfono
            FileOutputStream pp_out = null;
            FileOutputStream pp_out2 = null;
            FileOutputStream pp_out3 = null;
            File pp_img2 = new File(pp_img, filename);
            File pp_img3 = new File(pp_img, filename2);
            File pp_img4 = new File(pp_img, filename3);

            //PP NLJS 06/08/2017 Intento inicializar el FOS
            try {
                mOpenCvCameraView.disableView();
                pp_out = new FileOutputStream(pp_img2);
                pp_out2 = new FileOutputStream(pp_img3);
                pp_out3 = new FileOutputStream(pp_img4);
                pp_flag_comp = pp_bmp.compress(Bitmap.CompressFormat.PNG, 100, pp_out); // pp_bmp is your Bitmap instance
                pp_flag_comp2 = pp_bmp_ori.compress(Bitmap.CompressFormat.PNG, 100, pp_out2); // pp_bmp is your Bitmap instance
                pp_flag_comp3 = pp_bmp_draw.compress(Bitmap.CompressFormat.PNG, 100, pp_out3); // pp_bmp is your Bitmap instance
                pp_out.close();
                pp_out2.close();
                pp_out3.close();

                // PP NLJS 13/08/2017 Guardo la dirección de la foto que se acaba de guardar
                pp_imgAdd = pp_img2.getAbsolutePath();
                pp_imgAdd2 = pp_img3.getAbsolutePath();
                pp_imgAdd3 = pp_img4.getAbsolutePath();
                Log.d(TAG, "PP Función: SaveImage. La imagen se guardó exitosamente. pp_imgAdd : " + pp_imgAdd);

                if (mOpenCvCameraView != null) {
                    //Mat lol = mRgba.clone();
                    mOpenCvCameraView.disableView();
                    //mRgba = lol.clone();
                    Act(mOpenCvCameraView);

                } else {
                    Act(mOpenCvCameraView);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "PP Función: SaveImage. Error al crear el FOS. Compress : " + pp_flag_comp + " " + pp_flag_comp2 + " " + pp_flag_comp3);
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

    /**
     * NCH 11/10/2017
     * Botones (los 3) Funcion de pendiendo de la seleccion del Usuario
     *
     * @param view
     */

    public void BtnTickF(View view) {
        if (pp_num == 2) {
            //mRgba=dibujada.clone();
            //resultado2();
            resultado();
            SaveImage();
        }
        btntick.setVisibility(View.INVISIBLE);
        btncross.setVisibility(View.INVISIBLE);
        btnadd.setVisibility(View.INVISIBLE);
        pp_espera = false;
    }

    public void BtnCrossF(View view) {
        CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
        mRgba = mRgba_Buttons_backup.clone();
        pp_num--;
        btntick.setVisibility(View.INVISIBLE);
        btncross.setVisibility(View.INVISIBLE);
        btnadd.setVisibility(View.INVISIBLE);
        pp_espera = false;
    }

    public void BtnAddF(View view) {
        pp_num--;
        btntick.setVisibility(View.INVISIBLE);
        btncross.setVisibility(View.INVISIBLE);
        btnadd.setVisibility(View.INVISIBLE);
        pp_espera = false;
    }

    /*
        public void resultado2() {
            double down_rojo = -1, up_rojo = mRgba.cols() + 1, left_rojo = mRgba.rows() + 1, right_rojo = -1;
            double down_verde = -1, up_verde = mRgba.cols() + 1, left_verde = mRgba.rows() + 1, right_verde = -1;
            for (List<MatOfPoint> contornos : ListaContornosRojos) {
                for (MatOfPoint mop : contornos) {
                    org.opencv.core.Point[] points = mop.toArray();
                    for (int i = 0; i < points.length; i++) {
                        double x = points[i].x;
                        double y = points[i].y;
                        if (x > right_rojo) right_rojo = x;
                        if (x < left_rojo) left_rojo = x;
                        if (y > down_rojo) down_rojo = y;
                        if (y < up_rojo) up_rojo = y;
                    }
                }
            }
            for (List<MatOfPoint> contornos : ListaContornosVerdes) {
                for (MatOfPoint mop : contornos) {
                    org.opencv.core.Point[] points = mop.toArray();
                    for (int i = 0; i < points.length; i++) {
                        double x = points[i].x;
                        double y = points[i].y;
                        if (x > right_verde) right_verde = x;
                        if (x < left_verde) left_verde = x;
                        if (y > down_verde) down_verde = y;
                        if (y < up_verde) up_verde = y;
                    }
                }
            }
            Log.d(TAG, "resultado: puntos maximos rojos up:" + up_rojo + "  down:+" + down_rojo + "   left:" + left_rojo + "   right:" + right_rojo);
            Log.d(TAG, "resultado: puntos maximos verde up:" + up_verde + "  down:+" + down_verde + "   left:" + left_verde + "   right:" + right_verde);
            dibujo(new Point(right_rojo, up_rojo), new Point(left_rojo, down_rojo), 1);
            dibujo(new Point(right_verde, up_verde), new Point(left_verde, down_verde), 2);
            x_r_Pix = abs(right_rojo - left_rojo);
            y_r_Pix = abs(up_rojo - down_rojo);
            x_g_Pix = abs(right_verde - left_verde);
            y_g_Pix = abs(up_verde - down_verde);

            x_g_um = x_g_Pix * (x_r_cm / x_r_Pix);
            y_g_um = y_g_Pix * (y_r_cm / y_r_Pix);


            Log.d(TAG, "resultado: Tarjeta   x: " + x_r_Pix + "     y: " + y_r_Pix);
            Log.d(TAG, "resultado: Objeto   x: " + x_g_Pix + "     y: " + y_g_Pix);
            Log.d(TAG, "resultado: Tarjeta   x: " + x_r_cm + "     y: " + y_r_cm);
            Log.d(TAG, "resultado: Objeto   x: " + x_g_um + "     y: " + y_g_um);


        }
    */
    public void resultado() {
        /**
         *  NCH 14/10/2017
         *
         *  Esta funcion devuelve el resultado grafico de todoo el proceso
         *
         *  mRgba contiene la imagen final con los contornos
         *      - Contornos Rojo: Objeto a Medir (22/10/2017 NLJS Rojo es la tarjeta, objeto de referencia)
         *      - Contornos Verde: Objeto de referencia (22/10/2017 NLJS Verde es el objeto a medir)
         *  pp_mRgba_original contiene la imagen original
         *
         *  color
         */
        // PP NLJS 11/11/2017 Inicializa la matriz de búsqueda
        getDataMatrix();

        // PP NLJS 22/10/2017 Por la forma en la que se hace el recorrido me parece que los valores up corresponden a rows y los valores left a cols,
        // cols siempre es mayor a rows , por la orientacion esto significa que cols es el eje X, el que se recorre de izq a derecha, xq la matrix mRgba esta 'acostada'
        //int down_r = -1, up_r = mRgba.cols() + 1, left_r = mRgba.rows() + 1, right_r = -1;
        //int down_g = -1, up_g = mRgba.cols() + 1, left_g = mRgba.rows() + 1, right_g = -1;
        int down_r = -1, up_r = mRgba.rows() + 1, left_r = mRgba.cols() + 1, right_r = -1;
        int down_g = -1, up_g = mRgba.rows() + 1, left_g = mRgba.cols() + 1, right_g = -1;

        Log.d(TAG, "PP. Función: resultado: width: " + mRgba.cols());
        Log.d(TAG, "PP. Función: resultado: rowa: " + mRgba.rows());

        for (int x = 0; x < mRgba.rows(); x++) {
            // PP NLJS 11/11/2017 Obtiene la fila x, la guarda en una variable temporal
            ArrayList<double[]> temporal = find.get(x);

            for (int y = 0; y < mRgba.cols(); y++) {
                // PP NLJS 11/11/2017 Obtiene la pel pixel y, guarda el RGB en una variable temporal
                double[] data = temporal.get(y);

                double r = data[0];
                double g = data[1];
                double b = data[2];

                if (r == 255 && g == 0 && b == 0) {
                    if (x > down_r) down_r = x;
                    if (x < up_r) up_r = x;
                    if (y > right_r) right_r = y;
                    if (y < left_r) left_r = y;
                }
                if (r == 0 && g == 255 && b == 0) {
                    if (x > down_g) down_g = x;
                    if (x < up_g) up_g = x;
                    if (y > right_g) right_g = y;
                    if (y < left_g) left_g = y;
                }
            } //for cols
        } //for rows

        Log.d(TAG, "PP. Función: Resultado. puntos maximos up_r:" + up_r + "  down_r:" + down_r + "   left_r:" + left_r + "   right_r:" + right_r);
        Log.d(TAG, "PP. Función: Resultado.  puntos maximos up_g:" + up_g + "  down_g:" + down_g + "   left_g:" + left_g + "   right_g:" + right_g);

        // PP NLJS 06/11/2017 Se crea el objeto en el cual se dibujarán las líneas;
        dibujada = pp_mRgba_original.clone();


        // PP NLJS 21/10/2017 Guarda los 4 vertices como puntos y los une con lineas rectas;
        ArrayList<Point>verticesTar = new ArrayList<Point>(4);
        ArrayList<Point>verticesObj = new ArrayList<Point>(4);

        // PP NLJS 21/10/2017 Vértices tarjeta, es importante el orden en el que se mandan a llamar estas 4 lineas;
        findVertexes(left_r,right_r, 1,255,0,0,verticesTar);
        findVertexes(down_r,up_r,2,255,0,0,verticesTar);

        Log.d(TAG, "PP. Función: Resultado. Points:" + verticesTar);

        // PP NLJS 21/10/2017 Vértices objeto;
        findVertexes(left_g, right_g,1,0,255,0,verticesObj);
        findVertexes(down_g,up_g,2,0,255,0,verticesObj);

        Log.d(TAG, "PP. Función: Resultado. Points:" + verticesObj);

        // PP NLJS 11/11/2017 Calcular ángulo de rotación;
        double angleTar = findAngle(verticesTar.get(2),verticesTar.get(3));
        double angleObj = findAngle(verticesObj.get(2),verticesObj.get(3));

        // PP NLJS 11/11/2017 Corregir vértices;
        boolean flag = false;
        flag = rotateVertex(verticesTar, angleTar);

        // PP NLJS 11/11/2017 Contour es de apoyo para nosotros, cuando consideren que tiene buena precision comentar linea;
        if(flag == true)    contour(verticesTar,0,255,255);
        else Log.d(TAG, "PP. Función: Resultado. Error al calcular los neuvos vertices de la tarjeta." );

        flag = false;
        flag = rotateVertex(verticesObj, angleObj);
        if(flag == true)    contour(verticesObj,0,255,255);
        else Log.d(TAG, "PP. Función: Resultado. Error al calcular los neuvos vertices de la tarjeta." );

        // PP NLJS 12/11/2017 Calculo de dimension con la correccion de vértices;
        getRealDimensions(verticesTar,verticesObj);

        // PP NLJS 06/11/2017 Se dibujan los rectangulos;
        dibujo(new Point(right_r, up_r), new Point(left_r, down_r), 1);
        dibujo(new Point(right_g, up_g), new Point(left_g, down_g), 2);


        x_r_Pix = abs(up_r - down_r);
        y_r_Pix = abs(right_r - left_r);
        x_g_Pix = abs(up_g - down_g);
        y_g_Pix = abs(right_g - left_g);


        // PP NLJS 22/10/2017 No es necesario cambiar estas medidas porque no afectan en la formula;
        /*if (y_g_Pix > x_g_Pix) {
            Double x = y_g_Pix;
            y_g_Pix = x_g_Pix;
            x_g_Pix = x;
        }*/

        // PP NLJS 22/10/2017 Es necesario saber cual es la medida mayor en pixeles de la tarjeta (conocer x) para aplicar la formula correcta;
        // PP NLJS 22/10/2017 Bandera para saber si la tarjeta estaba parada (y era mayor que x);
        Boolean pp_flag = false;
        if (y_r_Pix > x_r_Pix) {
            pp_flag = true;
        }

        // PP NLJS 22/10/2017 Si la tarjeta estaba parada y_r_Pix debe ir en la primer formula,
        // recuerden que x_r_cm y y_r_cm tienen valores constantes (x_r_cm siempre es el mayor por eso si importa saber como esta la tarjeta y no importa como esta el objeto);
        if(pp_flag == true){
            x_g_um = (x_g_Pix * x_r_cm) / y_r_Pix;
            y_g_um = (y_g_Pix * y_r_cm) / x_r_Pix;
        }else {
            x_g_um = (x_g_Pix * x_r_cm) / x_r_Pix;
            y_g_um = (y_g_Pix * y_r_cm) / y_r_Pix;
        }

        Log.d(TAG, "resultado: x_g_um:" + x_g_um);
        Log.d(TAG, "resultado: y_g_um:" + y_g_um);

        String sw = "Medidas Obtenidas: " + String.format("%.2f", x_g_um) + " * " + String.format("%.2f", y_g_um);
        Imgproc.putText(dibujada, sw, new Point(100, 100), 1, 2, new Scalar(0, 0, 0, 255), 6, LINE_8, false);
        Imgproc.putText(dibujada, sw, new Point(100, 100), 1, 2, new Scalar(255, 255, 255, 255), 4, LINE_8, false);


        //Log.d(TAG, "resultado: Tarjeta   x: " + x_r_Pix + "     y: " + y_r_Pix);
        //Log.d(TAG, "resultado: Objeto   x: " + x_g_Pix + "     y: " + y_g_Pix);
        Log.d(TAG, "resultado: Tarjeta   x: " + x_r_cm + "     y: " + y_r_cm);
        Log.d(TAG, "resultado: Objeto   x: " + x_g_um + "     y: " + y_g_um);


    }

    public void dibujo(Point p1, Point p2, int i) {
        /**
         *  NCH 14/10/2017
         *  Esta funcion recibe los puntos (right-up) (left-down) y dibuja el rectangulo
         */

        if (i == 1) {

            //cvtColor(dibujada, dibujada, COLOR_GRAY2BGR);
            Imgproc.rectangle(dibujada, p1, p2, new Scalar(255, 0, 0, 255), 1);
        } else Imgproc.rectangle(dibujada, p1, p2, new Scalar(0, 255, 0, 255), 1);
    }


    void getDataMatrix(){
        /**
         * PP NLJS 11/11/2017
         * Copia la información de la imagen a un ListArray para reducir el tiempo en las búsquedas
         *
         */

        // PP NLJS 11/11/2017 Establece la primer dimension del arreglo, rows
        find = new ArrayList<ArrayList<double[]>>(mRgba.rows());

        for (int x = 0; x < mRgba.rows(); x++) {
            // PP NLJS 11/11/2017 Crea arreglo temporal para llenar e insertar, segunda dimension, cols
            ArrayList<double[]> temporal = new ArrayList<double[]>(mRgba.cols());

            for (int y = 0; y < mRgba.cols(); y++) {
                // PP NLJS 11/11/2017 Obtiene el pixel y su información, obligarnos a obtener el pixel en xy hace que tarde
                // PP NLJS 11/11/2017 con esta función podremos obtener la fila y luego acceder al pixel, reduciendo el tiempo
                double[] data = mRgba.get(x, y);

                // PP NLJS 11/11/2017 Agrega los valores RGB a la fila temporal en la posicion y
                temporal.add(y,data);
            } //for cols

            // PP NLJS 11/11/2017 Agrega la fila temporal en la posicion x
            find.add(x,temporal);
        } //for rows
        Log.d(TAG, "PP. Función: getDataMatrix. Concluido.");
    }

    public void findVertexes(int num1,int num2,int op,int r, int g, int b, ArrayList<Point> vertices){
        /**
         * PP NLJS 22/10/2017
         * Recorre la matriz con los puntos máximos para encontrar las coordenadas de los vertices
         *
         */
        int var1 = 0,var2 = 0, flag1 = 0, flag2 = 0;

        // PP NLJS 22/10/2017 Recorre la img de abajo a arriba
        if(op == 1){
            var1 = 0;
            var2 = mRgba.rows() - 1;
            int x2 = mRgba.rows() - 1,x;

            Log.d(TAG, "PP. Función: findVertexes: n1:" + (int)num1);
            Log.d(TAG, "PP. Función: findVertexes: n2:" + (int)num2);

            for (x = 0; x < mRgba.rows(); x++) {
                // PP NLJS 11/11/2017 Obtiene la fila x, la guarda en una variable temporal
                ArrayList<double[]> temporal = find.get(x);

                // PP NLJS 11/11/2017 Obtiene la pel pixel y, guarda el RGB en una variable temporal
                double[] data = temporal.get(num1);

                // PP NLJS 11/11/2017 Variables temporales para validar que solo un valor RGB este en 255, los demás deberán de ser 0
                int vartemp = 0;

                if(data[0] == r) vartemp++;
                if(data[1] == g) vartemp++;
                if(data[2] == b) vartemp++;

                if (vartemp == 3) {
                    var1 = x;
                    break;
                }

            } //for rows

            for (x2 = mRgba.rows() - 1; x2 > -1; x2--) {
                // PP NLJS 11/11/2017 Obtiene la fila x, la guarda en una variable temporal
                ArrayList<double[]> temporal2 = find.get(x2);

                // PP NLJS 11/11/2017 Obtiene la pel pixel y, guarda el RGB en una variable temporal
                double[] data2 = temporal2.get(num2);

                // PP NLJS 11/11/2017 Variables temporales para validar que solo un valor RGB este en 255, los demás deberán de ser 0
                int vartemp2 = 0;

                if(data2[0] == r) vartemp2++;
                if(data2[1] == g) vartemp2++;
                if(data2[2] == b) vartemp2++;

                if (vartemp2 == 3) {
                    var2 = x2;
                    break;
                }

            } //for rows

            // PP NLJS 11/11/2017 Dibuja las diagonales y guarda los vertices en el arreglo
            vertices.add(0,new Point(num2,var2));
            vertices.add(1,new Point(num1,var1));
            Imgproc.line(dibujada, vertices.get(0) , vertices.get(1),new Scalar(r, g, b, 255), 1);
        }else {
            var1 = 0;
            var2 = mRgba.cols()-1;

            int y2 = mRgba.cols() - 1, y;

            Log.d(TAG, "PP. Función: findVertexes: n1:" + (int)num1);
            Log.d(TAG, "PP. Función: findVertexes: n2:" + (int)num2);

            // PP NLJS 11/11/2017 Obtiene la fila x, la guarda en una variable temporal
            ArrayList<double[]> temporal = find.get(num1);
            ArrayList<double[]> temporal2 = find.get(num2);

            for (y = 0; y < mRgba.cols(); y++)   {
                // PP NLJS 11/11/2017 Obtiene la pel pixel y, guarda el RGB en una variable temporal
                double[] data = temporal.get(y);

                // PP NLJS 11/11/2017 Variables temporales para validar que solo un valor RGB este en 255, los demás deberán de ser 0
                int vartemp = 0;

                if(data[0] == r) vartemp++;
                if(data[1] == g) vartemp++;
                if(data[2] == b) vartemp++;

                if (vartemp == 3) {
                    var1 = y;
                    break;
                }
            } //for rows

            for (y2 = mRgba.cols() - 1; y2 > -1 ; y2--)   {
                // PP NLJS 11/11/2017 Obtiene la pel pixel y, guarda el RGB en una variable temporal
                double[] data2 = temporal2.get(y2);

                // PP NLJS 11/11/2017 Variables temporales para validar que solo un valor RGB este en 255, los demás deberán de ser 0
                int vartemp2 = 0;

                if(data2[0] == r) vartemp2++;
                if(data2[1] == g) vartemp2++;
                if(data2[2] == b) vartemp2++;

                if (vartemp2 == 3) {
                    var2 = y2;
                    break;
                }
            } //for rows

            // PP NLJS 11/11/2017 Dibuja las diagonales y guarda los vertices en el arreglo
            vertices.add(2,new Point(var1,num1));
            vertices.add(3,new Point(var2,num2));
            Imgproc.line(dibujada, vertices.get(2) , vertices.get(3),new Scalar(r, g, b, 255), 1);

        }
        Log.d(TAG, "PP. Función: findVertexes: var1:" + var1);
        Log.d(TAG, "PP. Función: findVertexes: var2:" + var2);
    }

    double findAngle(Point p1, Point p2){
        /**
         * PP NLJS 22/10/2017
         * A partir de analisis vectorial obtiene el angulo entre el plano y una de las diagonales
         *
         */

        Point o1 = new Point(0,mRgba.rows() - 1);
        Point o2 = new Point(mRgba.cols() - 1,mRgba.rows() - 1);
        // Imgproc.line(dibujada,  o1, o2 ,new Scalar(122, 100, 200, 255), 1);
        // Imgproc.line(dibujada,  o1, p1 ,new Scalar(122, 100, 200, 255), 1);
        // Imgproc.line(dibujada,  o1, p2 ,new Scalar(122, 100, 200, 255), 1);

        // PP NLJS 11/11/2017 Creación de vectores
        Point vector = new Point(p1.x - p2.x, p1.y - p2.y);
        Point base = new Point(o1.x - o2.x, o1.y - o2.y);

        Log.d(TAG, "PP. Función: findAngle. vector:" + vector + " base: " + base);

        // PP NLJS 11/11/2017 Producto punto
        double punto = vector.x * base.x + vector.y * base.y;

        // PP NLJS 11/11/2017 Producto cruz
        ArrayList<Double> determinante = new ArrayList<Double>(3);
        int h = 0;
        determinante.add(0, base.y * h - vector.y * h);
        determinante.add(1, vector.x * h - base.x * h);
        determinante.add(2, base.x * vector.y - base.y * vector.x);

        Log.d(TAG, "PP. Función: findAngle. determinante:" + determinante);

        // PP NLJS 11/11/2017 Normas
        double no = Math.sqrt( Math.pow(base.x,2) + Math.pow(base.y,2));
        double np = Math.sqrt( Math.pow(vector.x,2) + Math.pow(vector.y,2));
        double nd = Math.sqrt( Math.pow(determinante.get(0),2) + Math.pow(determinante.get(1),2) + Math.pow(determinante.get(2),2));

        Log.d(TAG, "PP. Función: findAngle. no: " + no + " np: " + np + " nd: " + nd);

        // PP NLJS 11/11/2017 Obtengo el álgulo, se hacen ambos para evitar perdida al transformar
        double sinangle = nd / (no * np);
        double cosangle = punto / (no * np);

        Log.d(TAG, "PP. Función: findAngle. asinangle: " + Math.asin(sinangle) + " acosangle: " + Math.acos(cosangle));
        Log.d(TAG, "PP. Función: findAngle. sinangle: " + sinangle + " cosangle: " + cosangle);

        double temporal = Math.acos(cosangle);
        if(temporal > 1) return  temporal - 0.5;

        return temporal;
    }

    boolean rotateVertex(ArrayList<Point> vertexes, double angle){
        /**
         * PP NLJS 22/10/2017
         * Calcula las nuevas coordenadas de los vertices
         *
         */

        // PP NLJS 11/11/2017 Matriz de rotacion, eje Z
        ArrayList<ArrayList<Double>> rotationZ = new ArrayList<ArrayList<Double>>(3);
        ArrayList<Double> temporal = new ArrayList<Double>(3);
        ArrayList<Double> temporal2 = new ArrayList<Double>(3);
        ArrayList<Double> temporal3 = new ArrayList<Double>(3);

        // PP NLJS 11/11/2017 Primer columna
        temporal.add(0,Math.cos(angle));
        temporal.add(1,Math.sin(angle));
        temporal.add(2,0.0);
        rotationZ.add(0,temporal);

        // PP NLJS 11/11/2017 Segunda columna
        temporal2.add(0,Math.sin(angle) * -1);
        temporal2.add(1,Math.cos(angle));
        temporal2.add(2,0.0);
        rotationZ.add(1,temporal2);

        // PP NLJS 11/11/2017 Tercer columna
        temporal3.add(0,0.0);
        temporal3.add(1,0.0);
        temporal3.add(2,1.0);
        rotationZ.add(2,temporal3);

        Log.d(TAG, "PP. Función: rotateVertex. Matriz Z: " + rotationZ);

        double tempx,tempy;
        for(int i = 0; i < 4; i++) {
            tempx = vertexes.get(i).x * rotationZ.get(0).get(0) + vertexes.get(i).y * rotationZ.get(1).get(0);
            tempy = vertexes.get(i).x * rotationZ.get(0).get(1) + vertexes.get(i).y * rotationZ.get(1).get(1);

            vertexes.get(i).x = tempx;
            vertexes.get(i).y = tempy;

        }
        Log.d(TAG, "PP. Función: rotateVertex. New vertex: " + vertexes);

        return true;
    }

    void contour(ArrayList<Point> vertices,int r, int g, int b){
        /**
         * PP NLJS 22/10/2017
         * Une los vértices corregidos con líneas
         *
         */

        Imgproc.line(dibujada,  vertices.get(0), vertices.get(2) ,new Scalar(r, g, b, 255), 1);
        Imgproc.line(dibujada,  vertices.get(1), vertices.get(2) ,new Scalar(r, g, b, 255), 1);
        Imgproc.line(dibujada,  vertices.get(1), vertices.get(3) ,new Scalar(r, g, b, 255), 1);
        Imgproc.line(dibujada,  vertices.get(3), vertices.get(0) ,new Scalar(r, g, b, 255), 1);
    }

    void getRealDimensions(ArrayList<Point> vertR, ArrayList<Point> vertG){
        /**
         * PP NLJS 22/10/2017
         * Obtiene las dimensiones una vez hecha la corrección de ángulo, se puede hacer un promedio entre
         * esta medida y la medida obtenida sin la corrección del ángulo
         *
         */
        // PP NLJS 11/11/2017 Busco los máximos y minimos de estos 4 vertices, una vez la correccion de angulo sea mas confiable no sera necesario
        double maxx = vertG.get(0).x;
        double minx = vertG.get(0).x;
        double maxy = vertG.get(0).y;
        double miny = vertG.get(0).y;

        for (int i = 1; i < 4 ; i++){
            Point temp = vertG.get(i);
            if(temp.x < minx) minx = temp.x;
            if(temp.x > maxx) maxx = temp.x;
            if(temp.y < miny) miny = temp.y;
            if(temp.y > maxy) maxy = temp.y;
        }

        double x_g_Pix = abs(maxy - miny);
        double y_g_Pix = abs(maxx - minx);

        //Log.d(TAG, "PP. Función: getRealDimensions. x: " + maxx + " " + minx);
        //Log.d(TAG, "PP. Función: getRealDimensions. y: " + maxy + " " + miny);
        //Log.d(TAG, "PP. Función: getRealDimensions. x_g_pix:" + x_g_Pix);
        //Log.d(TAG, "PP. Función: getRealDimensions. y_g_pix:" + y_g_Pix);

        maxx = vertR.get(0).x;
        minx = vertR.get(0).x;
        maxy = vertR.get(0).y;
        miny = vertR.get(0).y;


        for (int i = 1; i < 4 ; i++){
            Point temp = vertR.get(i);
            if(temp.x < minx) minx = temp.x;
            if(temp.x > maxx) maxx = temp.x;
            if(temp.y < miny) miny = temp.y;
            if(temp.y > maxy) maxy = temp.y;
        }

        double x_r_Pix = abs(maxy - miny);
        double y_r_Pix = abs(maxx - minx);

        //Log.d(TAG, "PP. Función: getRealDimensions. x: " + maxx + " " + minx);
        //Log.d(TAG, "PP. Función: getRealDimensions. y: " + maxy + " " + miny);
        //Log.d(TAG, "PP. Función: getRealDimensions. x_r_pix:" + x_r_Pix);
        //Log.d(TAG, "PP. Función: getRealDimensions. y_r_pix:" + y_r_Pix);

        // PP NLJS 22/10/2017 Es necesario saber cual es la medida mayor en pixeles de la tarjeta (conocer x) para aplicar la formula correcta;
        // PP NLJS 22/10/2017 Bandera para saber si la tarjeta estaba parada (y era mayor que x);
        Boolean pp_flag = false;
        if (y_r_Pix > x_r_Pix) {
            pp_flag = true;
        }

        // PP NLJS 22/10/2017 Si la tarjeta estaba parada y_r_Pix debe ir en la primer formula,
        // recuerden que x_r_cm y y_r_cm tienen valores constantes (x_r_cm siempre es el mayor por eso si importa saber como esta la tarjeta y no importa como esta el objeto);
        if(pp_flag == true){
            x_g_um = (x_g_Pix * x_r_cm) / y_r_Pix;
            y_g_um = (y_g_Pix * y_r_cm) / x_r_Pix;
        }else {
            x_g_um = (x_g_Pix * x_r_cm) / x_r_Pix;
            y_g_um = (y_g_Pix * y_r_cm) / y_r_Pix;
        }

        Log.d(TAG, "PP. Función: getRealDimensions. x_g_um:" + x_g_um);
        Log.d(TAG, "PP. Función: getRealDimensions. y_g_um:" + y_g_um);

        String sw = "PP. Función: getRealDimensions. Medidas Obtenidas: " + String.format("%.2f", x_g_um) + " * " + String.format("%.2f", y_g_um);
        //Imgproc.putText(dibujada, sw, new Point(100, 100), 1, 2, new Scalar(0, 0, 0, 255), 6, LINE_8, false);
        //Imgproc.putText(dibujada, sw, new Point(100, 100), 1, 2, new Scalar(255, 255, 255, 255), 4, LINE_8, false);


        //Log.d(TAG, "PP. Función: getRealDimensions. Tarjeta   x: " + x_r_Pix + "     y: " + y_r_Pix);
        //Log.d(TAG, "PP. Función: getRealDimensions. Objeto   x: " + x_g_Pix + "     y: " + y_g_Pix);
        Log.d(TAG, "PP. Función: getRealDimensions. Tarjeta   x: " + x_r_cm + "     y: " + y_r_cm);
        Log.d(TAG, "PP. Función: getRealDimensions. Objeto   x: " + x_g_um + "     y: " + y_g_um);



    }
}
