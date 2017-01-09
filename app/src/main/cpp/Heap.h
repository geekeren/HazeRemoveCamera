#pragma once
#include "Node.h"
class Heap
{
private:
	int heapSize;
	Node* heap = 0;
public:
	Heap();
	Heap(Node* heap, int size);
	~Heap();

	void MinHeapify(int);
	void BuildMinHeap(Node*, int);
	int Parent(int);
	int	Left(int);
	int	Right(int);
	int	top();
	void changeTopElem(int, int, int);
	Node* getMinHeap();
	void display();

	void copyHeap(Node *pNode, Node *heap, int size);
};

