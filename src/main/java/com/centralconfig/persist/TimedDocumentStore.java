package com.centralconfig.persist;

import com.centralconfig.model.Constants;
import com.centralconfig.model.DocPath;
import com.centralconfig.model.LeafNode;
import com.centralconfig.model.Value;
import com.centralconfig.model.YamlDocument;

import java.io.IOException;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by sabarivasan on 1/22/17.
 */
public class TimedDocumentStore {
    public static final String TIMED_DOC_STORE_PREFIX = "timeddocs" + DbSerializable.HIER_SEPARATOR;
    public static final String TIMED_KV_STORE_PREFIX = "timedkeyvalues" + DbSerializable.HIER_SEPARATOR;
    private final TimedKVStore kvStore;

    public TimedDocumentStore(TimedKVStore kvStore) {
        this.kvStore = kvStore;
    }

    // TODO: store document itself and individual key-values as a transaction
    public void upsertYamlDocument(YamlDocument yamlDoc, long timestamp, String nsPath) throws IOException {
        // Store document itself (just keys)
        kvStore.putAt(KeyHelper.docKey(nsPath), serKeys(yamlDoc), timestamp);

        // Store each key value at timestamp
        for (LeafNode leaf: yamlDoc.getLeaves()) {
            kvStore.putAt(KeyHelper.keyValueKey(nsPath, leaf.getLeaf().getDocPath()), leaf.getValue().ser(), timestamp);
        }
    }

    public Optional<YamlDocument> getYamlDocument(String nsPath, long timestamp) throws IOException {
        Optional<String> serKeys = kvStore.getValueAt(KeyHelper.docKey(nsPath), timestamp);
        if (serKeys.isPresent()) {
            String[] keys = serKeys.get().split(DbSerializable.SER_ROW_SEPARATOR);
            SortedSet<LeafNode> leaves = new TreeSet<>();
            for (String key: keys) {
                Optional<String> val = kvStore.getValueAt(KeyHelper.keyValueKey(nsPath, key), timestamp);
                if (val.isPresent()) {
                    leaves.add(new LeafNode(new DocPath(key, true), new Value(val.get())));
                }
            }
            return Optional.of(new YamlDocument(nsPath, leaves));
        } else {
            return Optional.empty();
        }
    }

    // TODO: Move this to YamlDocument
    private static String serKeys(YamlDocument yamlDoc) {
        StringBuilder sb = new StringBuilder(Constants.STRING_BUILDER_INIT_LENGTH_512);
        for (LeafNode leaf: yamlDoc.getLeaves()) {
            sb.append(leaf.getLeaf().getDocPath());
            sb.append(DbSerializable.SER_ROW_SEPARATOR);
        }
        return sb.toString();
    }


    /**
     * A utility class that constructs keys based on conventions
     */
    private static class KeyHelper {

        // Consul key for a specific timestamp
        private static String docKey(String nsPath) {
            return TIMED_DOC_STORE_PREFIX + nsPath;
        }

        // Consul key prefix for all timestamps of a given key
        private static String keyValueKey(String nsPath, String leafPath) {
            return TIMED_KV_STORE_PREFIX + fqLeafPath(nsPath, leafPath);
        }

        private static String fqLeafPath(String namespacePath, String docPath) {
            return namespacePath + DbSerializable.HIER_SEPARATOR + docPath;
        }

    }


}
