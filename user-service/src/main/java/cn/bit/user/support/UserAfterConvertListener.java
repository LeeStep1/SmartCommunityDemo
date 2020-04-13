package cn.bit.user.support;

import cn.bit.facade.model.user.User;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.string.StringUtil;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class UserAfterConvertListener extends AbstractMongoEventListener<User> {

    @Override
    public void onAfterConvert(AfterConvertEvent<User> event) {
        super.onAfterConvert(event);

        User user = event.getSource();
        setAge(user);
        // 身份证脱敏处理
        setSensitiveIdentityCard(user);
        // 手机号脱敏处理
//        setSensitivePhone(user);

        //如果用户名称、昵称为空则用脱敏的手机号作为用户名称
        setSensitiveName(user);
    }

    private void setAge(User user) {
        Date birthday = DateUtils.getDateByStr(user.getBirthday());
        if (birthday == null) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int yearOfNow = calendar.get(Calendar.YEAR);
        int monthOfNow = calendar.get(Calendar.MONTH);
        int dayOfNow = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTime(birthday);
        int yearOfBirthday = calendar.get(Calendar.YEAR);
        int monthOfBirthday = calendar.get(Calendar.MONTH);
        int dayOfBirthday = calendar.get(Calendar.DAY_OF_MONTH);

        int age = yearOfNow - yearOfBirthday;
        if (age < 0) {
            return;
        }

        if (monthOfNow - monthOfBirthday < 0 || dayOfNow - dayOfBirthday < 0) {
            age--;
        }
        user.setAge(age);
    }

    private void setSensitiveIdentityCard(User user) {
        if (StringUtil.isBlank(user.getIdentityCard())) {
            return;
        }
        user.setTempIdentityCard(user.getIdentityCard());
        user.setIdentityCard(StringUtil.desensitize(user.getIdentityCard(), 3, 4));
    }

    private void setSensitivePhone(User user) {
        if(!StringUtil.isNotNull(user.getPhone())){
            return;
        }
        user.setPhone(StringUtil.desensitize(user.getPhone(), 3, 2));
    }

    private void setSensitiveName(User user) {
        if (StringUtil.isBlank(user.getPhone())) {
            return;
        }

        if (!StringUtil.isBlank(user.getNickName())) {
            return;
        }

        // 昵称为空使用脱敏手机号
        if (StringUtil.isBlank(user.getNickName())) {
            user.setNickName(StringUtil.desensitize(user.getPhone(), 3, 2));
        }
        //现在需要实名，所以不需要脱敏的手机号作为用户名字
//        user.setName(String.valueOf(chars));
    }

}
