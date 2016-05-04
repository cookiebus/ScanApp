package com.example.snake.scanapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONObject;

public class ImageShowActivity extends AppCompatActivity {

    public ImageView imView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);
        imView = (ImageView) findViewById(R.id.image_show);

        Bundle bundle = this.getIntent().getExtras();
        final String imageUrl = bundle.getString("image_url");
        GetBitMap task = new GetBitMap(imageUrl);
        task.execute();
        // imView.setImageBitmap(task.bitMap());
    }

    private class GetBitMap extends AsyncTask<Void, Void, Boolean> {
        private String url = null;
        private Bitmap bm = null;

        private GetBitMap(String url) {
            super();
            this.url = url;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            this.bm = getHttpBitmap(url);
            Log.i("ImageShowActivity", "Finish doInbackground.");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i("ImageShowActivity", "onPostExecute: " + result.toString() );
            imView.setImageBitmap(bm);
        }

        public Bitmap bitMap() {
            return this.bm;
        }
        public Bitmap getHttpBitmap(String url) {
            URL myFileUrl = null;
            Bitmap bitmap = null;
            try {
                Log.d("ImageShowActivity", url);
                myFileUrl = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setConnectTimeout(0);
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("ImageShowActivity", "get BitMap finish!");
            return bitmap;
        }
    }
}
