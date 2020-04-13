package cn.bit.facade.vo.community;

import lombok.Data;

import java.io.Serializable;

/**
 * 坐标描点
 *
 * @author decai.liu
 * @version 1.0.0
 * @create 2019.01.03
 */
@Data
public class Points implements Serializable {

    /**
     * 横坐标
     */
    private Integer x;

    /**
     * 纵坐标
     */
    private Integer y;

    /**
     * 宽度
     */
    private Integer w;

    /**
     * 高度
     */
    private Integer h;

    private Points (){

    }

    public Points (int x, int y, int w, int h){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
}
