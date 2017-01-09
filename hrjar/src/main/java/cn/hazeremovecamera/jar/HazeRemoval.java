package cn.hazeremovecamera.jar;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by OptimusV5 on 2015/1/8.
 */
public class HazeRemoval {

    public int mWidth,mHeight;
    private int[][] inPixels;
    private int[][] outPixels;
    private double[][] guideImage;
    private double[][] tImage;
    private double[][] darkChanel;
    private int A;
    private final double T0 = 0.1;
    private final int MAXA = 185;
    private double Max;
    private double Min;

    public HazeRemoval(int[][] input) {

        mHeight= input.length;
        mWidth= input[0].length;
        inPixels = input;
        guideImage = new double[mHeight][mWidth];
        tImage = new double[mHeight][mWidth];
        darkChanel = new double[mHeight][mWidth];
        outPixels = new int[mHeight][mWidth];

        setInPixels();

    }

    public void process() {

        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        processDarkChanel();

        endTime = System.currentTimeMillis();
        System.out.println("processDarkChanel: " + (endTime - startTime) + "ms");
        startTime = System.currentTimeMillis();

        setA();

        endTime = System.currentTimeMillis();
        System.out.println("setA: " + (endTime - startTime) + "ms");
        startTime = System.currentTimeMillis();

        settImage();

        endTime = System.currentTimeMillis();
        System.out.println("settImage: " + (endTime - startTime) + "ms");
        startTime = System.currentTimeMillis();

        HazeFree();

        endTime = System.currentTimeMillis();
        System.out.println("HazeFree: " + (endTime - startTime) + "ms");
        startTime = System.currentTimeMillis();
    }


    public int[][] getPixels(){

        return outPixels;
    }



    private void setInPixels() {
        Max = 0;
        Min = 255;
        for (int i = 0; i < mWidth; i++) {
            for (int j = 0; j < mHeight; j++) {
                guideImage[j][i] = ((inPixels[j][i] & 0x00ff0000) >> 16) * 0.3 + ((inPixels[j][i] & 0x0000ff00) >> 8) * 0.59 + (inPixels[j][i] & 0x000000ff) * 0.11;
                if (guideImage[j][i] > Max)
                    Max = guideImage[j][i];
                if (guideImage[j][i] < Min)
                    Min = guideImage[j][i];
            }
        }
    }



