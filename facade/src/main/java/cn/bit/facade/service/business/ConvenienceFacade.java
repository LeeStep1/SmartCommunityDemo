package cn.bit.facade.service.business;

import cn.bit.facade.model.business.Convenience;
import cn.bit.facade.vo.business.ConvenienceVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by fxiao
 * on 2018/4/4
 */
public interface ConvenienceFacade {

    /**
     * 新增服务
     * @param convenience
     * @return
     */
    Convenience addConvenience(Convenience convenience) throws BizException;

    /**
     * 修改服务
     * @param convenience
     * @return
     */
    Convenience editConvenience(Convenience convenience) throws BizException;

    /**
     * 删除
     * @param id
     * @return
     */
    Convenience deleteConvenience(ObjectId id) throws BizException;

    /**
     * 获取服务列表
     * @param convenience
     * @return
     */
    List<Convenience> getConvenienceList(Convenience convenience) throws BizException;

    /**
     * 分页
     * @param convenience
     * @return
     */
    Page<Convenience> queryConveniencePage(Convenience convenience) throws BizException;

    /**
     * 查询
     * @param id
     * @return
     */
    Convenience findOne(ObjectId id) throws BizException;

    /**
     * 获取该社区的服务
     * @param communityId
     * @return
     */
    List<ConvenienceVO> getByCommunityId(ObjectId communityId, Integer size) throws BizException;
}
