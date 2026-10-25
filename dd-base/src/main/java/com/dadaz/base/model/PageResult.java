package com.dadaz.base.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "结果集")
    private List<T> list;
}
