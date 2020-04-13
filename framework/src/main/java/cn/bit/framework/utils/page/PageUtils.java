package cn.bit.framework.utils.page;

import cn.bit.framework.data.common.Page;

public class PageUtils {

    public static <T> Page<T> getPage(org.springframework.data.domain.Page<T> page){
        Page<T> target = new Page<T>();
        if(page == null || page.getTotalElements() == 0){
            return target;
        }
        target.setParam(page.getNumber() + 1, page.getTotalElements(), page.getSize(), page.getContent());
        return target;
    }
}
