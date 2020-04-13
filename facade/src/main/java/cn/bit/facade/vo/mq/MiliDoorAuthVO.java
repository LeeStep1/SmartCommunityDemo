package cn.bit.facade.vo.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class MiliDoorAuthVO extends DoorAuthVO implements Serializable {
    private Set<String> miliRId;

    private Long miliCId;
}
