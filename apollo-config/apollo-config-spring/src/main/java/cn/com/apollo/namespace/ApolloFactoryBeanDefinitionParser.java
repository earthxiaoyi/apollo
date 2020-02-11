package cn.com.apollo.namespace;

import cn.com.apollo.config.ApplicationConfig;
import cn.com.apollo.config.NameServiceConfig;
import cn.com.apollo.export.ServiceExport;
import cn.com.apollo.reference.ReferenceService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ApolloFactoryBeanDefinitionParser implements BeanDefinitionParser {

    private Class<?> type;

    public ApolloFactoryBeanDefinitionParser(Class<?> type) {
        this.type = type;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
        rootBeanDefinition.setBeanClass(type);
        rootBeanDefinition.setLazyInit(false);
        String id = element.getAttribute("id");
        if (id == null || id.trim().length() == 0) {
            String name = element.getAttribute("name");
            if (name != null && name.trim().length() > 0) {
                id = name;
            } else {
                String anInterface = element.getAttribute("interface");
                if (anInterface == null || anInterface.trim().length() == 0) {
                    String beanName = type.getName();
                    id = beanName;
                    int i = 1;
                    while (parserContext.getRegistry().containsBeanDefinition(id)) {
                        id = beanName + (i++);
                    }
                } else {
                    id = anInterface;
                }
            }
        }
        if (id != null && id.trim().length() > 0) {
            if (parserContext.getRegistry().containsBeanDefinition(id)) {
                throw new RuntimeException("重复的id：" + id);
            }
            parserContext.getRegistry().registerBeanDefinition(id, rootBeanDefinition);
            rootBeanDefinition.getPropertyValues().addPropertyValue("id", id);
        }
        if (NameServiceConfig.class.equals(type)) {
            parse(element, parserContext, rootBeanDefinition, "nameservice");
        } else if (ApplicationConfig.class.equals(type)) {
            parse(element, parserContext, rootBeanDefinition, "application");
        } else if (ReferenceService.class.equals(type)) {
            parse(element, parserContext, rootBeanDefinition, "reference");
        } else if (ServiceExport.class.equals(type)) {
            //TODO 解析bean标签可以用这个：BeanDefinitionHolder
            parse(element, parserContext, rootBeanDefinition, "service");
        }
        return rootBeanDefinition;
    }

    private void parse(Element element, ParserContext parserContext, RootBeanDefinition rootBeanDefinition, String tag) {
        for (Method method : type.getMethods()) {
            String name = method.getName();
            if (name.length() > 3
                    && Modifier.isPublic(method.getModifiers())
                    && method.getParameterCount() == 1
                    && name.startsWith("set")) {
                String property = name.substring(3).toLowerCase();
                if (!"id".contains(property)) {
                    String value = element.getAttribute(property);
                    if (value != null && value.trim().length() > 0) {
                        Object reference = value;
                        if ("ref".equals(property) && parserContext.getRegistry().containsBeanDefinition(value)) {
                            BeanDefinition beanDefinition = parserContext.getRegistry().getBeanDefinition(value);
                            if (!beanDefinition.isSingleton()) {
                                throw new RuntimeException("ref repeat，ref:" + value);
                            }
                            reference = new RuntimeBeanReference(value);
                        }
                        rootBeanDefinition.getPropertyValues().addPropertyValue(property, reference);
                    }
                }
            }
        }
    }

}
