package cn.bit.api.support.validation.utils;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AjaxUtils {
    private  static Logger logger=Logger.getLogger(AjaxUtils.class);
    private  static  ObjectMapper mapper = new ObjectMapper();
    /**
     * 验证是否是ajax请求
     * @param webRequest
     * @return
     */
    public static boolean isAjaxRequest(WebRequest webRequest) {
        String requestedWith = webRequest.getHeader("X-Requested-With");
        return requestedWith != null ? "XMLHttpRequest".equals(requestedWith) : false;
    }

    public static boolean isAjaxUploadRequest(WebRequest webRequest) {
        return webRequest.getParameter("ajaxUpload") != null;
    }
    public static  void writeJson(Object value, HttpServletResponse response){
        JsonGenerator jsonGenerator = null;
        try {
            jsonGenerator=mapper.getFactory().createGenerator(response.getOutputStream(), JsonEncoding.UTF8);
            if(jsonGenerator!=null){
                jsonGenerator.writeObject(value);
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }
    private AjaxUtils(){}
}
