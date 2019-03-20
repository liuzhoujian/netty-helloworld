package com.e_heartbeat;

import java.io.Serializable;
import java.util.Map;

/**
 * 心跳包
 */
public class HeartBeatMessage implements Serializable {

    private String ip;
    private Map<String, Object> cpuMsgMap;
    private Map<String, Object> memMsgMap;
    private Map<String, Object> fileSysMsgMap;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Map<String, Object> getCpuMsgMap() {
        return cpuMsgMap;
    }

    public void setCpuMsgMap(Map<String, Object> cpuMsgMap) {
        this.cpuMsgMap = cpuMsgMap;
    }

    public Map<String, Object> getMemMsgMap() {
        return memMsgMap;
    }

    public void setMemMsgMap(Map<String, Object> memMsgMap) {
        this.memMsgMap = memMsgMap;
    }

    public Map<String, Object> getFileSysMsgMap() {
        return fileSysMsgMap;
    }

    public void setFileSysMsgMap(Map<String, Object> fileSysMsgMap) {
        this.fileSysMsgMap = fileSysMsgMap;
    }

    @Override
    public String toString() {
        return "HeartBeatMessage{" +
                "ip='" + ip + '\'' +
                ", cpuMsgMap=" + cpuMsgMap +
                ", memMsgMap=" + memMsgMap +
                ", fileSysMsgMap=" + fileSysMsgMap +
                '}';
    }
}
