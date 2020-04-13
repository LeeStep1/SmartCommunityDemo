package cn.bit.facade.vo.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class ElevatorAuthVO extends DeviceAuthVO implements Serializable {
}
