package org.aksw.jena_sparql_api.algebra.utils;

import org.aksw.jena_sparql_api.algebra.analysis.VarInfo;
import org.apache.jena.sparql.algebra.Op;

/**
 * TODO Maybe rename to OpWithModifiers
 * The intent of this class is to capture a 'core' op together with specific modifiers to be
 * applied afterwards:
 *   - projection, distinct and filters
 *
 * Issue: In which order to apply these? distinct and filters can be swapped;
 * I suppose a reasonable order could be: filter, project, distinct
 *
 * Further, I think a OpWithModifiers is itself an Op again, so that the distinct / project / filter can be nested.
 *
 * @author raven
 *
 */
public class ProjectedOp {
    //protected VarInfo varInfo;
    //protected VarExprList projectVars;
    //protected boolean distinct;
    protected VarInfo varInfo;
    protected Op residualOp;


    // TODO: Add a Filter attribute (maybe require DNF)
    //protected Set<Set<Expr>> filterDnf;

//
//	public ProjectedOp(Set<Var> projectVars, boolean distinct, Op residualOp) {
//		super();
////		this.projectVars = new VarExprList();
////		for(Var v : projectVars) {
////			this.projectVars.add(v);
////		}
////
////		this.distinct = distinct;
//		this.varInfo = new VarInfo(projectVars, distinct == true ? 2 : 0);
//		this.residualOp = residualOp;
//	}

    public ProjectedOp(VarInfo varInfo, Op residualOp) {
        super();
//		this.projectVars = projection;
//		this.distinct = distinct;
        this.varInfo = varInfo;
        this.residualOp = residualOp;
    }

    public VarInfo getProjection() {
        return varInfo;
    }

    @Deprecated
    public boolean isDistinct() {
        return varInfo.getDistinctLevel() > 0;
    }

    public Op getResidualOp() {
        return residualOp;
    }

    @Override
    public String toString() {
        return "ProjectedOp [varInfo=" + varInfo + ", residualOp=" + residualOp + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((residualOp == null) ? 0 : residualOp.hashCode());
        result = prime * result + ((varInfo == null) ? 0 : varInfo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProjectedOp other = (ProjectedOp) obj;
        if (residualOp == null) {
            if (other.residualOp != null)
                return false;
        } else if (!residualOp.equals(other.residualOp))
            return false;
        if (varInfo == null) {
            if (other.varInfo != null)
                return false;
        } else if (!varInfo.equals(other.varInfo))
            return false;
        return true;
    }
}
