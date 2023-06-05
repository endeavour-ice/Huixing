package com.ice.hxy.mode.gpt;

import lombok.Data;

import java.util.List;

/**
 * @Author ice
 * @Date 2023/5/10 11:51
 * @Description: chat返回值
 */
@Data
public class ChatResponse {
    private String id;
    private String object;
    private String model;
    private Integer created;
    private Usage usage;
    private List<Choices> choices;
}

