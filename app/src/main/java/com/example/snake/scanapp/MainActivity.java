package com.example.snake.scanapp;

import android.graphics.Canvas;
import android.net.Uri;
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
import android.hardware.Camera.Size;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import android.util.DisplayMetrics;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;


public class MainActivity extends Activity implements SurfaceHolder.Callback {

    public Camera camera;
    public SurfaceView surfaceView;
    public SurfaceHolder surfaceHolder;
    public Button button;
    public ImageView img;

    public PictureCallback rawCallback;
    public ShutterCallback shutterCallback;
    public PictureCallback jpegCallback;
    public Camera.AutoFocusCallback mAutoFocusCallback;

    public Boolean start_scanning = false;
    private TimerTask mTimerTask;
    private Timer mTimer;

    private static final int TIME_OUT = 10 * 10000000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    public static final String SUCCESS = "1";
    public static final String FAILURE = "0";
    public DisplayMetrics dm = null;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.snake.scanapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.snake.scanapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /*
    private class CameraTimerTask extends TimerTask {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if(camera != null) {
                camera.autoFocus(mAutoFocusCallback);
            }
        }
    }
    */
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
                        String image_url = "http://120.27.109.190:8002/media/" + image_path;
                        if (success.equals("true") && image_path.length() > 0 && start_scanning) {
                            // Bitmap bm = getHttpBitmap(image_url);
                            // camera.stopPreview();
                            start_scanning = false;
                            Intent intent = new Intent(MainActivity.this, ImageShowActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("image_url", image_url);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                        Log.e("MainActivity", success + " " + image_url);
                        return true;
                    }
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                button.setText("Start");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        button = (Button) findViewById(R.id.stop_button);

        surfaceHolder = surfaceView.getHolder();

        dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

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
        /*
        mAutoFocusCallback = new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                // TODO Auto-generated method stub
                if(success) {
                    Log.i("MainActivity", "take picture.");
                    camera.takePicture(null, null, jpegCallback);
                    // camera.setOneShotPreviewCallback(null);
                    // Toast.makeText(getApplicationContext(), "自动聚焦成功" , Toast.LENGTH_SHORT).show();
                }
            }
        };
        */

        start_scanning = false;

        // mTimer = new Timer();
        // mTimerTask = new CameraTimerTask();
        // mTimer.schedule(mTimerTask, 0, 10);

        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MainActivity", "Change Status " + start_scanning.toString());
                if (start_scanning) {
                    start_scanning = false;
                    button.setText("Start");
                    handler.removeCallbacks(runnable);
                } else {
                    start_scanning = true;
                    button.setText("Stop");
                    handler.postDelayed(runnable, 4000);
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
                Toast.makeText(getApplicationContext(), "Post Success!", Toast.LENGTH_LONG).show();
                refreshCamera();
                try {
                    // camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

    //相机参数的初始化设置
    private void initCamera() {
        Camera.Parameters parameters = camera.getParameters();
        //parameters.setPictureSize(surfaceView.getWidth(), surfaceView.getHeight());  // 部分定制手机，无法正常识别该方法。
        // parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
        camera.setParameters(parameters);
        camera.startPreview();
        camera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上

    }

    public void setPreviewSize(Camera.Parameters parametes) {
        List localSizes = parametes.getSupportedPreviewSizes();
        Size biggestSize = null;
        Size fitSize = null;// 优先选屏幕分辨率
        Size targetSize = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(大)的size
        Size targetSiz2 = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(小)的size
        if (localSizes != null) {
            int cameraSizeLength = localSizes.size();
            for (int n = 0; n < cameraSizeLength; n++) {
                Size size = (Size) localSizes.get(n);
                if (biggestSize == null ||
                        (size.width >= biggestSize.width && size.height >= biggestSize.height)) {
                    biggestSize = size;
                }

                // DisplayMetrics dm = new DisplayMetrics();
                // SurfaceView.getWindowManager().getDefaultDisplay().getMetrics(dm);

                int screenWPixels = dm.widthPixels;
                int screenHPixels = dm.heightPixels;

                if (size.width == screenHPixels
                        && size.height == screenWPixels) {
                    fitSize = size;
                } else if (size.width == screenHPixels
                        || size.height == screenWPixels) {
                    if (targetSize == null) {
                        targetSize = size;
                    } else if (size.width < screenHPixels
                            || size.height < screenWPixels) {
                        targetSiz2 = size;
                    }
                }
            }

            if (fitSize == null) {
                fitSize = targetSize;
            }

            if (fitSize == null) {
                fitSize = targetSiz2;
            }

            if (fitSize == null) {
                fitSize = biggestSize;
            }
            parametes.setPreviewSize(fitSize.width, fitSize.height);
        }

    }

    /**
     * 输出的照片为最高像素
     */
    public void setPictureSize(Camera.Parameters parametes) {
        List localSizes = parametes.getSupportedPictureSizes();
        Size biggestSize = null;
        Size fitSize = null;// 优先选预览界面的尺寸
        Size previewSize = parametes.getPreviewSize();
        float previewSizeScale = 0;
        if (previewSize != null) {
            previewSizeScale = previewSize.width / (float) previewSize.height;
        }

        if (localSizes != null) {
            int cameraSizeLength = localSizes.size();
            for (int n = 0; n < cameraSizeLength; n++) {
                Size size = (Size) localSizes.get(n);
                if (biggestSize == null) {
                    biggestSize = size;
                } else if (size.width >= biggestSize.width && size.height >= biggestSize.height) {
                    biggestSize = size;
                }

                // 选出与预览界面等比的最高分辨率
                if (previewSizeScale > 0
                        && size.width >= previewSize.width && size.height >= previewSize.height) {
                    float sizeScale = size.width / (float) size.height;
                    if (sizeScale == previewSizeScale) {
                        if (fitSize == null) {
                            fitSize = size;
                        } else if (size.width >= fitSize.width && size.height >= fitSize.height) {
                            fitSize = size;
                        }
                    }
                }
            }

            // 如果没有选出fitSize, 那么最大的Size就是FitSize
            if (fitSize == null) {
                fitSize = biggestSize;
            }

            parametes.setPictureSize(fitSize.width, fitSize.height);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            System.err.println(e);
            return;
        }

        Camera.Parameters param;
        param = camera.getParameters();
        param.getSupportedPictureSizes();
        // param.setPreviewSize(352, 288);
        setPreviewSize(param);
        setPictureSize(param);

        camera.setParameters(param);
        camera.setDisplayOrientation(90);

        try {
            camera.setPreviewDisplay(holder);
            initCamera();
            camera.startPreview();
        } catch (Exception e) {
            System.err.println(e);
            return;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    initCamera();//实现相机的参数初始化
                    camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                }
            }
        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}