package com.ice.hxy;

import com.ice.hxy.util.SensitiveUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author ice
 * @Date 2023/5/28 9:57
 * @Description: 铭感词测试
 */
@SpringBootTest
public class SensTest {



    @Test
    void sensTest() {
        String sensitive = SensitiveUtils.sensitive("做爱");
        System.out.println(sensitive);
    }
}
