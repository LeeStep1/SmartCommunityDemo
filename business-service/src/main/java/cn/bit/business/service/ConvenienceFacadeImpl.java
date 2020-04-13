package cn.bit.business.service;

import cn.bit.business.dao.ConvenienceRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.business.Convenience;
import cn.bit.facade.service.business.ConvenienceFacade;
import cn.bit.facade.vo.business.ConvenienceVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by fxiao
 * on 2018/4/4
 */
@Service("convenienceFacade")
public class ConvenienceFacadeImpl implements ConvenienceFacade {

    @Autowired
    private ConvenienceRepository convenienceRepository;

    @Override
    public Convenience addConvenience(Convenience convenience) throws BizException {
        convenience.setCreateAt(new Date());
        convenience.setDataStatus(DataStatusType.VALID.KEY);
        return convenienceRepository.insert(convenience);
    }

    @Override
    public Convenience editConvenience(Convenience convenience) throws BizException {
        convenience.setUpdateAt(new Date());
        return convenienceRepository.updateOne(convenience);
    }

    @Override
    public Convenience deleteConvenience(ObjectId id) throws BizException {
        Convenience toUpdate = new Convenience();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        return convenienceRepository.updateByIdAndDataStatus(toUpdate, id, DataStatusType.VALID.KEY);
    }

    @Override
    public List<Convenience> getConvenienceList(Convenience convenience) throws BizException {
        convenience.setDataStatus(DataStatusType.VALID.KEY);
        return convenienceRepository.find(convenience, XSort.asc("createAt"));
    }

    @Override
    public Page<Convenience> queryConveniencePage(Convenience convenience) throws BizException {
        Pageable pageable = new PageRequest(convenience.getPage() - 1, convenience.getSize(),
                new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Convenience> resultPage =
                convenienceRepository.findByCommunityIdIgnoreNullAndNameRegexIgnoreNullAndDataStatus(
                        convenience.getCommunityId(),
                        StringUtil.makeQueryStringAllRegExp(convenience.getName()), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }

    @Override
    public Convenience findOne(ObjectId id) throws BizException {
        return convenienceRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public List<ConvenienceVO> getByCommunityId(ObjectId communityId, Integer size) throws BizException {
        List<ConvenienceVO> list = convenienceRepository.findByCommunityIdAndDataStatusOrderByRankAsc(communityId, DataStatusType.VALID.KEY, ConvenienceVO.class);
        if(size != null && size > 0 && list.size() > size){
            return list.subList(0, size);
        }
        return list;
//        return convenienceRepository.findListByCommunityId(communityId, size);
    }
}
