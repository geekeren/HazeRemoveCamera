package cn.wangbaiyuan.hazeremovecamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import cn.wangbaiyuan.hazeremovecamera.filter.HazeRemoveFilter;
import cn.wangbaiyuan.widget.CameraView;



public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("HazeRemoval");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button  btnImageView =(Button)findViewById(R.id.btnImageView);
        btnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,ImageViewActivity.class));
            }
        });
        CameraView cameraView = (CameraView)findViewById(R.id.cameraView);
        cameraView.bindActivity(this);
        cameraView.startCamera();


    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
