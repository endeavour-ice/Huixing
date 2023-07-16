package com.ice.hxy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.common.B;
import com.ice.hxy.mapper.TagsMapper;
import com.ice.hxy.mode.entity.Tags;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.UserTeam;
import com.ice.hxy.mode.enums.TagCategoryEnum;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.request.TagRequest;
import com.ice.hxy.mode.request.TeamTagRequest;
import com.ice.hxy.service.TagService.TagsService;
import com.ice.hxy.service.UserService.UserTeamService;
import com.ice.hxy.util.LongUtil;
import com.ice.hxy.util.SensitiveUtils;
import com.ice.hxy.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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
    @Resource
    private UserTeamService userTeamService;


    @Override
    public B<Boolean> addUserTag(TagRequest tagRequest) {
        User loginUser = UserUtils.getLoginUser();
        if (!UserRole.isAdmin(loginUser)) {
            return B.auth();
        }
        return addTag(tagRequest, TagCategoryEnum.USER, loginUser.getId()) ? B.ok() : B.error();
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

    @Override
    public B<List<String>> get(Long id, Long tid) {
        TagCategoryEnum tagEnumByValue = TagCategoryEnum.getTagEnumByValue(id);
        long category = tagEnumByValue.getValue();
        if (tagEnumByValue.equals(TagCategoryEnum.TEAM)) {
            User loginUser = UserUtils.getLoginUser();
            if (LongUtil.isEmpty(tid)) {
                return B.parameter();
            }
            Long count = userTeamService.lambdaQuery().eq(UserTeam::getUserId, loginUser.getId()).eq(UserTeam::getTeamId, tid).count();
            if (count == null||count<=0) {
                return B.parameter();
            }
            category = tid;
        }
        List<Tags> list = this.lambdaQuery().eq(Tags::getCategory, category).select(Tags::getTag).list();
        List<String> tags = list.stream().map(Tags::getTag).collect(Collectors.toList());


        return B.ok(tags);
    }
}
