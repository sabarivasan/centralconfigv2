package com.centralconfig.parse;

import com.centralconfig.model.DataType;
import com.centralconfig.model.DocPath;
import com.centralconfig.model.DocType;
import com.centralconfig.model.Document;
import com.centralconfig.model.LeafNode;
import com.centralconfig.model.Value;
import com.centralconfig.persist.DbSerializable;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Serialize/Deserialization utility for yaml/json documents:
 *  - from Yaml/Json to a Document and vice-versa
 *  - in our Database serialization format (for storage in a database)
 */
public final class YamlSerDeser {

    private YamlSerDeser() { }

    /**
     * Writes a document in requested DocType
     *
     * @param doc                   the document to write
     * @param docType               the type of document to write as
     * @param outputStream          the stream to write serialized content to
     * @param expandAliases         should aliases in the document be expanded?
     * @throws IOException          in case something goes wrong
     */
    public static void write(Document doc, DocType docType, OutputStream outputStream,
                                 boolean expandAliases) throws IOException {

        try (OutputStream os = outputStream) {
            JsonFactory factory = DocType.YAML == docType ? new YAMLFactory() : new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(factory);
            mapper.writerWithDefaultPrettyPrinter().writeValue(os, doc.getAsMap(expandAliases));
        }
    }

    /**
     * Parse a yaml document and return a Document
     *
     * @param namespacePath the namespace path (not used anywhere in the parsing)
     * @param doc           the input stream containing the yaml to parse. This stream will be closed
     * @return              a Document instance
     * @throws IOException
     */
    public static Document parse(final String namespacePath, final InputStream doc) throws IOException {

        try (InputStream is = doc) {
            SortedSet<LeafNode> leaves = new TreeSet<>();
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Map<?, ?> root = mapper.readValue(is, Map.class);
            List<String> currPath = new ArrayList<>();
            visitMap(leaves, root, currPath);
            return new Document(namespacePath, leaves);
        }
    }

    private static void visitMap(SortedSet<LeafNode> leaves, Map<?, ?> root, List<String> currPath) {
        for (Map.Entry<?, ?> entry : root.entrySet()) {
            String key = entry.getKey().toString();
            currPath.add(key);
            parseKeyValue(entry.getValue(), leaves, currPath);
            currPath.remove(currPath.size() - 1);
        }
    }

    private static void parseKeyValue(Object value,
                                      final SortedSet<LeafNode> leaves, final List<String> currPath) {
        if (value instanceof Map) {
            visitMap(leaves, (Map<?, ?>) value, currPath);
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            for (int i = 0; i < list.size(); i++) {
                currPath.add(DbSerializable.serializeArrayInd(i));
                parseKeyValue(list.get(i), leaves, currPath);
                currPath.remove(currPath.size() - 1);
            }
        } else {
            // Leaf node
            DataType dataType = DataType.inferType(value);
            if (dataType == null) {
                throw new InternalError(String.format("Could not recognize leaf value at path %s",
                                                      StringUtils.join(currPath, DbSerializable.HIER_SEPARATOR)));
            }
            leaves.add(new LeafNode(new DocPath(StringUtils.join(currPath, DbSerializable.HIER_SEPARATOR), true),
                                    new Value(value, dataType)));
        }
    }

}
