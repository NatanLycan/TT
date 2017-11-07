package org.opencv.samples.colorblobdetect;

import android.app.Activity;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class Galeria extends Activity {

//    private ArrayList<String> image_Title;
    private final String image_Title[]={"img1","img2"};
    private final Integer image_Ids[]={R.drawable.add,R.drawable.book};

//    public void obtenerLista(){
//        image_Title = new ArrayList<String>();
//        File sd = Environment.getExternalStorageDirectory();
//        String subcadena = "_draw.png";
//        String path = "Pictures/Proportion";
//        File directorio = new File(sd,path);
//        if(directorio.exists()){
//            for(File f: directorio.listFiles()){
//                if(f.isFile()){
//                    if ( f.getName().contains(subcadena)){
//                        image_Title.add(f.getName());
//                    }
//                }
//            }
//        }
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        for (int i = 0; i < image_Title.length; i++) {
            Cell cell = new Cell();
            cell.setTitle(image_Title[i]);
            cell.setImg(image_Ids[i]);
            theimage.add(cell);
        }
        return theimage;
    }
}
