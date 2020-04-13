package cn.bit.moment;

import cn.bit.facade.model.moment.Comment;
import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.model.moment.Silent;
import cn.bit.facade.service.moment.CommentFacade;
import cn.bit.facade.service.moment.MomentFacade;
import cn.bit.facade.service.moment.SilentFacade;
import cn.bit.facade.vo.IncrementalRequest;
import cn.bit.facade.vo.moment.MomentRequestVO;
import cn.bit.facade.vo.moment.RequestVO;
import cn.bit.facade.vo.moment.SilentRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.DateUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class UnitTest {

    @Autowired
    private MomentFacade momentFacade;

    @Autowired
    private CommentFacade commentFacade;

    @Autowired
    private SilentFacade silentFacade;

    @Test
    public void test1() {
        MomentRequestVO vo = new MomentRequestVO();
        vo.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        vo.setStatus(1);
        vo.setType(1);
        vo.setCreatorId(Collections.singleton(new ObjectId("5a915295c9661d002b2af9de")));
        Page<Moment> momentPage = momentFacade.queryPageByMomentRequest(vo, 0, null, 1, 10);
        System.out.println("aaa=" + momentPage.getRecords());

        RequestVO requestVO = new RequestVO();
//        requestVO.setStatus(2);
        requestVO.setReportNum(1);
        requestVO.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        momentPage = momentFacade.queryPageByRequestVO(requestVO, 0, 1, 10);
        System.out.println("bbb=" + momentPage.getRecords());
    }

    @Test
    public void test2(){
        RequestVO requestVO = new RequestVO();
        requestVO.setStatus(2);
        requestVO.setReportNum(1);
        requestVO.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        Page<Comment> commentPage = commentFacade.findPageByRequestVO(requestVO, 1, 10, 1001, 0);
        System.out.println("aaa=" + commentPage.getRecords());
    }

    @Test
    public void test3(){
        SilentRequest requestVO = new SilentRequest();
        requestVO.setStatus(0);
        requestVO.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        requestVO.setSilentUserId(Collections.singleton(new ObjectId("5a915295c9661d002b2af9de")));
        Page<Silent> silentPage = silentFacade.findPageBySilentRequest(requestVO, 1, 10);
        System.out.println("aaa=" + silentPage.getRecords());
    }

    @Test
    public void test4(){
        IncrementalRequest requestVO = new IncrementalRequest();
        requestVO.setStartAt(DateUtils.addDay(new Date(), -80));
        requestVO.setSort(0);
        List<Comment> list = commentFacade.incrementalMyCommentList(requestVO, 0, new ObjectId("5a82adf3b06c97e0cd6c0f3d"), new ObjectId("5a915295c9661d002b2af9de"));
        System.out.println("aaa=" + list);
    }

}
