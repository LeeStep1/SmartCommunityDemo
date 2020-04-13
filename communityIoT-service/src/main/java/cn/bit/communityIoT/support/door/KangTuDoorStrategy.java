package cn.bit.communityIoT.support.door;

import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.vo.communityIoT.door.DoorInfo;
import cn.bit.facade.vo.communityIoT.door.DoorInfoResult;
import cn.bit.facade.vo.communityIoT.door.DoorRequest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class KangTuDoorStrategy implements DoorStrategy {
	@Autowired
	private DoorFacade doorFacade;

	@Override
	public List<DoorInfo> listDoorInfo(DoorRequest doorRequest) {
		Card card = new Card();
		card.setKeyType(doorRequest.getKeyType());
		card.setKeyNo(doorRequest.getKeyNo());
		card.setKeyId(doorRequest.getKeyId());
		DoorInfoResult authDoorList;
		try {
			authDoorList = doorFacade.getAuthDoorList(card);
		} catch (Exception e) {
			log.warn("查询康途设备异常", e);
			return Collections.emptyList();
		}

		if (authDoorList == null || authDoorList.getData() == null || authDoorList.getData().size() == 0) {
			return Collections.emptyList();
		}
		Set<ObjectId> toQueryById = authDoorList.getData().stream().map(DoorInfo::getId).collect(Collectors.toSet());
		List<Door> doors = doorFacade.getDoorsInIds(toQueryById);
		authDoorList.getData().removeIf(doorInfo -> {
			for (Door door : doors) {
				if (door.getId().equals(doorInfo.getId())) {
					doorInfo.setDoor(door);
				}
			}
			return doorInfo.getServiceId() == null || !doorInfo.getServiceId().containsAll(doorRequest.getServiceId());
		});

		return new ArrayList<>(authDoorList.getData());
	}
}
