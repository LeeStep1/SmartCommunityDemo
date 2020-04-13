package cn.bit.property.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.PublishStatusType;
import cn.bit.facade.enums.PushStatusType;
import cn.bit.facade.model.property.Notice;
import cn.bit.facade.service.property.NoticeFacade;
import cn.bit.facade.vo.property.NoticeRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.property.dao.NoticeRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;

import static cn.bit.facade.exception.property.PropertyBizException.NOTICE_NULL;

@Service("noticeFacade")
@Slf4j
public class NoticeFacadeImpl implements NoticeFacade {

    @Autowired
    private NoticeRepository noticeRepository;

    /**
     * 新增公告
     * @param notice
     * @return
     */
    @Override
    public Notice addNotice(Notice notice) {
        notice.setCreateAt(new Date());
        notice.setPublishAt(new Date());
        notice.setPublishStatus(PublishStatusType.PUBLISHED.key);
        notice.setDataStatus(DataStatusType.VALID.KEY);
        notice.setPushStatus(PushStatusType.PUSHED.key());
        Notice insert = noticeRepository.insert(notice);
        if (insert == null) {
            throw BizException.DB_INSERT_RESULT_0;
        }
        return insert;
    }

    /**
     * 根据id删除公告
     * @param id
     * @return
     */
    @Override
    public Notice deleteNoticeById(ObjectId id) {
        Notice toUpdate = new Notice();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        return noticeRepository.updateById(toUpdate, id);
    }

    /**
     * （废弃）
     * 修改公告
     * @param notice
     * @return
     */
    @Override
    public Notice updateNotice(Notice notice) {
        Notice item = noticeRepository.findByIdAndDataStatus(notice.getId(), DataStatusType.VALID.KEY);
        if(item == null){
            throw NOTICE_NULL;
        }
        notice.setId(null);
        notice.setUpdateAt(new Date());
        Notice toUpdate = noticeRepository.updateById(notice, item.getId());
        return toUpdate;
    }

    /**
     * 根据id获取详细公告信息
     * @param id
     * @return
     */
    @Override
    public Notice getNoticeById(ObjectId id) {
        return noticeRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    /**
     * 分页获取公告信息
     * @param entity
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Notice> getNoticePage(NoticeRequest entity, int page, int size) {
        if (StringUtil.isNotNull(entity.getEndAt())) {
            entity.setEndAt(DateUtils.addDay(entity.getEndAt(), 1));
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Notice> resultPage =
                noticeRepository.findByCommunityIdAndCreateAtGreaterThanEqualIgnoreNullAndCreateAtLessThanIgnoreNullAndDataStatus(
                entity.getCommunityId(), entity.getStartAt(), entity.getEndAt(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }

    /**
     * （废弃）
     * 根据id发布公告
     * @param id
     * @return
     */
    @Override
    public Notice publishNoticeById(ObjectId id) {
        Date now = new Date();
        Notice toPublish = new Notice();
        toPublish.setPublishStatus(PublishStatusType.PUBLISHED.key);
        toPublish.setPublishAt(now);
        toPublish.setUpdateAt(now);
        return noticeRepository.updateById(toPublish, id);
    }

    /**
     * （废弃）
     * 根据id撤销公告
     * @param id
     * @return
     */
    @Override
    public Notice repealNoticeById(ObjectId id) {
        Date now = new Date();
        Notice notice = new Notice();
        notice.setPublishStatus(PublishStatusType.REPEAL.key);
        notice.setPushStatus(PushStatusType.UNPUSHED.key());
        notice.setUpdateAt(now);
        return noticeRepository.updateById(notice, id);
    }
}
