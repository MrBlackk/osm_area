package com.mrb;

public class Area implements Comparable{

    private Long id;
    private String name;
    private double area;

    public Area(Long id, String name, double area) {
        this.id = id;
        this.name = name;
        this.area = area;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getArea() {
        return area;
    }

    public int compareTo(Object o) {
        double compArea = ((Area) o).getArea();
        return Double.compare(area, compArea);
    }

    @Override
    public String toString() {
        return name + ": " + area;
    }
}
