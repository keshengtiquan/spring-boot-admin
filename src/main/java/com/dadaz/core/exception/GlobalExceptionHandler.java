package com.dadaz.core.exception;

import com.dadaz.core.enums.ResultCode;
import com.dadaz.core.model.R;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 约定：HTTP 状态码统一返回 200，真正的业务结果由响应体的 {@code code} 字段表达，
 * 前端拦截器只需判断 {@code code != 200} 即可。
 * 如需让 HTTP 状态码与业务码保持一致，去掉各方法上的 {@link ResponseStatus} 再自行调整即可。
 *
 * @author dadaz
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常：可预期，只记 warn，不打堆栈
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 [{}] {} -> code={}, message={}",
                request.getMethod(), request.getRequestURI(), e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * @RequestBody 上的 @Valid 校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败 {} -> {}", request.getRequestURI(), message);
        return R.fail(ResultCode.PARAM_ERROR, message);
    }

    /**
     * 表单 / 查询参数上的 @Valid 校验失败
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining("；"));
        log.warn("参数绑定失败 {} -> {}", request.getRequestURI(), message);
        return R.fail(ResultCode.PARAM_ERROR, message);
    }

    /**
     * 缺少必填请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingParameter(MissingServletRequestParameterException e) {
        return R.fail(ResultCode.PARAM_ERROR, "缺少必要参数：" + e.getParameterName());
    }

    /**
     * 参数类型不匹配，例如 id 传了非数字
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return R.fail(ResultCode.PARAM_ERROR, "参数 " + e.getName() + " 类型不正确");
    }

    /**
     * 请求体无法解析（JSON 格式错误、字段类型不符等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleMessageNotReadable(HttpMessageNotReadableException e,
                                            HttpServletRequest request) {
        log.warn("请求体解析失败 {} -> {}", request.getRequestURI(), e.getMessage());
        return R.fail(ResultCode.PARAM_ERROR, "请求体格式错误，请检查提交的 JSON 内容");
    }

    /**
     * 请求方法不支持，例如 GET 接口用 POST 调用
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return R.fail(ResultCode.METHOD_NOT_ALLOWED, e.getMessage());
    }

    /**
     * 静态资源 / 接口不存在
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public R<Void> handleNoResourceFound(NoResourceFoundException e,
                                         HttpServletRequest request) {
        log.warn("资源不存在 {} -> {}", request.getRequestURI(), e.getResourcePath());
        return R.fail(ResultCode.NOT_FOUND);
    }

    /**
     * 兜底异常：非预期错误，记 error 并打印堆栈，响应体不暴露内部细节
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}] {}", request.getMethod(), request.getRequestURI(), e);
        return R.fail(ResultCode.FAIL, "系统繁忙，请稍后重试");
    }

    /**
     * 字段错误格式化为「字段名: 提示」
     */
    private static String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
