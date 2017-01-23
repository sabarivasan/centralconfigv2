package com.centralconfig.publish;

import com.centralconfig.dw.CentralConfigConfiguration;
import com.centralconfig.model.Alias;
import com.centralconfig.model.ConfigChange;
import com.centralconfig.model.Delta;
import com.centralconfig.model.YamlDocument;
import com.centralconfig.persist.KVStore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by sabarivasan on 11/21/16.
 */
public class DocumentDependencyManager {

    // Key is source doc, value is dependencies of source (all docs that source depends on)
    private final Map<String, Set<String>> forward = new HashMap<>();

    // Key is source doc, value is all docs that depend on source doc
    private final Map<String, Set<String>> backward = new HashMap<>();

    private final CentralConfigConfiguration config;
    private final KVStore kvstore;
    private final ConfigChangePublisher configChangePublisher;

    public DocumentDependencyManager(CentralConfigConfiguration config, KVStore kvstore, ConfigChangePublisher
            configChangePublisher) {
        this.config = config;
        this.kvstore = kvstore;
        this.configChangePublisher = configChangePublisher;
    }


    // TODO: Receive a config change object here rather than the changed doc and update forward and backward correctly
    public void configChanged(YamlDocument doc, ConfigChange configChange) {
        updateForwardBackward(doc);


        Set<String> bds = backward.get(doc.getNamespacePath());
        if (bds != null) {
            SortedSet<Delta> backwardDeltas = new TreeSet<>();
            for (String nsPath: bds) {
                Optional<String> ser = kvstore.getValueAt(nsPath);
                if (ser.isPresent()) {
                    YamlDocument d = new YamlDocument(ser.get());
                    for (Alias alias : d.getAliases()) {
                        for (Delta forwardDelta : configChange.getDeltas()) {
                            int ind = forwardDelta.getLeafPath().indexOf(alias.getTo().getDocPath());
                            if (ind > -1) {
                                // TODO: fix this logic
                                Delta backwardDelta = new Delta(forwardDelta.getType(), forwardDelta.getLeafPath(),
                                                                forwardDelta.getOld(), forwardDelta.getNew());
                                backwardDeltas.add(backwardDelta);
                            }
                        }
                    }

                    ConfigChange backwardConfigChange = new ConfigChange(d.getNamespacePath(), configChange.getAuthor(),
                                                                         configChange.getModifiedAt(),
                                                                         backwardDeltas);
                    configChangePublisher.configChanged(d.getNamespacePath(), backwardConfigChange);
                }
            }
        }
    }

    private void updateForwardBackward(YamlDocument doc) {
        for (String dep: doc.getNamespacePathDependencies()) {
            Set<String> f = forward.get(doc.getNamespacePath());
            if (f == null) {
                f = new HashSet<>();
                forward.put(doc.getNamespacePath(), f);
            }
            f.add(dep);

            Set<String> b = backward.get(dep);
            if (b == null) {
                b = new HashSet<>();
                backward.put(dep, b);
            }
            b.add(doc.getNamespacePath());
        }
    }

}
