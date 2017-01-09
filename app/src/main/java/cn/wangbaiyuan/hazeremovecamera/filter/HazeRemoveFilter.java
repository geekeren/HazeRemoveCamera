
package cn.wangbaiyuan.hazeremovecamera.filter;

import android.graphics.Bitmap;

import cn.hazeremovecamera.jar.HazeRemoval;


/**
 * Created by BrainWang on 2016/12/31.
 */

public class HazeRemoveFilter {

    public static final int PROCESS_MODE_NATIVE = 0;
    public static final int PROCESS_MODE_JAVA = 1;

    public int mImageWidth;
    public int mImageHeight;
    private Bitmap inImage;
    private int[][] inPixels;
    private double[][] guideImage;

    public HazeRemoveFilter(Bitmap inImage) {
        this.inImage = inImage;
        mImageHeight = inImage.getHeight();
        mImageWidth = inImage.getWidth();
        inPixels = new int[mImageHeight][mImageWidth];
        guideImage = new double[mImageHeight][mImageWidth];
    }

    public void process(int mode) {


        for (int i = 0; i < mImageHeight; i++) {
            inImage.getPixels(inPixels[i], 0, mImageWidth, 0, i, mImageWidth, 1);

        }

        int[][] outPixels = new int[0][0];
        if(mode==PROCESS_MODE_NATIVE)
            outPixels = processImage(inPixels, mImageWidth, mImageHeight);
        else if(mode == PROCESS_MODE_JAVA) 
            outPixels = processImageByJava(inPixels);
        for (int i = 0; i < mImageHeight; i++) {
            inImage.setPixels(outPixels[i], 0, mImageWidth, 0, i, mImageWidth, 1);

//            int[] data = new int[mImageWidth*mImageHeight];
//            inImage.getPixels(data,0,mImageWidth,0,0,mImageWidth,mImageHeight);
        }

    }

    private native int[][] processImage(int[][] inPixels, int mImageWidth, int mImageHeight);

    private  int[][] processImageByJava(int[][] inPixels){
        HazeRemoval hazeRemoval = new HazeRemoval(inPixels);
        hazeRemoval.process();
        return hazeRemoval.getPixels();
    }
    public Bitmap getBitmap() {
//        int[] data = new int[mImageWidth*mImageHeight];
//        inImage.getPixels(data,0,mImageWidth,0,0,mImageWidth,mImageHeight);
        return inImage;
    }

}
