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
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.JsonReader;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public static String[] votes;
    public static int urlIndex = 0;
    private String imgId;
    private String fakes;
    private String reals;
    private String confid;
    public static String URL1;
    public static boolean stopVoting = false;
    private TextView textView;
    private boolean isLast;
    public static ImageView overlayImg;


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
        //controller.setOnTouchListener(this);
        new GetVotes().execute(URL);
        new DownloadImage().execute(URL);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            if(motionEvent.getX() > controller.getLeft() && motionEvent.getX() < controller.getRight() && motionEvent.getY() > controller.getTop()){
                this.controllerTouched = true;
               // Toast.makeText(Voter.this, motionEvent.getX() + "," + motionEvent.getY() + ", " + controller.getTop() + "-" + controller.getBottom(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(Voter.this, this.isReal + ", confidence: " + this.confidence, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(Voter.this, "TESHT", Toast.LENGTH_SHORT).show();
                    this.imgId = "1";
                    this.confid = "" + this.confidence;
                    this.URL1 = "http://medicinina.me/augmented/updateVotes.php" + "?operation=insert&imageId=" + Voter.urlIndex + "&isFake=" + this.fakes + "&isReal=" + this.reals + "&confidence=" + this.confid;
                    System.out.println(URL1);
                    if(!Voter.stopVoting){
                        new GetUrlContentTask().execute(URL1);
                        new DownloadImage().execute(URL);
                    }
                    String[] URL2 = new String[]{};

                    new GetSpecific().execute(URL2);
                }

                if(urlIndex == urls.length) {
                    this.isLast = true;
                }

                Bundle newBundle = new Bundle();
                newBundle.putBoolean("thinksIsReal", this.isReal);
                newBundle.putInt("thinksWithConfidence", this.confidence);
                newBundle.putInt("fakeThinkersPercent", 10);
                newBundle.putInt("fakeThinkersConfidence", 10);
                newBundle.putInt("realThinkersPercent", 10);
                newBundle.putInt("realThinkersConfidence", 10);
                newBundle.putBoolean("isActuallyReal", true);
                newBundle.putBoolean("isLastImage", this.isLast);
                this.showFragment(newBundle);

                controller.setX(this.initialX);
                controller.setY(this.initialY);
                break;
        }

        return true;
    }

    public void showFragment(Bundle bundle) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        ResultFragment resultFragment = new ResultFragment();
        resultFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.fragment_container, resultFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

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
                    System.out.println(line);
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
            mProgressDialog.dismiss();
            if(Voter.urlIndex < Voter.urls.length - 1 ){
                Voter.urlIndex++;
            } else {
                Voter.stopVoting = true;
            }
        }
    }

    private class GetUrlContentTask extends AsyncTask<String, Integer, String> {
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
            System.out.println(result);
        }
    }

    private class GetVotes extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... URL) {
            String line = "";
            try {
                URL url = new URL("http://medicinina.me/augmented/GetVotesData.php");
                HttpURLConnection mUrlConnection = (HttpURLConnection) url.openConnection();
                mUrlConnection.setDoInput(true);
                InputStream is = new BufferedInputStream(mUrlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder total = new StringBuilder();
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                    System.out.println(line);
                }
                Voter.votes = total.toString().split(" ");
                System.out.println("TEST");
                System.out.println(votes.length);
                String total2 = total.substring(1, total.length()-1);
                total2 = total2.replace("},{", "}@{" );
                Voter.votes = total2.split("@");
                for(int i = 0; i< votes.length; i++) {
                    System.out.println(votes[i]);
                }
                JSONObject obj = new JSONObject();
                for(int i = 0; i< votes.length; i++) {
                    obj = new JSONObject(votes[i]);
                    System.out.println(obj.getString("confidence"));
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return line;
        }
    }

    private class GetSpecific extends AsyncTask<String, Void, String> {
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
                    System.out.println(line);
                }
                Voter.votes = total.toString().split(" ");
                System.out.println("TEST");
                // String total2 = total.substring(1, total.length()-1);
                // total2 = total2.replace("},{", "}@{" );
                // Voter.votes = total2.split("@");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return line;
        }
    }


    @Override
    public void onBackPressed() {
        Voter.urlIndex = 0;
        super.onBackPressed();
    }
}