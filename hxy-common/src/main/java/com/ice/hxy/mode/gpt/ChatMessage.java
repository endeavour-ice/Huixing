package com.ice.hxy.mode.gpt;

import com.google.gson.Gson;
import com.ice.hxy.util.GsonUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/5/10 11:30
 * @Description: TODO
 */
@Data
public class ChatMessage {
    private String model;
    private List<CUser> messages=new ArrayList<>();

    @Override
    public String toString() {
        Gson gson = GsonUtils.getGson();
        return gson.toJson(this);
    }

}