    private void processDarkChanel() {
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int red = (inPixels[i][j] & 0x00ff0000) >> 16;
                int green = (inPixels[i][j] & 0x0000ff00) >> 8;
                int blue = (inPixels[i][j] & 0x000000ff);
                darkChanel[i][j] = red > green ? green : red;
                if (darkChanel[i][j] > blue)
                    darkChanel[i][j] = blue;
            }
        }
        MinFilterMatrix(darkChanel, mHeight, mWidth, 15);
    }

    private void setA() {
        int size = (int) (0.001 * mHeight * mWidth);
        Node[] array = new Node[size];
        int count = 0;
        for (int i = 0; i <= size / mWidth; i++) {
            for (int j = 0; j < mWidth; j++) {
                array[count] = new Node(i, j, (int) darkChanel[i][j]);
                if (++count == size)
                    break;
            }
        }
        Heap heap = new Heap();
        heap.BuildMinHeap(array, size);
        for (int i = size / mWidth; i < mHeight; i++) {
            if (i == size / mWidth) {
                for (int j = size % mWidth; j < mWidth; j++) {
                    if (inPixels[i][j] > heap.top())
                        heap.changeTopElem(i, j, (int) darkChanel[i][j]);
                }
            } else {
                for (int j = 0; j < mWidth; j++) {
                    if (inPixels[i][j] > heap.top())
                        heap.changeTopElem(i, j, (int) darkChanel[i][j]);
                }
            }
        }
        array = heap.getMinHeap();
        int max = 0;
        int index = 0;
        for (int i = 0; i < size; i++) {
            int red = (inPixels[array[i].getH()][array[i].getW()] & 0x00ff0000) >> 16;
            int green = (inPixels[array[i].getH()][array[i].getW()] & 0x0000ff00) >> 8;
            int blue = (inPixels[array[i].getH()][array[i].getW()] & 0x000000ff);
            if (max < red + blue + green) {
                max = red + blue + green;
                index = i;
            }
        }
        A = inPixels[array[index].getH()][array[index].getW()];
    }

    private void settImage() {
        int r = ((A & 0x00ff0000) >> 16) > MAXA ? MAXA : ((A & 0x00ff0000) >> 16);
        int g = ((A & 0x0000ff00) >> 8) > MAXA ? MAXA : ((A & 0x0000ff00) >> 8);
        int b = (A & 0x000000ff) > MAXA ? MAXA : (A & 0x000000ff);

        double max = 0;
        double min = 255;


        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                double red = ((inPixels[i][j] & 0x00ff0000) >> 16) / (double) r;
                double green = ((inPixels[i][j] & 0x0000ff00) >> 8) / (double) g;
                double blue = ((inPixels[i][j] & 0x000000ff)) / (double) b;
                tImage[i][j] = red < green ? red : green;
                if (tImage[i][j] > blue)
                    tImage[i][j] = blue;
                if (tImage[i][j] > max)
                    max = tImage[i][j];
                if (tImage[i][j] < min)
                    min = tImage[i][j];
            }
        }
        MinFilterMatrix(tImage, mHeight, mWidth, 15);

        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                tImage[i][j] = (tImage[i][j] - min) / (max - min);
                tImage[i][j] = 1 - 0.95 * tImage[i][j];
            }
        }
        tImage = GuideImageFilter(tImage, mHeight, mWidth, 105);
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                if (tImage[i][j] > max)
                    max = tImage[i][j];
                if (tImage[i][j] < min)
                    min = tImage[i][j];
            }
        }
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                tImage[i][j] = (tImage[i][j] - min) / (max - min);
            }
        }
    }

    private void HazeFree() {
        int r = ((A & 0x00ff0000) >> 16) > MAXA ? MAXA : ((A & 0x00ff0000) >> 16);
        int g = ((A & 0x0000ff00) >> 8) > MAXA ? MAXA : ((A & 0x0000ff00) >> 8);
        int b = (A & 0x000000ff) > MAXA ? MAXA : (A & 0x000000ff);
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int red = ((inPixels[i][j] & 0x00ff0000) >> 16) - r;
                int green = ((inPixels[i][j] & 0x0000ff00) >> 8) - g;
                int blue = (inPixels[i][j] & 0x000000ff) - b;
                double t = tImage[i][j] > T0 ? tImage[i][j] : T0;
                red = (int) (red / t);
                green = (int) (green / t);
                blue = (int) (blue / t);
                red += r;
                if (red > 255) red = 255;
                if (red < 0) red = 0;
                green += g;
                if (green > 255) green = 255;
                if (green < 0) green = 0;
                blue += b;
                if (blue > 255) blue = 255;
                if (blue < 0) blue = 0;
                outPixels[i][j] = ((red << 16) | (green << 8) | blue);
            }
        }
        outPixels = Color_His(outPixels,mHeight,mWidth);
        autoBright(outPixels, mHeight, mWidth);
    }

    private void MinFilterMatrix(double[][] inPixels, int height, int width, int r) {
        for (int i = 0; i < height; i++) {
            double[] g = new double[width];
            double[] h = new double[width];
            for (int j = 0; j < width - width % r; j++) {
                if (j % r == 0) {
                    g[j] = inPixels[i][j];
                    h[j + r - 1] = inPixels[i][j + r - 1];
                } else {
                    g[j] = g[j - 1] > inPixels[i][j] ? inPixels[i][j] : g[j - 1];
                    h[j + r - 2 * (j % r) - 1] = h[j + r - 2 * (j % r)] > inPixels[i][j + r - 2 * (j % r) - 1] ? inPixels[i][j + r - 2 * (j % r) - 1] : h[j + r - 2 * (j % r)];
                }
                if (j >= r)
                    inPixels[i][j - r / 2] = h[j - r + 1] > g[j] ? g[j] : h[j - r + 1];
            }
            for (int j = 0; j <= r / 2; j++) {
                inPixels[i][j] = h[0] > g[j + r / 2 - 1] ? g[j + r / 2 - 1] : h[0];
            }
            for (int j = width - width % r; j < width; j++) {
                if (j % r == 0)
                    g[j] = inPixels[i][j];
                else
                    g[j] = g[j - 1] > inPixels[i][j] ? inPixels[i][j] : g[j - 1];
            }
            for (int j = width - width % r - r / 2; j < width - r / 2; j++) {
                inPixels[i][j] = h[j - r / 2] > g[j + r / 2 - 1] ? g[j + r / 2 - 1] : h[j - r / 2];
            }
            for (int j = width - r / 2; j < width; j++) {
                inPixels[i][j] = h[j - r / 2] > g[width - 1] ? g[width - 1] : h[j - r / 2];
            }
        }
        for (int i = 0; i < width; i++) {
            double[] g = new double[height];
            double[] h = new double[height];
            for (int j = 0; j < height - height % r; j++) {
                if (j % r == 0) {
                    g[j] = inPixels[j][i];
                    h[j + r - 1] = inPixels[j + r - 1][i];
                } else {
                    g[j] = g[j - 1] > inPixels[j][i] ? inPixels[j][i] : g[j - 1];
                    h[j + r - 2 * (j % r) - 1] = h[j + r - 2 * (j % r)] > inPixels[j + r - 2 * (j % r) - 1][i] ?
                            inPixels[j + r - 2 * (j % r) - 1][i] : h[j + r - 2 * (j % r)];
                }
                if (j >= r)
                    inPixels[j - r / 2][i] = h[j - r + 1] > g[j] ? g[j] : h[j - r + 1];
            }
            for (int j = 0; j <= r / 2; j++) {
                inPixels[j][i] = h[0] > g[j + r / 2 - 1] ? g[j + r / 2 - 1] : h[0];
            }
            for (int j = height - height % r; j < height; j++) {
                if (j % r == 0)
                    g[j] = inPixels[j][i];
                else
                    g[j] = g[j - 1] > inPixels[j][i] ? inPixels[j][i] : g[j - 1];
            }
            for (int j = height - height % r - r / 2; j < height - r / 2; j++) {
                inPixels[j][i] = h[j - r / 2] > g[j + r / 2 - 1] ? g[j + r / 2 - 1] : h[j - r / 2];
            }
            for (int j = height - r / 2; j < height; j++) {
                inPixels[j][i] = h[j - r / 2] > g[height - 1] ? g[height - 1] : h[j - r / 2];
            }
        }
    }

    private double[][] GuideImageFilter(double[][] inPixels, int height, int width, int r) {
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                guideImage[i][j] = (guideImage[i][j] - Min) / (Max - Min);
            }
        }
        double[][] meanI = BoxFilter(guideImage, height, width, r);
        double[][] meanP = BoxFilter(inPixels, height, width, r);
        double[][] corrI = BoxFilter(dotProduct(guideImage, guideImage, height, width), height, width, r);
        double[][] corrIP = BoxFilter(dotProduct(guideImage, inPixels, height, width), height, width, r);
        double[][] varI = mMinus(corrI, dotProduct(meanI, meanI, height, width), height, width);
        double[][] covIP = mMinus(corrIP, dotProduct(meanI, meanP, height, width), height, width);
        double[][] a = dotDivide(covIP, mPlus(varI, 0.0001, height, width), height, width);
        double[][] b = mMinus(meanP, dotProduct(a, meanI, height, width), height, width);
        double[][] meanA = BoxFilter(a, height, width, r);
        double[][] meanB = BoxFilter(b, height, width, r);
        return mPlus(dotProduct(meanA, guideImage, height, width), meanB, height, width);
    }

    private double[][] BoxFilter(double[][] inPixels, int height, int width, int r) {
        double[][] ans = new double[height][width];
        double[][] fin = new double[height][width];
        for (int i = 0; i < height; i++) {
            double sum = 0;
            for (int j = 0; j <= r / 2; j++) {
                sum += inPixels[i][j];
            }
            for (int j = 0; j < r / 2; j++) {
                ans[i][j] = sum / (r / 2 + j + 1);
                sum += inPixels[i][j + r / 2 + 1];
            }
            ans[i][r / 2] = sum / r;
            for (int j = r / 2 + 1; j < width - r / 2; j++) {
                sum = sum - inPixels[i][j - r / 2 - 1] + inPixels[i][j + r / 2];
                ans[i][j] = sum / r;
            }
            for (int j = width - r / 2; j < width; j++) {
                sum = sum - inPixels[i][j - r / 2 - 1];
                ans[i][j] = sum / (r - j + width - r / 2 - 1);
            }
        }
        for (int i = 0; i < width; i++) {
            double sum = 0;
            for (int j = 0; j <= r / 2; j++) {
                sum += ans[j][i];
            }
            for (int j = 0; j < r / 2; j++) {
                fin[j][i] = sum / (r / 2 + j + 1);
                sum += ans[j + r / 2 + 1][i];
            }
            fin[r / 2][i] = sum / r;
            for (int j = r / 2 + 1; j < height - r / 2; j++) {
                sum = sum - ans[j - r / 2 - 1][i] + ans[j + r / 2][i];
                fin[j][i] = sum / r;
            }
            for (int j = height - r / 2; j < height; j++) {
                sum = sum - ans[j - r / 2 - 1][i];
                fin[j][i] = sum / (r - j + height - r / 2 - 1);
            }
        }
        return fin;
    }

    private double[][] dotProduct(double[][] m1, double[][] m2, int h, int w) {
        ;
        double[][] ans = new double[h][w];
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                ans[i][j] = m1[i][j] * m2[i][j];
        return ans;
    }

    private double[][] dotDivide(double[][] m1, double[][] m2, int h, int w) {
        double[][] ans = new double[h][w];
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                ans[i][j] = m1[i][j] / m2[i][j];
        return ans;
    }

    private double[][] mMinus(double[][] m1, double[][] m2, int h, int w) {
        double[][] ans = new double[h][w];
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                ans[i][j] = m1[i][j] - m2[i][j];
        return ans;
    }

    private double[][] mPlus(double[][] m, double e, int h, int w) {
        double[][] ans = new double[h][w];
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                ans[i][j] = m[i][j] + e;
        return ans;
    }

    private double[][] mPlus(double[][] m1, double[][] m2, int h, int w) {
        double[][] ans = new double[h][w];
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                ans[i][j] = m1[i][j] + m2[i][j];
        return ans;
    }

    private int[][] Color_His(int[][] input, int h, int w) {
        int[][] rImage = new int[h][w];
        int[][] gImage = new int[h][w];
        int[][] bImage = new int[h][w];
        int[] rgrays = new int[256];
        int[] ggrays = new int[256];
        int[] bgrays = new int[256];
        double[] hisImage = new double[256];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                bImage[i][j] = (0x000000ff & input[i][j]);
                bgrays[bImage[i][j]]++;
                gImage[i][j] = ((0x0000ff00 & input[i][j]) >> 8);
                ggrays[gImage[i][j]]++;
                rImage[i][j] = ((0x00ff0000 & input[i][j]) >> 16);
                rgrays[rImage[i][j]]++;
            }
        }
        for (int i = 0; i < 256; i++) {
            hisImage[i] = (double) (rgrays[i] + ggrays[i] + bgrays[i]) / 3;
        }
        double now = 0.0;
        for (int i = 0; i < 256; i++) {
            now += hisImage[i];
            hisImage[i] = 255 * now / (w * h);
        }
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                rImage[i][j] = (int) (hisImage[rImage[i][j]]);
                gImage[i][j] = (int) (hisImage[gImage[i][j]]);
                bImage[i][j] = (int) (hisImage[bImage[i][j]]);
            }
        }
        int[][] output = new int[h][w];
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                output[i][j] = ((rImage[i][j]) << 16) | ((gImage[i][j]) << 8) | (bImage[i][j]);

        return output;
    }

    private void autoBright(int[][] input, int h, int w) {
        int[][] rImage = new int[h][w];
        int[][] gImage = new int[h][w];
        int[][] bImage = new int[h][w];
        int[] rgrays = new int[256];
        int[] ggrays = new int[256];
        int[] bgrays = new int[256];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                bImage[i][j] = (0x000000ff & input[i][j]);
                bgrays[bImage[i][j]]++;
                gImage[i][j] = ((0x0000ff00 & input[i][j]) >> 8);
                ggrays[gImage[i][j]]++;
                rImage[i][j] = ((0x00ff0000 & input[i][j]) >> 16);
                rgrays[rImage[i][j]]++;
            }
        }
        colorMap(rgrays, h, w);
        colorMap(ggrays, h, w);
        colorMap(bgrays, h, w);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                rImage[i][j] = (rgrays[rImage[i][j]]);
                gImage[i][j] = (ggrays[gImage[i][j]]);
                bImage[i][j] = (bgrays[bImage[i][j]]);
            }
        }
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                input[i][j] = ((rImage[i][j]) << 16) | ((gImage[i][j]) << 8) | (bImage[i][j]);
    }

    private void colorMap(int[] arr, int h, int w) {
        int Max = 0, Min = 0, sum = 0;
        double low = 0.1;
        double high = 0.1;
        for (int i = 0; i < 256; i++) {
            sum += arr[i];
            if (sum >= w * h * low * 0.01) {
                Min = i;
                break;
            }
        }
        sum = 0;
        for (int i = 255; i >= 0; i--) {
            sum += arr[i];
            if (sum >= w * h * high * 0.01) {
                Max = i;
                break;
            }
        }
//        System.out.println(Max + " " + Min);
        for (int i = 0; i < 256; i++) {
            if (Max == Min)
                break;
            if (i < Min)
                arr[i] = 0;
            else if (i > Max)
                arr[i] = 255;
            else
                arr[i] = (i - Min) * 255 / (Max - Min);
        }
    }


    public static void main(String[] args) {
//        HazeRemoval hazeRemoval = new HazeRemoval("assets/test.jpg");
//        hazeRemoval.run("output/test.jpg");
//        for (int i = 0; i < 15; i++) {
//            HazeRemoval hazeRemoval = new HazeRemoval("E:\\input\\t" + i + ".jpg");
//            hazeRemoval.run("E:\\output\\t_" + i + "_.jpg");
//        }

    }
}
