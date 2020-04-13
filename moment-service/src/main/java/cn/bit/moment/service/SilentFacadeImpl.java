package cn.bit.moment.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.MomentMessageType;
import cn.bit.facade.enums.SilentStatusType;
import cn.bit.facade.exception.moment.MomentException;
import cn.bit.facade.model.moment.Comment;
import cn.bit.facade.model.moment.Message;
import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.model.moment.Silent;
import cn.bit.facade.service.moment.SilentFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.vo.moment.SilentRequest;
import cn.bit.facade.vo.moment.SilentVO;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.redis.RedisTemplateUtil;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.moment.dao.CommentRepository;
import cn.bit.moment.dao.MessageRepository;
import cn.bit.moment.dao.MomentRepository;
import cn.bit.moment.dao.SilentRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.CommonBizException.DATA_INVALID;
import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.moment.MomentException.SILENT_ID_IS_NULL;
import static cn.bit.facade.exception.user.UserBizException.USER_ID_NULL;

@Component("silentFacade")
@Slf4j
public class SilentFacadeImpl implements SilentFacade {

    @Autowired
    private SilentRepository silentRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserFacade userFacade;

    /**
     * 禁言用户
     *
     * @param silentVO
     * @param communityId
     * @param operatorId
     * @return
     */
    @Override
    public Message silentUser(SilentVO silentVO, ObjectId communityId, ObjectId operatorId) {
        Silent silent = silentRepository.upsertSilent(silentVO, communityId, operatorId);
        if (silent == null) {
            return null;
        }
        String key = communityId + "_" + silent.getSilentUserId();
        // 负数则已经过期
        Long silentSeconds = DateUtils.secondsBetween(new Date(), silent.getSilentEndAt());
        log.info("禁言时长（秒）：" + silentSeconds);
        if (silentSeconds > 0) {
            // 禁言时长
            RedisTemplateUtil.set(
                    key, DateUtils.formatDate(silent.getSilentEndAt(), DateUtils.DATE_FORMAT_DATETIME), silentSeconds);
            log.info("禁言到：" + RedisTemplateUtil.getStr(key));
        }
        // 写入消息记录表
        Message toAddMsg = new Message();
        toAddMsg.setType(MomentMessageType.SILENT.getKey());
        toAddMsg.setCommunityId(silent.getCommunityId());
        toAddMsg.setNoticeTo(silent.getSilentUserId());
        toAddMsg.setCreatorId(operatorId);
        toAddMsg.setCreateAt(silent.getUpdateAt());
        toAddMsg.setContent(DateUtils.formatDate(silent.getSilentEndAt(), DateUtils.DATE_FORMAT_DATETIME));
        toAddMsg.setDataStatus(DataStatusType.VALID.KEY);
        return messageRepository.insert(toAddMsg);
    }

    /**
     * 解除禁言
     *
     * @param id
     * @return
     */
    @Override
    public boolean relieveSilentUser(ObjectId id) {
        Silent silent = silentRepository.relieveSilentUser(id);
        if (silent == null) {
            return Boolean.FALSE;
        }
        String key = silent.getCommunityId() + "_" + silent.getSilentUserId();
        RedisTemplateUtil.del(key);
        return Boolean.TRUE;
    }

    /**
     * 分页查询禁言列表
     *
     * @param silentRequest
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Silent> findPageBySilentRequest(SilentRequest silentRequest, int page, int size) {
        silentRequest.setSilentUserName(null);
        Page<Silent> pageList = silentRepository.findPageBySilentRequest(silentRequest, page, size);
        if (pageList == null || pageList.getTotal() == 0) {
            log.info("findPageBySilentRequest end return null !!!");
            return pageList;
        }
        this.packageSilent(pageList.getRecords(), silentRequest.getSilentUserId());
        return pageList;
    }

    /**
     * 检查禁言状态
     *
     * @param uid
     * @param communityId
     * @return
     */
    @Override
    public boolean checkSilentUser(ObjectId uid, ObjectId communityId) {
        if (uid == null) {
            throw USER_ID_NULL;
        }
        if (communityId == null) {
            throw COMMUNITY_ID_NULL;
        }
        String key = communityId + "_" + uid;
        String silentEndAt = RedisTemplateUtil.getStr(key);
        if (StringUtil.isNotNull(silentEndAt)) {
            throw new MomentException(1320020, "你已被系统禁言，终止时间为 " + silentEndAt);
        }
        return false;
    }

