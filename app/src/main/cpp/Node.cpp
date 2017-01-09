#include "Node.h"


Node::Node()
{
}

Node::Node(int h, int w , int value)
{
	Node::h = h;
	Node::w = w;
	Node::value = value;
}

Node::~Node()
{
}

int Node::getH()
{
	return h;
}

int Node::getW()
{
	return w;
}

int Node::getValue()
{
	return value;
}

void Node::setH(int h)
{
	Node::h = h;
}

void Node::setW(int w)
{
	Node::w = w;
}

void Node::setValue(int value)
{
	Node::value = value;
}
