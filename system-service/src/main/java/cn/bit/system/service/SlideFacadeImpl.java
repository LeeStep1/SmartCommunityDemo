package cn.bit.system.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.SlidePublishType;
import cn.bit.facade.model.system.Slide;
import cn.bit.facade.service.system.SlideFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.exceptions.BizException;
import cn.bit.system.dao.SlideRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("slideFacade")
@Slf4j
public class SlideFacadeImpl implements SlideFacade {


    @Autowired
    private SlideRepository slideRepository;

    /**
     * 新增轮播图
     * @param slide
     * @return
     */
    @Override
    public Slide addSlide(Slide slide) throws BizException {
        Date now = new Date();
        if (slide.getPublished() == null) {
            slide.setPublished(SlidePublishType.UNPUBLISH.key);
        }
        // 发布状态
        if (slide.getPublished() == SlidePublishType.PUBLISH.key) {
            slide.setPublishAt(now);
        }
        slide.setCreateAt(now);
        slide.setUpdateAt(now);
        slide.setDataStatus(DataStatusType.VALID.KEY);
        return slideRepository.insert(slide);
    }

    /**
     * 根据id删除轮播图
     * @param id
     */
    @Override
    public Slide deleteSlideById(ObjectId id) throws BizException {
        Slide slide = new Slide();
        slide.setPublished(SlidePublishType.UNPUBLISH.key);
        slide.setDataStatus(DataStatusType.INVALID.KEY);
        slide.setUpdateAt(new Date());
        return slideRepository.updateById(slide, id);
    }

    /**
     * 修改轮播图
     * @param slide
     * @return
     */
    @Override
    public Slide updateSlide(Slide slide) throws BizException {
        ObjectId id = slide.getId();
        slide.setId(null);
        slide.setUpdateAt(new Date());
        slide.setDataStatus(DataStatusType.VALID.KEY);
        return slideRepository.updateById(slide, id);
    }

    /**
     * 根据ID获取轮播图
     * @param id
     * @return
     */
    @Override
    public Slide getSlideById(ObjectId id) throws BizException {
        return slideRepository.findById(id);
    }

    /**
     * 分页获取已发布的轮播图列表
     * @param slide
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Slide> getAllSlidesPage(Slide slide, int page, int size) throws BizException {
        slide.setDataStatus(DataStatusType.VALID.KEY);
        return slideRepository.findPage(slide, page, size, XSort.desc("publishAt"));
    }

    /**
     * 获取已发布的轮播图列表（不分页）
     * @param slide
     * @return
     */
    @Override
    public List<Slide> getPublishedSlidesList(Slide slide) throws BizException {
        slide.setPublished(SlidePublishType.PUBLISH.key);
        slide.setDataStatus(DataStatusType.VALID.KEY);
        return slideRepository.find(slide, XSort.desc("publishAt"));
    }

    /**
     * 根据id发布轮播图
     * @param id
     * @return
     * @throws BizException
     */
    @Override
    public Slide publishSlideById(ObjectId id) throws BizException {
        Slide slide = new Slide();
        slide.setPublished(SlidePublishType.PUBLISH.key);
        slide.setPublishAt(new Date());
        slide.setUpdateAt(new Date());
        return slideRepository.updateById(slide, id);
    }

    /**
     * 根据id撤回轮播图
     * @param id
     * @return
     * @throws BizException
     */
    @Override
    public Slide retractSlideById(ObjectId id) throws BizException {
        Slide slide = new Slide();
        slide.setPublished(SlidePublishType.UNPUBLISH.key);
        slide.setUpdateAt(new Date());
        return slideRepository.updateById(slide, id);
    }
}
