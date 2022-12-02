package cn.bixin.sona.data;

/**
 * 长连厂商枚举
 * @author luokun
 */
public enum ConnectSupplierEnum {
    /**
     * 自建长连
     */
    MERCURY("MERCURY");

    private String value;

    ConnectSupplierEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
