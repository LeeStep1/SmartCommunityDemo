package cn.bit.facade.vo.statistics;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ExpirePropertyBillResponse implements Serializable {
    /**
     * 超期业主分块集合(只显示前20个)
     */
    private List<Section> proprietorSections;

    @Data
    public static class Section extends cn.bit.facade.vo.statistics.Section {
        /**
         * 总数
         */
        private Long total;
        /**
         * 手机号
         */
        private String phone;
        /**
         * 房间名称
         */
        private String roomName;
    }
}
