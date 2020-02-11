package cn.com.apollo.namespace;

import cn.com.apollo.config.ApplicationConfig;
import cn.com.apollo.config.NameServiceConfig;
import cn.com.apollo.export.ServiceExport;
import cn.com.apollo.reference.ReferenceService;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class ApolloNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("nameservice",new ApolloFactoryBeanDefinitionParser(NameServiceConfig.class));
        registerBeanDefinitionParser("application",new ApolloFactoryBeanDefinitionParser(ApplicationConfig.class));
        registerBeanDefinitionParser("reference",new ApolloFactoryBeanDefinitionParser(ReferenceService.class));
        registerBeanDefinitionParser("service",new ApolloFactoryBeanDefinitionParser(ServiceExport.class));
    }

}
