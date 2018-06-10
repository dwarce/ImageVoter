package com.example.razpe.imagevoter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


public class Voter extends Activity implements View.OnTouchListener, ResultFragment.OnFragmentInteractionListener {

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
    private boolean controllerTouched = false;
    public static String[] urls;
    public static int urlIndex = 0;
    private String imgId;
    private String fakes;
    private String reals;
    private String confid;
    public static String URL1;
    public static boolean stopVoting = false;
    private TextView textView;
    private boolean isLast;
    public static boolean fragmentOpen = false;
    public static ImageView overlayImg;
    public static int percentOfFakes = -1;
    public static int percentOfReals = -1;
    public static int fakeConfidencePercent = -1;
    public static int realConfidencePercent = -1;
    public static int isActuallyReal = -1;
    public static  Bitmap currentImage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter);
        image =  findViewById(R.id.imageView);
        controller = findViewById(R.id.imageView2);
        controller.getX();
        controller.getY();
        this.textView = findViewById(R.id.textView);
        this.overlayImg = findViewById(R.id.imageView4);
        FrameLayout layout = findViewById(R.id.frameLayout);
        layout.setOnTouchListener(this);
        new DownloadImage().execute(URL);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(this.fragmentOpen) {
            return true;
        }
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            if(motionEvent.getX() > controller.getLeft() && motionEvent.getX() < controller.getRight() && motionEvent.getY() > controller.getTop()){
                this.controllerTouched = true;
            }
        }
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
                if(this.moving && this.controllerTouched){
                    x = motionEvent.getX();
                    y = motionEvent.getY();
                    controller.setX(x - controller.getWidth()/2);
                    controller.setY(y - controller.getHeight()/2);
                    this.endX = motionEvent.getX() - controller.getWidth()/2;
                    this.endY = motionEvent.getY() - controller.getHeight()/2;
                    if(this.endX > this.initialX) {
                        this.isReal = true;
                        this.overlayImg.setBackgroundResource(R.drawable.stamp_real);

                    } else {
                        this.isReal = false;
                        this.overlayImg.setBackgroundResource(R.drawable.stamp_fake);
                    }
                    this.confidence = (int) Math.ceil((this.initialY - this.endY) / initialY  *100);
                    if(this.confidence > 100) {
                        this.confidence = 100;
                    }else if(this.confidence < 0) {
                        this.confidence = 0;
                    }
                    this.textView.setText("Confidence: " + this.confidence);

                }
                break;
            case MotionEvent.ACTION_UP:
                this.textView.setText("");
                this.endX = motionEvent.getX() - controller.getWidth()/2;
                this.endY = motionEvent.getY()  - controller.getHeight()/2;
                this.confidence = (int) Math.ceil((this.initialY - this.endY) / initialY  *100);
                if(this.confidence > 100) {
                    this.confidence = 100;
                }else if(this.confidence < 0) {
                    this.confidence = 0;
                }
                this.confid += this.confidence;
                this.moving = false;

                if(this.endX > this.initialX) {
                    this.isReal = true;
                    this.fakes = "0";
                    this.reals = "1";
                } else {
                    this.isReal = false;
                    this.fakes = "1";
                    this.reals = "0";
                }
                if(this.controllerTouched){
                    this.imgId = "1";
                    this.confid = "" + this.confidence;
                    this.URL1 = "http://medicinina.me/augmented/updateVotes.php" + "?operation=insert&imageId=" + Voter.urlIndex + "&isFake=" + this.fakes + "&isReal=" + this.reals + "&confidence=" + this.confid;
                    if(!Voter.stopVoting){
                        new UpdateVotes().execute(URL1);
                    }
                    String urlToGet = "http://medicinina.me/augmented/GetSpecificVotes.php" + "?operation=getPercentOfFake&imageId=" + Voter.urlIndex;
                    String[] URL2 = urlToGet.split("@");
                    new getPercentOfFakes().execute(URL2);

                    urlToGet = "http://medicinina.me/augmented/GetSpecificVotes.php" + "?operation=getConfidenceOfFake&imageId=" + Voter.urlIndex;
                    URL2 = urlToGet.split("@");
                    new getConfidenceOfFakes().execute(URL2);

                    urlToGet = "http://medicinina.me/augmented/GetSpecificVotes.php" + "?operation=getConfidenceOfReal&imageId=" + Voter.urlIndex;
                    URL2 = urlToGet.split("@");
                    new getConfidenceOfReals().execute(URL2);

                    urlToGet = "http://medicinina.me/augmented/GetSpecificVotes.php" + "?operation=checkImageReal&imageId=" + Voter.urlIndex;
                    URL2 = urlToGet.split("@");
                    new checkImageReal().execute(URL2);

                }
                if(urlIndex == urls.length - 1) {
                    this.isLast = true;
                    System.out.println("THIS IS LAST IMAGE");
                }
                this.showFragment();
                controller.setX(this.initialX);
                controller.setY(this.initialY);
                break;
        }

        return true;
    }

    public void showFragment() {
        System.out.println(Voter.percentOfFakes + " " +  Voter.percentOfReals + " " + Voter.fakeConfidencePercent + " " + Voter.realConfidencePercent + " " + Voter.isActuallyReal);
        if(Voter.percentOfFakes != -1 && Voter.percentOfReals != -1 && Voter.fakeConfidencePercent != -1 && Voter.realConfidencePercent != -1 && Voter.isActuallyReal != -1){
            Bundle bundle = new Bundle();
            bundle.putBoolean("thinksIsReal", this.isReal);
            bundle.putInt("thinksWithConfidence", this.confidence);
            bundle.putInt("fakeThinkersPercent", Voter.percentOfFakes);
            bundle.putInt("fakeThinkersConfidence", Voter.fakeConfidencePercent);
            bundle.putInt("realThinkersPercent", Voter.percentOfReals);
            bundle.putInt("realThinkersConfidence", Voter.realConfidencePercent);
            bundle.putInt("isActuallyReal", Voter.isActuallyReal);
            bundle.putBoolean("isLastImage", this.isLast);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);
            ResultFragment resultFragment = new ResultFragment();
            resultFragment.setArguments(bundle);
            fragmentTransaction.add(R.id.fragment_container, resultFragment);
            fragmentTransaction.commit();
            Voter.fragmentOpen = true;
            Voter.percentOfFakes = -1;
            Voter.percentOfReals = -1;
            Voter.fakeConfidencePercent = -1;
            Voter.realConfidencePercent = -1;
            Voter.isActuallyReal = -1;
        }
    }

    @Override
    public void onFragmentClosed() {
        System.out.println("FRAGMENT CLOSED, REALLY!");
        System.out.println(Voter.urlIndex);
        System.out.println(Voter.urls.length);
        this.fragmentOpen = false;
        if(this.isLast) {
            this.finish();
        }

        new DownloadImage().execute(URL);
    }


    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
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
            try {
                URL url = new URL("http://medicinina.me/augmented/getImage.php");
                HttpURLConnection mUrlConnection = (HttpURLConnection) url.openConnection();
                mUrlConnection.setDoInput(true);
                InputStream is = new BufferedInputStream(mUrlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder total = new StringBuilder();

                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }
                Voter.urls = total.toString().split(" ");


            } catch (MalformedURLException e ) {
                e.printStackTrace();
            } catch (IOException e ) {
                e.printStackTrace();
            }

            Bitmap bitmap = null;
            try {
                InputStream input = new java.net.URL(Voter.urls[Voter.urlIndex]).openStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            image.setImageBitmap(result);
            Voter.currentImage = result;
            mProgressDialog.dismiss();
            if(Voter.urlIndex < Voter.urls.length - 1 ){
                Voter.urlIndex++;
            } else {
                Voter.stopVoting = true;
            }
        }
    }

    private class UpdateVotes extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String content = "", line;
                while ((line = rd.readLine()) != null) {
                    content += line + "\n";
                }
                return content;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            Voter.overlayImg.setBackgroundResource(R.color.transparent);
        }
    }
    private class getPercentOfFakes extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... URL) {
            String line = "";
            try {
                URL url = new URL(URL[0]);
                HttpURLConnection mUrlConnection = (HttpURLConnection) url.openConnection();
                mUrlConnection.setDoInput(true);
                InputStream is = new BufferedInputStream(mUrlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder total = new StringBuilder();
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                    Voter.percentOfFakes = ((int) Float.parseFloat(line));
                    Voter.percentOfReals = 100 - percentOfFakes;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return line;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            showFragment();
        }
    }

    private class getConfidenceOfFakes extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... URL) {
            String line = "";
            try {
                URL url = new URL(URL[0]);
                HttpURLConnection mUrlConnection = (HttpURLConnection) url.openConnection();
                mUrlConnection.setDoInput(true);
                InputStream is = new BufferedInputStream(mUrlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder total = new StringBuilder();

                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                    Voter.fakeConfidencePercent = ((int) Float.parseFloat(line));
                    if(line == "" || line == null) {
                        Voter.fakeConfidencePercent = 0;
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(Voter.fakeConfidencePercent == -1){
                Voter.fakeConfidencePercent = 0;
            }
            return line;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            showFragment();
        }
    }

    private class getConfidenceOfReals extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... URL) {
            String line = "";
            try {
                URL url = new URL(URL[0]);
                HttpURLConnection mUrlConnection = (HttpURLConnection) url.openConnection();
                mUrlConnection.setDoInput(true);
                InputStream is = new BufferedInputStream(mUrlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder total = new StringBuilder();
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                    Voter.realConfidencePercent = ((int) Float.parseFloat(line));
                    if(line == "" || line == null) {
                        Voter.fakeConfidencePercent = 0;
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(Voter.fakeConfidencePercent == -1){
                Voter.fakeConfidencePercent = 0;
            }
            return line;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            showFragment();
        }
    }

    private class checkImageReal extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... URL) {
            String line = "";
            try {
                URL url = new URL(URL[0]);
                HttpURLConnection mUrlConnection = (HttpURLConnection) url.openConnection();
                mUrlConnection.setDoInput(true);
                InputStream is = new BufferedInputStream(mUrlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder total = new StringBuilder();
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                    Voter.isActuallyReal = Integer.parseInt(line);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return line;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            showFragment();
        }
    }


    @Override
    public void onBackPressed() {
        Voter.urlIndex = 0;
        super.onBackPressed();
    }
}