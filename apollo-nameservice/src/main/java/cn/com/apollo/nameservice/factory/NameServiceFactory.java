package cn.com.apollo.nameservice.factory;

import cn.com.apollo.common.spi.SPI;
import cn.com.apollo.nameservice.NameService;

/**
 * @author jiaming
 */
@SPI("zookeeper")
public interface NameServiceFactory {

    /**
     * 获取NameService的抽象方法
     *
     * @param address
     * @return NameService
     */
    NameService getNameService(String address);

}
