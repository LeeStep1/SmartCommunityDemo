package cn.bit.facade.service.property;

import cn.bit.facade.model.property.Notice;
import cn.bit.facade.vo.property.NoticeRequest;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

public interface NoticeFacade {

    /**
     * 新增公告
     * @param notice
     * @return
     */
    Notice addNotice(Notice notice);

    /**
     * 根据id删除公告
     * @param id
     * @return
     */
    Notice deleteNoticeById(ObjectId id);

    /**
     * 修改公告
     * @param notice
     * @return
     */
    @Deprecated
    Notice updateNotice(Notice notice);

    /**
     * 根据id获取详细公告信息
     * @param id
     * @return
     */
    Notice getNoticeById(ObjectId id);

    /**
     * 分页获取公告信息
     * @param entity
     * @param page
     * @param size
     * @return
     */
    Page<Notice> getNoticePage(NoticeRequest entity, int page, int size);

    /**
     * 根据id发布公告
     * @param id
     * @return
     */
    @Deprecated
    Notice publishNoticeById(ObjectId id);

    /**
     * 根据id撤销公告
     * @param id
     * @return
     */
    @Deprecated
    Notice repealNoticeById(ObjectId id);
}
