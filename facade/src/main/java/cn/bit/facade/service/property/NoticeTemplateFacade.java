package cn.bit.facade.service.property;

import cn.bit.facade.model.property.NoticeTemplate;
import cn.bit.facade.vo.property.NoticeTemplatePageQuery;
import cn.bit.facade.vo.property.NoticeTemplateVO;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.List;

public interface NoticeTemplateFacade {

    /**
     * 新增模板
     * @param template
     * @return
     */
    NoticeTemplate addNoticeTemplate(NoticeTemplate template);

    /**
     * 根据id删除模板
     * @param id
     * @return
     */
    NoticeTemplate deleteNoticeTemplateById(ObjectId id);

    /**
     * 修改模板
     * @param template
     * @return
     */
    NoticeTemplate modifyNoticeTemplate(NoticeTemplate template);

    /**
     * 根据id获取模板详情
     * @param id
     * @return
     */
    NoticeTemplate findNoticeTemplateById(ObjectId id);

    /**
     * 分页获取模板信息
     * @param query
     * @return
     */
    Page<NoticeTemplate> listNoticeTemplates(NoticeTemplatePageQuery query);

    /**
     * 根据社区获取模板id及title列表
     * @param communityId
     * @return
     */
    List<NoticeTemplateVO> listNoticeTemplates(ObjectId communityId);
}
