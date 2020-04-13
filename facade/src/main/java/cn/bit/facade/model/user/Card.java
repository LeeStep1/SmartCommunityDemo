package cn.bit.facade.model.user;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@Document(collection = "U_CARD")
@CompoundIndexes({
        @CompoundIndex(def = "{'keyNo' : 1, 'keyId' : 1}", background = true),
        @CompoundIndex(def = "{'userId' : 1, 'communityId' : 1, 'keyType' : -1, 'roomName' : 1}", background = true),
        @CompoundIndex(def = "{'communityId' : 1, 'keyNo' : 1, 'keyType' : 1}", background = true)
})
public class Card implements Serializable {
    @Id
    private ObjectId id;
    /**
     * 用户ID
     */
    @Indexed(background = true)
    private ObjectId userId;
    /**
     * 用户姓名
     */
    private String name;
    /**
     * 用户联系电话
     */
    private String phone;
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 卡片类型  1:手机MAC； 2:蓝牙卡MAC； 4:IC卡UID； 8:二维码信息号
     */
    @NotNull(message = "凭证类型不能为空", groups = {GetCertificateDevice.class, QueryKeyType.class})
    private Integer keyType;
    /**
     * 卡片流水号
     */
    @NotNull(message = "凭证ID不能为空", groups = {GetCertificateDevice.class})
    @Indexed(background = true)
    private String keyId;
    /**
     * 卡号
     */
    @NotNull(message = "卡号不能为空", groups = {QueryKeyType.class})
    @Indexed(background = true)
    private String keyNo;
    /**
     * 备注
     */
    private String remark;

    /**
     * 是否读取(0:未读取；1：已读取)
     */
    private Integer isProcessed;

    /**
     * 是否有效(0:失效；1：有效；2：申请中)
     */
    private Integer validState;
    /**
     * 过期时间
     */
    @Indexed(background = true)
    private Date processTime;
    /**
     * 可使用次数
     */
    private Integer useTimes;

    /**
     * 开始时间
     */
    private Date startDate;

    /**
     * 结束时间
     */
    private Date endDate;

    /**
     * 控制类型（1表示有效期控制；2表示时段控制；4表示星期控制；8表示直达召梯控制）
     * 默认是8
     */
    private Integer controlType;

    /**
     * 房间名称
     */
    private Set<String> roomName;
    /**
     * 创建人ID
     */
    private ObjectId createId;
    /**
     * 创建时间
     */
    private Date createAt;
    /**
     * 更新时间
     */
    private Date updateAt;
    /**
     * 数据状态（1：有效；0：无效）
     */
    @Indexed(background = true)
    private Integer dataStatus;

    /**
     * 协议
     */
    private String protocolKey;

    /**
     * 申请二维码记录房间ID
     */
    private ObjectId roomId;

    public interface GetCertificateDevice {
    }

    public interface GetCardInfo {
    }

    public interface QueryKeyType {
    }
}
