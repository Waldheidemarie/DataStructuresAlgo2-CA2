package sample;

public class Landmark extends MapPoint {


    public Landmark(String type, String name, double xCo, double yCo) {
        super(type, name, xCo, yCo);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getxCo() {
        return xCo;
    }

    public void setxCo(int xCo) {
        this.xCo = xCo;
    }

    public double getyCo() {
        return yCo;
    }

    public void setyCo(int yCo) {
        this.yCo = yCo;
    }
}
