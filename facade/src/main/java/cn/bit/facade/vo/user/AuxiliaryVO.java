package cn.bit.facade.vo.user;

import cn.bit.facade.model.user.User;
import cn.bit.framework.constant.GlobalConstants;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.Pattern;

public class AuxiliaryVO {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空", groups = {User.Add.class})
    @Pattern(regexp = GlobalConstants.REGEX_PHONE, message = "手机号码格式有误")
    @Indexed(background = true, unique = true)
    private String phone;

    /**
     * 昵称
     */
    private String nickName;

    private ObjectId userId;

    private ObjectId roomId;

    private Integer relationship;

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public ObjectId getRoomId() {
        return roomId;
    }

    public void setRoomId(ObjectId roomId) {
        this.roomId = roomId;
    }

    public Integer getRelationship() {
        return relationship;
    }

    public void setRelationship(Integer relationship) {
        this.relationship = relationship;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}


