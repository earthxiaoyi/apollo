package cn.com.apollo.nameservice.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * @author jiaming
 */
public class CuratorClient {

    private CuratorFramework client = null;

    public CuratorClient(String url, int timeout) {
        client = CuratorFrameworkFactory.builder()
                .connectString(url)
                .connectionTimeoutMs(timeout)
                .retryPolicy(new RetryNTimes(1, 1000))
                .build();
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState state) {
                if (state == ConnectionState.LOST) {

                } else if (state == ConnectionState.CONNECTED) {

                } else if (state == ConnectionState.RECONNECTED) {

                }
            }
        });
        client.start();
    }

    public void createPersistent(String path) {
        try {
            client.create().creatingParentsIfNeeded().forPath(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void createEphemeral(String path) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public boolean checkExist(String path) {
        try {
            Stat stat = client.checkExists().forPath(path);
            if (stat == null) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public PathChildrenCache pathChildrenCache(String path, boolean cacheData) {
        PathChildrenCache cache = new PathChildrenCache(client, path, cacheData);
        return cache;
    }

}
