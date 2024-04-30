package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ImageView meuImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        meuImageView = findViewById(R.id.meuImageView);
    }


    public void tirarFotoClick(View view) {
        //abrirCameraMiniatura();
        abrirCameraFotoCompleta();
    }

    static final int CODIGO_REQUISICAO = 1;

    private void abrirCameraMiniatura() {
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Testa-se se existe um aplicativo para a câmera
        if (it.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(it, CODIGO_REQUISICAO);
        }
        else{
            Toast.makeText(this, "erro", Toast.LENGTH_SHORT).show();
        }
    }

    //onActivityResult para capturar a miniatura da foto tirada pela câmera
    /*@Override
    protected void onActivityResult(int codigoTela, int codigoResultado, Intent data) {
        super.onActivityResult(codigoTela, codigoResultado, data);
        // Testa-se se foi a tela da câmera que finalizou (codigoTela== CODIGO_REQUISICAO) E se a câmera foi finalizada após uma foto ter sido tirada (codigoResultado == RESULT_OK)
        if (codigoTela== CODIGO_REQUISICAO && codigoResultado == RESULT_OK) {
            // Recuperamos os parâmetros retornados pela câmera
            Bundle parametros = data.getExtras();
            // Recuperamos o parâmetro data, que corresponde ao bitmap do thumbnail
            Bitmap imagemBitmapMiniatura = (Bitmap) parametros.get("data");
            // definimos em um ImageView o thumbnail como imagem
            meuImageView.setImageBitmap(imagemBitmapMiniatura);
        }
    }*/


    String caminhoImagem;
    File arquivoImagem;
    Bitmap imagemBitmapCompleta;
    private static final String ROOT_URL = "http://172.28.176.1/fazerUpload.php";

    //onActivityResult para capturar a imagem completa
    @Override
    protected void onActivityResult(int codigoTela, int codigoResultado, Intent data) {
        super.onActivityResult(codigoTela, codigoResultado, data);
        // Testa-se se foi a tela da câmera que finalizou (codigoTela== CODIGO_REQUISICAO) E se a câmera foi finalizada após uma foto ter sido tirada (codigoResultado == RESULT_OK)
        if (codigoTela== CODIGO_REQUISICAO_2 && codigoResultado == RESULT_OK) {
            // Recuperamos o arquivo da imagem
            arquivoImagem = new File(caminhoImagem);
            // Testamos se o arquivo de fato existe
            if (arquivoImagem.exists()) {
                // Recuperamos, a partir do arquivo, o bitmap da imagem
                imagemBitmapCompleta = BitmapFactory.decodeFile(
                        arquivoImagem.getAbsolutePath());
                // adicionamos ao ImageView a imagem que foi salva
                meuImageView.setImageBitmap(imagemBitmapCompleta);
            }
        }
    }


    private File criaArquivoImagem() throws IOException {
        // Cria-se um arquivo de imagem cujo nome sera a hora do sistema
        String horarioSistema = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nomeArquivoImagem = "JPEG_" + horarioSistema + "_";
        File caminhoPastaImagens = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagem = File.createTempFile(nomeArquivoImagem,".jpg",caminhoPastaImagens);

        // Salva o arquivo: o caminho do arquivo salvo deverá ser passado por parâmetro para a câmera
        caminhoImagem = imagem.getAbsolutePath();
        return imagem;
    }

    static final int CODIGO_REQUISICAO_2 = 2;

    private void abrirCameraFotoCompleta() {
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Testa-se se existe um aplicativo para a câmera
        if (it.resolveActivity(getPackageManager()) != null) {
            // Criamos o arquivo onde a foto será salva
            File arquivoFoto = null;
            try {
                arquivoFoto = criaArquivoImagem();
                // se o arquivo da foto foi criado com sucesso
                if (arquivoFoto != null) {
                    // adicionamos um parâmetro referente ao arquivo que foi criado
                    Uri UriFoto = FileProvider.getUriForFile(this,"com.example.android.fileprovider",arquivoFoto);
                    it.putExtra(MediaStore.EXTRA_OUTPUT, UriFoto);
                    // inicializamos o aplicativo da câmera
                    startActivityForResult(it, CODIGO_REQUISICAO_2);
                }
            } catch (IOException ex) {
                // Caso aconteça algum erro...
            }

        }
    }

    private static final int REQUEST_PERMISSIONS = 100;
    public void fazerUploadClick(View view) {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                //PEDE PERMISSÃO DE ACESSO A LEITURA E ESCRITA DE DISCO
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
        } else {
            //Faz o upload da imagem
            uploadBitmap(imagemBitmapCompleta);
        }
    }

    private void uploadBitmap(final Bitmap bitmap) {

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, ROOT_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}