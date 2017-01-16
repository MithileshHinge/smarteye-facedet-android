package com.example.sibhali.facedet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;

import java.io.FileInputStream;

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
        ToggleButton xB = (ToggleButton) findViewById(R.id.xB);
        assert xB != null;

        Intent intent = getIntent();
        String imageName = intent.getStringExtra("image_name");
        if (imageName != null){
            frame = getImageBitmap(context, imageName);
            jIV.setImageBitmap(frame);
        }

        xB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    servername = jIP.getText().toString();
                    SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("Pref_IP", servername);
                    editor.commit();

                    Intent intent = new Intent(getBaseContext(), NotifyService.class);
                    MainActivity.this.startService(intent);
                }else {
                    Intent intent = new Intent();
                    intent.setAction(NotifyService.ACTION);
                    intent.putExtra("RQS", NotifyService.RQS_STOP_SERVICE);
                    sendBroadcast(intent);
                }
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

    public Bitmap getImageBitmap(Context context,String name){
        name=name+".jpg";
        try{
            FileInputStream fis = context.openFileInput(name);
            Bitmap b = BitmapFactory.decodeStream(fis);
            fis.close();
            return b;
        }
        catch(Exception e){
        }
        return null;
    }


}
