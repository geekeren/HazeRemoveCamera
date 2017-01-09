#pragma once
class Node
{
private:
	int h, w, value;
public:
	Node();
	Node(int, int, int);

	~Node();

	int getH();
	int getW();
	int getValue();
	void setH(int h);
	void setW(int w);
	void setValue(int value);
};

