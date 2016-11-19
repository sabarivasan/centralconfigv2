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
}
