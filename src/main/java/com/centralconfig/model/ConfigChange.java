package com.centralconfig.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.SortedSet;

/**
 * A class that represents the atomic set of changes to a document, by an author.
 *
 * TODO: If we decide to go with 1 key-value per leaf node storage strategy, we need to find a way to
 * do atomic changes at a document-level
 */
public class ConfigChange {

    @JsonProperty
    private final String author;

    @JsonProperty
    private final long modifiedAt;

    @JsonProperty
    private final SortedSet<Delta> deltas;

    public ConfigChange(String author, long modifiedAt, SortedSet<Delta> deltas) {
        this.author = author;
        this.modifiedAt = modifiedAt;
        this.deltas = deltas;
    }

    public String getAuthor() {
        return author;
    }

    public long getModifiedAt() {
        return modifiedAt;
    }

    public SortedSet<Delta> getDeltas() {
        return deltas;
    }

}
