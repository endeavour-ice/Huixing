package com.ice.hxy.service.impl.post;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.mapper.PostGroupMapper;
import com.ice.hxy.mode.entity.PostGroup;
import com.ice.hxy.service.PostService.PostGroupService;
import org.springframework.stereotype.Service;

@Service
public class PostGroupServiceImpl extends ServiceImpl<PostGroupMapper, PostGroup> implements PostGroupService {
}
