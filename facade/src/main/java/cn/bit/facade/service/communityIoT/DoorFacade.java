package cn.bit.facade.service.communityIoT;

import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.vo.community.zhfreeview.CommunityParams;
import cn.bit.facade.vo.community.zhfreeview.DeviceParam;
import cn.bit.facade.vo.community.zhfreeview.DeviceParams;
import cn.bit.facade.vo.communityIoT.DeviceRequest;
import cn.bit.facade.vo.communityIoT.door.DoorInfo;
import cn.bit.facade.vo.communityIoT.door.DoorInfoResult;
import cn.bit.facade.vo.communityIoT.door.DoorRequest;
import cn.bit.facade.vo.communityIoT.door.DoorVo;
import cn.bit.facade.vo.communityIoT.door.freeview.UserFeature;
import cn.bit.facade.vo.mq.FreeViewDoorAuthVO;
import cn.bit.facade.vo.mq.KangTuDoorAuthVO;
import cn.bit.facade.vo.mq.MiliDoorAuthVO;
import cn.bit.facade.vo.user.card.CardVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bson.types.ObjectId;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DoorFacade {
    Door getDoorById(ObjectId id);

    /**
     * 添加门禁消息
     * @param entity
     * @return
     */
    Door addDoor(Door entity);

    void saveDoors(List<Door> doors);

    Door deleteDoor(ObjectId id);

    Door updateDoor(Door entity);

    /**
     *
     * @param doorRequest
     * @return
     */
    List<Door> getServiceDoors(DoorRequest doorRequest) throws BizException;

    /**
     *
     * @param doorRequest
     * @return
     */
//    List<Door> getAllDoors(DoorRequest doorRequest);

    /**
     *
     * @param doorRequest
     * @param page
     * @param size
     * @return
     */
    Page<Door> getBluetoothDoors(DoorRequest doorRequest, int page, int size);

    /**
     * 根据设备ID查询
     * @param deviceId
     * @return
     */
    boolean getDoorByDeviceId(Long deviceId);

    /**
     * 根据mac地址查门禁
     * @param communityId
     * @param MacAddress
     * @return
     */
    Door getDoorByCommunityIdAndMacAddress(ObjectId communityId, String MacAddress);

    /**
     * 绑定门禁设备到社区和楼栋
     * @param entity
     * @return
     */
    Door bindDoor(Door entity);

    /**
     * 查找门禁档案
     * @param entity
     * @param page
     * @param size
     * @return
     */
    Page<DoorVo> getAllDoorsRecord(Door entity, Integer page, Integer size);

    /**
     * 根据社区ID查询
     * @param communityId
     * @return
     */
    List<Door> getDoorByCommunityIdAndBuildingId(ObjectId communityId, ObjectId buildingId);

    /**
     * 根据id集合查找对应的门禁设备
     * @param doorIds
     * @return
     */
    List<Door> getDoorsInIds(Set<ObjectId> doorIds);

    /**
     * 查看卡号的授权门禁列表
     * @param card
     * @return
     * @throws Exception
     */
    DoorInfoResult getAuthDoorList(Card card);

    /**
     * 获取社区门和楼栋门
     * @param buildingIds
     * @param communityId
     * @return
     */
    List<Door> getBuildingAndCommunityDoor(Set<ObjectId> buildingIds, ObjectId communityId);

    /**
     * 根据品牌获取社区门和楼栋门
     * @param buildingIds
     * @param communityId
     * @param brandNo
     * @return
     */
    List<Door> getBuildingAndCommunityDoorByBrandNo(Set<ObjectId> buildingIds, ObjectId communityId, Integer brandNo);

    /**
     * 根据设备ID查询
     * @param deviceId
     * @return
     */
    Door getDoorByDeviceIdAndBrandNoAndDeviceCode(Long deviceId, Integer brandNo,String deviceCode);

    /**
     * 通过附加条件获取社区门和楼栋门
     * @param doorRequest
     * @return
     */
    List<Door> getBuildingAndCommunityDoorByDoorRequest(DoorRequest doorRequest);

    List<Object> getAllAuthListInDoors(Card card, List<Object> authDeviceList);
    /*=====================================[远程调用接口]==========================================*/

    /**
     * 远程开门
     * @param door 门禁设备
     * @param appId 第三方所需id
     * @return
     */
    Boolean remoteOpenDoor(Door door, String appId) throws Exception;

    JSONObject viewCardPermissionDetail(Card card);

    /**
     * 查看设备详情
     * @param card
     * @return
     */
    JSONObject getCardPermissionDetail(Card card);

    /**
     * 根据社区ID统计门禁数量
     * @param communityId
     * @return
     */
    Long countDoorByCommunityId(ObjectId communityId);

    /**
     * 根据社区ID统计故障门禁数量
     * @param communityId
     * @return
     */
    Long countFaultedDoorByCommunityId(ObjectId communityId);

    /**
     * 更新门禁权限
     * @param deviceAuthVO
     * @return
     */
    boolean updateKangTuDoorAuth(KangTuDoorAuthVO deviceAuthVO);

    /**
     * 更新米立权限
     * @param miliDoorVO
     * @throws Exception
     */
    Set<Long> updateMiliDoorAuth(MiliDoorAuthVO miliDoorVO) throws Exception;

    /**
     * 更新全视通权限
     * @param freeViewDoorAuthVO
     * @throws Exception
     */
    void updateFreeViewDoorAuth(FreeViewDoorAuthVO freeViewDoorAuthVO) throws Exception;

    /**
     * 删除全视通权限
     * @param freeViewDoorAuthVO
     * @throws Exception
     */
    void deleteFreeViewDoorAuth(FreeViewDoorAuthVO freeViewDoorAuthVO) throws Exception;

    /**
     * 删除米立权限
     * @param miliUId
     */
    void deleteMiliDoorAuth(String miliUId) throws Exception;

    /**
     * 删除康途权限
     * @param deviceAuthVO
     */
    boolean deleteKangTuDoorAuth(KangTuDoorAuthVO deviceAuthVO);

    /**
     * 覆盖康途权限
     * @param deviceAuthVO
     */
    boolean coverKangTuDoorAuth(KangTuDoorAuthVO deviceAuthVO);

    /**
     * 覆盖米立授权
     * @param miliDoorAuthVO
     * @return
     */
    Set<Long> coverMiliDoorAuth(MiliDoorAuthVO miliDoorAuthVO) throws Exception;

    /**
     * 覆盖全视通权限
     * @param freeViewDoorAuthVO
     */
    void coverFreeViewDoorAuth(FreeViewDoorAuthVO freeViewDoorAuthVO) throws Exception;

    /**
     * 全视通开启读卡模式
     * @param door
     * @param phoneLast4Num
     */
    void openDeviceReadCard(Door door, String phoneLast4Num);

    /**
     * 根据URL获取返回值
     * @param url
     * @param params
     * @return
     */
    String getFreeViewByUrlPOST(String url, CommunityParams params) throws UnsupportedEncodingException;

    /**
     * 根据URL获取返回值
     * @param url
     * @param params
     * @return
     * @throws URISyntaxException
     */
    String getFreeViewByUrlGET(String url, Map params) throws UnsupportedEncodingException;

    /**
     * PUT
     * @param url
     * @param object
     * @return
     * @throws UnsupportedEncodingException
     */
    String getFreeViewByUrlPUT(String url, Object object) throws UnsupportedEncodingException;

    /**
     *
     * @param cardVO
     * @param doors
     */
    void applyFreeViewUserCard(CardVO cardVO, List<Door> doors, Integer client);

    /**
     * 全视通实体卡销卡
     * @param keyNo
     */
    void deleteFreeViewUserCard(String keyNo);

    List<DoorInfo> getFreeViewAuthDoor(Set<ObjectId> buildingId, ObjectId userId, ObjectId communityId);

    /**
     * 获取用户蓝牙或远程门禁列表
     * @param doorRequest
     * @return
     */
    List<DoorInfo> listDoorInfoByDoorRequest(DoorRequest doorRequest);

    void applyFreeViewSecretCode(CardVO finalCardVO, List<Door> freeViewDoors);

    Door updateFreeViewDeviceState(DeviceParam deviceParam, DeviceParams deviceParams);


    /**
     * 添加设备人脸识别
     * @param userFeature
     * @return
     */
    String addUserFeatureInDoor(UserFeature userFeature);

    void deleteUserFeatureInDoor(String featureCode);

    JSONArray getUserFeatureInDoor(ObjectId userId);

    /**
     * 查询社区下的所有门禁列表
     * @param communityId
     * @return
     */
    List<Door> getDoorsByCommunityId(ObjectId communityId);

    String getAccessToken();

    void setAccessToken(String accessToken);

    List<Door> getDoorByTerminalCode(DeviceRequest deviceRequest);

    /**
     * 非掌居宝用户删除权限
     * @param room
     * @param keyNo
     */
    void deleteFreeViewCardWithoutUserInfo(Room room, String keyNo);
}
