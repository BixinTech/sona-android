package cn.bixin.sona.driver.factory;

import cn.bixin.sona.component.SonaComponent;
import cn.bixin.sona.component.connection.mercury.ChatRoomMercuryConnection;
import cn.bixin.sona.component.connection.mercury.TeamMercuryConnection;
import cn.bixin.sona.data.ConnectTypeEnum;
import cn.bixin.sona.data.entity.SonaRoomData;
import cn.bixin.sona.driver.ComponentProducer;
import cn.bixin.sona.driver.RoomDriver;

public class ConnectionFactory implements ComponentProducer.ComponentFactory {

    @Override
    public SonaComponent createComponent(RoomDriver roomDriver) {
        if (roomDriver.getProvider().acquire(SonaRoomData.class) != null) {
            SonaRoomData sonaRoomData = roomDriver.getProvider().acquire(SonaRoomData.class);
            if (sonaRoomData == null || sonaRoomData.imInfo == null) {
                return null;
            }

            if (ConnectTypeEnum.CHATROOM.getValue().equals(sonaRoomData.imInfo.getModule())) {
                return new ChatRoomMercuryConnection(roomDriver);
            } else if (ConnectTypeEnum.TEAM.getValue().equals(sonaRoomData.imInfo.getModule())) {
                return new TeamMercuryConnection(roomDriver);
            }
        }
        return null;
    }
}
