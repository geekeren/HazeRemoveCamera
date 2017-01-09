#include "HazeRemoveFilter.h"
#include "Heap.h"


void HazeRemoveFilter::processDarkChanel() {
    darkChanel = new double *[mImageHeight];
    for (int i = 0; i < mImageHeight; i++) {
        darkChanel[i] = new double[mImageWidth];
        for (int j = 0; j < mImageWidth; j++) {
            int red = (inPixels[i][j] & 0x00ff0000) >> 16;
            int green = (inPixels[i][j] & 0x0000ff00) >> 8;
            int blue = (inPixels[i][j] & 0x000000ff);
            darkChanel[i][j] = red > green ? green : red;
            if (darkChanel[i][j] > blue)
                darkChanel[i][j] = blue;
            if (guideImage[i][j] > Max)
                Max = guideImage[i][j];
            if (guideImage[i][j] < Min)
                Min = guideImage[i][j];
        }
    }
    minFilterMatrix(darkChanel, mImageHeight, mImageWidth, 15);
}

void HazeRemoveFilter::setA() {
    int size = (int) (0.001 * mImageHeight * mImageWidth);
    Node *array = new Node[size];
    int count = 0;
    for (int i = 0; i <= size / mImageWidth; i++) {
        for (int j = 0; j < mImageWidth; j++) {
            array[count] = Node(i, j, (int) darkChanel[i][j]);
            if (++count == size)
                break;
        }
    }
    Heap heap;
    heap.BuildMinHeap(array, size);
    for (int i = size / mImageWidth; i < mImageHeight; i++) {
        if (i == size / mImageWidth) {
            for (int j = size % mImageWidth; j < mImageWidth; j++) {
                if (inPixels[i][j] > heap.top())
                    heap.changeTopElem(i, j, (int) darkChanel[i][j]);
            }
        }
        else {
            for (int j = 0; j < mImageWidth; j++) {
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
    delete[] array;
}

void HazeRemoveFilter::settImage() {
    int r = ((A & 0x00ff0000) >> 16) > MAXA ? MAXA : ((A & 0x00ff0000) >> 16);
    int g = ((A & 0x0000ff00) >> 8) > MAXA ? MAXA : ((A & 0x0000ff00) >> 8);
    int b = (A & 0x000000ff) > MAXA ? MAXA : (A & 0x000000ff);

    double max = 0;
    double min = 255;

    tImage = new double *[mImageHeight];
    for (int i = 0; i < mImageHeight; i++) {
        tImage[i] = new double[mImageWidth];
        for (int j = 0; j < mImageWidth; j++) {
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
    MinFilterMatrix(tImage, mImageHeight, mImageWidth, 15);

    for (int i = 0; i < mImageHeight; i++) {
        for (int j = 0; j < mImageWidth; j++) {
            tImage[i][j] = (tImage[i][j] - min) / (max - min);
            tImage[i][j] = 1 - 0.95 * tImage[i][j];
        }
    }
    tImage = GuideImageFilter(tImage, mImageHeight, mImageWidth, 105);
    for (int i = 0; i < mImageHeight; i++) {
        for (int j = 0; j < mImageWidth; j++) {
            if (tImage[i][j] > max)
                max = tImage[i][j];
            if (tImage[i][j] < min)
                min = tImage[i][j];
        }
    }
    for (int i = 0; i < mImageHeight; i++) {
        for (int j = 0; j < mImageWidth; j++) {
            tImage[i][j] = (tImage[i][j] - min) / (max - min);
        }
    }
}

void HazeRemoveFilter::hazeFree() {
    outPixels = inPixels;
    int r = ((A & 0x00ff0000) >> 16) > MAXA ? MAXA : ((A & 0x00ff0000) >> 16);
    int g = ((A & 0x0000ff00) >> 8) > MAXA ? MAXA : ((A & 0x0000ff00) >> 8);
    int b = (A & 0x000000ff) > MAXA ? MAXA : (A & 0x000000ff);
    for (int i = 0; i < mImageHeight; i++) {
        for (int j = 0; j < mImageWidth; j++) {
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
//    Color_His(outPixels, mImageHeight, mImageWidth);
    autoBright(outPixels, mImageHeight, mImageWidth);
}

void HazeRemoveFilter::minFilterMatrix(double **inPixels, int height, int width, int r) {
    for (int i = 0; i < height; i++) {
        double g[width];
        double h[width];
        for (int j = 0; j < width - width % r; j++) {
            if (j % r == 0) {
                g[j] = inPixels[i][j];
                h[j + r - 1] = inPixels[i][j + r - 1];
            }
            else {
                g[j] = g[j - 1] > inPixels[i][j] ? inPixels[i][j] : g[j - 1];
                h[j + r - 2 * (j % r) - 1] =
                        h[j + r - 2 * (j % r)] > inPixels[i][j + r - 2 * (j % r) - 1] ? inPixels[i][
                                j + r - 2 * (j % r) - 1] : h[j + r - 2 * (j % r)];
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
        double g[height];
        double h[height];
        for (int j = 0; j < height - height % r; j++) {
            if (j % r == 0) {
                g[j] = inPixels[j][i];
                h[j + r - 1] = inPixels[j + r - 1][i];
            }
            else {
                g[j] = g[j - 1] > inPixels[j][i] ? inPixels[j][i] : g[j - 1];
                h[j + r - 2 * (j % r) - 1] =
                        h[j + r - 2 * (j % r)] > inPixels[j + r - 2 * (j % r) - 1][i] ?
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

void HazeRemoveFilter::MinFilterMatrix(double **inPixels, int height, int width, int r) {
    for (int i = 0; i < height; i++) {
        double g[width];
        double h[width];
        for (int j = 0; j < width - width % r; j++) {
            if (j % r == 0) {
                g[j] = inPixels[i][j];
                h[j + r - 1] = inPixels[i][j + r - 1];
            }
            else {
                g[j] = g[j - 1] > inPixels[i][j] ? inPixels[i][j] : g[j - 1];
                h[j + r - 2 * (j % r) - 1] =
                        h[j + r - 2 * (j % r)] > inPixels[i][j + r - 2 * (j % r) - 1] ? inPixels[i][
                                j + r - 2 * (j % r) - 1] : h[j + r - 2 * (j % r)];
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
        double g[height];
        double h[height];
        for (int j = 0; j < height - height % r; j++) {
            if (j % r == 0) {
                g[j] = inPixels[j][i];
                h[j + r - 1] = inPixels[j + r - 1][i];
            }
            else {
                g[j] = g[j - 1] > inPixels[j][i] ? inPixels[j][i] : g[j - 1];
                h[j + r - 2 * (j % r) - 1] =
                        h[j + r - 2 * (j % r)] > inPixels[j + r - 2 * (j % r) - 1][i] ?
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

double **HazeRemoveFilter::GuideImageFilter(double **inPixels, int height, int width, int r) {
    for (int i = 0; i < mImageHeight; i++) {
        for (int j = 0; j < mImageWidth; j++) {
            guideImage[i][j] = (guideImage[i][j] - Min) / (Max - Min);
        }
    }
    double **meanI = BoxFilter(guideImage, height, width, r);
    double **meanP = BoxFilter(inPixels, height, width, r);
    double **corrI = BoxFilter(dotProduct(guideImage, guideImage, height, width), height, width, r);
    double **corrIP = BoxFilter(dotProduct(guideImage, inPixels, height, width), height, width, r);
    double **varI = mMinus(corrI, dotProduct(meanI, meanI, height, width), height, width);
    double **covIP = mMinus(corrIP, dotProduct(meanI, meanP, height, width), height, width);
    double **a = dotDivide(covIP, mPlus(varI, 0.0001, height, width), height, width);
    double **b = mMinus(meanP, dotProduct(a, meanI, height, width), height, width);
    delete[] meanI;
    delete[] meanP;
    delete[] corrI;
    delete[] corrIP;
    delete[] varI;
    delete[] covIP;
    double **meanA = BoxFilter(a, height, width, r);
    double **meanB = BoxFilter(b, height, width, r);
    delete[] a;
    delete[] b;
    return mPlus(dotProduct(meanA, guideImage, height, width), meanB, height, width);
}

double **HazeRemoveFilter::BoxFilter(double **inPixels, int height, int width, int r) {
    double **ans = new double *[height];
    double **fin = new double *[height];

    for (int i = 0; i < height; i++) {
        ans[i] = new double[width];
        fin[i] = new double[width];
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

    delete[]ans;
    return fin;
}

double **HazeRemoveFilter::dotProduct(double **m1, double **m2, const int h, int w) {

    double **ans = new double *[h];
    for (int i = 0; i < h; i++) {
        ans[i] = new double[w];
        for (int j = 0; j < w; j++)
            ans[i][j] = m1[i][j] * m2[i][j];
    }

    return ans;
}

double **HazeRemoveFilter::dotDivide(double **m1, double **m2, int h, int w) {
    double **ans = new double *[h];
    for (int i = 0; i < h; i++) {
        ans[i] = new double[w];
        for (int j = 0; j < w; j++)
            ans[i][j] = m1[i][j] / m2[i][j];
    }

    return ans;
}

double **HazeRemoveFilter::mMinus(double **m1, double **m2, int h, int w) {
    double **ans = new double *[h];
    for (int i = 0; i < h; i++) {
        ans[i] = new double[w];
        for (int j = 0; j < w; j++)
            ans[i][j] = m1[i][j] - m2[i][j];
    }
    return ans;
}

double **HazeRemoveFilter::mPlus(double **m, double e, int h, int w) {
    double **ans = new double *[h];
    for (int i = 0; i < h; i++) {
        ans[i] = new double[w];
        for (int j = 0; j < w; j++)
            ans[i][j] = m[i][j] + e;
    }

    return ans;
}

double **HazeRemoveFilter::mPlus(double **m1, double **m2, int h, int w) {
    double **ans = new double *[h];
    for (int i = 0; i < h; i++) {
        ans[i] = new double[w];
        for (int j = 0; j < w; j++)
            ans[i][j] = m1[i][j] + m2[i][j];
    }

    return ans;
}

void HazeRemoveFilter::Color_His(int **input, int h, int w) {
    int **rImage = new int *[h];
    int **gImage = new int *[h];
    int **bImage = new int *[h];
    int rgrays[256];
    int ggrays[256];
    int bgrays[256];
    double hisImage[256];
    for (int i = 0; i < h; i++) {
        rImage[i] = new int[w];
        gImage[i] = new int[w];
        bImage[i] = new int[w];
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

    for (int i = 0; i < h; i++)
        for (int j = 0; j < w; j++)
            input[i][j] = ((rImage[i][j]) << 16) | ((gImage[i][j]) << 8) | (bImage[i][j]);
    delete[]rImage;
    delete[]gImage;
    delete[]bImage;

}

void HazeRemoveFilter::autoBright(int **input, int h, int w) {
    int **rImage = new int *[h];
    int **gImage = new int *[h];
    int **bImage = new int *[h];
    int rgrays[256];
    int ggrays[256];
    int bgrays[256];
    for (int i = 0; i < h; i++) {
        rImage[i] = new int[w];
        gImage[i] = new int[w];
        bImage[i] = new int[w];
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

    delete[]rImage;
    delete[]gImage;
    delete[]bImage;
}

void HazeRemoveFilter::colorMap(int *arr, int h, int w) {
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

void HazeRemoveFilter::process() {
    processDarkChanel();
    setA();
    settImage();
    hazeFree();
}


HazeRemoveFilter::~HazeRemoveFilter() {
    if (guideImage != 0)
        delete[]guideImage;
    if (tImage != 0)
        delete[]tImage;

    if (darkChanel != 0)
        delete[]darkChanel;
    if (inPixels != 0)
        delete[]inPixels;
//    delete []outPixels;
}

HazeRemoveFilter::HazeRemoveFilter(int **inPixels, double **guideImage, int i, int i1) {
    HazeRemoveFilter::inPixels = inPixels;
    HazeRemoveFilter::guideImage = guideImage;
    mImageWidth = i;
    mImageHeight = i1;

}






