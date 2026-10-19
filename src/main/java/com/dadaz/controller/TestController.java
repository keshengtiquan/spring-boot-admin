package com.dadaz.controller;

import com.dadaz.core.exception.BusinessException;
import com.dadaz.core.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="测试接口")
@RestController
@RequestMapping("/test")
public class TestController {

    private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");
    @Operation(summary = "测试接口")
    @GetMapping("/hello")
    public R<String> test(){
        sys_user_logger.info("记录日志");
        return R.success("你好");
    }
}
