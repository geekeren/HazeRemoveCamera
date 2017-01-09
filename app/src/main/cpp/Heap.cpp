#include "Heap.h"
#include<cstring>


Heap::Heap() {
}


Heap::Heap(Node *heap, int size) {
    Heap::heapSize = size;
    Heap::heap = new Node[size];
    copyHeap(Heap::heap, heap, size);
}

Heap::~Heap() {
    //delete heap;
}

void Heap::MinHeapify(int i) {
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

void Heap::BuildMinHeap(Node *heap, int heapSize) {
    Heap::heapSize = heapSize;
    if (Heap::heap != 0)
        delete Heap::heap;
    Heap::heap = new Node[heapSize];
    copyHeap(Heap::heap, heap, heapSize);
    for (int i = heapSize / 2 - 1; i >= 0; i--)
        MinHeapify(i);
}

int Heap::Parent(int i) {
    return (i - 1) / 2;
}

int Heap::Left(int i) {
    return 2 * (i + 1) - 1;
}

int Heap::Right(int i) {
    return 2 * (i + 1);
}

int Heap::top() {
    return heap[0].getValue();
}

void Heap::changeTopElem(int h, int w, int i) {
    heap[0].setH(h);
    heap[0].setW(w);
    heap[0].setValue(i);
    MinHeapify(0);
}

Node *Heap::getMinHeap() {
    return heap;
}

void Heap::display() {
}

void Heap::copyHeap(Node *dest, Node *src, int size) {

    for (int i = 0; i < size; i++) {
        Node srcNode = src[i];
        dest[i] = Node(srcNode.getH(),srcNode.getW(),src->getValue());
    }
}


