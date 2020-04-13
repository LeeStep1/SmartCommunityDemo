package cn.bit.api.support;

import cn.bit.common.facade.enums.OsEnum;
import lombok.Value;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * 记录应用信息
 *
 * @author jianming.fan
 * @date 2018-11-08
 */
@Value
public class AppSubject implements Serializable {
    private ObjectId appId;
    private Integer client;
    private Integer partner;
    private OsEnum osEnum;
    private ObjectId accAppId;
    private ObjectId pushAppId;

    public AppSubject(ObjectId appId, Integer client, Integer partner, OsEnum osEnum, ObjectId accAppId,
                      ObjectId pushAppId) {
        this.appId = appId;
        this.client = client;
        this.partner = partner;
        this.osEnum = osEnum;
        this.accAppId = accAppId;
        this.pushAppId = pushAppId;
    }
}
