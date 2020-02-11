package cn.com.apollo.nameservice;

import cn.com.apollo.common.Constant;
import cn.com.apollo.common.URI;
import cn.com.apollo.nameservice.curator.CuratorClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ZookeeperNameService implements NameService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final ConcurrentHashMap<String, ZookeeperNameService> zookeeperRegisterMap = new ConcurrentHashMap<>();
    private String url;
    private volatile boolean init = false;
    private CuratorClient client = null;
    private Notify notify;
    private static ExecutorService executorService = new ThreadPoolExecutor(10, 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new ThreadFactory() {
        private AtomicInteger atomicInteger = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("zookeeper-nameservice-pool-" + atomicInteger.incrementAndGet());
            return thread;
        }
    });


    public ZookeeperNameService(String url, Notify notify) {
        this.url = url;
        this.notify = notify;
        if (!init) {
            this.client = new CuratorClient(url, 5000);
        }
    }

    public ZookeeperNameService(String url) {
        this.url = url;
        if (!init) {
            this.client = new CuratorClient(url, 5000);
        }
    }

    @Override
    public void register(String serviceName, URI uri) {
        if (serviceName == null || "".equals(serviceName.trim())) {
            throw new RuntimeException("serviceName is not null");
        }
        String nameService = "/" + Constant.NAME_SERVICE_PATH + "/" + serviceName;
        try {
            //检查路径是否存在，不存在则创建
            if (client.checkExist(nameService)) {
                //创建nameservice
                client.createPersistent(nameService);
            }
            String nameServiceProvider = nameService + "/" + Constant.PROVIDER;
            if (client.checkExist(nameServiceProvider)) {
                //创建provider
                client.createPersistent(nameServiceProvider);
            }
            //验证服务是否已经被本机注册
            List<String> providerList = client.getChildren(nameServiceProvider);
            if (!providerList.isEmpty()) {
                String serviceKey = uri.getUriString();
                if (providerList.contains(serviceKey)) {
                    log.warn("service is register,servicename:{},uri:{}", serviceName, uri.getUriString());
                    return;
                }
            }
            //注册节点
            String provider = uri.getUriString();
            String path = nameServiceProvider + "/" + URLEncoder.encode(provider, "UTF-8");
            client.createEphemeral(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URI subscribe(String serviceName, URI uri) {
        if (serviceName == null || "".equals(serviceName.trim())) {
            throw new RuntimeException("serviceName is not null");
        }
        try {
            String nameService = "/" + Constant.NAME_SERVICE_PATH + "/" + serviceName;
            if (client.checkExist(nameService)) {
                client.createPersistent(nameService);
            }
            //创建consumer
            String nameServiceConsumer = nameService + "/" + Constant.CONSUMER;
            if (client.checkExist(nameServiceConsumer)) {
                client.createPersistent(nameServiceConsumer);
            }
            List<String> consumerList = client.getChildren(nameServiceConsumer);
            if (!consumerList.isEmpty()) {
                String serviceKey = uri.getUriString();
                if (consumerList.contains(serviceKey)) {
                    return uri;
                }
            }
            String consumer = nameServiceConsumer + "/" + URLEncoder.encode(uri.getUriString(), "UTF-8");
            if (client.checkExist(consumer)) {
                client.createEphemeral(consumer);
            }
            //订阅服务
            String serviceProviderPath = nameService + "/" + Constant.PROVIDER;
            List<String> providerList = client.getChildren(serviceProviderPath);
            List<URI> uris = new ArrayList<>(providerList.size());
            for (String providerPath : providerList) {
                uris.add(URI.toURI(URLDecoder.decode(providerPath, "UTF-8")));
            }
            notify.notify(uris);
            //添加监听
            addListener(serviceProviderPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return uri;
    }

    @Override
    public void onListener(Notify notify) {
        setNotify(notify);
    }

    public void addListener(String path) {
        PathChildrenCache cache = client.pathChildrenCache(path, true);
        try {
            cache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                    List<ChildData> childDataList = event.getInitialData();
                    if (childDataList != null && !childDataList.isEmpty()) {
                        List<URI> services = new ArrayList<>(childDataList.size());
                        for (ChildData childData : childDataList) {
                            services.add(URI.toURI(childData.getPath()));
                        }
                        notify.notify(services);
                    }
                }
            }, executorService);
            cache.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setNotify(Notify notify) {
        this.notify = notify;
    }
}