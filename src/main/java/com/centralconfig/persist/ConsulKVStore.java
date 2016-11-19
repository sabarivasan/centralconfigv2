package com.centralconfig.persist;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.sun.jersey.core.util.Base64;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A Consul client from Ecwid (https://github.com/Ecwid/consul-api)
 * that stores one key/value for each leaf node on a document
 *
 * Created by sviswanathan on 2/11/16.
 */
public class ConsulKVStore implements KVStore {

   private ConsulClient client;

   public ConsulKVStore(String endpoint) {
      int ind = endpoint.lastIndexOf(":");
      client = new ConsulClient(endpoint.substring(0, ind),
                     Integer.parseInt(endpoint.substring(ind + 1)));
   }

   @Override
   public void put(String key, String value) throws IOException {
      if (!client.setKVValue(key, value).getValue()) {
         throw new IOException("Write failed");
      }
   }

   @Override
   public Optional<String> getValueAt(String key) {
      Response<GetValue> val = client.getKVValue(key);
      return val.getValue() != null ? Optional.of(Base64.base64Decode(val.getValue().getValue())) : Optional.empty();
   }

   @Override
   public Map<String, String> getHierarchyAt(String key) {
//           , Function<String, String> keyTransform) {
      Response<List<GetValue>> vals = client.getKVValues(key);
      if (vals.getValue() != null) {
//         if (keyTransform != null) {
//            return vals.getValue().stream().collect(Collectors.toMap(
//                  gv -> keyTransform.apply(gv.getKey()),
//                  gv -> Base64.base64Decode(gv.getValue())));
//         } else {
            return vals.getValue().stream().collect(Collectors.toMap(GetValue::getKey,
                  gv -> Base64.base64Decode(gv.getValue())));
//         }
      } else {
         return new HashMap<>();
      }
   }

   //@Override
   public Optional<Collection<String>> getKeysAt(String key) {
      Response<List<String>> keys = client.getKVKeysOnly(key);
      return keys.getValue() != null ? Optional.of(keys.getValue()) : Optional.empty();
   }

   @Override
   public void deleteKey(String key) {
      client.deleteKVValue(key);
   }

   @Override
   public void deleteHierarchyAt(String key) {
      client.deleteKVValues(key);
   }


}
