package cn.bit.api.support.aop;

import cn.bit.facade.sensitiveword.dto.SegmentWordDTO;
import cn.bit.facade.sensitiveword.service.SensitiveFacade;
import cn.bit.facade.vo.moment.CommentVO;
import cn.bit.facade.vo.moment.MomentVO;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;

import static cn.bit.facade.exception.moment.MomentException.CONTENT_IS_NULL;
import static cn.bit.facade.exception.moment.MomentException.SENSITIVE_WORDS_EXIST;

@Aspect
@Configuration
@Slf4j
public class MomentAspect {

    @Resource
    private SensitiveFacade sensitiveFacade;

    @Before("execution(public * cn.bit.api.controller.v1.MomentController.addComment(..)) " +
            "|| execution(public * cn.bit.api.controller.v1.MomentController.addMoment(..))")
    public void beforeMomentService(JoinPoint joinPoint) {
        Object args[] = joinPoint.getArgs();
        String content = null;
        if (args[0] instanceof MomentVO) {
            MomentVO momentVO = (MomentVO) args[0];
            content = momentVO.getContent();
            List<String> photos = momentVO.getPhotos();
            if (StringUtil.isBlank(content) && CollectionUtils.isEmpty(photos)) {
                throw CONTENT_IS_NULL;
            }
        } else if (args[0] instanceof CommentVO) {
            CommentVO commentVO = (CommentVO) args[0];
            content = commentVO.getContent();
        }

        if (StringUtil.isNotBlank(content)) {
            // 校验敏感词
            List<SegmentWordDTO> words = sensitiveFacade.matchSensitiveWords(content);
            if (words != null && !words.isEmpty()) {
                log.warn("文本内容包含敏感词：{}", words);
                throw SENSITIVE_WORDS_EXIST;
            }
        }
    }
}
