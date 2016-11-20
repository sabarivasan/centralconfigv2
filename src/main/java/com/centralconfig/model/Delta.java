package com.centralconfig.model;

/**
 * Delta between 2 yaml documents
 */
public class Delta {

    /**
     * Type of change
     */
    public enum DeltaType {
        ADDED,
        DELETED,
        MODIFIED,
    }

    private final DeltaType type;
    private final String leafPath;
    private final Value old;
    private final Value nouveau;

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

    public Value getNew() {
        return nouveau;
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
}
