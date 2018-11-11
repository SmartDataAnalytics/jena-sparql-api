package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;


// Converte inverse of a link into a reverse link
public class PathTransformCanonicalizeReverseLinks
	extends PathTransformCopyBase
{
	@Override
	public Path transform(P_Inverse path, Path subPath) {
		Path result =
				subPath instanceof P_Link ? new P_ReverseLink(((P_Path0)subPath).getNode()) :
				new P_Inverse(subPath);
		
//		Path result =
//				subPath instanceof P_Inverse ? ((P_Inverse)subPath).getSubPath() :
//				subPath instanceof P_Link ? new P_ReverseLink(((P_Path0)subPath).getNode()) :
//				subPath instanceof P_ReverseLink ? new P_Link(((P_Path0)subPath).getNode()) :
//				new P_Inverse(subPath);

		return result;
	}	
}
