package com.ice.hxy.controller.TagController;

import com.ice.hxy.common.B;
import com.ice.hxy.mode.request.TagRequest;
import com.ice.hxy.service.TagService.TagsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    @GetMapping("/get")
    public B<List<String>> getTag(@RequestParam("id") Long id,
                                  @RequestParam(value = "tid",required = false)Long tid) {
        return tagsService.get(id,tid);
    }
}
