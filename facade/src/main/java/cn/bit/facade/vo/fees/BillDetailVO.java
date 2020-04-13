package cn.bit.facade.vo.fees;

import cn.bit.facade.model.fees.PropBillDetail;
import cn.bit.facade.model.fees.PropertyBill;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xiaoxi.lao
 *
 * @description
 * @create: 2018/3/14
 **/
@Data
public class BillDetailVO implements Serializable {

    private PropertyBill propertyBill;
    private List<PropBillDetail> billDetailList;
}
