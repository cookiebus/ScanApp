package com.example.snake.scanapp;

import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

import android.os.PersistableBundle;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.AsyncTask;

import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.util.Log;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONObject;


public class MainActivity extends Activity implements SurfaceHolder.Callback {

    public Camera camera;
    public SurfaceView surfaceView;
    public SurfaceHolder surfaceHolder;

    public Camera.PictureCallback rawCallback;
    public Camera.ShutterCallback shutterCallback;
    public Camera.PictureCallback jpegCallback;
    public Boolean start_scanning = false;

    private static final int TIME_OUT = 10*10000000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    public static final String SUCCESS="1";
    public static final String FAILURE="0";

    private class UploadFileTask extends AsyncTask<Void, Void, Boolean> {
        private byte[] data = null;

        private UploadFileTask(byte[] data) {
            super();
            this.data = data;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String RequestURL = "http://120.27.109.190:8003/api/upload/";
            String BOUNDARY = UUID.randomUUID().toString(); //边界标识 随机生成
            String PREFIX = "--", LINE_END = "\r\n";
            String CONTENT_TYPE = "multipart/form-data"; //内容类型
            Log.i("MainActivity", "Start to upload file......");
            try {
                URL url = new URL(RequestURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(TIME_OUT);
                conn.setConnectTimeout(TIME_OUT);
                conn.setDoInput(true); //允许输入流
                conn.setDoOutput(true); //允许输出流
                conn.setUseCaches(false); //不允许使用缓存
                conn.setRequestMethod("POST"); //请求方式
                conn.setRequestProperty("Charset", CHARSET);
                Log.i("MainActivity", "Set code...");
                //设置编码
                conn.setRequestProperty("connection", "keep-alive");
                conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

                if (data != null) {
                    /** * 当文件不为空，把文件包装并且上传 */
                    OutputStream outputSteam = conn.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(outputSteam);
                    StringBuffer sb = new StringBuffer();
                    sb.append(PREFIX);
                    sb.append(BOUNDARY);
                    sb.append(LINE_END);

                    String filename = String.format("capture-%d.jpg", System.currentTimeMillis());
                    Log.i("MainActivity", filename);
                    sb.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + filename + "\"" + LINE_END);
                    sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
                    sb.append(LINE_END);
                    dos.write(sb.toString().getBytes());
                    int len = data.length;
                    dos.write(data, 0, len);
                    dos.write(LINE_END.getBytes());
                    byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                    dos.write(end_data);
                    dos.flush();
                    int res = conn.getResponseCode();
                    Log.e("MainActivity", "response code:" + res);
                    if (HttpURLConnection.HTTP_OK == res) {
                        //当正确响应时处理数据
                        sb.setLength(0);
                        String readLine;
                        BufferedReader responseReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((readLine = responseReader.readLine()) != null) {
                            sb.append(readLine);
                        }
                        responseReader.close();
                        JSONObject v = new JSONObject(sb.toString());
                        String success = v.optString("success");
                        String image_path = v.optString("image_path");
                        Log.e("MainActivity", success + " " + image_path);
                        return true;
                    }
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        Button button = (Button) findViewById(R.id.stop_button);

        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (start_scanning) {
                    Log.i("MainActivity", "take picture.");
                    camera.takePicture(null, null, jpegCallback);
                    handler.postDelayed(this, 10000);
                }
            }
        };

        start_scanning = false;

        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MainActivity", "Change Status " + start_scanning.toString());
                if (start_scanning) {
                    start_scanning = false;
                    handler.removeCallbacks(runnable);
                } else {
                    start_scanning = true;
                    handler.postDelayed(runnable, 1);
                }
            }
        });

        jpegCallback = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i("MainActivity", "Get Data From Camera: " + data.length);
                UploadFileTask task = new UploadFileTask(data);
                Log.i("MainActivity", "" + "execute upload...");
                task.execute();
                /*
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(String.format("/sdcard/%d.jpg", System.currentTimeMillis()));
                    outStream.write(data);
                    outStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
                */
                /*Toast.makeText(getApplicationContext(), "Picture Saved", Toast.LENGTH_LONG).show();*/
                refreshCamera();
                try {
                    // camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void captureImage(View v) throws IOException {
        camera.takePicture(null, null, jpegCallback);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        try {
            camera.stopPreview();
        } catch (Exception e) {
        }
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    */

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
        }

        catch (RuntimeException e) {
            System.err.println(e);
            return;
        }

        Camera.Parameters param;
        param = camera.getParameters();
        param.setPreviewSize(352, 288);
        camera.setParameters(param);

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }

        catch (Exception e) {
            System.err.println(e);
            return;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}
