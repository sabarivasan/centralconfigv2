package com.centralconfig.model;

import com.centralconfig.persist.DbSerializable;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A document represents a yaml/json document with all its keys
 */
public class Document implements DbSerializable<Document> {
    private final String namespacePath;
    private final SortedSet<LeafNode> leaves;

    public Document(String ser) {
        String[] rows = ser.split(DbSerializable.SER_ROW_SEPARATOR);
        namespacePath = rows[0].split(DbSerializable.SER_KEY_VALUE_SEPARATOR)[1];
        SortedSet<LeafNode> leaves = new TreeSet<>();
        for (int n = 1; n < rows.length; n++) {
            leaves.add(new LeafNode(rows[n]));
        }
        this.leaves = Collections.unmodifiableSortedSet(leaves);
    }

    public Document(String namespacePath, SortedSet<LeafNode> leaves) {
        this.namespacePath = namespacePath;
        this.leaves = Collections.unmodifiableSortedSet(leaves);
    }


    public String getNamespacePath() {
        return namespacePath;
    }

    public SortedSet<LeafNode> getLeaves() {
        return leaves;
    }

    @Override
    public String ser() {
        StringBuilder sb = new StringBuilder(Constants.STRING_BUILDER_INIT_LENGTH_1024);
        serTo(sb);
        return sb.toString();
    }

    @Override
    public void serTo(StringBuilder sb) {
        sb.append(DbSerializable.SER_NAMESPACE_PATH_KEY).append(
                DbSerializable.SER_KEY_VALUE_SEPARATOR).append(namespacePath)
                .append(DbSerializable.SER_ROW_SEPARATOR);
        for (LeafNode leaf: leaves) {
            leaf.serTo(sb);
            sb.append(DbSerializable.SER_ROW_SEPARATOR);
        }
    }

    @Override
    public Document deser(String ser) {
        return new Document(ser);
    }

    public void expandInDocAliases() {
        throw new NotImplementedException();
    }

    public Map<?, ?> getAsMap() {
        Map root = new HashMap<>();
        for (LeafNode leaf: leaves) {
            visitLeaf(leaf, root);
        }
        return root;
    }

    private void visitLeaf(LeafNode leaf, Map root) {
        String[] parts = StringUtils.split(leaf.getLeaf().getDocPath(), DbSerializable.HIER_SEPARATOR);
        Object parent = root;
        for (int n = 0; n < parts.length; n++) {
            if (DbSerializable.isSerializedArray(parts[n])) {
                List p = (List) parent;
                int ind = DbSerializable.deserializeArrayInd(parts[n]);
                if (n == parts.length - 1) {
                    // Leaf
                    p.add(leaf.getValue().getValue());
                } else if (p.size() <= ind) {
                    Object child = DbSerializable.isSerializedArray(parts[n + 1])
                            ?  new ArrayList<>()
                            : new HashMap<>();
                    p.add(child);
                    parent = child;
                } else {
                    parent = p.get(ind);
                }
            } else {
                Map p = (Map) parent;
                Object v = p.get(parts[n]);
                if (v == null) {
                    if (n == parts.length - 1) {
                        // Leaf
                        p.put(parts[n], leaf.getValue().getValue());
                    } else {
                        Object child = DbSerializable.isSerializedArray(parts[n + 1])
                                ?  new ArrayList<>()
                                : new HashMap<>();
                        p.put(parts[n], child);
                        parent = child;
                    }
                } else {
                    parent = v;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Document document = (Document) o;

        if (!getNamespacePath().equals(document.getNamespacePath())) return false;
        return getLeaves().equals(document.getLeaves());

    }

    @Override
    public int hashCode() {
        int result = getNamespacePath().hashCode();
        result = 31 * result + getLeaves().hashCode();
        return result;
    }
}
