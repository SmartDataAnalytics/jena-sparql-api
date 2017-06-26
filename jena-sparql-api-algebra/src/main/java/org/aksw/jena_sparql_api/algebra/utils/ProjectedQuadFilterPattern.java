package org.aksw.jena_sparql_api.algebra.utils;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class ProjectedQuadFilterPattern {

    protected Set<Var> projectVars;
    protected QuadFilterPattern quadFilterPattern;
    // TODO Maybe add a distinct flag
    protected boolean isDistinct;

    public ProjectedQuadFilterPattern(Set<Var> projectVars,
            QuadFilterPattern qfp, boolean isDistinct) {
        super();
        this.projectVars = projectVars;
        this.quadFilterPattern = qfp;
        this.isDistinct = isDistinct;
    }

    public boolean isDistinct() {
    	return isDistinct;
    }

    public Set<Var> getProjectVars() {
        return projectVars;
    }

    public QuadFilterPattern getQuadFilterPattern() {
        return quadFilterPattern;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isDistinct ? 1231 : 1237);
		result = prime * result + ((projectVars == null) ? 0 : projectVars.hashCode());
		result = prime * result + ((quadFilterPattern == null) ? 0 : quadFilterPattern.hashCode());
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
		ProjectedQuadFilterPattern other = (ProjectedQuadFilterPattern) obj;
		if (isDistinct != other.isDistinct)
			return false;
		if (projectVars == null) {
			if (other.projectVars != null)
				return false;
		} else if (!projectVars.equals(other.projectVars))
			return false;
		if (quadFilterPattern == null) {
			if (other.quadFilterPattern != null)
				return false;
		} else if (!quadFilterPattern.equals(other.quadFilterPattern))
			return false;
		return true;
	}

    @Override
    public String toString() {
        return "ProjectedQuadFilterPattern [projectVars=" + projectVars
                + ", qfp=" + quadFilterPattern + ", isDistinct=" + isDistinct+ "]";
    }
}
