package com.centralconfig.parse;

import com.centralconfig.model.DocType;
import com.centralconfig.model.YamlDocument;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * Unit tests for serialization/deserialization:
 *  - from Yaml/Json to a Document and vice-versa
 *  - in our Database serialization format
 */
public class YamlSerDeserTest {

    @Test
    public void testSerDeser() throws IOException {
        for (DocType docTypeToTest: DocType.YAML_JSON) {

            // Parse canonical document
            String ext = docTypeToTest.toString().toLowerCase();
            YamlDocument doc = YamlSerDeser.parse("blah", getClass().getResourceAsStream("canonical." + ext));

            // Verify that there are 23 leaf nodes
            TestCase.assertEquals(23, doc.getLeaves().size());

            String serialized = doc.ser();
            List<String> actualDbSer = IOUtils.readLines(new StringReader(serialized));
            List<String> expectedDbSer = IOUtils.readLines(getClass().getResourceAsStream("canonical.properties"));
            TestCase.assertEquals(expectedDbSer, actualDbSer);
            TestCase.assertEquals(24, actualDbSer.size());

            // Serialize to file
            File temp = File.createTempFile("test", ext);
            temp.deleteOnExit();
            YamlSerDeser.write(doc, docTypeToTest, new FileOutputStream(temp), false);

            // Parse serialized file
            YamlDocument doc2 = YamlSerDeser.parse("blah", new FileInputStream(temp));

            // Test the document instances are equal
            TestCase.assertEquals(doc2, doc);

            // Test the map instances are equal
            TestCase.assertEquals(doc.getAsMap(false), doc2.getAsMap(false));
        }
    }
}
