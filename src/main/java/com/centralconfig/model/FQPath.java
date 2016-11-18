package com.centralconfig.model;


import com.centralconfig.persist.DbSerializable;
import org.apache.commons.lang.StringUtils;

/**
 * This class represents a full-qualified path including namespace path and document path.
 * Document paths may represent leaf nodes or partial paths (sub-trees).
 */
public class FQPath implements Comparable<FQPath> {


    private final String fullPath;
    private final int numNamespaceLevels;
    private final boolean isLeaf;
    private String namespacePath = null;
    private String docPath = null;


    public FQPath(String fullPath, int numNamespaceLevels, boolean isLeaf) {
        this.fullPath = fullPath;
        this.numNamespaceLevels = numNamespaceLevels;
        this.isLeaf = isLeaf;

        String[] parts = fullPath.split(DbSerializable.HIER_SEPARATOR);
        if (parts.length < numNamespaceLevels) {
            throw new IllegalArgumentException("fullPath doesn't agree with number of namespace levels expected");
        }
        namespacePath = StringUtils.join(parts, DbSerializable.HIER_SEPARATOR, 0, numNamespaceLevels);
        docPath = StringUtils.join(parts, DbSerializable.HIER_SEPARATOR, numNamespaceLevels, parts.length);
    }

    public FQPath(String namespacePath, String docPath, int numNamespaceLevels, boolean isLeaf) {
        this.namespacePath = StringUtils.chomp(namespacePath, DbSerializable.HIER_SEPARATOR);
        this.docPath = docPath != null ? docPath : "";
        this.fullPath = this.namespacePath + DbSerializable.HIER_SEPARATOR + this.docPath;
        this.numNamespaceLevels = numNamespaceLevels;
        this.isLeaf = isLeaf;
    }

    /**
     *
     * @return The full path including the namespace path
     */
    public String getFullPath() {
        return fullPath;
    }

    /**
     *
     * @return The namespace portion of the path
     */
    public String getNamespacePath() {
        return namespacePath;
    }

    /**
     *
     * @return The document portion of the path without the namespace path
     */
    public String getDocPath() {
        return docPath;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FQPath FQPath = (FQPath) o;

        return getFullPath().equals(FQPath.getFullPath());
    }

    @Override
    public int hashCode() {
        return getFullPath().hashCode();
    }

    @Override
    public int compareTo(FQPath o) {
        return this.getFullPath().compareTo(o.getFullPath());
    }
}
