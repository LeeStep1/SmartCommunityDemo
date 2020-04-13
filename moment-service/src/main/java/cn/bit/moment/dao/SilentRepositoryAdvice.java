package cn.bit.moment.dao;

import cn.bit.facade.model.moment.Comment;
import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.model.moment.Silent;
import cn.bit.facade.vo.moment.SilentRequest;
import cn.bit.facade.vo.moment.SilentVO;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

public interface SilentRepositoryAdvice {

    Silent upsertSilent(SilentVO silentVO, ObjectId communityId, ObjectId operatorId);

    Silent relieveSilentUser(ObjectId id);

    Page<Silent> findPageBySilentRequest(SilentRequest silentRequest, int page, int size);

    Silent upsertSilentByMomentAndOperatorId(Moment moment, ObjectId operatorId);

    Silent upsertSilentByCommentAndOperatorId(Comment comment, ObjectId operatorId);
}
