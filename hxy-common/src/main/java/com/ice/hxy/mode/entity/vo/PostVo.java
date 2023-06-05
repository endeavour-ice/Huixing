package com.ice.hxy.mode.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/2/14 10:53
 * @Description: Post 返回的数据
 */
@Data
public class PostVo implements Serializable {
    private static final long serialVersionUID = -7434749056772195178L;
    private String content;
    private Long groupId;
    private Long id;
    private Integer thumb;
    private Integer collect;
    private PostUserVo userVo;
    private String tag;
    private List<CommentVo> commentList;
    private boolean hasThumb =false;
    private boolean hasCollect = false;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createTime;

}

