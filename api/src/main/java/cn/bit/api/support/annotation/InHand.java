package cn.bit.api.support.annotation;

import java.lang.annotation.*;

/**
 * Created by fxiao
 * on 2018/3/23
 * 待办事项
 * 注解
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InHand {

    /**
     * 数据是否处理
     * 1：未处理；0：已处理
     * 未处理，新增到代办表
     * 已处理，修改待办状态
     * @return
     */
    DataStatus dataStatus();

    /**
     * 任务类型
     * 0：没有
     * 1：故障报修
     * 、、、
     * 其它待补充
     * @return
     */
    TaskType taskType();

    enum TaskType {
        None(0, "没有"), fault(1, "故障报修");

        private Integer key;

        private String value;

        TaskType(Integer key, String value) {
            this.key = key;
            this.value = value;
        }

        public Integer getKey() {
            return key;
        }

        public void setKey(Integer key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    enum DataStatus {
        valid(1, "有效"), unvalid(0, "无效");

        private Integer key;

        private String value;

        DataStatus(Integer key, String value){
            this.key = key;
            this.value = value;
        }

        public Integer getKey() {
            return key;
        }

        public void setKey(Integer key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
