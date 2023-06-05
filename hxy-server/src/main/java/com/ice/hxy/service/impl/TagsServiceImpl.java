package com.ice.hxy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.common.B;
import com.ice.hxy.mapper.TagsMapper;
import com.ice.hxy.mode.entity.Tags;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.enums.TagCategoryEnum;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.request.TagRequest;
import com.ice.hxy.mode.request.TeamTagRequest;
import com.ice.hxy.service.TagService.TagsService;
import com.ice.hxy.util.SensitiveUtils;
import com.ice.hxy.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 标签表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-05-27
 */
@Service
@Slf4j
public class TagsServiceImpl extends ServiceImpl<TagsMapper, Tags> implements TagsService {


    @Override
    public B<Boolean> addUserTag(TagRequest tagRequest) {
        User loginUser = UserUtils.getLoginUser();
        if (!UserRole.isAdmin(loginUser)) {
            return B.auth();
        }
        return addTag(tagRequest, TagCategoryEnum.USER, loginUser.getId()) ? B.ok() : B.error();
    }

    @Override
    public B<Boolean> addPostTag(TagRequest tagRequest) {
        User loginUser = UserUtils.getLoginUser();
        if (!UserRole.isAdmin(loginUser)) {
            return B.auth();
        }
        return addTag(tagRequest, TagCategoryEnum.POST, loginUser.getId()) ? B.ok() : B.error();
    }

    @Override
    public B<Boolean> addTeamTag(TeamTagRequest tagRequest) {
        User loginUser = UserUtils.getLoginUser();
        if (!UserRole.isAdmin(loginUser)) {
            return B.auth();
        }
        return addTag(tagRequest, TagCategoryEnum.TEAM, loginUser.getId()) ? B.ok() : B.error();
    }

    @Override
    public B<Boolean> addTeamPostTag(TagRequest tagRequest) {
        User loginUser = UserUtils.getLoginUser();
        if (!UserRole.isAdmin(loginUser)) {
            return B.auth();
        }
        return addTag(tagRequest, TagCategoryEnum.TEAM_POST, loginUser.getId()) ? B.ok() : B.error();
    }

    @Override
    public B<Boolean> addIndexTag(TagRequest tagRequest) {
        return null;
    }

    public boolean addTagBatch(List<String> tags,TagCategoryEnum categoryEnum, long userId,Long teamId) {
        if (tags.isEmpty()) {
            return false;
        }
        if (categoryEnum == TagCategoryEnum.TEAM && teamId < 0) {
            return false;
        }
        List<Tags> tagsArrayList = new ArrayList<>();
        for (String tag : tags) {
            Tags ta = new Tags();
            ta.setTagType("通用");
            ta.setTag(SensitiveUtils.sensitive(tag));
            ta.setCreatorId(userId);
            if (categoryEnum == TagCategoryEnum.TEAM && teamId > 0) {
                ta.setCategory(teamId);
            }else {
                ta.setCategory(teamId);
            }

            ta.setTagNum(0);
            tagsArrayList.add(ta);
        }
        return this.saveBatch(tagsArrayList);

    }
    public boolean addTag(TagRequest tagRequest, TagCategoryEnum categoryEnum, long userId) {

        if (tagRequest == null) {
            return false;
        }
        String tag = tagRequest.getTag();
        String tagType = tagRequest.getTagType();
        if (!StringUtils.hasText(tag)) {
            return false;
        }
        if (categoryEnum == null || userId < 0) {
            return false;
        }
        try {
            tag = SensitiveUtils.sensitive(tag);
            tagType = SensitiveUtils.sensitive(tagType);
        } catch (Exception e) {
            log.error("addTag sensitive userId:{}, category:{}, error:{}"
                    , userId, categoryEnum.getValue(), e.getMessage());
        }

        Tags tags = new Tags();
        tags.setTagType(tagType);
        tags.setTag(tag);
        tags.setCreatorId(userId);
        if (categoryEnum == TagCategoryEnum.TEAM_POST && tagRequest instanceof TeamTagRequest) {
            TeamTagRequest teamTagRequest = (TeamTagRequest) tagRequest;
            Long teamId = teamTagRequest.getTeamId();
            tags.setCategory(teamId);
        } else {
            tags.setCategory(categoryEnum.getValue());
        }

        return this.save(tags);
    }


}
