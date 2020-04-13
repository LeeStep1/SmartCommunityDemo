package cn.bit.business.service;

import cn.bit.business.dao.BizSlideRepository;
import cn.bit.business.dao.ShopRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.SlidePublishType;
import cn.bit.facade.model.business.BizSlide;
import cn.bit.facade.model.business.Shop;
import cn.bit.facade.service.business.BizSlideFacade;
import cn.bit.facade.vo.business.SlideVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static cn.bit.facade.exception.business.BusinessException.*;

/**
 * Created by fxiao
 * on 2018/4/2
 */
@Service("bizSlideFacade")
public class BizSlideFacadeImpl implements BizSlideFacade {

    @Autowired
    private BizSlideRepository slideRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Override
    public BizSlide addSlide(BizSlide bizSlide) throws BizException {
        /*if (bizSlide.getShopName() == null) {
            Shop shop = shopRepository.findById(bizSlide.getId());
            bizSlide.setShopName(shop == null ? null : shop.getName());
        }*/
        bizSlide.setCreateAt(new Date());
        bizSlide.setUpdateAt(bizSlide.getCreateAt());
        bizSlide.setPublished(SlidePublishType.UNPUBLISH.key);
        bizSlide.setDataStatus(DataStatusType.VALID.KEY);
        return slideRepository.insert(bizSlide);
    }

    @Override
    public BizSlide editSlide(BizSlide bizSlide) throws BizException {
        BizSlide toGet = slideRepository.findByIdAndDataStatus(bizSlide.getId(), DataStatusType.VALID.KEY);
        if (toGet == null) {
            throw BIZ_SLIDE_NULL;
        }
        if (SlidePublishType.PUBLISH.key.equals(toGet.getPublished())) {
            throw BIZ_SLIDE_REJECT_EDIT;
        }
        /*if (bizSlide.getShopId() != null) {
            Shop shop = shopRepository.findByIdAndDataStatus(bizSlide.getShopId(), DataStatusType.VALID.KEY);
            bizSlide.setShopName(shop == null ? null : shop.getName());
        }*/
        bizSlide.setUpdateAt(new Date());
        return slideRepository.updateOne(bizSlide,
                bizSlide.getGotoType() == 1? "href": "shopId",
                bizSlide.getGotoType() == 1? null: "shopName");
    }

    @Override
    public BizSlide deleteSlide(ObjectId id) throws BizException {
        if (id == null) {
            throw BIZ_SLIDE_ID_NULL;
        }
        BizSlide toUpdate = new BizSlide();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        return slideRepository.updateById(toUpdate, id);
    }

    @Override
    public Page<BizSlide> querySlidePage(BizSlide bizSlide) throws BizException {
        Pageable pageable = new PageRequest(bizSlide.getPage() - 1, bizSlide.getSize(), new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<BizSlide> resultPage =
                slideRepository.findByShopNameRegexIgnoreNullAndTitleRegexIgnoreNullAndDataStatus(
                        StringUtil.makeQueryStringAllRegExp(bizSlide.getShopName()),
                        bizSlide.getTitle(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }

    @Override
    public BizSlide findOne(ObjectId id) throws BizException {
        return slideRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public List<BizSlide> getSlideList(BizSlide bizSlide) throws BizException {
        bizSlide.setDataStatus(DataStatusType.VALID.KEY);
        return slideRepository.find(bizSlide, XSort.asc("rank"));
    }

    @Override
    public Long countPublish(ObjectId communityId) throws BizException {
        return slideRepository.countByCommunityIdAndPublishedAndDataStatus(communityId, SlidePublishType.PUBLISH.key, DataStatusType.VALID.KEY);
    }

    @Override
    public List<SlideVO> getByCommunityId(ObjectId communityId) throws BizException {
        return slideRepository.findByCommunityIdAndPublishedAndDataStatusOrderByRankAsc(communityId, SlidePublishType.PUBLISH.key, DataStatusType.VALID.KEY, SlideVO.class);
//        return slideRepository.findListByCommunityId(communityId);
    }

    /**
     * 发布轮播图
     *
     * @param id
     * @return
     */
    @Override
    public BizSlide publishSlide(ObjectId id) {
        BizSlide toGet = slideRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
        if (toGet == null) {
            throw BIZ_SLIDE_NULL;
        }
        // 获取已发布的轮播图数量
        Long count = slideRepository.countByCommunityIdAndPublishedAndDataStatus(toGet.getCommunityId(), SlidePublishType.PUBLISH.key, DataStatusType.VALID.KEY);
        if (count > 4) {
            throw BIZ_SLIDE_MAX_NUM;
        }
        BizSlide toUpdate = new BizSlide();
        toUpdate.setPublished(SlidePublishType.PUBLISH.key);
        toUpdate.setPublishAt(new Date());
        return slideRepository.updateById(toUpdate, id);
    }

    /**
     * 撤销轮播图
     *
     * @param id
     * @return
     */
    @Override
    public BizSlide revokeSlide(ObjectId id) {
        BizSlide toGet = slideRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
        if (toGet == null) {
            throw BIZ_SLIDE_NULL;
        }
        BizSlide toUpdate = new BizSlide();
        toUpdate.setPublished(SlidePublishType.UNPUBLISH.key);
        toUpdate.setPublishAt(new Date());
        return slideRepository.updateById(toUpdate, id);
    }
}
