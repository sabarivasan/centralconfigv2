package com.centralconfig.publish;

import com.centralconfig.dw.CentralConfigConfiguration;
import com.centralconfig.model.ConfigChange;
import com.centralconfig.model.ConfigChangeSubscription;
import com.cvent.JsonSerializer;
import com.cvent.client.RetrofitClientProvider;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.client.Response;
import retrofit.converter.JacksonConverter;
import retrofit.http.Body;
import retrofit.http.POST;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Publishes config changes to subsribers depending on the strategy chosen in the configuration
 * @author sabarivasan
 */
public class ConfigChangePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigChangePublisher.class);

    private final CentralConfigConfiguration config;

    private final Map<String, ConfigChangeSubscription> subscriptionsByName = new HashMap<>();
    private final Map<String, Set<ConfigChangeSubscription>> subscriptionsByNamespacePath = new HashMap<>();
    private final Map<String, PublisherTransport> clients = new HashMap<>();

    private final ExecutorService publishPool = Executors.newSingleThreadExecutor();

    public ConfigChangePublisher(CentralConfigConfiguration config) {
        this.config = config;
    }

    public void subscribe(ConfigChangeSubscription subscription) {
        subscriptionsByName.put(subscription.getName(), subscription);
        Set<ConfigChangeSubscription> subs = subscriptionsByNamespacePath.get(subscription.getNamespacePath());
        if (subs == null) {
            subs = new HashSet<>();
            subscriptionsByNamespacePath.put(subscription.getNamespacePath(), subs);
        }
        subs.add(subscription);
    }

    public ConfigChangeSubscription deleteSubscription(String name) {
        return subscriptionsByName.remove(name);
    }

    public void configChanged(String namespacePath, ConfigChange configChange) {
        publishPool.submit(new Worker(namespacePath, configChange));
    }

    /**
     * Worker thread that publishes config changes to a document
     */
    private final class Worker implements Runnable {

        private final String namespacePath;
        private final ConfigChange configChange;

        private Worker(String namespacePath, ConfigChange configChange) {
            this.namespacePath = namespacePath;
            this.configChange = configChange;
        }

        @Override
        public void run() {
            try {
                if (config.getConfigChangePublish().getStrategy().shouldPublishToSubscribers()) {
                    Set<ConfigChangeSubscription> subscriptions = subscriptionsByNamespacePath.get(namespacePath);
                    if (subscriptions != null) {
                        for (ConfigChangeSubscription subscription : subscriptions) {
                            LOG.info(String.format("Publishing config change by %s to %s to %s",
                                                   configChange.getAuthor(),
                                                   namespacePath, subscription.getPublishUrl()));
                            publish(subscription.getPublishUrl());
                        }
                    }
                }
                if (config.getConfigChangePublish().getStrategy().shouldPublishToPublishUrl()) {
                    LOG.info(String.format("Publishing config change by %s to %s to %s", configChange.getAuthor(),
                                           namespacePath, config.getConfigChangePublish().getPublishUrl()));
                    publish(config.getConfigChangePublish().getPublishUrl());
                }
            } catch (Throwable t) {
                LOG.error(String.format("Exception occurred while publishing config change for %s",
                                        namespacePath), t);
            }
        }

        private void publish(String publishUrl) {
            try {
                PublisherTransport client = clients.get(publishUrl);
                if (client == null) {
                    client = new RetrofitClientProvider<>(publishUrl, PublisherTransport.class,
                                                          new JacksonConverter(JsonSerializer.copyObjectMapper(
                                                               JsonSerializer.ObjectMapperType.allowUnknownProperties,
                                                               JsonSerializer.getObjectMapper().enable(
                                                                       SerializationFeature.INDENT_OUTPUT))))
                                                                .getClient();
                    clients.put(publishUrl, client);
                }
                client.publish(configChange);
            } catch (Exception e) {
                LOG.error(String.format("Exception occurred while publishing config change for %s to %s",
                                        namespacePath, publishUrl), e);
            }
        }
    }


    /**
     * Interface publishing a config change to a url
     */
    private interface PublisherTransport {

        @POST("/publish")
        Response publish(@Body ConfigChange configChange);

    }
}
