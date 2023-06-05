package com.ice.hxy.once.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.hxy.mode.entity.OpLog;
import com.ice.hxy.service.IOpLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/4/22 20:17
 * @Description: TODO
 */
@Component
public class ExcelService {
    @Autowired
    private IOpLogService logService;
    public void simpleWrite(ServletOutputStream outputStream) {
        // 注意 simpleWrite在数据量不大的情况下可以使用（5000以内，具体也要看实际情况），数据量大参照 重复多次写入
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        // 如果这里想使用03 则 传入excelType参数即可
        try (ExcelWriter excelWriter = EasyExcel.write(outputStream, OpLog.class).autoCloseStream(Boolean.FALSE).build()) {
            // 这里注意 如果同一个sheet只要创建一次
            WriteSheet writeSheet = EasyExcel.writerSheet("日志").build();
            // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来
            for (int i = 1;; i++) {
                Page<OpLog> page = new Page<>(i,100);
                Page<OpLog> logPage = logService.page(page);
                List<OpLog> list = logPage.getRecords();
                // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                excelWriter.write(list, writeSheet);
                if (!logPage.hasNext()) {
                    break;
                }
            }
        }

    }


}
