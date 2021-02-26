package org.aksw.jena_sparql_api.utils.io;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.writer.WriterStreamRDFBase;

public class WriterStreamRDFBaseUtils {
    /**
     * Hack to change the value of the {@link WriterStreamRDFBase}'s final nodeToLabel field
     * Source: https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection
     */
    public static void setLabelToNode(WriterStreamRDFBase writer, LabelToNode labelToNode) {
        try {
            Field nodeToLabelField = WriterStreamRDFBase.class.getDeclaredField("nodeToLabel");
            nodeToLabelField.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(nodeToLabelField, nodeToLabelField.getModifiers() & ~Modifier.FINAL);
            nodeToLabelField.set(writer, labelToNode);

            Method setFormatterMethod = WriterStreamRDFBase.class.getDeclaredMethod("setFormatter");
            setFormatterMethod.setAccessible(true);
            setFormatterMethod.invoke(writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

