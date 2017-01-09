package cn.hazeremovecamera.jar;

/**
 * Created by OptimusV5 on 2015/1/8.
 */
public class Heap {
    private int heapSize;
    private Node[] heap;
    Heap(){

    }
    Heap(Node[] heap, int heapSize){
        this.heapSize = heapSize;
        this.heap = new Node[heapSize];
        System.arraycopy(heap,0,this.heap,0,heapSize);
    }
    private void MinHeapify(int i){
        int l = Left(i);
        int r = Right(i);
        int smallest;
        if (l < heapSize && heap[l].getValue() < heap[i].getValue())
            smallest = l;
        else
            smallest = i;
        if (r < heapSize && heap[r].getValue() < heap[smallest].getValue())
            smallest = r;
        if (smallest != i) {
            Node temp = heap[smallest];
            heap[smallest] = heap[i];
            heap[i] = temp;
            MinHeapify(smallest);
        }
    }
    public void BuildMinHeap(Node[] heap, int heapSize) {
        this.heapSize = heapSize;
        this.heap = new Node[heapSize];
        System.arraycopy(heap,0,this.heap,0,heapSize);
        for (int i = heapSize / 2 - 1; i >= 0 ; i--)
            MinHeapify(i);
    }
    private int Parent(int i) {
        return (i - 1) / 2;
    }
    private int Left(int i) {
        return 2 * (i + 1) - 1;
    }
    private int Right(int i) {
        return 2 * (i + 1);
    }
    public int top() {
        return heap[0].getValue();
    }
    public void changeTopElem(int h, int w, int i) {
        heap[0].setH(h);
        heap[0].setW(w);
        heap[0].setValue(i);
        MinHeapify(0);
    }
    public Node[] getMinHeap() {
        return heap;
    }
    public void display() {
        for (int i = 0; i < heapSize; i++)
            System.out.print(heap[i].getValue() + " ");
    }
}
