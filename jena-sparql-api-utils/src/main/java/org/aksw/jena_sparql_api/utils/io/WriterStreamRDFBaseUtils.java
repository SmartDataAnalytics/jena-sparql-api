package org.aksw.jena_sparql_api.utils.io;

import java.lang.reflect.Method;

import org.aksw.commons.util.reflect.ClassUtils;
import org.apache.jena.irix.IRIx;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.writer.WriterStreamRDFBase;

public class WriterStreamRDFBaseUtils {


    /**
     * Hack to change the value of the {@link WriterStreamRDFBase}'s final nodeToLabel field
     * Source: https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection
     */
    public static void setNodeToLabel(WriterStreamRDFBase writer, NodeToLabel nodeToLabel) {
        try {
            ClassUtils.setFieldValue(WriterStreamRDFBase.class, "nodeToLabel", writer, nodeToLabel);
//            Field nodeToLabelField = WriterStreamRDFBase.class.getDeclaredField("nodeToLabel");
//            nodeToLabelField.setAccessible(true);
//
//            Field modifiersField = Field.class.getDeclaredField("modifiers");
//            modifiersField.setAccessible(true);
//            modifiersField.setInt(nodeToLabelField, nodeToLabelField.getModifiers() & ~Modifier.FINAL);
//            nodeToLabelField.set(writer, nodeToLabel);

            Method setFormatterMethod = WriterStreamRDFBase.class.getDeclaredMethod("setFormatter");
            setFormatterMethod.setAccessible(true);
            setFormatterMethod.invoke(writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setNodeFormatter(WriterStreamRDFBase writer, NodeFormatterTTL fmt) {
        ClassUtils.setFieldValue(WriterStreamRDFBase.class, "fmt", writer, fmt);
    }

    public static void setNodeFormatterIRIx(WriterStreamRDFBase writer, IRIx baseIri) {
        NodeFormatterTTL fmt = ClassUtils.getFieldValue(WriterStreamRDFBase.class, "fmt", writer);
        setBaseIRI(fmt, baseIri);
    }

    // TODO move to NodeFormatterTTLUtils
    public static void setBaseIRI(NodeFormatterTTL fmt, IRIx baseIri) {
        ClassUtils.setFieldValue(NodeFormatterTTL.class, "baseIRI", fmt, baseIri);
    }

    /** Return the internal prefix map */
    public static PrefixMap getPrefixMap(WriterStreamRDFBase writer) {
        PrefixMap result = ClassUtils.getFieldValue(WriterStreamRDFBase.class, "pMap", writer);
//            Field pMapField = WriterStreamRDFBase.class.getDeclaredField("pMap");
//            pMapField.setAccessible(true);
//            result = (PrefixMap)pMapField.get(writer);
//            pMapField.setAccessible(false);

        return result;
    }
}

