package com.centralconfig.persist;

/**
 * Created by sabarivasan on 11/16/16.
 */
public interface DbSerializable<T> {

    // Generates the node name for the database-serialized array index
    static String serializeArrayInd(int ind) {
        return String.format("%s%04d", SER_ARRAY_PREFIX, ind);
    }

    // Tests whether the serialized node name represents an array index
    static boolean isSerializedArray(String serializedNodeName) {
        return serializedNodeName.startsWith(SER_ARRAY_PREFIX);
    }

    // Deserialize an array index from serialized node name
    static int deserializeArrayInd(String serializedNodeName) {
        return Integer.parseInt(serializedNodeName.substring(SER_ARRAY_PREFIX.length()));
    }

    String ser();

    void serTo(StringBuilder sb);

    T deser(String ser);

    // the separator between levels of the tree hierarchy
    String HIER_SEPARATOR = "/";
    // The prefix for anchors in a document
    String SER_ARRAY_PREFIX = "...";
    // The separator between field and value when serializing documents
    String SER_DATA_TYPE_SEPARATOR = ";";
    // The separator between key and value when serializing documents
    String SER_KEY_VALUE_SEPARATOR = "=";
    // The separator between rows (each key-value) when serializing documents
    String SER_ROW_SEPARATOR = "\n";
    // The separator between rows (each key-value) when serializing documents
    String SER_NAMESPACE_PATH_KEY = "__NAMESPACE";
}
