package com.ice.hxy.controller.TagController;

import com.ice.hxy.common.B;
import com.ice.hxy.mode.request.TagRequest;
import com.ice.hxy.service.TagService.TagsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 标签表 前端控制器
 * </p>
 *
 * @author ice
 * @since 2023-05-27
 */
@RestController
@RequestMapping("/tag")
public class TagsController {
    @Resource
    private TagsService tagsService;

    @PostMapping("/user/add")
    public B<Boolean> addUserTag(@RequestBody TagRequest tagRequest) {
        return tagsService.addUserTag(tagRequest);
    }

}
