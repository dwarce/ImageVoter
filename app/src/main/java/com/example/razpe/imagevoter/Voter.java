package com.example.razpe.imagevoter;

import java.io.InputStream;

import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Voter extends Activity implements View.OnTouchListener {

    String URL = "https://cdn.pixabay.com/photo/2017/04/05/11/56/image-in-the-image-2204798_960_720.jpg";
    ImageView image;
    ImageView controller;
    ProgressDialog mProgressDialog;
    private float initialX, initialY = 0.0f;
    private float endX, endY = 0.0f;
    private float x, y = 0.0f;
    private boolean moving;
    private boolean flag = false;
    private int confidence = 0;
    private boolean isReal;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter);
        image =  findViewById(R.id.imageView);
        controller = findViewById(R.id.imageView2);
        controller.setOnTouchListener(this);
        new DownloadImage().execute(URL);
    }



    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(!this.flag) {
                    this.initialX = this.controller.getX();
                    this.initialY = this.controller.getY();
                    this.flag = true;
                }
                this.moving = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if(this.moving){
                    x = motionEvent.getX();
                    y = motionEvent.getY();
                    controller.setX(x - controller.getWidth()/2);
                    controller.setY(y - controller.getHeight()/2);
                }
                break;
            case MotionEvent.ACTION_UP:
                this.endX = motionEvent.getX();
                this.endY = motionEvent.getY();
                this.confidence = (int) Math.ceil((this.initialY - this.endY) / initialY  *100);
                this.moving = false;
                controller.setX(this.initialX);
                controller.setY(this.initialY);

                if(this.endX > this.initialX) {
                    this.isReal = true;
                    Toast.makeText(Voter.this, "REAL, " + confidence +"% " + initialY +" " + Math.abs(endY), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Voter.this, "FAKE, " + confidence +"%", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return true;
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(Voter.this);
            mProgressDialog.setTitle("Download Image");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                InputStream input = new java.net.URL(imageURL).openStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            image.setImageBitmap(result);
            mProgressDialog.dismiss();
        }
    }
}