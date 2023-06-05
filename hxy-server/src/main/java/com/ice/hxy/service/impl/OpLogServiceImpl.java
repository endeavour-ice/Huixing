package com.ice.hxy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.mapper.OpLogMapper;
import com.ice.hxy.mode.entity.OpLog;
import com.ice.hxy.service.IOpLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志记录 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-05-08
 */
@Service
public class OpLogServiceImpl extends ServiceImpl<OpLogMapper, OpLog> implements IOpLogService {

}
