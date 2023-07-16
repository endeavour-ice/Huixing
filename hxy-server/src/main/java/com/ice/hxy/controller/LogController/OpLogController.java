package com.ice.hxy.controller.LogController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.hxy.annotation.RedissonLock;
import com.ice.hxy.common.B;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.mode.entity.OpLog;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.request.LogRequest;
import com.ice.hxy.once.excel.ExcelService;
import com.ice.hxy.service.IOpLogService;
import com.ice.hxy.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * <p>
 * 操作日志记录 前端控制器
 * </p>
 *
 * @author ice
 * @since 2023-05-08
 */
@RestController
@RequestMapping("/log")
public class OpLogController {
    @Autowired
    private ExcelService excelService;
    @Autowired
    private IOpLogService logService;

    // 获取日志信息
    @PostMapping("/list")
    public B<Page<OpLog>> list(@RequestBody LogRequest logRequest) {
        if (logRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = UserUtils.getLoginUser();
        if (!UserRole.isAdmin(loginUser)) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        long pageNum = logRequest.getCurrent();
        long pageSize = logRequest.getSize();
        Page<OpLog> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OpLog> wrapper = new QueryWrapper<>();
        String name = logRequest.getName();
        Long exTime = logRequest.getExTime();
        LocalDateTime opTime = logRequest.getOpTime();
        boolean error = logRequest.isError();
        wrapper.and(StringUtils.hasText(name), w -> w.like("op_name", name))
                .and(exTime > 0, w -> wrapper.ge("ex_time", exTime))
                .and(opTime != null, w -> {
                    if (opTime != null) {
                        w.ge("op_time", opTime.with(LocalTime.MIN))
                                .and(wq -> wq.lt("op_time", opTime.with(LocalTime.MAX)));
                    }
                }).and(error, w -> w.eq("status", 1));
        Page<OpLog> opLogPage = logService.page(page, wrapper);
        return B.ok(opLogPage);
    }

    /**
     * 下载 日志
     *
     * @param response
     * @throws IOException
     */
    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("日志", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        excelService.simpleWrite(outputStream);
    }

    /**
     * 删除所有的日志
     */
    @PostMapping("/delAll")
    @RedissonLock
    public B<Boolean> delAll() {
        User loginUser = UserUtils.getLoginUser();
        if (!UserRole.isRoot(loginUser)) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        long count = logService.count();
        if (count <= 0) {
            return B.ok();
        }
        return B.ok(logService.lambdaUpdate().ge(OpLog::getId, 0).remove());
    }
}
