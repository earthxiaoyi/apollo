package cn.com.apollo.filter;

import cn.com.apollo.common.Constant;
import cn.com.apollo.common.Invocation;
import cn.com.apollo.common.Result;
import cn.com.apollo.common.URI;
import cn.com.apollo.common.exception.RpcException;
import cn.com.apollo.common.spi.Active;
import cn.com.apollo.invoke.Invoker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author jiaming
 */
@Active(group = {Constant.PROVIDER}, value = Constant.TPS_FILTER_KEY)
public class TpsFilter implements Filter {

    private final Limit limit = new Limit();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws Exception {
        if (!limit.isPassable(invoker.getUri())) {
            throw new RpcException("invoke service " + invoker.getInterface().getName() + " fail. because exceed service max tps");
        }
        return invoker.invoke(invocation);
    }

    @Override
    public Result onResponse(Invoker<?> invoker, Result result, Invocation invocation) {
        return result;
    }

}

/**
 * @author jiaming
 */
class Limit {

    private final ConcurrentHashMap<String, State> states = new ConcurrentHashMap<>();

    public boolean isPassable(URI uri) {
        int tps = uri.getParameter(Constant.TPS_FILTER_KEY, -1);
        long interval = uri.getParameter(Constant.TPS_INTERVAL_KEY, Constant.TPS_INTERVAL_NUM);
        String serviceKey = uri.getServiceKey();
        if (tps > 0) {
            State state = states.get(serviceKey);
            if (state == null) {
                states.putIfAbsent(serviceKey, new State(serviceKey, tps, interval));
                state = states.get(serviceKey);
            } else {
                //if state exist,rebuild
                if (state.getTps() != tps || state.getInterval() != interval) {
                    states.putIfAbsent(serviceKey, new State(serviceKey, tps, interval));
                    state = states.get(serviceKey);
                }
            }
            return state.isPassable();
        } else {
            State state = states.get(serviceKey);
            if (state != null) {
                states.remove(serviceKey);
            }
        }
        return true;
    }

    static class State {

        private String name;
        private int tps;
        private long interval;
        private LongAdder longAdder;
        private long lastTime;

        public State(String name, int tps, long interval) {
            this.name = name;
            this.tps = tps;
            this.interval = interval;
            this.lastTime = System.currentTimeMillis();
            this.longAdder = buildLongAdder(tps);
        }

        public boolean isPassable() {
            long now = System.currentTimeMillis();
            if (now > lastTime + interval) {
                lastTime = System.currentTimeMillis();
                this.longAdder = buildLongAdder(tps);
            }
            if (longAdder.sum() < 0) {
                return false;
            }
            longAdder.decrement();
            return true;
        }

        private LongAdder buildLongAdder(int rate) {
            LongAdder longAdder = new LongAdder();
            longAdder.add(rate);
            return longAdder;
        }

        public int getTps() {
            return tps;
        }

        public long getInterval() {
            return interval;
        }

    }

}