package cn.hazeremovecamera.jar;

/**
 * Created by OptimusV5 on 2015/1/8.
 */
public class Node {
    private int h;
    private int w;
    private int value;
    Node() {

    }
    Node(int h, int w,int value) {
        this.h = h;
        this.w = w;
        this.value = value;
    }

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }

    public int getValue() {
        return value;
    }
    public void setH(int h) {
        this.h = h;
    }
    public void setW(int w) {
        this.w = w;
    }
    public void setValue(int value){
        this.value = value;
    }
}
