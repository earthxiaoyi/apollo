package cn.com.apollo.reference;

import cn.com.apollo.cluster.Cluster;
import cn.com.apollo.cluster.dictionary.RegisterServiceDictionary;
import cn.com.apollo.cluster.dictionary.ServiceDictionary;
import cn.com.apollo.common.URI;
import cn.com.apollo.common.spi.ServiceLoad;
import cn.com.apollo.config.NameServiceConfig;
import cn.com.apollo.invoke.Invoker;
import cn.com.apollo.nameservice.NameService;
import cn.com.apollo.nameservice.factory.NameServiceFactory;

public class ApolloReference implements Reference {

    private NameServiceFactory nameServiceFactory = ServiceLoad.getServiceLoad(NameServiceFactory.class).getDefaultService();
    private Cluster cluster = ServiceLoad.getServiceLoad(Cluster.class).getDefaultService();

    @Override
    public <T> Invoker<T> refer(Class<?> type, NameServiceConfig nameServiceConfig, URI uri) {
        ServiceDictionary dictionary = new RegisterServiceDictionary(type);
        NameService nameService = nameServiceFactory.getNameService(nameServiceConfig.getAddress());
        nameService.onListener(dictionary);
        return doRefer(type, cluster, nameService, dictionary, uri);
    }

    private <T> Invoker<T> doRefer(Class<?> type, Cluster cluster, NameService nameService,
                                   ServiceDictionary serviceDictionary, URI uri) {
        Invoker invoker = cluster.add(serviceDictionary);
        nameService.subscribe(type.getName(), uri);
        return invoker;
    }

}
