package cn.com.apollo.nameservice.factory;

import cn.com.apollo.nameservice.NameService;
import cn.com.apollo.nameservice.ZookeeperNameServiceImpl;

/**
 * @author jiaming
 */
public class ZookeeperNameServiceFactory implements NameServiceFactory {

    @Override
    public NameService getNameService(String address) {

        return new ZookeeperNameServiceImpl(address);
    }

}
