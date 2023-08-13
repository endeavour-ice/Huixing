package com.ice.hxy.mode.enums;

/**
 * @Author ice
 * @Date 2023/7/21 10:51
 * @Description: TODO
 */
public enum LoginType {
    Account(0,"账号"),
    WX(1, "微信"),
    QQ(2, "QQ");

    LoginType(int value, String type) {
        this.value = value;
        this.type = type;
    }

    private int value;
    private String type;

    public int getValue() {
        return value;
    }

    public static String getType(Integer value) {
        LoginType[] values = LoginType.values();
        for (LoginType loginType : values) {
            if (value.equals(loginType.value)) {
                return loginType.type;
            }
        }
        return Account.type;
    }

}
