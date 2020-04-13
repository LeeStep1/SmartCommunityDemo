package cn.bit.moment.dao;

import cn.bit.facade.model.moment.Comment;
import cn.bit.facade.vo.IncrementalRequest;
import org.bson.types.ObjectId;

import java.util.List;

public interface CommentRepositoryAdvice {

    Comment updateNumByIdAndFieldName(ObjectId commentId, String fieldName, int num);

    List<Comment> incrementalCommentList(IncrementalRequest incrementalRequest);

    List<Comment> incrementalMyCommentList(IncrementalRequest incrementalRequest, ObjectId communityId, ObjectId uid);

    Long statisticsComment(ObjectId communityId, ObjectId creatorId);
}
