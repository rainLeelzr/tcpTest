package avatar.rain.tcp;

/**
 * tcp包的body部分的数据类型
 */
public enum PacketBodyType {

    Proto(0),

    Json(1);

    private int type;

    public int getType() {
        return type;
    }

    PacketBodyType(int type) {
        this.type = type;
    }
}
