package cn.bit.framework.storage;/**
 * Created by terry on 2016/8/6.
 */

/**
 * @author terry
 * @create 2016-08-06 16:51
 **/
public class AppendResult extends StorageService.StorageResult {

    private Long nextPosition;

    public Long getNextPosition() {
        return nextPosition;
    }

    public void setNextPosition(Long nextPosition) {
        this.nextPosition = nextPosition;
    }
}
