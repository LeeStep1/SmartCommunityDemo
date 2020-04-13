package cn.bit.api.config;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RocketConfig {

    @Value("${rocket.nameserver.address}")
    private String nameserver;

    @Value("${rocket.api.producer}")
    private String apiProducer;

    @Bean
    public DefaultMQProducer getProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(nameserver);
        producer.setProducerGroup(apiProducer);
        producer.start();
        return producer;
    }
}
