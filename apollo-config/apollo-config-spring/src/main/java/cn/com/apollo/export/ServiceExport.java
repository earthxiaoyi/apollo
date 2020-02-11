package cn.com.apollo.export;

import cn.com.apollo.config.ApplicationConfig;
import cn.com.apollo.config.NameServiceConfig;
import cn.com.apollo.config.ServiceConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceExport extends ServiceConfig implements InitializingBean, DisposableBean,
        ApplicationContextAware, ApplicationListener<ContextRefreshedEvent>, BeanNameAware {

    private ApplicationContext applicationContext;

    private volatile boolean isExport = false;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void destroy() throws Exception {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 这里加载其他标签的配置
        // 初始化一些配置信息，应用名称、注册中心
        //1.获取application应用信息
        if (getApplicationConfig() == null) {
            Map<String, ApplicationConfig> applicationConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ApplicationConfig.class, false, false);
            if (applicationConfigMap!=null && !applicationConfigMap.isEmpty()) {
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
            if (nameServiceConfigMap!=null && !nameServiceConfigMap.isEmpty()) {
                List<NameServiceConfig> nameServiceList = new ArrayList<>();
                for (NameServiceConfig nameServiceConfig : nameServiceConfigMap.values()) {
                    nameServiceList.add(nameServiceConfig);
                }
                setNameServiceConfigList(nameServiceList);
            }
        }
    }

    @Override
    public void setBeanName(String s) {

    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //服务发布
        if (!isExport()) {
            export();
        }
    }

    private boolean isExport() {
        return isExport;
    }
}
