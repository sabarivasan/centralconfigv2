package com.centralconfig.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Delta between 2 yaml documents
 * //TODO:can we make this class immutable and still compatible with Jackson serialization?
 */
public class Delta implements Comparable<Delta> {

    /**
     * Type of change
     */
    public enum DeltaType {
        ADDED,
        DELETED,
        MODIFIED,
    }

    @JsonProperty
    private DeltaType type;

    @JsonProperty
    private String leafPath;

    @JsonProperty
    private Value old;

    @JsonProperty("new")
    private Value nouveau;

    public Delta() { }

    public Delta(DeltaType type, String leafPath, Value old, Value nouveau) {
        this.type = type;
        this.leafPath = leafPath;
        this.old = old;
        this.nouveau = nouveau;
    }

    public DeltaType getType() {
        return type;
    }

    public String getLeafPath() {
        return leafPath;
    }

    public Value getOld() {
        return old;
    }

    public void setType(DeltaType type) {
        this.type = type;
    }

    public void setLeafPath(String leafPath) {
        this.leafPath = leafPath;
    }

    public void setOld(Value old) {
        this.old = old;
    }

    public Value getNew() {
        return nouveau;
    }

    public void setNew(Value newVal) {
        this.nouveau = newVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(Constants.STRING_BUILDER_INIT_LENGTH_32);
        switch (type) {
            case ADDED: sb.append("+ "); break;
            case DELETED: sb.append("- "); break;
            case MODIFIED: sb.append("~ "); break;
            default:
        }
        sb.append(leafPath).append('=');
        switch (type) {
            case ADDED: sb.append(nouveau); break;
            case DELETED: sb.append(old); break;
            case MODIFIED: sb.append(nouveau).append(" (Old Value=").append(old); break;
            default:
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Delta delta = (Delta) o;

        return getLeafPath().equals(delta.getLeafPath());
    }

    @Override
    public int hashCode() {
        return getLeafPath().hashCode();
    }

    @Override
    public int compareTo(Delta o) {
        return getLeafPath().compareTo(o.getLeafPath());
    }
}
