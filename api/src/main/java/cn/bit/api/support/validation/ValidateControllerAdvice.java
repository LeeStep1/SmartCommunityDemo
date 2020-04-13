package cn.bit.api.support.validation;


import cn.bit.api.support.validation.utils.AjaxUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一处理验证失败异常
 * 使用此切片后@Valid注解验证的参数后不用再加Errors或Bindingesult
 * @author yg.huang
 * @version v1.0
 *          DATE  2016/11/25
 */
@ControllerAdvice
public class ValidateControllerAdvice {

    Logger logger = Logger.getLogger(ValidateControllerAdvice.class);
    /**
     * bean校验未通过异常
     *
     * @see javax.validation.Valid
     * @see org.springframework.validation.Validator
     * @see org.springframework.validation.DataBinder
     */
    @ExceptionHandler(BindException.class)
    public String validExceptionHandler(BindException e, WebRequest request, HttpServletResponse response) {

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError error:fieldErrors){
            logger.error(error.getField()+":"+error.getDefaultMessage());
        }
        request.setAttribute("fieldErrors",fieldErrors,WebRequest.SCOPE_REQUEST);
        if(AjaxUtils.isAjaxRequest(request)){
            Map<String,Object> attrMap=new HashMap<String, Object>();
            String[] atrrNames=request.getAttributeNames(WebRequest.SCOPE_REQUEST);
            for(String attr:atrrNames){
                Object value=request.getAttribute(attr,WebRequest.SCOPE_REQUEST);
                if(value instanceof Serializable){
                    attrMap.put(attr,value);
                }
            }
            AjaxUtils.writeJson(attrMap,response);
            return null;
        }
        return "/validError";
    }

}
