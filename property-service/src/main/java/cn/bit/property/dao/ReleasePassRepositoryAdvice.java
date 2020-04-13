package cn.bit.property.dao;

import cn.bit.facade.vo.property.ReleasePassRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;

/**
 * Created by decai.liu
 *
 * @description
 * @create: 2018/3/27
 **/
public interface ReleasePassRepositoryAdvice {

    Page getReleasePassPage(ReleasePassRequest request, int page, int size, XSort createAt);
}
