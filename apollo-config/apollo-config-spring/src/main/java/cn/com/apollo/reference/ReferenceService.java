package cn.com.apollo.reference;

import cn.com.apollo.config.ApplicationConfig;
import cn.com.apollo.config.NameServiceConfig;
import cn.com.apollo.config.ReferenceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReferenceService extends ReferenceConfig implements FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ApplicationContext applicationContext;

    public ReferenceService() {
        super();
    }

    @Override
    public Object getObject() {
        return get();
    }

    @Override
    public Class<?> getObjectType() {
        return getInterfaceClass();
    }

    @Override
    public void destroy() throws Exception {
        //TODO 销毁对象
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化一些配置信息，应用名称、注册中心
        //1.获取application应用信息
        if (getApplicationConfig() == null) {
            Map<String, ApplicationConfig> applicationConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ApplicationConfig.class, false, false);
            if (applicationConfigMap != null && !applicationConfigMap.isEmpty()) {
                ApplicationConfig applicationConfig = null;
                for (Map.Entry<String, ApplicationConfig> entry : applicationConfigMap.entrySet()) {
                    applicationConfig = entry.getValue();
                }
                setApplicationConfig(applicationConfig);
            }
        }
        //2.获取注册中心配置
        if (getNameServiceConfigList() == null) {
            Map<String, NameServiceConfig> nameServiceConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, NameServiceConfig.class, false, false);
            if (nameServiceConfigMap != null && !nameServiceConfigMap.isEmpty()) {
                List<NameServiceConfig> nameServiceList = new ArrayList<>();
                for (NameServiceConfig nameServiceConfig : nameServiceConfigMap.values()) {
                    nameServiceList.add(nameServiceConfig);
                }
                setNameServiceConfigList(nameServiceList);
            }
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
