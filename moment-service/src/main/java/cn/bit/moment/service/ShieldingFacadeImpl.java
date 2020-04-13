package cn.bit.moment.service;

import cn.bit.facade.enums.*;
import cn.bit.facade.model.moment.Comment;
import cn.bit.facade.model.moment.Message;
import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.service.moment.ShieldingFacade;
import cn.bit.facade.vo.moment.ShieldingVO;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.moment.dao.CommentRepository;
import cn.bit.moment.dao.MessageRepository;
import cn.bit.moment.dao.MomentRepository;
import cn.bit.moment.dao.SilentRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

import static cn.bit.facade.exception.moment.MomentException.*;

@Component("shieldingFacade")
@Slf4j
public class ShieldingFacadeImpl implements ShieldingFacade {

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SilentRepository silentRepository;

    /**
     * 管理员屏蔽言论
     *
     * @param shieldingVO
     * @param operatorId
     * @return
     */
    @Override
    public Message shieldingSpeechByManager(ShieldingVO shieldingVO, ObjectId operatorId) {
        return shieldingSpeech(shieldingVO, operatorId, Boolean.FALSE);
    }

    /**
     * 系统自动屏蔽言论
     *
     * @param shieldingVO
     * @return
     */
    @Override
    public Message shieldingSpeechBySystem(ShieldingVO shieldingVO) {
        shieldingVO.setReason("被举报次数过多");
        return shieldingSpeech(shieldingVO, null, Boolean.TRUE);
    }

    private Message shieldingSpeech(ShieldingVO shieldingVO, ObjectId operatorId, boolean isAutoShielding) {
        if (shieldingVO.getSpeechId() == null) {
            throw SPEECH_ID_IS_NULL;
        }
        if (shieldingVO.getType() == null) {
            throw TYPE_IS_NULL;
        }
        if (!StringUtil.isNotNull(shieldingVO.getReason())) {
            throw REASON_IS_NULL;
        }
        // 屏蔽动态
        if (shieldingVO.getType() == SpeechType.MOMENT.getKey()) {
            this.checkMoment(shieldingVO.getSpeechId());
            Moment moment = new Moment();
            moment.setId(shieldingVO.getSpeechId());
            moment.setShieldingReason(shieldingVO.getReason());
            moment.setShieldingAt(new Date());
            moment.setStatus(MomentStatusType.HANDSHIELDING.getKey());
            if (isAutoShielding) {//系统自动屏蔽
                moment.setStatus(MomentStatusType.AUDOSHIELDING.getKey());
            }
            moment = momentRepository.updateOne(moment);
            if (moment == null) {
                return null;
            }
            // 禁言记录中加入被屏蔽的动态ID
            silentRepository.upsertSilentByMomentAndOperatorId(moment, operatorId);

            // 写入消息记录表
            Message toAddMsg = this.packageShieldingMessageByMoment(moment);
            toAddMsg.setType(MomentMessageType.SHIELDINGMOMENT.getKey());
            toAddMsg.setNoticeTo(moment.getCreatorId());
            // 操作人员(系统自动屏蔽时，没有操作人员)
            toAddMsg.setCreatorId(operatorId);
            toAddMsg.setContent(moment.getShieldingReason());
            // 消息创建时间应该与屏蔽时间一致
            toAddMsg.setCreateAt(moment.getShieldingAt());
            toAddMsg.setDataStatus(DataStatusType.VALID.KEY);
            toAddMsg = messageRepository.insert(toAddMsg);
            return toAddMsg;
        }
        // 屏蔽评论
        if (shieldingVO.getType() == SpeechType.COMMENT.getKey()) {
            this.checkComment(shieldingVO.getSpeechId());
            Comment comment = new Comment();
            comment.setId(shieldingVO.getSpeechId());
            comment.setShieldingReason(shieldingVO.getReason());
            comment.setShieldingAt(new Date());
            comment.setStatus(CommentStatusType.HANDSHIELDING.getKey());
            if (isAutoShielding) {
                comment.setStatus(CommentStatusType.AUDOSHIELDING.getKey());
            }
            comment = commentRepository.updateOne(comment);
            if (comment == null) {
                return null;
            }
            momentRepository.updateNumByIdAndFieldName(comment.getMomentId(), "commentNum", -1);
            // 禁言记录中加入被屏蔽的评论ID
            silentRepository.upsertSilentByCommentAndOperatorId(comment, operatorId);

            // 写入消息记录表
            Moment moment = momentRepository.findById(comment.getMomentId());
            Message toAddMsg = this.packageShieldingMessageByMoment(moment);
            toAddMsg.setType(MomentMessageType.SHIELDINGCOMMENT.getKey());
            toAddMsg.setNoticeTo(comment.getCreatorId());
            // 操作人员(系统自动屏蔽时，没有操作人员)
            toAddMsg.setCreatorId(operatorId);
            toAddMsg.setContent(comment.getShieldingReason());
            // 消息创建时间应该与屏蔽时间一致
            toAddMsg.setCreateAt(comment.getShieldingAt());
            toAddMsg.setDataStatus(DataStatusType.VALID.KEY);
            toAddMsg = messageRepository.insert(toAddMsg);
            return toAddMsg;
        }
        return null;
    }

    private Message packageShieldingMessageByMoment(Moment moment) {
        Message toAddMsg = new Message();
        toAddMsg.setCommunityId(moment.getCommunityId());
        toAddMsg.setMomentId(moment.getId());
        toAddMsg.setMomentType(moment.getType());
        toAddMsg.setMomentContent(moment.getContent());
        toAddMsg.setMomentPhotos(moment.getPhotos());
        return toAddMsg;
    }

    private Moment checkMoment(ObjectId momentId) {
        if (momentId == null) {
            throw MOMENT_ID_IS_NULL;
        }
        Moment toGet = momentRepository.findById(momentId);
        if (toGet == null
                || toGet.getDataStatus() == DataStatusType.INVALID.KEY
                || !Arrays.asList(MomentStatusType.AUTOREVIEWED.getKey(), MomentStatusType.REVIEWED.getKey()).contains(toGet.getStatus())) {
            log.info("checkMoment end 动态不存在或已被屏蔽");
            throw MOMENT_IS_DELETED;
        }
        return toGet;
    }

    private Comment checkComment(ObjectId id) {
        if (id == null) {
            throw COMMENT_ID_IS_NULL;
        }
        Comment toGet = commentRepository.findById(id);
        if (toGet == null || toGet.getDataStatus() == DataStatusType.INVALID.KEY
                || toGet.getStatus() != CommentStatusType.NORMAL.getKey()) {
            log.info("checkComment end 评论不存在或已被屏蔽");
            throw COMMENT_IS_DELETED;
        }
        return toGet;
    }
}
