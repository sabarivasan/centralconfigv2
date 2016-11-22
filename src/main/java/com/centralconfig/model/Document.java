package com.centralconfig.model;

import com.centralconfig.parse.YamlDiffer;
import com.centralconfig.persist.DbSerializable;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A document represents a yaml/json document with all its keys
 */
public class Document implements DbSerializable<Document> {
    private final String namespacePath;
    private final SortedSet<LeafNode> leaves;
    private SortedSet<LeafNode> leavesAliasesExpanded = null;
    private final int numNamespaceLevels;
    private Map<String, Value> leafValuesByPath = null;
    private Set<Alias> aliases = null;
    private Set<String> namespaceDependencies = null;

    public Document(String ser) {
        String[] rows = ser.split(DbSerializable.SER_ROW_SEPARATOR);
        namespacePath = rows[0].split(DbSerializable.SER_KEY_VALUE_SEPARATOR)[1];
        this.numNamespaceLevels = namespacePath.split(DbSerializable.HIER_SEPARATOR).length;
        SortedSet<LeafNode> lvs = new TreeSet<>();
        Map<String, Value>  lValuesByPath = new HashMap<>();
        for (int n = 1; n < rows.length; n++) {
            LeafNode leafNode = new LeafNode(rows[n]);
            lvs.add(leafNode);
            lValuesByPath.put(leafNode.getLeaf().getDocPath(), leafNode.getValue());
        }
        this.leafValuesByPath = Collections.unmodifiableMap(lValuesByPath);
        this.leaves = Collections.unmodifiableSortedSet(lvs);
    }

    public Document(String namespacePath, SortedSet<LeafNode> leaves) {
        this.namespacePath = namespacePath;
        this.numNamespaceLevels = namespacePath.split(DbSerializable.HIER_SEPARATOR).length;
        this.leaves = Collections.unmodifiableSortedSet(leaves);
    }


    public SortedSet<Delta> compareWith(Document right) {
        return YamlDiffer.compare(this, right);
    }

    public String getNamespacePath() {
        return namespacePath;
    }

    public SortedSet<LeafNode> getLeaves() {
        return leaves;
    }

    // Return sub-tree given an intermediate (or leaf) node
    private SortedSet<LeafNode> getLeaves(String startingWithDocPath) {
        SortedSet<LeafNode> subtree = new TreeSet<>();
        for (LeafNode l: leaves) {
            if (l.getLeaf().getDocPath().startsWith(startingWithDocPath)) {
                subtree.add(l);
            }
        }
        return subtree;
    }


    public Value getValueForLeaf(String leafPath) {
        constructLeafValuesByPath();
        return leafValuesByPath.get(leafPath);
    }

    private void constructLeafValuesByPath() {
        if (leafValuesByPath == null) {
            leafValuesByPath = new HashMap<>();
            for (LeafNode leaf: leaves) {
                leafValuesByPath.put(leaf.getLeaf().getDocPath(), leaf.getValue());
            }
        }
    }

    @Override
    public String ser() {
        StringBuilder sb = new StringBuilder(Constants.LENGTH_1024);
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


    // The set of namespace paths this document depends on
    public Set<String> getNamespacePathDependencies() {
        initAliasesDependencies();
        return namespaceDependencies;
    }

    public Set<Alias> getAliases() {
        initAliasesDependencies();
        return aliases;
    }

    private void initAliasesDependencies() {
        if (aliases == null) {
            aliases = new HashSet<>();
            Set<String> nsDependencies = new HashSet<>();
            for (LeafNode leaf: leaves) {
                if (leaf.isAlias()) {
                    YPath from = new YPath(namespacePath, leaf.getLeaf().getDocPath());
                    YPath to = new YPath(leaf.getValue().getAliasDestination(), numNamespaceLevels);
                    aliases.add(new Alias(from, to));
                    nsDependencies.add(to.getNamespacePath());
                }
            }
            this.namespaceDependencies = Collections.unmodifiableSet(nsDependencies);
        }
    }

    /**
     * Provide the set of dependent documents so that the aliases can be expanded
     * //TODO: implement in-doc alias expansion
     * //TODO: Should we support nested alias references?
     * @param dependentDocs
     */
    public void expandAliases(Map<String, Document> dependentDocs) {
        initAliasesDependencies();
        if (leavesAliasesExpanded == null) {
            leavesAliasesExpanded = new TreeSet<>();
            for (LeafNode leaf: leaves) {
                if (leaf.isAlias()) {
                    YPath from = new YPath(namespacePath, leaf.getLeaf().getDocPath());
                    YPath to = new YPath(leaf.getValue().getAliasDestination(), numNamespaceLevels);
                    Document dependent = dependentDocs.get(to.getNamespacePath());
                    if (dependent == null) {
                        throw new IllegalStateException(String.format("Dependent doc not provided. From:%s To:%s",
                                                                      from.getFullPath(), to.getFullPath()));
                    }
                    SortedSet<LeafNode> depLeaves = dependent.getLeaves(to.getDocPath());
                    for (LeafNode dep: depLeaves) {
                        if (dep.getLeaf().getDocPath().equals(to.getDocPath())) {
                            // This means it is an alias to a leaf node (primitive)
                            leavesAliasesExpanded.add(new LeafNode(
                                    new DocPath(from.getDocPath(), true),
                                    dependent.getValueForLeaf(dep.getLeaf().getDocPath())));
                        } else {
                            String suffix = StringUtils.substringAfter(dep.getLeaf().getDocPath(), to.getDocPath());
                            leavesAliasesExpanded.add(new LeafNode(new DocPath(from.getDocPath() + suffix, true),
                                                      dep.getValue()));
                        }
                    }
                } else {
                    leavesAliasesExpanded.add(leaf);
                }
            }
        }
    }

    public Map<?, ?> getAsMap(boolean expandAliases) {
        if (expandAliases && leavesAliasesExpanded == null) {
            throw new InternalError("getAsMap(expandAliases=true) called before expandAliases()");
        }
        Map root = new HashMap<>();
        for (LeafNode leaf: (expandAliases ? leavesAliasesExpanded : leaves)) {
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Document document = (Document) o;

        if (!getNamespacePath().equals(document.getNamespacePath())) {
            return false;
        }
        return getLeaves().equals(document.getLeaves());
    }

    @Override
    public int hashCode() {
        int result = getNamespacePath().hashCode();
        result = 31 * result + getLeaves().hashCode();
        return result;
    }

}
