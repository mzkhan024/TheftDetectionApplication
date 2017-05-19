package com.microtree.www.theftdetectionapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    static String TAG = "MZK_APP";
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private Camera mCamera;
    CamPreview camPreview;
    WindowManager wm;

    private Session session;
    String SUBJECT= "MAIL FROM THEFT APP";
    String MAIL_MSG, EMAIL;
    String FIlE, LAT, LONG;
    SharedPreferences sharedPreferences;




    public MyService(){

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getBaseContext(), "Service Started", Toast.LENGTH_LONG).show();
        sharedPreferences = getSharedPreferences(Constants.SharedPref.PrefName, MODE_PRIVATE);
        EMAIL = sharedPreferences.getString(Constants.SharedPref.EMAIL, "");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("HardwareIds")
        String serial = tm.getSimSerialNumber();
        Log.d(TAG, serial);
        if (sharedPreferences.getBoolean(Constants.SharedPref.STATUS, false)){
            if (sharedPreferences.getString(Constants.SharedPref.SIM_SERIAL_NUMBER, "").equals(serial)){
                stopSelf();
                Toast.makeText(getBaseContext(), "Service Stop Same SIM", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Service stop same sim");
            }else{
                TakePic();
            }
        }else {
            stopSelf();
            Toast.makeText(getBaseContext(), "Service Stop Status Not Active", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Service Stop its Deactivated");
        }


        return super.onStartCommand(intent, flags, startId);

    }
    private void TakePic(){
        Toast.makeText(getBaseContext(), "Taking Pic", Toast.LENGTH_LONG).show();
        StartCamera();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (CheckNetworkConnection.isConnectionAvailable(getBaseContext())) {
                    mCamera.takePicture(null, null, mPicture);
                }else{
                    releaseCamera();
                    try_again();

                }
            }
        }, 2000);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Started");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission not allowed");
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null){
            LAT = String.valueOf(mLastLocation.getLatitude());
            LONG = String.valueOf(mLastLocation.getLongitude());
            Log.d(TAG, "Latitude : "+String.valueOf(mLastLocation.getLatitude()));
            Log.d(TAG, "Longitude : "+String.valueOf(mLastLocation.getLongitude()));

        }else {
            Log.d(TAG,"Location Null");

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"Location Suspended");


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"Location: "+connectionResult.getErrorMessage());


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        Log.d(TAG, "Service Destroyed");
    }

    private void StartCamera(){
        mCamera = getCameraInstance();
        camPreview = new CamPreview(getBaseContext(),mCamera);
        wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSPARENT);

        params.height = 1;
        params.width = 1;
        wm.addView(camPreview, params);

    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(1);
        }
        catch (Exception e){
            e.printStackTrace();

        }
        return c;
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
        wm.removeView(camPreview);
    }



    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile();

            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                releaseCamera();
                FIlE = pictureFile.getAbsolutePath();
                if (LAT!=null) {
                    MAIL_MSG = "Location of the device: " +
                            "\n\nLink: " + "http://www.google.com/maps/place/" + LAT + "," + LONG + " \n\n";
                }else {
                    MAIL_MSG = "Location not available";
                }
                Toast.makeText(getBaseContext(), "Sending Mail", Toast.LENGTH_LONG).show();
                new SendMail().execute();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    private void try_again(){
        Log.d(TAG, "try again after 10 min");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (CheckNetworkConnection.isConnectionAvailable(getBaseContext())) {
                    TakePic();
                }else{
                    try_again();
                }
            }
        }, 600000);

    }

    private static Uri getOutputMediaFileUri(){
        return Uri.fromFile(getOutputMediaFile());
    }

    private static File getOutputMediaFile(){


        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyTheftApp");
        Log.d(TAG, mediaStorageDir.getAbsolutePath());

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        Log.d(TAG, "IMAGE: "+mediaFile.getAbsolutePath());


        return mediaFile;
    }

    private class SendMail extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Properties property = new Properties();
            property.put("mail.smtp.host", "bh-28.webhostbox.net");
            property.put("mail.smtp.socketFactory.port", "465");
            property.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            property.put("mail.smtp.auth", "true");
            property.put("mail.smtp.port", "465");


            session = Session.getDefaultInstance(property,
                    new javax.mail.Authenticator() {

                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(Constants.Email.SENDER, Constants.Email.PASSWORD);
                        }
                    });

            try {


                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(Constants.Email.SENDER));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL));
                message.setSubject(SUBJECT);
                MimeMultipart multipart = new MimeMultipart("related");
                BodyPart messageBodyPart = new MimeBodyPart();
                String htmlText = "<H4>"+MAIL_MSG+"</H4><img src=\"cid:image\" style=\"width:600px\">";
                messageBodyPart.setContent(htmlText, "text/html");
                multipart.addBodyPart(messageBodyPart);
                messageBodyPart = new MimeBodyPart();
                DataSource fds = new FileDataSource(FIlE);
                messageBodyPart.setDataHandler(new DataHandler(fds));
                messageBodyPart.setHeader("Content-ID", "<image>");
                multipart.addBodyPart(messageBodyPart);
                message.setContent(multipart);
                Transport.send(message);
                Log.d(TAG, "Sending Mail to User");

            } catch (MessagingException e) {
                Mail();
                e.printStackTrace();
            }


            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "Mail Sent");
            Toast.makeText(getBaseContext(), "Mail Sent", Toast.LENGTH_LONG).show();
            stopSelf();


        }
    }
    private void Mail(){
        new SendMail2().execute();
    }

    private class SendMail2 extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Sending Mail... Again");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Properties property = new Properties();
            property.put("mail.smtp.host", "smtp.gmail.com");
            property.put("mail.smtp.socketFactory.port", "465");
            property.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            property.put("mail.smtp.auth", "true");
            property.put("mail.smtp.port", "465");


            session = Session.getDefaultInstance(property,
                    new javax.mail.Authenticator() {

                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(Constants.Email.SENDER, Constants.Email.PASSWORD);
                        }
                    });

            try {

                MimeMessage mime = new MimeMessage(session);
                mime.setFrom(new InternetAddress(Constants.Email.SENDER));
                mime.addRecipient(Message.RecipientType.TO, new InternetAddress(EMAIL));
                mime.setSubject(SUBJECT);
                mime.setText(MAIL_MSG+" \n\nPicture Not Available From This Device");
                Transport.send(mime);
            } catch (MessagingException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "Sent Successfully");
            stopSelf();
        }
    }



}
