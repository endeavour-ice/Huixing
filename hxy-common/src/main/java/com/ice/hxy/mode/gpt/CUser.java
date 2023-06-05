package com.ice.hxy.mode.gpt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CUser{
    private String role;
    private String content;
}