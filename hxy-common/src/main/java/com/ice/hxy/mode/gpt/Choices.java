package com.ice.hxy.mode.gpt;

import lombok.Data;

@Data
public class Choices{
    private CUser message;
    private String finish_reason;
    private Integer index;
}