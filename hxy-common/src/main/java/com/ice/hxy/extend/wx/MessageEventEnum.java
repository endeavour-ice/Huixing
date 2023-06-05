package com.ice.hxy.extend.wx;

public enum MessageEventEnum {
    SUBSCRIBE("subscribe","用户关注"),
    SCAN("SCAN", "用户扫描"),
    UNSUBSCRIBE("unsubscribe", "用户取消");


    private String name;
    private String dec;

    MessageEventEnum(String name, String dec) {
        this.name = name;
        this.dec = dec;
    }

    public String getName() {
        return name;
    }

    public String getDec() {
        return dec;
    }
}
