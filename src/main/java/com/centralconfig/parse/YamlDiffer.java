package com.centralconfig.parse;


import com.centralconfig.model.Delta;
import com.centralconfig.model.Document;
import com.centralconfig.model.LeafNode;
import com.centralconfig.model.Value;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Class that compares 2 documents and produces a detailed diff
 */
public final class YamlDiffer {

    private YamlDiffer() { }

    public static SortedSet<Delta> compare(Document left, Document right) {
        SortedSet<Delta> deltas = new TreeSet<>();
        for (LeafNode l: left.getLeaves()) {
            String leafPath = l.getLeaf().getDocPath();
            Value rVal = right.getValueForLeaf(leafPath);
            if (rVal == null) {
                deltas.add(new Delta(Delta.DeltaType.DELETED, leafPath, l.getValue(), null));
            } else if (!rVal.equals(l.getValue())) {
                deltas.add(new Delta(Delta.DeltaType.MODIFIED, leafPath, l.getValue(), rVal));
            }
        }
        for (LeafNode r: right.getLeaves()) {
            String leafPath = r.getLeaf().getDocPath();
            Value lVal = left.getValueForLeaf(leafPath);
            if (lVal == null) {
                deltas.add(new Delta(Delta.DeltaType.ADDED, leafPath, null, r.getValue()));
            }
        }
        return deltas;
    }
}
