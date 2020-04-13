package cn.bit.api.support;

import cn.bit.common.facade.exception.UnknownException;
import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import static cn.bit.api.support.ErrorCodeEnum.*;

/**
 * Created by Administrator on 2017/11/25 0025.
 */
@ControllerAdvice
@Slf4j
@Component
public class ErrorHandler {

    @ExceptionHandler(BindException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public ApiResult handleBindException(BindException ex) {
        log.error(ex.getMessage(), ex);
        return ApiResult.error(ERR_CHECK_PARAM.getErrorCode(), ex.getFieldError()
                .getDefaultMessage());
    }

    @ExceptionHandler(BizException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public ApiResult handleBindException(BizException ex) {
        log.error(ex.getMessage(), ex);
        return ApiResult.error(ex);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public ApiResult handleAllException(Throwable ex, HttpServletRequest req, HttpServletResponse res) {
        log.error(ex.getMessage(), ex);
        return ApiResult.error(ERR_SYS_PARAM.getErrorCode(), ERR_SYS_PARAM.getErrorDesc());
    }

    /**
     * 参数验证、绑定异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public ApiResult handleValidateException(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);
        return ApiResult.error(ERR_CHECK_PARAM.getErrorCode(),ex.getBindingResult()
                .getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public ApiResult handleMissingParamsException(MissingServletRequestParameterException ex) {
        log.error(ex.getMessage(), ex);
        return ApiResult.error(ERR_MISS_PARAM.getErrorCode(), "缺失参数:" + ex.getParameterName());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public ApiResult handleConstraintViolationException(ConstraintViolationException ex) {
        log.error(ex.getMessage(), ex);
        return ApiResult.error(ERR_CHECK_PARAM.getErrorCode(), ex.getConstraintViolations().iterator().next().getMessage());
    }

    @ExceptionHandler(UnknownException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public ApiResult handleUnknownException(UnknownException ex) {
        return ApiResult.error(ex.getCode(), ex.getSubMsg());
    }
}
