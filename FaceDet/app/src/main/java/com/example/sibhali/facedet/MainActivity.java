package com.example.sibhali.facedet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    public static ImageView jIV;
    public static Context context;
    public static EditText jIP;
    public static Bitmap frame = null;
    public static String servername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String lastIP = sp.getString("Pref_IP", "");
        jIV = (ImageView) findViewById(R.id.xIV);
        jIP = (EditText) findViewById(R.id.xIP);
        jIP.setText(lastIP);
        Button xB = (Button) findViewById(R.id.xB);
        final Button xBStop = (Button) findViewById(R.id.xBStop);
        assert xB != null;
        assert xBStop != null;


        xB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                servername = jIP.getText().toString();
                SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("Pref_IP", servername);
                editor.commit();

                Intent intent = new Intent(getBaseContext(), com.example.sibhali.facedet.NotifyService.class);
                MainActivity.this.startService(intent);

                xBStop.setVisibility(View.VISIBLE);

            }
        }
        );

        xBStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(NotifyService.ACTION);
                intent.putExtra("RQS", NotifyService.RQS_STOP_SERVICE);
                sendBroadcast(intent);
            }
        });
    }
    @Override
    protected void onStop() {
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (jIP == null) editor.putString("Pref_IP", servername);
        else editor.putString("Pref_IP", jIP.getText().toString());
        editor.commit();
        super.onStop();
    }



}
