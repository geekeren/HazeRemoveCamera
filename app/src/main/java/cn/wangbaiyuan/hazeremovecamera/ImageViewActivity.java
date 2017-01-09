package cn.wangbaiyuan.hazeremovecamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;

import java.io.IOException;
import java.io.InputStream;


import cn.wangbaiyuan.hazeremovecamera.filter.HazeRemoveFilter;

public class ImageViewActivity extends AppCompatActivity {


    Bitmap bitmap;

    public boolean isProcessed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final Button buttonHazeRemove = (Button) findViewById(R.id.buttonHazeRemove);
        final Switch switch1 = (Switch) findViewById(R.id.switch1);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        progressBar.setVisibility(View.VISIBLE);
                        buttonHazeRemove.setEnabled(false);
                        break;
                    case 1:
                        buttonHazeRemove.setEnabled(true);
                        imageView.setImageBitmap(bitmap);
                        progressBar.setVisibility(View.GONE);
                        if (isProcessed)
                            buttonHazeRemove.setText("查看原图");
                        else
                            buttonHazeRemove.setText("去雾霾");
                        break;
                }
                super.handleMessage(msg);
            }
        };
        final Runnable openRawImg = new Runnable() {
            @Override
            public void run() {
                try {
                    handler.sendEmptyMessage(0);
                    InputStream in = getResources().getAssets().open("test.jpg");
                    bitmap = BitmapFactory.decodeStream(in);
                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    isProcessed = false;
                    handler.sendEmptyMessage(1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        HandlerThread processThread = new HandlerThread("processThread");
        processThread.start();
        final Handler handler2 = new Handler(processThread.getLooper());
        handler2.post(openRawImg);

        final Runnable processImg = new Runnable() {
            @Override
            public void run() {

                handler.sendEmptyMessage(0);
                HazeRemoveFilter hazeRemoveFilter = new HazeRemoveFilter(bitmap);
                if (switch1.isChecked())
                    hazeRemoveFilter.process(HazeRemoveFilter.PROCESS_MODE_NATIVE);
                else
                    hazeRemoveFilter.process(HazeRemoveFilter.PROCESS_MODE_JAVA);

                bitmap = hazeRemoveFilter.getBitmap();
                isProcessed = true;
                handler.sendEmptyMessage(1);


            }
        };
        buttonHazeRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isProcessed)
                    handler2.post(processImg);

                else
                    handler2.post(openRawImg);


            }
        });
    }
}
