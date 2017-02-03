package research.dlsu.cacaoapp;

/**
 * Created by courtneyngo on 7/9/15.
 */
public class Coordinate {

    private double x;
    private double y;

    public Coordinate(){}

    public Coordinate(double x, double y){
        this.x = x;
        this.y = y;
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

    public double distanceFrom(Coordinate c){
        return (float) Math.sqrt(Math.pow(this.x-c.getX(),2)+Math.pow(this.y-c.getY(),2));
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
