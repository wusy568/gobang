package pers.wu.gobang.ui;

import javafx.scene.paint.Color;

@SuppressWarnings("all")
public class Chess {
    private double x;
    private double y;
    private Color color;
    public Chess() {
    }

    public Chess(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return x + "," + y + "," + color ;
    }
}