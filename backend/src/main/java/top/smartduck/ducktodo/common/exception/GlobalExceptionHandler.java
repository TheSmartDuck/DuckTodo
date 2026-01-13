package top.smartduck.ducktodo.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import top.smartduck.ducktodo.common.result.R;
import top.smartduck.ducktodo.common.enums.ResultCode;


/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: uri={}, code={}, msg={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常 (Validation/JSR303)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String msg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数校验异常: uri={}, msg={}", request.getRequestURI(), msg);
        return R.fail(ResultCode.BAD_REQUEST, msg);
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e, HttpServletRequest request) {
        String msg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数绑定异常: uri={}, msg={}", request.getRequestURI(), msg);
        return R.fail(ResultCode.BAD_REQUEST, msg);
    }

    /**
     * 认证异常 (Spring Security)
     */
    @ExceptionHandler(AuthenticationException.class)
    public R<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证失败: uri={}, msg={}", request.getRequestURI(), e.getMessage());
        return R.fail(ResultCode.UNAUTHORIZED, "认证失败，请重新登录");
    }

    /**
     * 权限不足异常 (Spring Security)
     * 禁止使用 403 状态码，改为返回 BAD_REQUEST(400)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("权限不足: uri={}, msg={}", request.getRequestURI(), e.getMessage());
        return R.fail(ResultCode.BAD_REQUEST, "权限不足");
    }

    /**
     * 404 异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("路径不存在: uri={}", request.getRequestURI());
        return R.fail(ResultCode.NOT_FOUND, "请求路径不存在");
    }

    /**
     * 全局兜底异常
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统内部异常: uri={}", request.getRequestURI(), e);
        return R.error("系统繁忙，请稍后重试");
    }
}
