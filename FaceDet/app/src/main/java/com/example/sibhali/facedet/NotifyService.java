package com.example.sibhali.facedet;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sibhali on 12/19/2016.
 */
public class NotifyService extends Service {

    final static String ACTION = "NotifyServiceAction";
    final static String STOP_SERVICE = "";
    final static int RQS_STOP_SERVICE = 1;

    NotifyServiceReceiver notifyServiceReceiver;

    private static final int MY_NOTIFICATION_ID = 1;
    private static final int MY_WARNING_ID2 = 3;
    //public static String servername;
    private NotificationManager notificationManager;

    private Bitmap frame;


    private static Thread t;

    @Override
    public void onCreate() {
        notifyServiceReceiver = new NotifyServiceReceiver();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(notifyServiceReceiver, intentFilter);

        //Send notification

        final Context context = getApplicationContext();
        String notifTitle = "Someone's at your door!";
        String notifText = "";

        Context warning_context2 = getApplicationContext();
        String warnTitle2 = "Suspicious activity";
        String warnText2 = "Alert!";

        final NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notif).setContentTitle(notifTitle).setContentText(notifText).setAutoCancel(false);
        final NotificationCompat.Builder warnBuilder2 = new NotificationCompat.Builder(warning_context2).setSmallIcon(R.drawable.ic_warn).setContentTitle(warnTitle2).setContentText(warnText2).setAutoCancel(false);

        notifBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        warnBuilder2.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Toast.makeText(this, "Notification service started", Toast.LENGTH_LONG).show();

        final Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);

        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);

        SharedPreferences sp = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        final String servername = sp.getString("Pref_IP", "0");
        Log.d("servername", servername);

        final Handler handler = new Handler();

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Socket client = new Socket(servername, 6667);
                        System.out.println("CONNECTED "+"to " + servername);
                        InputStream in = client.getInputStream();
                        //OutputStream out = client.getOutputStream();
                        int p = in.read();
                        System.out.println(p);
                        client.close();

                        try {
                            Socket socket_frame = new Socket(servername, 6666);
                            System.out.println("RECEIVING FRAMES");

                            InputStream in_frame = socket_frame.getInputStream();
                            frame = BitmapFactory.decodeStream(new FlushedInputStream(in_frame));
                        }catch (IOException e1){
                            e1.printStackTrace();
                        }

                        System.out.println("FRAME RECEIVED");

                        String imageName = getCurrentTimeStamp();
                        saveImage(context, frame, imageName);

                        myIntent.putExtra("image_name", imageName);
                        stackBuilder.addNextIntent(myIntent);

                        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                        NotificationCompat.BigPictureStyle bps = new NotificationCompat.BigPictureStyle().bigPicture(frame);
                        notifBuilder.setStyle(bps);
                        warnBuilder2.setStyle(bps);

                        notifBuilder.setContentIntent(pendingIntent);
                        warnBuilder2.setContentIntent(pendingIntent);

                        if (MainActivity.jIV != null){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.jIV.setImageBitmap(frame);
                                }
                            });
                        }

                        if (p == 1) {
                            notificationManager.notify(MY_NOTIFICATION_ID, notifBuilder.build());
                        }

                        if (p == 3) {
                            System.out.println("WARNING.......");
                            notificationManager.notify(MY_WARNING_ID2, warnBuilder2.build());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("DESTROYEDDD!", "HAHAHA!");
        Toast.makeText(NotifyService.this, "Notification service stopped", Toast.LENGTH_SHORT).show();
        this.unregisterReceiver(notifyServiceReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class NotifyServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int rqs = intent.getIntExtra("RQS", 0);
            if (rqs == RQS_STOP_SERVICE){
                stopSelf();
            }
        }
    }

    public void saveImage(Context context, Bitmap b,String name){
        name=name+".jpg";
        FileOutputStream out;
        try {
            out = context.openFileOutput(name, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
