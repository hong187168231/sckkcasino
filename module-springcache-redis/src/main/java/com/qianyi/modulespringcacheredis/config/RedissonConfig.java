package com.qianyi.modulespringcacheredis.config;

import jodd.util.StringUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author puff
 * @desc Redisson 配置
 */
@Configuration
public class RedissonConfig {


    @Autowired
    private RedissonProperties redissonProperties;

    @Bean
    public RedissonClient redisson() {
        return Redisson.create(singleConfiguration());
    }


    private Config singleConfiguration() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress("redis://" + redissonProperties.getHost() + ":" + redissonProperties.getPort());
        singleServerConfig.setConnectTimeout(30000).setSubscriptionsPerConnection(5000);
        String password = redissonProperties.getPassword();
        if (StringUtil.isNotBlank(password)) {
            singleServerConfig.setPassword(password);
        }
        singleServerConfig.setDatabase(redissonProperties.getDatabase());
        return config;
    }


}
