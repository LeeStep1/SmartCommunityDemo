package cn.bit.facade.vo.user;

import cn.bit.facade.model.user.User;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * Created by terry on 2018/1/14.
 */

public class UserVO implements Serializable{

    private User user;
    private String token;
    private String accid;
    private String imToken;
    private Boolean newGuy;
    /**
     * 上次登录时间
     */
    private Date lastLoginAt;

    public UserVO(){
        user = new User();
    }

    public UserVO(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public ObjectId getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getName() {
        return user.getName();
    }

    public String getPhone() {
        return user.getPhone();
    }

    public void setEmail(String email) {
        user.setEmail(email);
    }

    public String getNickName() { return user.getNickName(); }

    public Set<String> getRoles() {
        return user.getRoles();
    }

    public void setRoles(Set<String> roles) {
        user.setRoles(roles);
    }

    public Set<String> getPermissions() {
        return user.getPermissions();
    }

    public void setPermissions(Set<String> permissions) {
        user.setPermissions(permissions);
    }

    public void setId(ObjectId id) {
        user.setId(id);
    }

    public void setName(String name) {
        user.setName(name);
    }

    public void setPhone(String phone) {
        user.setPhone(phone);
    }

    public void setNickName(String nickName) { user.setNickName(nickName);};

    public void setTelPhone(String telPhone) { user.setTelPhone(telPhone); }

    public void setIdentityCard(String IDCard) { user.setIdentityCard(IDCard); }

    public String getIdentityCard() { return user.getIdentityCard(); }

    public void setBirthday(String birthday) { user.setBirthday(birthday);}

    /**
     * 获取头像
     * @return
     */
    public String getHeadImg() {
        return user.getHeadImg();
    }
    public void setHeadImg(String headImg) {
        user.setHeadImg(headImg);
    }
    /**
     * 获取访问凭证
     * @return
     */
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getAttach() {
        return user.getAttach();
    }

    public String getBdaddr() {
        return user.getBdaddr();
    }

    public Integer getSex() {
        return user.getSex();
    }

    public Integer getAge() {
        return user.getAge();
    }

    public Boolean getInternal() {
        return user.getInternal();
    }

    public String getImToken() { return imToken; }

    public void setImToken(String imToken) { this.imToken = imToken; }

    public String getAccid() {
        return accid;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }

    public String getBirthday(){
        return user.getBirthday();
    }

    public Boolean getNewGuy() {
        return newGuy;
    }

    public void setNewGuy(Boolean newGuy) {
        this.newGuy = newGuy;
    }

    public Date getCreateAt() {
        return user.getCreateAt();
    }

    public Date getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Date lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
