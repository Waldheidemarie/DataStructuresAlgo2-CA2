package sample;

public class MapPoint {
    public String type;
    public String name;
    public double xCo;
    public double yCo;

    public MapPoint(String type, String name, double xCo, double yCo) {
        this.type = type;
        this.name = name;
        this.xCo = xCo;
        this.yCo = yCo;
    }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

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
