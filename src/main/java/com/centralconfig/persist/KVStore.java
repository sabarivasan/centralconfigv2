package com.centralconfig.persist;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * A key-value store, with either native or artificial support for hierarchies.
 *
 * This interface does not use any proprietary data types (like Document).
 * This makes it easy to have fewer KVStore implementations for different databases, for example
 *
 *
 */
public interface KVStore {

    void put(String key, String value) throws IOException;

    Optional<String> getValueAt(String key);

    Map<String, String> getHierarchyAt(String key);

    void deleteKey(String key);

    void deleteHierarchyAt(String key);

}
