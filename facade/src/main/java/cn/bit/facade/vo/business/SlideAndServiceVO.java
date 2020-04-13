package cn.bit.facade.vo.business;

import cn.bit.facade.model.business.BizSlide;
import cn.bit.facade.model.business.Convenience;
import cn.bit.facade.model.system.Slide;
import org.springframework.core.Conventions;

import java.io.Serializable;
import java.util.List;

/**
 * Created by fxiao
 * on 2018/4/4
 */
public class SlideAndServiceVO implements Serializable{
    /**
     * 轮播图
     */
    private List<SlideVO> slideVOS;
    /**
     * 服务
     */
    private List<ConvenienceVO> convenienceVOS;

    public List<SlideVO> getSlideVOS() {
        return slideVOS;
    }

    public void setSlideVOS(List<SlideVO> slideVOS) {
        this.slideVOS = slideVOS;
    }

    public List<ConvenienceVO> getConvenienceVOS() {
        return convenienceVOS;
    }

    public void setConvenienceVOS(List<ConvenienceVO> convenienceVOS) {
        this.convenienceVOS = convenienceVOS;
    }
}
