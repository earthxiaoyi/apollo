package cn.com.apollo.common.spi;

import cn.com.apollo.common.Constant;
import cn.com.apollo.common.URI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI 拓展类
 *
 * @param <T>
 * @author jiaming
 */
public class ServiceLoad<T> {

    private static final ConcurrentHashMap<Class<?>, ServiceLoad<?>> SERVICE_LOADS = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<?>, Object> SERVICE_INSTANCES = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Holder<Object>> cacheInstance = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Active> cacheActives = new ConcurrentHashMap<>();

    public static final String SERVICES_PATH = "META-INF/services/";

    private Holder<Map<String, Class<?>>> cachedClass = new Holder<>();

    private String defaultServiceName;

    private Class<?> type;

    public ServiceLoad(Class<?> type) {
        this.type = type;
    }

    public static <T> ServiceLoad<T> getServiceLoad(Class<T> t) {
        if (t == null) {
            throw new IllegalArgumentException("class is not === null");
        }
        if (!t.isInterface()) {
            throw new IllegalArgumentException("class is not a interface");
        }
        if (!t.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("class is not a spi annotation");
        }
        ServiceLoad<T> serviceLoad = (ServiceLoad<T>) SERVICE_LOADS.get(t);
        if (serviceLoad == null) {
            serviceLoad = new ServiceLoad<T>(t);
            SERVICE_LOADS.putIfAbsent(t, new ServiceLoad<T>(t));
        }
        return serviceLoad;
    }

    public List<T> getActiveServices(URI uri, String[] values, String group) {
        List<T> services = new ArrayList<>();
        List<String> valueList = values == null ? new ArrayList<>() : Arrays.asList(values);
        getServiceClasses();
        for (Map.Entry<String, Active> entry : cacheActives.entrySet()) {
            String name = entry.getKey();
            Active active = entry.getValue();
            String[] activeGroups = active.group();
            String activeValue = active.value();
            if (isGroup(group, activeGroups)) {
                if (isActiveValues(uri, activeValue)) {
                    Object instance = createService(name);
                    if (!valueList.contains(Constant.DEFAULT_REMOVE_KEY + name)) {
                        services.add((T) instance);
                    }
                }
            }
        }
        services.sort(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                }
                if (o2 == null) {
                    return 1;
                }
                if (o1.equals(o2)) {
                    return 0;
                }
                Active active1 = o1.getClass().getAnnotation(Active.class);
                Active active2 = o2.getClass().getAnnotation(Active.class);
                return active1.order() > active2.order() ? 1 : -1;
            }
        });

        // 加载用户自定义filter
        List<T> usrs = new ArrayList<>();
        for (String value : valueList) {
            if (!valueList.contains(Constant.DEFAULT_REMOVE_KEY + value)) {
                T instance = getService(value);
                usrs.add(instance);
            }
        }
        if (!usrs.isEmpty()) {
            services.addAll(usrs);
        }
        return services;
    }

    private boolean isActiveValues(URI uri, String activeValue) {
        if (activeValue == null || activeValue.length() == 0) {
            return true;
        }
        String paramsValue = uri.getParameter(activeValue);
        if (paramsValue != null) {
            return true;
        }
        return false;
    }

    private boolean isGroup(String group, String[] activeGroups) {
        if (group == null || group.length() < 0) {
            return true;
        }
        for (String activeGroup : activeGroups) {
            if (group.equals(activeGroup)) {
                return true;
            }
        }
        return false;
    }

    public T getDefaultService() {
        String defaultServiceName = getDefaultServiceName();
        Holder<Object> holder = cacheInstance.get(defaultServiceName);
        if (holder == null) {
            return getService(defaultServiceName);
        }
        return (T) holder.getValue();
    }

    private String getDefaultServiceName() {
        if (defaultServiceName == null) {
            SPI spi = type.getAnnotation(SPI.class);
            defaultServiceName = spi.value().trim();
        }
        return defaultServiceName;
    }

    private Map<String, Class<?>> getServiceClasses() {
        Map<String, Class<?>> classes = cachedClass.getValue();
        if (classes == null) {
            classes = new HashMap<>(8);
            loadClasses(classes, SERVICES_PATH, type.getName());
            cachedClass.setValue(classes);
        }
        return classes;
    }

    public T getService(String name) {
        if (name == null || name.length() <= 0) {
            throw new IllegalArgumentException("name == null");
        }
        Holder<Object> holder = getOrCreateHolder(name);
        Object instance = holder.getValue();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.getValue();
                if (instance == null) {
                    instance = createService(name);
                    holder.setValue(instance);
                }
            }
        }
        return (T) instance;
    }

    private Object createService(String name) {
        Class<?> aClass = getServiceClasses().get(name);
        if (aClass == null) {
            throw new IllegalArgumentException("class is not find");
        }
        try {
            Object instance = SERVICE_INSTANCES.get(aClass);
            if (instance == null) {
                instance = aClass.newInstance();
                SERVICE_INSTANCES.putIfAbsent(aClass, instance);
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalArgumentException(t.getMessage(), t);
        }
    }

    private Holder<Object> getOrCreateHolder(String name) {
        Holder<Object> holder = cacheInstance.get(name);
        if (holder == null) {
            holder = new Holder<>();
            cacheInstance.putIfAbsent(name, holder);
        }
        return holder;
    }

    public void loadClasses(Map<String, Class<?>> classes, String dir, String type) {
        String fileName = dir + type;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(fileName);
        if (url != null) {
            try {
                InputStreamReader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        try {
                            int len = line.indexOf("=");
                            String name = null;
                            String clz = null;
                            if (len > 0) {
                                name = line.substring(0, len).trim();
                                clz = line.substring(len + 1).trim();
                            }
                            if (clz != null) {
                                Class<?> cls = classLoader.loadClass(clz);
                                if (cls == null) {
                                    throw new IllegalArgumentException(clz + " class not find");
                                }
                                //active
                                Active active = cls.getAnnotation(Active.class);
                                if (active != null) {
                                    cacheActives.put(name, active);
                                }
                                classes.put(name, cls);
                            }
                        } catch (Exception e) {
                            throw new IllegalArgumentException("load class fail,class:" + line, e);
                        }
                    }
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }


}
