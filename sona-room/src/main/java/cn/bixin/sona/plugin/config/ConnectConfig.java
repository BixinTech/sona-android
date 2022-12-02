package cn.bixin.sona.plugin.config;

import java.util.HashMap;

public class ConnectConfig extends PluginConfig {

    private HashMap<String, Object> extension;

    public void setExtension(HashMap<String, Object> extension) {
        this.extension = extension;
    }

    public HashMap<String, Object> getExtension() {
        return extension;
    }
}
