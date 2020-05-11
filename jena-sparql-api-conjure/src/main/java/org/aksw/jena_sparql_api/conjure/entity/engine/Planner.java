package org.aksw.jena_sparql_api.conjure.entity.engine;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aksw.dcat.ap.domain.api.Checksum;
import org.aksw.jena_sparql_api.conjure.entity.algebra.Op;
import org.aksw.jena_sparql_api.conjure.entity.algebra.OpCode;
import org.aksw.jena_sparql_api.conjure.entity.algebra.OpConvert;
import org.aksw.jena_sparql_api.conjure.entity.algebra.OpPath;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;



//
//interface OpTransform {
//	Op transform(OpValue op);
//	Op transform(OpVar op);
//	Op transform(OpCode op, Op subOp);
//	Op transform(OpConvert op, Op subOp);
//}

public class Planner {


    /**
     * Provide a concrete plan that converts the given source file entity to a file in the target
     * with the given content type and encoding
     *
     * 'Concrete' refers to the fact that the file's content hash is taken into account
     * TODO Separate abstract and concrete plans? Abstract plan would be the
     * operator expression that  the conversion
     *
     * @param source
     * @param contentType
     * @param encodings
     * @return
     * @throws Exception
     */
    public static Op createPlan(
            RdfHttpEntityFile source,
//			PathGroup srcPathGroup,
//			PathGroup tgtPathGroup,
            //Path relTgtBasePath, // relative path of the target resource
            String tgtContentType,
            List<String> tgtEncodings) {
//		String srcContentType = source.getContentType().getElements()[0].get
        RdfEntityInfo info = source.getCombinedInfo().as(RdfEntityInfo.class);
        String srcContentType = info.getContentType();

//		HashInfo hashInfo = info.as(HashInfo.class);
        Checksum hashInfo = info.getHash("sha256");
        String checksum = Optional.ofNullable(hashInfo)
                .map(Checksum::getChecksum)
                .orElseThrow(() -> new RuntimeException("Planning requires content hash but none assigned to " + source.getAbsolutePath()));

        // The var name is a reference to the source file
        String srcRef = source.getAbsolutePath().toString();
        Map<String, String> varToHash = Collections.singletonMap(srcRef, checksum);
        Op op = OpPath.create(srcRef);

        List<String> srcEncodings = info.getContentEncodings();//getValues(source.getContentEncoding(), HttpHeaders.CONTENT_ENCODING);

        boolean requiresContentConversion = !srcContentType.equals(tgtContentType);

        // Find out how many encodings are the same from the start of the lists
        // This is only valid if there is no content type conversion involved
        int offset = 0;
        if(!requiresContentConversion) {
            int min = Math.min(srcEncodings.size(), tgtEncodings.size());
            for(int i = 0; i < min; ++i) {
                if(srcEncodings.get(i) == tgtEncodings.get(i)) {
                    ++offset; // == i + 1
                } else {
                    break;
                }
            }
        }

        // Decode up to the final offset of the common path head
        for(int i = srcEncodings.size() - 1; i >= offset; --i) {
            String srcEncoding = srcEncodings.get(i);
            op = OpCode.create(op, srcEncoding, true);
        }

        if(requiresContentConversion) {
            op = OpConvert.create(op, srcContentType, tgtContentType);
        }

        // Encode from the final offset of the common path head
        for(int i = offset; i < tgtEncodings.size(); ++i) {
            String tgtEncoding = tgtEncodings.get(i);
            op = OpCode.create(op, tgtEncoding, false);
        }

        return op;
    }
}
