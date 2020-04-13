package cn.bit.api.support.annotation;

import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.push.PushPointEnum;

import java.lang.annotation.*;

/**
 * 发送推送注解，作用于方法上.
 * 标注此注解的方法，会根据推送配置判断是否需要推送及如何推送.
 * 如果需要指定推送节点的名称，需要设定value属性。默认为
 * "{方法所在类的全限定名}.{方法名}({参数类型的全限定名})"
 * <p>
 * 如果需要指定推送的数据对象，可以通过以下4种方式：
 * </p>
 * <p>
 * 1. 标注该注解的方法返回值为ApiResult类型，ApiResult对象的data属性将作为数据
 *    据对象
 * </p>
 * <p>
 * 2. 标注该注解的方法返回值为WrapResult类型，把原本的返回值作为WrapResult对象
 *    的actualResult属性（处理推送后会进行unwrap，返回原本的返回值），把
 *    WrapResult的data属性作为数据对象.
 *    如:
 *    <blockquote><pre>
 *    public ApiResult doSomething() {
 *           ...
 *           return ApiResult.ok();
 *    }
 *    </pre></blockquote>
 *    改为如下：
 *    <blockquote><pre>
 *    public WrapResult doSomething() {
 *        ...
 *        return new WrapResult(ApiResult.ok(), data);
 *    }
 *    </pre></blockquote>
 * </p>
 * <p>
 * 3. 标注该注解的方法返回值为除上述两种类型外的其他类型，将首先尝试获取该类型对象
 *    的data属性，如果存在则把data属性作为数据对象；否则将整个返回值作为数据对象.
 * </p>
 * <p>
 * 4. 标注该注解的方法参数加上@SendPush.Data注解，标识该参数作为数据对象.
 *    如：
 *    <blockquote><pre>
 *    public ApiResult doSomething(@SendPush.Data Object data) {
 *        ...
 *        return ApiResult.ok();
 *    }
 *    </pre></blockquote>
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SendPush {

    /**
     * 推送节点标识
     */
    String value() default "";

    ClientType[] clientTypes();

    /**
     * 推送范围
     */
    Scope scope();

    /**
     * 推送单位
     */
    Unit unit() default Unit.USER;

    /**
     * 是否推送ApiResult对象的data属性
     */
    boolean pushData() default true;

    /**
     * 功能节点
     * @return
     */
    PushPointEnum point();

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface Data {

        String value() default "";

    }

    enum Scope {
        ALL("all"), COMMUNITY("communityId"), BUILDING("buildingId"), ROOM("roomId");

        private String value;

        Scope(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    enum Unit {
        USER("userIds"), TAG("tags");

        String value;

        Unit(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

}
