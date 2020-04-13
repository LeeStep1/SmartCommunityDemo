package cn.bit.facade.vo.user;

import cn.bit.framework.constant.GlobalConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 米粒用户信息表
 */
@Data
@NoArgsConstructor
public class Proprietor implements Serializable {

    /**
     * id
     */
    private Long proprietor_id;
    /**
     * 名称
     */
    @NotBlank(message = "业主名称不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = GlobalConstants.REGEX_PHONE, message = "手机号码格式有误")
    private String phone;

    @NotBlank(message = "身份证不能为空")
    private String id_number;

    @NotNull(message = "社区ID不能为空")
    private Long community_id;

    @NotNull(message = "房间ID不能为空")
    private Long room_id;

    @NotBlank(message = "用户关系不能为空")
    private String role;

    @NotBlank(message = "性别不能为空")
    private String sex;

    /**
     * 添加家人成功后，如果业主下已有云对讲设备，返回次id
     */
    private Long yun_prortetor_id;

    public Proprietor(String name, String phone, String id_number, Long community_id, Long room_id, String sex) {
        this.name = name;
        this.phone = phone;
        this.id_number = id_number;
        this.community_id = community_id;
        this.room_id = room_id;
        this.sex = sex;
    }
}
