package com.centralconfig.model;

/**
 * This class represents a document path which may be a leaf or partial (sub-tree)
 * This class is completely agnostic of the concept of namespaces.
 * A collection of leaf DocPath instances represent any yaml document
 */
public class DocPath implements Comparable<DocPath> {
    private final String docPath;
    private final boolean isLeaf;


    public DocPath(String docPath, boolean isLeaf) {
        this.docPath = docPath;
        this.isLeaf = isLeaf;
    }


    public String getDocPath() {
        return docPath;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DocPath docPath1 = (DocPath) o;
        return getDocPath().equals(docPath1.getDocPath());
    }

    @Override
    public int hashCode() {
        return getDocPath().hashCode();
    }

    @Override
    public int compareTo(DocPath o) {
        return docPath.compareTo(o.docPath);
    }
}