    /**
     * 根据禁言记录ID查询被屏蔽的评论
     *
     * @param id
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Comment> queryShieldingCommentById(ObjectId id, Integer page, Integer size) {
        Silent toGet = this.toCheck(id);
        if (toGet.getNewShieldingCommentIds() == null || toGet.getNewShieldingCommentIds().isEmpty()) {
            return new Page<>();
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Comment> pageList =
                commentRepository.findByIdIn(toGet.getNewShieldingCommentIds(), pageable);
        return PageUtils.getPage(pageList);
    }

    /**
     * 根据禁言记录ID查询被屏蔽的动态
     *
     * @param id
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Moment> queryShieldingMomentById(ObjectId id, Integer page, Integer size) {
        Silent toGet = this.toCheck(id);
        if (toGet.getNewShieldingMomentIds() == null || toGet.getNewShieldingMomentIds().isEmpty()) {
            return new Page<>();
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Moment> pageList =
                momentRepository.findByIdIn(toGet.getNewShieldingMomentIds(), pageable);
        return PageUtils.getPage(pageList);
    }

    private Silent toCheck(ObjectId id) {
        if (id == null) {
            throw SILENT_ID_IS_NULL;
        }
        Silent silent = silentRepository.findById(id);
        if (silent == null) {
            throw DATA_INVALID;
        }
        return silent;
    }

    private void packageSilent(List<Silent> pageList, Collection<ObjectId> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            // 遍历得到被禁言者的id集合
            userIds = pageList.stream().map(Silent::getSilentUserId).collect(Collectors.toSet());
            if (userIds == null || userIds.isEmpty()) {
                log.info("userIds is null, packageSilent end return pageList without any silentUser info !!!");
                return;
            }
        }
        // 根据id集合查询被禁言者的信息集合
        List<UserVO> userVOList = userFacade.findByIds(new HashSet<>(userIds));
        if (userVOList == null || userVOList.isEmpty()) {
            log.info("userList is null, packageSilent end return pageList without any silentUser info !!!");
            return;
        }
        Map<ObjectId, UserVO> userVOMap = new HashMap<>();
        userVOList.forEach(userVO -> userVOMap.put(userVO.getId(), userVO));

        // 封装被禁言者的个人信息（头像，名字）
        for (Silent silent : pageList) {
            silent.setNewShieldingMomentNum(
                    silent.getNewShieldingMomentIds() == null ? 0 : silent.getNewShieldingMomentIds().size());
            silent.setNewShieldingCommentNum(
                    silent.getNewShieldingCommentIds() == null ? 0 : silent.getNewShieldingCommentIds().size());
            silent.setShieldingMomentNum(
                    silent.getShieldingMomentIds() == null ? 0 : silent.getShieldingMomentIds().size());
            silent.setShieldingCommentNum(
                    silent.getShieldingCommentIds() == null ? 0 : silent.getShieldingCommentIds().size());

            silent.setStatus(SilentStatusType.RELIEVE.getKey());
            if (silent.getSilentEndAt() != null && silent.getSilentEndAt().after(new Date())) {
                silent.setStatus(SilentStatusType.SILENT.getKey());
            }
            // 被禁言者
            UserVO userVO = userVOMap.get(silent.getSilentUserId());
            if (userVO != null) {
                silent.setSilentUserName(StringUtil.isNotNull(userVO.getName()) ? userVO.getName() : userVO.getPhone());
            }
        }
    }
}
