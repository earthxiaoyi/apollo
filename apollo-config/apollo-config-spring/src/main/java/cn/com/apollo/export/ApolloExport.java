package cn.com.apollo.export;

import cn.com.NettyServer;
import cn.com.apollo.common.Constant;
import cn.com.apollo.common.URI;
import cn.com.apollo.common.spi.ServiceLoad;
import cn.com.apollo.config.NameServiceConfig;
import cn.com.apollo.filter.InvokerFilterChain;
import cn.com.apollo.handler.ApolloHandler;
import cn.com.apollo.invoke.Invoker;
import cn.com.apollo.model.ServiceModel;
import cn.com.apollo.model.ServiceProviders;
import cn.com.apollo.nameservice.NameService;
import cn.com.apollo.nameservice.factory.NameServiceFactory;
import cn.com.apollo.proxy.jdk.JdkProxyFactory;

import java.util.concurrent.ConcurrentHashMap;

public class ApolloExport implements Export {

    private static final ConcurrentHashMap<String, NettyServer> server = new ConcurrentHashMap<>();

    private NameServiceFactory nameServiceFactory = ServiceLoad.getServiceLoad(NameServiceFactory.class).getDefaultService();

    @Override
    public void export(Object ref, Class<?> interfaceClass, NameServiceConfig nameServiceConfig,
                       URI uri) {
        //获取注册中心
        NameService nameService = nameServiceFactory.getNameService(nameServiceConfig.getAddress());
        //导出服务
        doExport(ref, nameService, uri, interfaceClass);
    }

    /**
     * 导出服务
     */
    private void doExport(Object ref, NameService nameService,
                          URI uri, Class<?> interfaceClass) {
        //缓存本地服务
        String serviceKey = uri.getServiceKey();
        Invoker invoker = JdkProxyFactory.getInvoker(ref, (Class) interfaceClass, uri);
        //组装拦截链
        invoker = InvokerFilterChain.buildInvokerChain(invoker, Constant.PROVIDER_FILTER, Constant.PROVIDER);
        ServiceModel serviceModel = new ServiceModel(invoker, interfaceClass, uri.getServiceName());
        ServiceProviders.setServiceModel(serviceKey, serviceModel);
        //开启本地服务
        startServer(uri);
        //使用注册中心发布服务
        nameService.register(uri.getServiceName(), uri);
    }


    /**
     * 开启服务端口
     *
     * @param uri
     */
    private void startServer(URI uri) {
        String address = uri.getHost() + ":" + uri.getPort();
        if (server.get(address) == null) {
            NettyServer nettyServer = new NettyServer(uri, new ApolloHandler());
            nettyServer.start();
            server.putIfAbsent(address, nettyServer);
        }
    }

}
