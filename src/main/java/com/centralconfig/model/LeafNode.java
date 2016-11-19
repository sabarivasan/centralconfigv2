package com.centralconfig.model;

import com.centralconfig.persist.DbSerializable;

/**
 * Created by sabarivasan on 11/16/16.
 */
public class LeafNode implements Comparable<LeafNode>, DbSerializable<LeafNode> {

    private final DocPath leaf;
    private final Value value;

    public LeafNode(String ser) {
        String[] parts = ser.split(DbSerializable.SER_KEY_VALUE_SEPARATOR);
        leaf = new DocPath(parts[0], true);
        value = new Value(parts[1]);
    }

    public LeafNode(DocPath leaf, Value value) {
        this.leaf = leaf;
        this.value = value;
    }

    public DocPath getLeaf() {
        return leaf;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LeafNode leafNode = (LeafNode) o;

        return leaf.equals(leafNode.leaf);
    }

    @Override
    public int hashCode() {
        return leaf.hashCode();
    }


    @Override
    public int compareTo(LeafNode o) {
        return leaf.compareTo(o.leaf);
    }

    @Override
    public String ser() {
        StringBuilder sb = new StringBuilder();
        serTo(sb);
        return sb.toString();
    }

    @Override
    public void serTo(StringBuilder sb) {
        sb.append(leaf.getDocPath()).append(DbSerializable.SER_KEY_VALUE_SEPARATOR);
        value.serTo(sb);
    }

    @Override
    public LeafNode deser(String ser) {
        return new LeafNode(ser);
    }
}
