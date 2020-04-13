package cn.bit.facade.vo.user.mili;

/**
 * 小区社区坐标
 */
public class Location {

    /**
     * 经度
     */
    private String lng;
    /**
     * 维度
     */
    private String lat;

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public Location() {
    }
}
