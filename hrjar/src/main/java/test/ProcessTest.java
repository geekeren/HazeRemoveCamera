package test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import cn.hazeremovecamera.jar.HazeRemoval;

/**
 * Created by BrainWang on 2017/1/1.
 */

public class ProcessTest {
    private BufferedImage inImage;
    private BufferedImage outImage;


    public static void main(String[] args) {
        ProcessTest ProcessTest = new ProcessTest();
        ProcessTest.readImage("in.jpg");
        int[][] inPixels = new int[ProcessTest.inImage.getHeight()][ProcessTest.inImage.getWidth()];
        for (int i = 0; i < ProcessTest.inImage.getWidth(); i++) {
            for (int j = 0; j < ProcessTest.inImage.getHeight(); j++) {
                inPixels[j][i] = ProcessTest.inImage.getRGB(i, j);
            }
        }
        HazeRemoval hazeRemoval = new HazeRemoval(inPixels);
        hazeRemoval.process();
        int[][] in =hazeRemoval.getPixels();
        //        temp = BoxFilter(guideImage,inImage.getHeight(),inImage.getWidth(),5);
        for (int i = 0; i < ProcessTest.inImage.getHeight(); i++)
            for (int j = 0; j < ProcessTest.inImage.getWidth(); j++)
                ProcessTest.outImage.setRGB(j, i, in[i][j]);
//                outImage.setRGB(j, i, ((int)(temp[i][j]) << 16)|((int)(temp[i][j]) << 8)|((int)(temp[i][j])));
        ProcessTest.saveImage("out.jpg");

    }



    private void saveImage(String fileName) {
        try {
            ImageIO.write(outImage, "jpeg", new File(fileName));
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }
    private void readImage(String fileName) {
        File imageFile = new File(fileName);
        try {
            inImage = ImageIO.read(imageFile);
            BufferedImage rgbImage = new BufferedImage(inImage.getWidth(null), inImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
            outImage = new BufferedImage(inImage.getWidth(null), inImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
            rgbImage.getGraphics().drawImage(inImage, 0, 0, inImage.getWidth(null), inImage.getHeight(null), null);
            inImage = rgbImage;
        } catch (Exception ioE) {
            ioE.printStackTrace();
        }
    }

}
