package top.jxnu;

public class SimpleSeatBean {

    private String id;
    private int s;
    private String t;
    private String x;
    private String y;

    public SimpleSeatBean() {
    }

    public SimpleSeatBean(String id, int s, String t, String x, String y) {
        this.id = id;
        this.s = s;
        this.t = t;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getS() {
        return s;
    }

    public void setS(int s) {
        this.s = s;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
