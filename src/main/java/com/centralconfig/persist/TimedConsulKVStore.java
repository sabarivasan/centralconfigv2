package com.centralconfig.persist;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.sun.jersey.core.util.Base64;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An implementation of TimedKVStore for Consul key-value store
 *
 * Created by sabarivasan on 1/19/17.
 */
public class TimedConsulKVStore implements TimedKVStore {
    static final String KEY_TIMESTAMP_SEPARATOR = "/";

    // We only support timestamps in millis that are 13 digits long
    // In other words, from Sun, 09 Sep 2001 01:46:40 GMT through Sat, 20 Nov 2286 17:46:39.999 GMT
    // We do this for convenience and to avoid 0-padding of keys, etc
    public static final int TIMESTAMP_LENGTH = 13;

    private ConsulClient client;

    public TimedConsulKVStore(String endpoint) {
        int ind = endpoint.lastIndexOf(":");
        client = new ConsulClient(endpoint.substring(0, ind),
                                  Integer.parseInt(endpoint.substring(ind + 1)));
    }

    @Override
    public void putNow(String key, String value) throws IOException {
        putAt(key, value, System.currentTimeMillis());
    }

    @Override
    public void putAt(String key, String value, long timestampMillis) throws IOException {
        if (!client.setKVValue(KeyHelper.key(key, timestampMillis), value).getValue()) {
            throw new IOException("Write failed");
        }
    }

    @Override
    public Optional<String> getValueAt(String key, long timestampMillis) {
        String keyPrefix = KeyHelper.keyPrefixForAllTimestamps(key);
        Response<List<String>> r = client.getKVKeysOnly(keyPrefix);
        List<String> values = r.getValue();
        Response<GetValue> val = null;
        if (values != null) {
            SortedSet<String> sortedKeys = new TreeSet<>(values); // natural ordering, all timestamps have same length
            String matchedKey = SearchHelper.searchKeyAtTs(sortedKeys, keyPrefix, timestampMillis);
            if (matchedKey != null) {
                val = client.getKVValue(matchedKey);
            }
        }
        return val != null && val.getValue() != null ? Optional.of(Base64.base64Decode(val.getValue().getValue()))
                : Optional.empty();

    }

    @Override
    public void deleteAllValuesFor(String key) {
        List<String> keys = client.getKVKeysOnly(KeyHelper.keyPrefixForAllTimestamps(key)).getValue();
        if (keys != null) {
            for (String k : keys) {
                client.deleteKVValue(k);
            }
        }
    }

    private static class KeyHelper {

        // Consul key for a specific timestamp
        private static final String key(String key, long timestampMillis) {
            String tsMillisStr = String.valueOf(timestampMillis);
            if (TIMESTAMP_LENGTH != tsMillisStr.length()) {
                throw new IllegalArgumentException("TimestampMillis must be 13 digits long");
            }
            return keyPrefixForAllTimestamps(key)+ tsMillisStr;
        }

        // Consul key prefix for all timestamps of a given key
        private static final String keyPrefixForAllTimestamps(String key) {
            return key + KEY_TIMESTAMP_SEPARATOR;
        }

    }

    private static class SearchHelper {

        /**
         *              V1           V2                V3
         *              |-------------|-----------------|
         * time->       TS1         TS2                TS3
         *
         *      getAt(K, TS1 - 1) = absent
         *      getAt(K, ts) = V1 where V1 <= ts < V2
         *      getAt(K, ts) = V2 where V2 <= ts < V3
         *      getAt(K, ts) = V3 where ts >= TS3
         *
         */
        private static String searchKeyAtTs(SortedSet<String> sortedKeys, String commonKeyPrefix,
                                            long tsToSearch) {
            long prev = Long.MIN_VALUE;
            String prevKey = null;
            for (String k: sortedKeys) {
                long ts = Long.parseLong(k.substring(commonKeyPrefix.length()));
                if (tsToSearch >= prev && tsToSearch < ts) {
                    return prevKey;
                }
                prevKey = k;
            }
            return prevKey;
        }
    }
}
