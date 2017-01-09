#pragma once

class HazeRemoveFilter {
private:


    double **guideImage = 0;
    double **tImage = 0;
    double **darkChanel = 0;
    int A;
    const double T0 = 0.1;
    const int MAXA = 185;
    double Max = 0;
    double Min = 255;
    int mImageWidth;
    int mImageHeight;

    void processDarkChanel();

    void setA();

    void settImage();

    void hazeFree();

    void minFilterMatrix(double **inPixels, int height, int width, int r);

    void MinFilterMatrix(double **inPixels, int height, int width, int r);

public:
    int **inPixels = 0;
    int **outPixels = 0;

    double **GuideImageFilter(double **inPixels, int height, int width, int r);

    double **BoxFilter(double **inPixels, int height, int width, int r);

    double **dotProduct(double **m1, double **m2, int h, int w);

    double **dotDivide(double **m1, double **m2, int h, int w);

    double **mMinus(double **m1, double **m2, int h, int w);

    double **mPlus(double **m, double e, int h, int w);

    double **mPlus(double **m1, double **m2, int h, int w);

    void Color_His(int **input, int h, int w);

    void autoBright(int **input, int h, int w);

    void colorMap(int *arr, int h, int w);

    HazeRemoveFilter(int **pInt, double **pDouble, int i, int i1);

    ~HazeRemoveFilter();

    void process();
};

