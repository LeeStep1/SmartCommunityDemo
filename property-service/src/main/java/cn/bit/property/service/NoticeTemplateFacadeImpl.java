package cn.bit.property.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.property.NoticeTemplate;
import cn.bit.facade.service.property.NoticeTemplateFacade;
import cn.bit.facade.vo.property.NoticeTemplatePageQuery;
import cn.bit.facade.vo.property.NoticeTemplateVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.property.dao.NoticeTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static cn.bit.facade.exception.CommonBizException.DATA_INVALID;

@Service("noticeTemplateFacade")
@Slf4j
public class NoticeTemplateFacadeImpl implements NoticeTemplateFacade {

    @Autowired
    private NoticeTemplateRepository templateRepository;

    /**
     * 新增模板
     *
     * @param template
     * @return
     */
    @Override
    public NoticeTemplate addNoticeTemplate(NoticeTemplate template) {
        template.setDataStatus(DataStatusType.VALID.KEY);
        template.setCreateAt(new Date());
        template.setUpdateAt(template.getCreateAt());
        return templateRepository.insert(template);
    }

    /**
     * 根据id删除模板
     *
     * @param id
     * @return
     */
    @Override
    public NoticeTemplate deleteNoticeTemplateById(ObjectId id) {
        NoticeTemplate toGet = templateRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
        if (toGet == null) {
            throw DATA_INVALID;
        }
        NoticeTemplate toDelete = new NoticeTemplate();
        toDelete.setDataStatus(DataStatusType.INVALID.KEY);
        toDelete.setUpdateAt(new Date());
        return templateRepository.updateByIdAndDataStatus(toDelete, id, DataStatusType.VALID.KEY);
    }

    /**
     * 修改模板
     *
     * @param toModify
     * @return
     */
    @Override
    public NoticeTemplate modifyNoticeTemplate(NoticeTemplate toModify) {
        NoticeTemplate toGet = templateRepository.findByIdAndDataStatus(toModify.getId(), DataStatusType.VALID.KEY);
        if (toGet == null) {
            throw DATA_INVALID;
        }
        toModify.setCommunityId(null);
        toModify.setCreateAt(null);
        toModify.setDataStatus(null);
        toModify.setUpdateAt(new Date());
        return templateRepository.updateWithUnsetIfNullThumbnailUrlByIdAndDataStatus(
                toModify, toModify.getId(), DataStatusType.VALID.KEY);
    }

    /**
     * 根据id获取模板详情
     *
     * @param id
     * @return
     */
    @Override
    public NoticeTemplate findNoticeTemplateById(ObjectId id) {
        return templateRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    /**
     * 分页获取模板信息
     *
     * @param query
     * @return
     */
    @Override
    public Page<NoticeTemplate> listNoticeTemplates(NoticeTemplatePageQuery query) {
        if(query == null){
            query = new NoticeTemplatePageQuery();
        }
        if (query.getCommunityId() == null) {
            log.info("分页查询公共模板社区ID为空，直接返回null");
            return new Page<>();
        }
        query.setPage(Optional.ofNullable(query.getPage()).orElse(1));
        query.setSize(Optional.ofNullable(query.getSize()).orElse(10));

        Pageable pageable = new PageRequest(
                query.getPage() - 1, query.getSize(), new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<NoticeTemplate> templatePage =
                templateRepository.findByCommunityIdAndNameRegexAndTitleRegexAndDataStatusAllIgnoreNull(
                        query.getCommunityId(), StringUtil.makeQueryStringAllRegExp(query.getName()),
                        StringUtil.makeQueryStringAllRegExp(query.getTitle()), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(templatePage);
    }

    /**
     * 根据社区获取模板id及title列表
     *
     * @param communityId
     * @return
     */
    @Override
    public List<NoticeTemplateVO> listNoticeTemplates(ObjectId communityId) {
        return templateRepository.findByCommunityIdAndDataStatus(communityId, DataStatusType.VALID.KEY, NoticeTemplateVO.class);
    }
}
