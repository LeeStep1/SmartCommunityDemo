package cn.bit.facade.enums;

/**
 * 言论类型
 */
public enum SpeechType {

    //动态
    MOMENT(1),
    //评论
    COMMENT(2);

    private int key;

    SpeechType(int key){
        this.key = key;
    }

    public int getKey(){
        return this.key;
    }
}
