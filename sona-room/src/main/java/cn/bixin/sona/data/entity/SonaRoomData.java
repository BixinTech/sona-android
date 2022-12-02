package cn.bixin.sona.data.entity;

import java.util.Map;

public class SonaRoomData {

    public static final int sdkVersion = 4;
    public String roomId;//sona自己的roomId，不是三方sdk的roomId
    public String guestUid;
    public String addr;
    public String nickname;
    public String productCode;
    public String productCodeAlias;
    public RoomInfo.IMConfig imInfo;
    public RoomInfo.StreamConfig streamInfo;
    public volatile boolean roomCertified;
    public Map<String, Object> extra;

}
