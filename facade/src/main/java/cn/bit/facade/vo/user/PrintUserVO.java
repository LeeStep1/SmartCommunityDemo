package cn.bit.facade.vo.user;

import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.model.vehicle.Apply;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PrintUserVO implements Serializable {

    private UserToRoom userToRoom;

    private List<UserToRoom> inhabitantList;

    private List<Apply> carList;

}
