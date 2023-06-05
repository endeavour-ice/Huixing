package com.ice.hxy.mode.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;


import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/3/27 11:06
 * @Description: TODO
 */
@Data
public class PostESDTO {
    //private static final String DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @Id
    private String id;


    private Long userId;


    private String content;


    private List<String> tags;
    //@Field(index = false,store = true,format = DateFormat.basic_date_time,type = FieldType.Date)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime createTime;

    //@Field(index = false,store = true,format = DateFormat.basic_date_time,type = FieldType.Date)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime updateTime;

    private Integer isDelete;

}
