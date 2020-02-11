package cn.com.apollo.export;

import cn.com.apollo.common.URI;
import cn.com.apollo.common.spi.SPI;
import cn.com.apollo.config.NameServiceConfig;

@SPI("export")
public interface Export {

    void export(Object ref, Class<?> interfaceClass, NameServiceConfig nameServiceConfig, URI uri);

}
