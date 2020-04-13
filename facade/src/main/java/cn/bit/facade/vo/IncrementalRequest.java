package cn.bit.facade.vo;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 增量查询请求参数
 */
@Data
public class IncrementalRequest implements Serializable {

    private Date startAt;

    private ObjectId momentId;

    /**
     * 动态类型
     */
    private Integer momentType;
    /**
     * 排序方式
     */
    @NotNull(message = "数据排序方式不能为空")
    private Integer sort;

    private Integer size = 10;

}
