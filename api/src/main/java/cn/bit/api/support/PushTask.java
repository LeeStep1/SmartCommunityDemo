package cn.bit.api.support;

import lombok.Data;

@Data
public class PushTask {

    private PushTarget pushTarget;

    private Object dataObject;

}
