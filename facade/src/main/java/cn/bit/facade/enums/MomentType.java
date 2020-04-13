package cn.bit.facade.enums;

/**
 * 动态类型
 */
public enum MomentType {

    //邻里社交
    NEIGHBOUR(1),
    //二手交易
    TRANSACTIONS(3),
    //悬赏求助
    REWARDHELP(2);

    private int key;

    MomentType(int key){
        this.key = key;
    }

    public int getKey(){
        return this.key;
    }
}
