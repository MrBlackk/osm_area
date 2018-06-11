package com.mrb;

public class Distance implements Comparable{

    private long id;
    private String name;
    private double distance;

    public Distance(long id, String name, double distance) {
        this.id = id;
        this.name = name;
        this.distance = distance;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(Object o) {
        double compDistance = ((Distance) o).getDistance();
        return Double.compare(compDistance, distance);
    }

    @Override
    public String toString() {
        return name + ": " + distance;
    }
}
