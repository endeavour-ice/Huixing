package com.ice.hxy.service.OssService;

import com.ice.hxy.common.B;
import com.ice.hxy.util.ResponseEmail;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ice
 * @date 2022/9/17 12:48
 */

public interface OssService {
    B<String> upload(MultipartFile file, HttpServletRequest request);



    B<Boolean> sendRegisterEMail(ResponseEmail email, HttpServletRequest request);

    B<String> upFileByTeam(MultipartFile file, Long teamID);

    B<Boolean> sendForgetEMail(ResponseEmail email, HttpServletRequest request);

    B<Boolean> sendBinDingEMail(ResponseEmail email, HttpServletRequest request);
}
