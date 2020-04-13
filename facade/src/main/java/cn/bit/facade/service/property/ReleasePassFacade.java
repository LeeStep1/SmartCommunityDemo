package cn.bit.facade.service.property;

import cn.bit.facade.model.property.ReleasePass;
import cn.bit.facade.vo.property.ReleasePassRequest;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.List;

public interface ReleasePassFacade {

 /**
  * 添加放行条
  * @param releasePass
  * @return
  */
 ReleasePass addReleasePass(ReleasePass releasePass);

 /**
  * 删除放行条
  * @param id
  */
 boolean deleteReleasePass(ObjectId id);

 /**
  * 更改数据状态
  * @param id
  * @return
  */
 ReleasePass changeStatus(ObjectId id);

 /**
  * 修改放行条
  * @param releasePass
  * @return
  */
 ReleasePass updateReleasePass(ReleasePass releasePass);

 /**
  * 根据id获取放行条
  * @param id
  * @param communityId
  * @return
  */
 ReleasePass getReleasePassByIdAndCommunityId(ObjectId id, ObjectId communityId);

 /**
  * 列表查询放行条
  * @param communityId
  * @return
  */
 List<ReleasePass> getReleasePassList(ObjectId communityId);

 /**
  * 分页查询放行条
  * @param request
  * @param page
  * @param size
  * @return
  */
 Page<ReleasePass> getReleasePassPage(ReleasePassRequest request, int page, int size);

 /**
  * 审批放行条
  * @param releasePass
  * @return
  */
 ReleasePass checkReleasePass(ReleasePass releasePass);

}
