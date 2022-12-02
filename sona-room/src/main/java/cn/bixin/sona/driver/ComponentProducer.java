package cn.bixin.sona.driver;

import androidx.annotation.Nullable;

import cn.bixin.sona.component.SonaComponent;
import cn.bixin.sona.driver.factory.AudioFactory;
import cn.bixin.sona.driver.factory.ConnectionFactory;

public class ComponentProducer {

    @Nullable
    public static ComponentFactory getFactory(ComponentType coreType) {
        ComponentFactory sonaCoreFactory = null;
        switch (coreType) {
            case IM:
                sonaCoreFactory = new ConnectionFactory();
                break;
            case AUDIO:
                sonaCoreFactory = new AudioFactory();
                break;
            default:
                break;
        }
        return sonaCoreFactory;
    }

    public interface ComponentFactory {
        /**
         * 创建component
         *
         * @param roomDriver
         * @return
         */
        @Nullable
        SonaComponent createComponent(RoomDriver roomDriver);
    }
}
