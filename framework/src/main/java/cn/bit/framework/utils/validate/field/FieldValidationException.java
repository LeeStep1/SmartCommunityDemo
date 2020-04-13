package cn.bit.framework.utils.validate.field;/**
 * Created by terry on 2016/9/2.
 */

/**
 * @author terry
 * @create 2016-09-02 17:42
 **/
public class FieldValidationException extends RuntimeException {

    public FieldValidationException(){
    }
    public FieldValidationException(String message) {
        super(message);
    }
}
