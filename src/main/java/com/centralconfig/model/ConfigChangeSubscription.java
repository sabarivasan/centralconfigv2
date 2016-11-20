package com.centralconfig.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Subscribers interesting in config changes will post to ConfigChangeResource
 * with an instance of this class as the body
 */
public class ConfigChangeSubscription {

    @JsonProperty
    @NotNull
    private String name;

    @JsonProperty
    @NotNull
    private String publishUrl;

    @JsonProperty
    @NotNull
    private String namespacePath;

    @JsonProperty
    @NotNull @NotEmpty
    private List<String> yPaths;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespacePath() {
        return namespacePath;
    }

    public void setNamespacePath(String namespacePath) {
        this.namespacePath = namespacePath;
    }

    public String getPublishUrl() {
        return publishUrl;
    }

    public void setPublishUrl(String publishUrl) {
        this.publishUrl = publishUrl;
    }

    public List<String> getyPaths() {
        return yPaths;
    }

    public void setyPaths(List<String> yPaths) {
        this.yPaths = yPaths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConfigChangeSubscription that = (ConfigChangeSubscription) o;
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
