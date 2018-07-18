package com.example.dave.filedownloader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    EditText editText;

    Button button;
    private static final int MY_PERMISSION = 1;

    ProgressDialog mProgressdialog;

    double file_size = 0;
    String file_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText editText = (EditText)findViewById(R.id.urlPaste);

         button = (Button)findViewById(R.id.Btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSION);
                }else {
                    File  dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/MyDownloadedFiles/");
                    try {
                        dir.mkdir();
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Cannot Create Folder!", Toast.LENGTH_SHORT).show();
                    }
                    //from where to download your file
                    final EditText inputNames = findViewById(R.id.urlPaste);

                    InputStream isr = new InputStream() {
                        @Override
                        public int read() throws IOException {
                            String string = inputNames.toString();
                                return 0;
                        }
                    };

                   /* new DownloadTask().execute("string");*/
                    new DownloadTask().execute(String.valueOf(isr));

                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION:{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();

                    File  dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/MyDownloadedFiles/");
                    try {
                        dir.mkdir();
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Cannot Create Folder!", Toast.LENGTH_SHORT).show();
                    }
                    Object isr = new Object();
                    final AsyncTask<String, Integer, String> execute = new DownloadTask().execute(String.valueOf(isr));
                    /*new DownloadTask().get("string");*/
                    /*new DownloadTask().execute("http://radefffactory.com/FibonacciBenchmark/txt");*/

                }else {
                    Toast.makeText(this, "Permission Not Granted!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Integer,String>{

        @Override
        protected String doInBackground(String... strings) {
            file_name = strings[0].substring(strings[0].lastIndexOf("/") + 1 );

            try {
                InputStream inputStream = null;
                OutputStream outputStream= null;
                HttpURLConnection httpURLConnection = null;
                try {
                    URL url = new URL(strings[0]);
                    httpURLConnection= (HttpURLConnection) url.openConnection();
                    httpURLConnection.connect();

                    if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK){
                        return "Server returned HTTP" + httpURLConnection.getResponseCode()+" " +
                                httpURLConnection.getResponseCode();
                    }
                    int file_length = httpURLConnection.getContentLength();
                    file_size = file_length;

                    inputStream = httpURLConnection.getInputStream();
                    outputStream = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/MyDownloadedFiles/" + file_name);

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = inputStream.read(data)) != -1 ){
                        if (isCancelled()){
                            return null;
                        }
                        total += count;
                        if (file_length > 0){
                            publishProgress((int) (total*100/file_length));
                        }
                        outputStream.write(data,0,count);
                    }

            } catch (Exception e) {
                    return e.toString();
            }finally {
                    try {
                        if (outputStream !=null){
                            outputStream.close();
                        }
                        if (inputStream != null){
                            inputStream.close();
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }if (httpURLConnection != null){
                        httpURLConnection.disconnect();
                    }
                }
                }finally{
                    }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressdialog = new ProgressDialog(MainActivity.this);
            mProgressdialog.setTitle("Downloading....");
            mProgressdialog.setMessage("File size: 0MB");
            mProgressdialog.setIndeterminate(true);
            mProgressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressdialog.setCancelable(true);

            mProgressdialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    Toast.makeText(MainActivity.this, "Download Cancelled", Toast.LENGTH_SHORT).show();
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/MyDownloadedFiles/" + file_name);
                    try {
                        dir.delete();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            mProgressdialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            mProgressdialog.setIndeterminate(false);
            mProgressdialog.setMax(100);
            mProgressdialog.setProgress(values[0]);
            mProgressdialog.setMessage("File size:"+ new  DecimalFormat("##.##").format(file_size / 1000000) + "MB");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressdialog.dismiss();
            if (result != null){
                Toast.makeText(MainActivity.this, "Error: " +result, Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(MainActivity.this, "Downloaded", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
