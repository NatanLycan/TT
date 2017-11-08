package org.opencv.samples.colorblobdetect;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class Galeria extends Activity {

    private ArrayList<String> image_Title2=new ArrayList<String>();
//    private String image_Title[]={"img1","img2"};
    private ArrayList<Bitmap> image_Ids=new ArrayList<Bitmap>();//={R.drawable.add,Integer.parseInt(image_Title2.get(0))};

    public void obtenerLista(){
        image_Title2 = new ArrayList<String>();
        File sd = Environment.getExternalStorageDirectory();
        String subcadena = "_draw.png";
        String path = "Pictures/Proportion";
        File directorio = new File(sd,path);
        if(directorio.exists()){
            for(File f: directorio.listFiles()){
                if(f.isFile()){
                    if ( f.getName().contains(subcadena)){
                        image_Title2.add(f.getName().substring(0,13));
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap pp_bitmap = BitmapFactory.decodeFile("/storage/emulated/0/"+path+"/"+f.getName(), options);
                        //Rotar Imagen
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap imagenRotada = Bitmap.createBitmap(pp_bitmap , 0, 0, pp_bitmap .getWidth(), pp_bitmap.getHeight(), matrix, true);
                        image_Ids.add(imagenRotada);
                    }
                }
            }
        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        obtenerLista();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galeria);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.gallery);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),3);
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<Cell> cells = prepareData();
        MyAdapter adapter = new MyAdapter(getApplicationContext(),cells);
        recyclerView.setAdapter(adapter);

    }

    private ArrayList<Cell> prepareData() {
        ArrayList<Cell> theimage = new ArrayList<Cell>();
        for (int i = 0; i < image_Title2.size(); i++) {
            Cell cell = new Cell();
            cell.setTitle(image_Title2.get(i));
            cell.setImg(image_Ids.get(i));
            theimage.add(cell);
        }
        return theimage;
    }
}
