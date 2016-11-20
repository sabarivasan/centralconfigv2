package com.centralconfig.dw;

import com.cvent.CventConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The implementation of Central Config service as a Cvent Dropwizard service
 */
public class CentralConfigConfiguration extends CventConfiguration {

    @JsonProperty
    private List<NamespaceLevel> namespaceLevels;

    @JsonProperty
    private String consulEndpoint;

    @JsonProperty
    private ConfigChangePublish configChangePublish;

    public int getNumNamespaceLevels() {
        return getNamespaceLevels().size();
    }

    public List<NamespaceLevel> getNamespaceLevels() {
        return namespaceLevels;
    }

    public void setNamespaceLevels(List<NamespaceLevel> namespaceLevels) {
        this.namespaceLevels = namespaceLevels;
    }

    public String getConsulEndpoint() {
        return consulEndpoint;
    }

    public void setConsulEndpoint(String consulEndpoint) {
        this.consulEndpoint = consulEndpoint;
    }

    public ConfigChangePublish getConfigChangePublish() {
        return configChangePublish;
    }

    public void setConfigChangePublish(ConfigChangePublish configChangePublish) {
        this.configChangePublish = configChangePublish;
    }

    /**
     * Namespace level
     */
    public static class NamespaceLevel {
        @JsonProperty
        private String label;

        @JsonProperty
        private String defaultValue;

        @JsonProperty
        private String description;

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

    /**
     * Config for publishing config changes
     */
    public static class ConfigChangePublish {

        /**
         * Controls which destinations we publish config changes to.
         * - Subscribers: We publish to all subscribers who registered with us (ConfigChangeResource)
         * - PublishUrl: We publish to the configured publishUrl
         * - Both: Both the above
         * - None: Config changes will not be published
         */
        public enum Strategy {
            Subscribers,
            PublishUrl, // TODO: Come up with a better name for this (like ConfiguredUrl?)
            Both,
            None;

            public boolean shouldPublishToSubscribers() {
                return Subscribers == this || Both == this;
            }

            public boolean shouldPublishToPublishUrl() {
                return PublishUrl == this || Both == this;
            }
        }

        @JsonProperty
        private Strategy strategy;

        @JsonProperty
        private String publishUrl;


        public Strategy getStrategy() {
            return strategy;
        }

        public void setStrategy(Strategy strategy) {
            this.strategy = strategy;
        }

        public String getPublishUrl() {
            return publishUrl;
        }

        public void setPublishUrl(String publishUrl) {
            this.publishUrl = publishUrl;
        }
    }
}
