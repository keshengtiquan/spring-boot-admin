package com.dadaz.admin.module.test.controller;

import com.dadaz.base.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="测试接口")
@RestController
@RequestMapping("/test")
public class TestController {

    @Operation(summary = "测试接口")
    @GetMapping("/hello")
    public R<String> test(){
        return R.success("你好");
    }
}
