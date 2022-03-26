package com.franosch.bwinf.muellabfuhr.model;

public class Neighbor extends Edge{
    protected Neighbor(Node from, Node to, double weight) {
        super(new Path(weight, from, to), weight);
    }

    public Node getNeighbor(Node current){
        return super.getPath().getFrom().equals(current) ? super.getPath().getTo() : super.getPath().getFrom();
    }
}
