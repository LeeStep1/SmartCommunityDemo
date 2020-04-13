package cn.bit.communityIoT.support.processor;

import cn.bit.facade.vo.mq.DeviceAuthVO;
import cn.bit.facade.vo.mq.ThirdPartInfoCallbackVO;

public interface DeviceAuthProcessor<T extends DeviceAuthVO> {
    /**
     * 授权流程
     * @param deviceAuthVO
     * @throws Exception
     */
    ThirdPartInfoCallbackVO add(T deviceAuthVO) throws Exception;

    /**
     * 删除权限流程
     * @param deviceAuthVO
     * @throws Exception
     */
    ThirdPartInfoCallbackVO delete(T deviceAuthVO) throws Exception;

    /**
     * 覆盖权限流程
     * @param deviceAuthVO
     * @throws Exception
     */
    ThirdPartInfoCallbackVO cover(T deviceAuthVO) throws Exception;
}
