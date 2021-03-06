package ru.mtl.voidvoice.motion_treker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leapmotion.leap.Vector;

public class Point3d {
    private final static double TOLERANCE_DISTANCE = 10.0;

    @JsonIgnore
    private long id;

    private double x;
    private double y;
    private double z;

    public Point3d() {}

    public Point3d(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3d(Vector v){
        x = v.getX();
        y = v.getY();
        z = v.getZ();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId(){
        return id;
    }

    public static double distance(Point3d first, Point3d second) {
        return first.distance(second);
    }

    public double distance(Point3d point3d) {
        return Math.sqrt((x - point3d.x) * (x - point3d.x) + (y - point3d.y) * (y - point3d.y) + (z - point3d.z) * (z - point3d.z));
    }
}
