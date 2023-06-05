package com.ice.hxy.service.TagService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.entity.Tags;
import com.ice.hxy.mode.enums.TagCategoryEnum;
import com.ice.hxy.mode.request.TagRequest;
import com.ice.hxy.mode.request.TeamTagRequest;

import java.util.List;

/**
 * <p>
 * 标签表 服务类
 * </p>
 *
 * @author ice
 * @since 2023-05-27
 */
public interface TagsService extends IService<Tags> {

    B<Boolean> addUserTag(TagRequest tagRequest);

    B<Boolean> addPostTag(TagRequest tagRequest);

    B<Boolean> addTeamTag(TeamTagRequest tagRequest);

    B<Boolean> addTeamPostTag(TagRequest tagRequest);
    B<Boolean> addIndexTag(TagRequest tagRequest);



    boolean addTagBatch(List<String> tags, TagCategoryEnum categoryEnum, long userId, Long teamId);

    boolean addTag(TagRequest tagRequest, TagCategoryEnum categoryEnum, long userId);


}

