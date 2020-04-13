package cn.bit.api.controller.v1;

import cn.bit.api.support.*;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.api.support.annotation.SendPush;
import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.ParamConfigType;
import cn.bit.facade.enums.ParamKeyType;
import cn.bit.facade.enums.SpeechType;
import cn.bit.facade.enums.push.PushPointEnum;
import cn.bit.facade.model.community.Parameter;
import cn.bit.facade.model.moment.*;
import cn.bit.facade.service.community.ParameterFacade;
import cn.bit.facade.service.moment.*;
import cn.bit.facade.service.user.HouseholdFacade;
import cn.bit.facade.vo.IncrementalRequest;
import cn.bit.facade.vo.moment.*;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static cn.bit.framework.exceptions.BizException.OPERATION_FAILURE;

@RestController
@RequestMapping(value = "/v1/mom", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class MomentController {

    @Autowired
    private MomentFacade momentFacade;

    @Autowired
    private CommentFacade commentFacade;

    @Autowired
    private PraiseFacade praiseFacade;

    @Autowired
    private ReportFacade reportFacade;

    @Autowired
    private ParameterFacade parameterFacade;

    @Autowired
    private MessageFacade messageFacade;

    @Autowired
    private ShieldingFacade shieldingFacade;

    @Autowired
    private SilentFacade silentFacade;

    @Autowired
    private HouseholdFacade householdFacade;

    //=============================================moment-start=========================================================
    @PostMapping(name = "发布动态", path = "/moment/publish")
    @Authorization
    public ApiResult addMoment(@RequestBody @Validated MomentVO momentVO) {
        momentVO.setCommunityId(SessionUtil.getCommunityId());
        Parameter parameter = parameterFacade.findByTypeAndKeyAndCommunityId(ParamConfigType.MOMENT.getKey(),
                ParamKeyType.AUTOAUDITMOMENT.name(), momentVO.getCommunityId());
        Moment moment = momentFacade.addMoment(momentVO, SessionUtil.getAppSubject().getPartner(),
                SessionUtil.getTokenSubject().getUid(),
                parameter == null || parameter.getValue() == null ? false : Boolean.parseBoolean(parameter.getValue()));
        return moment != null ? ApiResult.ok(moment) : ApiResult.error(-1, "发布动态失败");
    }

    @GetMapping(name = "审核动态信息", path = "/moment/{id}/audit")
    @Authorization
    public ApiResult auditMoment(@PathVariable ObjectId id, Integer status) {
        Moment Moment = momentFacade.auditMoment(id, status, SessionUtil.getTokenSubject().getUid());
        return ApiResult.ok(Moment);
    }

    @PostMapping(name = "动态分页", path = "/moment/page")
    @Authorization
    public ApiResult queryPageMoment(@RequestBody @Validated MomentRequestVO requestVO,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        ObjectId currUserId = null;
        requestVO.setCommunityId(SessionUtil.getCommunityId());
        if (StringUtil.isNotBlank(requestVO.getCreatorName())) {
            Set<ObjectId> userIds = householdFacade.listHouseholds(requestVO.getCommunityId(), requestVO.getCreatorName());
            if (userIds.isEmpty()) {
                return ApiResult.ok(new Page<>());
            }
            requestVO.setCreatorId(userIds);
        }
        Page<Moment> momentPage = momentFacade.queryPageByMomentRequest(requestVO,
                SessionUtil.getAppSubject().getPartner(), currUserId, page, size);
        return ApiResult.ok(momentPage);
    }

    @PostMapping(name = "增量获取用户动态列表", path = "/moment/oneself/incremental-list")
    @Authorization
    public ApiResult incrementalMyMomentList(@RequestBody @Validated IncrementalRequest incrementalRequest) {
        ObjectId currUserId = SessionUtil.getTokenSubject().getUid();
        ObjectId communityId = SessionUtil.getCommunityId();
        List<Moment> myMomentList = momentFacade.incrementalMyMomentList(incrementalRequest,
                SessionUtil.getAppSubject().getPartner(), communityId, currUserId);
        return ApiResult.ok(myMomentList);
    }

    @PostMapping(name = "增量获取动态列表", path = "/moment/incremental-list")
    @Authorization
    public ApiResult incrementalMomentList(@RequestBody @Validated IncrementalRequest incrementalRequest) {
        ObjectId currUserId = null;
        Integer client = SessionUtil.getAppSubject().getClient();
        // 住户端需要查询动态是否已点赞，已举报
        currUserId = SessionUtil.getTokenSubject().getUid();
        List<Moment> momentPage = momentFacade.incrementalMomentList(incrementalRequest,
                SessionUtil.getAppSubject().getPartner(), SessionUtil.getCommunityId(), currUserId);
        return ApiResult.ok(momentPage);
    }

    @PostMapping(name = "被举报的动态分页", path = "/moment/page-with-report")
    @Authorization
    public ApiResult queryPageMomentWithReport(@RequestBody @Validated RequestVO requestVO,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        requestVO.setCommunityId(SessionUtil.getCommunityId());
        Parameter parameter = parameterFacade.findByTypeAndKeyAndCommunityId(ParamConfigType.MOMENT.getKey(),
                ParamKeyType.MOMENTWARNINGREPORTNUM.name(), requestVO.getCommunityId());
        requestVO.setReportNum(parameter == null || parameter.getValue() == null ? 1 : Integer.parseInt(parameter.getValue()));

        if (StringUtil.isNotBlank(requestVO.getCreatorName())) {
            Set<ObjectId> userIds = householdFacade.listHouseholds(requestVO.getCommunityId(), requestVO.getCreatorName());
            if (userIds.isEmpty()) {
                return ApiResult.ok(new Page<>());
            }
            requestVO.setCreatorId(userIds);
        }
        Page<Moment> pageList = momentFacade.queryPageByRequestVO(
                requestVO, SessionUtil.getAppSubject().getPartner(), page, size);
        return ApiResult.ok(pageList);
    }

    @GetMapping(name = "动态详情", path = "/moment/{id}/detail")
    @Authorization
    public ApiResult getMoment(@PathVariable ObjectId id) {
        ObjectId currUserId = null;
        AppSubject appSubject = SessionUtil.getAppSubject();
        Integer client = appSubject.getClient();
        if (client == ClientType.HOUSEHOLD.value()) {
            currUserId = SessionUtil.getTokenSubject().getUid();
        }
        Moment Moment = momentFacade.findByIdAndCurrentUser(id, currUserId, client, appSubject.getPartner());
        return ApiResult.ok(Moment);
    }

    @GetMapping(name = "删除动态", path = "/moment/{id}/delete")
    @Authorization
    public ApiResult deleteMoment(@PathVariable ObjectId id) {
        boolean result = momentFacade.deleteById(id, SessionUtil.getTokenSubject().getUid());
        return result ? ApiResult.ok() : ApiResult.error(-1, "删除失败");
    }

    //================================================moment-end========================================================

    //=============================================comment-start========================================================

    @GetMapping(name = "动态的评论分页", path = "/comment/{momentId}/detail")
    @Authorization
    public ApiResult getCommentsByMoment(@PathVariable() ObjectId momentId,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        ObjectId currUserId = null;
        AppSubject appSubject = SessionUtil.getAppSubject();
        Integer client = appSubject.getClient();
        Page<Comment> pageList = commentFacade.findPageByMomentId(momentId, currUserId, client, appSubject.getPartner(),
                page, size);
        return ApiResult.ok(pageList);
    }

    @PostMapping(name = "增量获取动态的评论", path = "/comment/incremental-list")
    @Authorization
    public ApiResult incrementalCommentList(@RequestBody @Validated IncrementalRequest incrementalRequest) {
        ObjectId currUserId = null;
        AppSubject appSubject = SessionUtil.getAppSubject();
        Integer client = appSubject.getClient();
        currUserId = SessionUtil.getTokenSubject().getUid();
        List<Comment> commentList =
                commentFacade.incrementalCommentList(incrementalRequest, currUserId, client, appSubject.getPartner());
        return ApiResult.ok(commentList);
    }

    @PostMapping(name = "被举报的评论分页", path = "/comment/page-with-report")
    @Authorization
    public ApiResult getCommentsByCommunity(@RequestBody @Validated RequestVO requestVO,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        requestVO.setCommunityId(SessionUtil.getCommunityId());
        Parameter parameter = parameterFacade.findByTypeAndKeyAndCommunityId(ParamConfigType.MOMENT.getKey(),
                ParamKeyType.COMMENTWARNINGREPORTNUM.name(), requestVO.getCommunityId());
        requestVO.setReportNum(parameter == null || parameter.getValue() == null ? 1 : Integer.parseInt(parameter.getValue()));
        if (StringUtil.isNotBlank(requestVO.getCreatorName())) {
            Set<ObjectId> userIds = householdFacade.listHouseholds(requestVO.getCommunityId(), requestVO.getCreatorName());
            if (userIds.isEmpty()) {
                return ApiResult.ok(new Page<>());
            }
            requestVO.setCreatorId(userIds);
        }
        AppSubject appSubject = SessionUtil.getAppSubject();
        Page<Comment> pageList = commentFacade.findPageByRequestVO(requestVO, page, size, appSubject.getClient(),
                appSubject.getPartner());
        return ApiResult.ok(pageList);
    }

    @GetMapping(name = "某禁言记录关联的被屏蔽且未处理的动态列表", path = "/silent/{id}/shielding/moment")
    @Authorization
    public ApiResult queryPageShieldingMoment(@PathVariable("id") ObjectId id,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        Page<Moment> momentPage = silentFacade.queryShieldingMomentById(id, page, size);
        return ApiResult.ok(momentPage);
    }

    @GetMapping(name = "某禁言记录关联的被屏蔽且未处理的评论列表", path = "/silent/{id}/shielding/comment")
    @Authorization
    public ApiResult queryPageShieldingComment(@PathVariable("id") ObjectId id,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        Page<Comment> commentPage = silentFacade.queryShieldingCommentById(id, page, size);
        return ApiResult.ok(commentPage);
    }

    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = ClientType.HOUSEHOLD,
            pushData = false,
            point = PushPointEnum.COMMENT
    )
    @PostMapping(name = "发布评论", path = "/comment/answer")
    @Authorization
    public ApiResult addComment(@RequestBody @Validated CommentVO commentVO) {
        CommentMsgVO commentMsgVO = commentFacade.addComment(commentVO, SessionUtil.getAppSubject().getPartner(),
                SessionUtil.getTokenSubject().getUid());
        if (commentMsgVO == null || commentMsgVO.getComment() == null) {
            throw OPERATION_FAILURE;
        }
        if (commentMsgVO.getMessage() == null) {
            return ApiResult.ok(commentMsgVO.getComment());
        }
        // 推送message
        PushTask pushTask = this.packagePushTask(commentMsgVO.getMessage());
        return WrapResult.create(ApiResult.ok(commentMsgVO.getComment()), pushTask);
    }

    @GetMapping(name = "删除评论", path = "/comment/{id}/delete")
    @Authorization
    public ApiResult deleteComment(@PathVariable ObjectId id) {
        Comment toDelete = commentFacade.deleteById(id, SessionUtil.getTokenSubject().getUid());
        return toDelete == null ? ApiResult.error(-1, "删除失败") : ApiResult.ok();
    }

    @PostMapping(name = "增量获取用户在某社区的评论", path = "/comment/oneself/incremental-list")
    @Authorization
    public ApiResult incrementalMyCommentList(@RequestBody @Validated IncrementalRequest incrementalRequest) {
        List<Comment> commentList = commentFacade.incrementalMyCommentList(incrementalRequest,
                SessionUtil.getAppSubject().getPartner(), SessionUtil.getCommunityId(),
                SessionUtil.getTokenSubject().getUid());
        return ApiResult.ok(commentList);
    }
    //=============================================comment-end==========================================================

    //=============================================praise-start=========================================================
    @SendPush(
            clientTypes = {ClientType.HOUSEHOLD},
            scope = SendPush.Scope.COMMUNITY,
            point = PushPointEnum.PRAISE
    )
    @GetMapping(name = "点赞某动态", path = "/praise/{momentId}/add")
    @Authorization
    public ApiResult addPraise(@PathVariable ObjectId momentId) {
        Message message = praiseFacade.addPraise(momentId, SessionUtil.getAppSubject().getPartner(),
                SessionUtil.getTokenSubject().getUid());
        // 第一次点赞，需要推送
        if (message != null) {
            log.info("第一次点赞，需要推送 message ");
            PushTask pushTask = this.packagePushTask(message);
            return WrapResult.create(ApiResult.ok(), pushTask);
        }
        return ApiResult.ok();
    }

    // 封装消息通知推送实体
    private PushTask packagePushTask(Message message) {
        PushTask pushTask = new PushTask();
        PushTarget pushTarget = new PushTarget();
        pushTarget.setUserIds(Collections.singleton(message.getNoticeTo()));
        pushTask.setPushTarget(pushTarget);
        pushTask.setDataObject(message);
        return pushTask;
    }

    @GetMapping(name = "取消点赞某动态", path = "/praise/{momentId}/cancel")
    @Authorization
    public ApiResult cancelPraise(@PathVariable ObjectId momentId) {
        praiseFacade.deletePraise(momentId, SessionUtil.getTokenSubject().getUid());
        return ApiResult.ok();
    }

    @PostMapping(name = "增量获取某动态的点赞列表", path = "/praise/incremental-list")
    @Authorization
    public ApiResult incrementalPraiseList(@RequestBody @Validated IncrementalRequest incrementalRequest) {
        AppSubject appSubject = SessionUtil.getAppSubject();
        List<Praise> praiseList = praiseFacade.incrementalPraiseList(incrementalRequest, appSubject.getClient(),
                appSubject.getPartner());
        return ApiResult.ok(praiseList);
    }

    @PostMapping(name = "增量获取某用户点赞列表", path = "/praise/oneself/incremental-list")
    @Authorization
    public ApiResult incrementalMyPraiseList(@RequestBody @Validated IncrementalRequest incrementalRequest) {
        List<Praise> praiseList = praiseFacade.incrementalMyPraiseList(incrementalRequest,
                SessionUtil.getAppSubject().getPartner(), SessionUtil.getCommunityId(),
                SessionUtil.getTokenSubject().getUid());
        return ApiResult.ok(praiseList);
    }

    //=============================================praise-end===========================================================

    //====================================举报=====report-start=========================================================

    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = ClientType.HOUSEHOLD,
            point = PushPointEnum.REPORT
    )
    @PostMapping(name = "举报评论或动态", path = "/report/add")
    @Authorization
    public ApiResult addReport(@RequestBody @Validated(ReportVO.Add.class) ReportVO reportVO) {
        reportVO.setCommunityId(SessionUtil.getCommunityId());
        /**
         * 获取每人每天的举报上限数
         */
        Parameter param = parameterFacade.findOneByTypeAndKey(ParamConfigType.MOMENT.getKey(),
                ParamKeyType.REPORTEXPECTNUM.name());
        int reportExpectNum = 10;//default value = 10
        if (param != null && StringUtil.isNotNull(param.getValue())) {
            reportExpectNum = Integer.parseInt(param.getValue());
        }
        Report report = reportFacade.addReport(reportVO, SessionUtil.getTokenSubject().getUid(), reportExpectNum);
        if (report == null) {
            throw OPERATION_FAILURE;
        }

        //获取社区动态配置参数
        List<Parameter> parameterList = parameterFacade.findByTypeAndCommunityId(ParamConfigType.MOMENT.getKey(),
                reportVO.getCommunityId());
        Map<String, Integer> paramMap = new HashMap();
        if (parameterList != null && parameterList.size() > 0) {
            parameterList.stream().filter(
                    parameter -> Arrays.asList(ParamKeyType.MOMENTSHIELDINGREPORTNUM.name(),
                            ParamKeyType.COMMENTSHIELDINGREPORTNUM.name())
                            .contains(parameter.getKey())).forEach(
                    parameter -> paramMap.put(parameter.getKey(),
                            parameter.getValue() == null ? 0 : Integer.parseInt(parameter.getValue())));
        } else {
            log.info("该社区没有配置自动屏蔽动态/评论的举报数量");
            paramMap.put(ParamKeyType.MOMENTSHIELDINGREPORTNUM.name(), 0);
            paramMap.put(ParamKeyType.COMMENTSHIELDINGREPORTNUM.name(), 0);
        }
        int shieldingMomentReportNum = paramMap.get(ParamKeyType.MOMENTSHIELDINGREPORTNUM.name());
        int shieldingCommentReportNum = paramMap.get(ParamKeyType.COMMENTSHIELDINGREPORTNUM.name());

        ShieldingVO shieldingVO = new ShieldingVO();
        shieldingVO.setSpeechId(reportVO.getSpeechId());
        shieldingVO.setType(reportVO.getType());
        Message message = null;
        PushTask pushTask;
        // 当前总举报次数
        int reportNum = report.getReportNum();
        // 举报动态，并且已达到自动屏蔽的条件
        if (report.getType() == SpeechType.MOMENT.getKey()
                && reportNum >= shieldingMomentReportNum && shieldingMomentReportNum > 0) {
            log.info("举报次数达到自动屏蔽的条件，自动屏蔽该动态");
            message = shieldingFacade.shieldingSpeechBySystem(shieldingVO);
        }
        // 举报评论，并且已达到自动屏蔽的条件
        if (report.getType() == SpeechType.COMMENT.getKey()
                && reportNum >= shieldingCommentReportNum && shieldingCommentReportNum > 0) {
            log.info("举报次数达到自动屏蔽的条件，自动屏蔽该评论");
            message = shieldingFacade.shieldingSpeechBySystem(shieldingVO);
        }
        // 不需要推送
        if (message == null) {
            return ApiResult.ok();
        }
        pushTask = this.packagePushTask(message);
        return WrapResult.create(ApiResult.ok(), pushTask);
    }

    @PostMapping(name = "动态或评论的举报分页", path = "/report/list")
    @Authorization
    public ApiResult queryPageForReport(@RequestBody @Validated(ReportVO.Search.class) ReportVO reportVO,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        Page<Report> reportPage = reportFacade.queryPageByCommunityIdAndSpeechIdAndType(
                SessionUtil.getCommunityId(), reportVO, page, size);
        return ApiResult.ok(reportPage);
    }
    //=============================================report-end===========================================================

    //=============================================shielding-start======================================================
    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = ClientType.HOUSEHOLD,
            point = PushPointEnum.SHIELDING
    )
    @PostMapping(name = "屏蔽动态或评论", path = "/shielding/speech")
    @Authorization
    public ApiResult shieldingSpeech(@RequestBody @Validated ShieldingVO shieldingVO) {
        Message message = shieldingFacade.shieldingSpeechByManager(shieldingVO, SessionUtil.getTokenSubject().getUid());
        if (message == null) {
            throw OPERATION_FAILURE;
        }
        PushTask pushTask = this.packagePushTask(message);
        return WrapResult.create(ApiResult.ok(), pushTask);
    }
    //=============================================shielding-end========================================================

    //=============================================silent-start=========================================================
    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = ClientType.HOUSEHOLD,
            point = PushPointEnum.SILENT
    )
    @PostMapping(name = "禁言用户", path = "/silent/user")
    @Authorization
    public ApiResult silentUser(@RequestBody @Validated SilentVO silentVO) {
        Message message = silentFacade.silentUser(silentVO, SessionUtil.getCommunityId(),
                SessionUtil.getTokenSubject().getUid());
        if (message == null) {
            throw OPERATION_FAILURE;
        }
        PushTask pushTask = this.packagePushTask(message);
        return WrapResult.create(ApiResult.ok(), pushTask);
    }

    @GetMapping(name = "解除禁言", path = "/silent/{id}/relieve")
    @Authorization
    public ApiResult silentUser(@PathVariable ObjectId id) {
        boolean result = silentFacade.relieveSilentUser(id);
        if (!result) {
            throw OPERATION_FAILURE;
        }
        return ApiResult.ok();
    }

    @PostMapping(name = "某社区禁言用户分页", path = "/silent/page")
    @Authorization
    public ApiResult silentPageList(@RequestBody @Validated SilentRequest silentRequest,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        silentRequest.setCommunityId(SessionUtil.getCommunityId());
        if (StringUtil.isNotBlank(silentRequest.getSilentUserName())) {
            Set<ObjectId> userIds = householdFacade.listHouseholds(silentRequest.getCommunityId(), silentRequest.getSilentUserName());
            if (userIds.isEmpty()) {
                return ApiResult.ok(new Page<>());
            }
            silentRequest.setSilentUserName(null);
            silentRequest.setSilentUserId(userIds);
        }
        Page<Silent> pageList = silentFacade.findPageBySilentRequest(silentRequest, page, size);
        return ApiResult.ok(pageList);
    }
    //=============================================silent-end===========================================================

    //=============================================message-start========================================================

    @PostMapping(name = "增量获取某社区消息列表", path = "/message/incremental-list")
    @Authorization
    public ApiResult queryPageForMessage(@RequestBody @Validated IncrementalRequest incrementalRequest) {
        List<Message> messageList = messageFacade.findByIncrementalRequest(incrementalRequest,
                SessionUtil.getAppSubject().getPartner(), SessionUtil.getCommunityId(),
                SessionUtil.getTokenSubject().getUid());
        return ApiResult.ok(messageList);
    }
    //=============================================message-end==========================================================

    //===========================================statistics-start=======================================================
    @GetMapping(name = "个人动态/评论/点赞的数量统计", path = "/statistics")
    @Authorization
    public ApiResult statisticsMoment() {
        StatisticsVO statisticsVO = new StatisticsVO();
        ObjectId communityId = SessionUtil.getCommunityId();
        ObjectId creatorId = SessionUtil.getTokenSubject().getUid();
        statisticsVO.setMomentAmount(momentFacade.statisticsMoment(communityId, creatorId));
        statisticsVO.setCommentAmount(commentFacade.statisticsComment(communityId, creatorId));
        statisticsVO.setPraiseAmount(praiseFacade.statisticsPraise(communityId, creatorId));
        return ApiResult.ok(statisticsVO);
    }

    //===========================================statistics-end=========================================================
}
