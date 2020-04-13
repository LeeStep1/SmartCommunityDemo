package cn.bit.facade.vo.push;

import java.io.Serializable;

public class PushResult implements Serializable {

    private String messageId;

    private Integer sendNum;

    public PushResult(String messageId, int sendNum) {
        this.messageId = messageId;
        this.sendNum = sendNum;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Integer getSendNum() {
        return sendNum;
    }

    public void setSendNum(Integer sendNum) {
        this.sendNum = sendNum;
    }
}
