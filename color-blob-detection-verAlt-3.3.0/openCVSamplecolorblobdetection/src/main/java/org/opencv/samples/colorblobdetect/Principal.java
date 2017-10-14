package org.opencv.samples.colorblobdetect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

public class Principal extends Activity {


    RadioButton rd_centimetros = null;
    RadioButton rd_pulgadas = null;
    RadioButton rd_pies = null;
    Button boton_camara = null;
    Button btnAceptar = null;
    Dialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        boton_camara = (Button) findViewById(R.id.boton_camara);
        btnAceptar = (Button) findViewById(R.id.btn_aceptar);
        boton_camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(Principal.this);
                dialog.setContentView(R.layout.dialog_medidas);
                dialog.setTitle("Elige las medidas a utilizar");
                dialog.setCancelable(true);

                rd_centimetros = (RadioButton) dialog.findViewById(R.id.rd_centimetros);
                rd_pulgadas = (RadioButton) dialog.findViewById(R.id.rd_pulgadas);
                rd_pies = (RadioButton) dialog.findViewById(R.id.rd_pies);
                btnAceptar = (Button) dialog.findViewById(R.id.btn_aceptar);

                btnAceptar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String MEDIDA = "";
                        if (rd_centimetros.isChecked()) {
                            MEDIDA = "CENTIMETROS";
                        } else if (rd_pulgadas.isChecked()) {
                            MEDIDA = "PULGADAS";
                        } else if (rd_pies.isChecked()) {
                            MEDIDA = "PIES";
                        }
                        if (MEDIDA!= "") {
                            Intent intent = new Intent(Principal.this, ColorBlobDetectionActivity.class);
                            intent.putExtra("MEDIDA", MEDIDA);
                            Principal.this.startActivity(intent);
                        } else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(Principal.this);
                            builder.setMessage("Por favor selecciona una medida.")
                                    .setCancelable(false)
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog2, int id) {
                                            if (dialog != null) {
                                                dialog.dismiss();
                                                dialog2.dismiss();
                                            }
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();

                        }
                    }
                });
                dialog.show();
            }
        });


    }
}
