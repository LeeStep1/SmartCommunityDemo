package cn.bit.framework.data.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用分页结果
 * Created by Administrator on 2018/1/22 0022.
 */
public class Page<T> implements Serializable {

    private static int DEFAULT_PAGE_SIZE = 20;

    @Setter
    private int pageSize;

    @Getter
    @Setter
    private long total;

    @Getter
    @Setter
    private List<T> records;

    @Getter
    @Setter
    private long currentPage;

    public long getTotalPage() {
        if (total % pageSize == 0)
            return total / pageSize;
        else
            return total / pageSize + 1;
    }

    public boolean hasNextPage() {
        return this.getCurrentPage() < this.getTotalPage() - 1;
    }

    /**
     * 该页是否有上一页.
     */
    public boolean hasPreviousPage() {
        return this.getCurrentPage() > 1;
    }

    /**
     * 构造方法，只构造空页.
     */
    public Page() {
        this(0, 0, DEFAULT_PAGE_SIZE, new ArrayList<T>());
    }

    public Page(long currentPage, long total, int pageSize, List<T> records) {
        setParam(currentPage, total, pageSize, records);
    }

    public void setParam(long currentPage, long total, int pageSize, List<T> records) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.total = total;
        this.records = records;
    }
}
