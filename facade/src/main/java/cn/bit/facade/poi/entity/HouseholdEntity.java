package cn.bit.facade.poi.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.bit.common.facade.constant.RegexConstants;
import cn.bit.framework.constant.GlobalConstants;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 住房档案导入模板
 */
@Data
public class HouseholdEntity implements Serializable {

    @Excel(name = "*房号", width = 20)
    @NotBlank(message = "房号不能为空")
    private String roomLocation;

    @Excel(name = "*业主姓名", width = 16)
    @NotBlank(message = "业主姓名不能为空")
    @Length(max = 15, min = 1, message = "姓名最大长度为15")
    private String userName;

    @Excel(name = "*手机号码", width = 12)
    @NotBlank(message = "手机号码不能为空")
    @Pattern(regexp = GlobalConstants.REGEX_PHONE, message = "手机号码格式有误")
    private String phone;

    @Excel(name = "*身份证号", width = 20)
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = RegexConstants.REGEX_IDENTITY_CARD, message = "身份证号码不合法")
    private String identityCard;

    @Excel(name = "紧急联系人", width = 16)
    @Length(max = 15, message = "紧急联系人最大长度为15")
    private String contactsName;

    @Excel(name = "联系人电话", width = 12)
    @Pattern(regexp = GlobalConstants.REGEX_PHONE, message = "联系人电话格式有误")
    private String contactsPhone;

    @Excel(name = "*楼栋ID", isColumnHidden = true)
    @NotBlank(message = "楼栋ID不能为空")
    private String buildingId;

    @Excel(name = "*楼栋名称", isColumnHidden = true)
    @NotBlank(message = "楼栋名称不能为空")
    private String buildingName;

    @Excel(name = "*房间ID", isColumnHidden = true)
    @NotBlank(message = "房间ID不能为空")
    private String roomId;

    @Excel(name = "*房间名称", isColumnHidden = true)
    @NotBlank(message = "房间名称不能为空")
    private String roomName;
}
