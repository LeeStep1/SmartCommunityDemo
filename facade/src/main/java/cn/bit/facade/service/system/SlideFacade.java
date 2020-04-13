package cn.bit.facade.service.system;

import cn.bit.facade.model.system.Slide;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;


public interface SlideFacade {


    /**
     * 新增轮播图
     * @param slide
     * @return
     */
    Slide addSlide(Slide slide) throws BizException;

    /**
     * 根据id删除轮播图
     * @param id
     */
    Slide deleteSlideById(ObjectId id) throws BizException;

    /**
     * 修改轮播图
     * @param slide
     * @return
     */
    Slide updateSlide(Slide slide) throws BizException;

    /**
     * 根据ID获取轮播图
     * @param id
     * @return
     */
    Slide getSlideById(ObjectId id) throws BizException;

    /**
     * 分页获取轮播图列表
     * @param slide
     * @param page
     * @param size
     * @return
     */
    Page<Slide> getAllSlidesPage(Slide slide, int page, int size) throws BizException;

    /**
     * 获取已发布的轮播图列表（不分页）
     * @param slide
     * @return
     */
    List<Slide> getPublishedSlidesList(Slide slide) throws BizException;
    /**
     * 根据id发布轮播图
     * @param id
     * @return
     * @throws BizException
     */
    Slide publishSlideById(ObjectId id) throws BizException;

    /**
     * 根据id撤回轮播图
     * @param id
     * @return
     * @throws BizException
     */
    Slide retractSlideById(ObjectId id) throws BizException;

}
