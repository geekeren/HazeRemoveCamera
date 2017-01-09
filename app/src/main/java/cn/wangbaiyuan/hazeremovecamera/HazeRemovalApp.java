package cn.wangbaiyuan.hazeremovecamera;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by BrainWang on 2017/1/1.
 */

public class HazeRemovalApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
