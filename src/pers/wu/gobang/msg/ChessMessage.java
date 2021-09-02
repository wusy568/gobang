package pers.wu.gobang.msg;

import java.io.Serializable;

public class ChessMessage implements Serializable {
    private double x;
    private double y;
    private boolean isBlack;

    public ChessMessage() {
    }

    public ChessMessage(double x, double y, boolean isBlack) {
        super();
        this.x = x;
        this.y = y;
        this.isBlack = isBlack;
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

    public boolean isBlack() {
        return isBlack;
    }

    public void setBlack(boolean isBlack) {
        this.isBlack = isBlack;
    }

    @Override
    public String toString() {
        return "ChessMessage [x=" + x + ", y=" + y + ", isBlack=" + isBlack + "]";
    }
}
