package cn.bit.communityIoT.support.freeview;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FreeViewToken {
    Token token() default Token.None;

    enum Token {
        None(0, "不获取token"), Get(1, "获取token");

        private Integer key;

        private String value;

        Token(Integer key, String value) {
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
