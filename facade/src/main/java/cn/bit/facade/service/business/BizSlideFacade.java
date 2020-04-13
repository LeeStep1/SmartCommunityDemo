package cn.bit.facade.service.business;

import cn.bit.facade.model.business.BizSlide;
import cn.bit.facade.vo.business.SlideVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by fxiao
 * on 2018/4/2
 */
public interface BizSlideFacade {
    /**
     * 新增轮播图
     * @param bizSlide
     * @return
     */
    BizSlide addSlide(BizSlide bizSlide) throws BizException;
    /**
     * 修改轮播图
     * @param bizSlide
     * @return
     */
    BizSlide editSlide(BizSlide bizSlide) throws BizException;
    /**
     * 删除
     * @param id
     * @return
     */
    BizSlide deleteSlide(ObjectId id) throws BizException;
    /**
     * 轮播图分类
     * @param bizSlide
     * @return
     */
    Page<BizSlide> querySlidePage(BizSlide bizSlide) throws BizException;
    /**
     * 查询
     * @param id
     * @return
     */
    BizSlide findOne(ObjectId id) throws BizException;
    /**
     * 获取列表
     * @param bizSlide
     * @return
     */
    List<BizSlide> getSlideList(BizSlide bizSlide) throws BizException;

    /**
     * 统计轮播图的发布数量
     * @param communityId
     * @return
     */
    Long countPublish(ObjectId communityId) throws BizException;

    /**
     * 获取该社区的发布的轮播图
     * @param communityId
     * @return
     */
    List<SlideVO> getByCommunityId(ObjectId communityId) throws BizException;

    /**
     * 发布轮播图
     * @param id
     * @return
     */
	BizSlide publishSlide(ObjectId id);

    /**
     * 撤销轮播图
     * @param id
     * @return
     */
    BizSlide revokeSlide(ObjectId id);
}
