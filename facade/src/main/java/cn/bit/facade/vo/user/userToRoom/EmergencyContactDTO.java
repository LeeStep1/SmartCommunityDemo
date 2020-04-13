package cn.bit.facade.vo.user.userToRoom;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * 房间紧急联系人
 *
 * @author decai.liu
 * @version 1.0.0
 * @create 2018.11.29
 */
@Data
public class EmergencyContactDTO implements Serializable {

    @NotBlank(message = "紧急联系人姓名不能为空")
    private String name;

    @NotBlank(message = "紧急联系人电话不能为空")
    private String phone;
}
