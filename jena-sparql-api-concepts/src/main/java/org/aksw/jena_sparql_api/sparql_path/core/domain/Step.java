package org.aksw.jena_sparql_api.sparql_path.core.domain;

public class Step {
	private String propertyName;
	private boolean isInverse; // Follow property in inverse direction if true

	public Step(String propertyName, boolean isInverse) {
		this.propertyName = propertyName;
		this.isInverse = isInverse;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public boolean isInverse() {
		return isInverse;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isInverse ? 1231 : 1237);
		result = prime * result
				+ ((propertyName == null) ? 0 : propertyName.hashCode());
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
		Step other = (Step) obj;
		if (isInverse != other.isInverse)
			return false;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (!propertyName.equals(other.propertyName))
			return false;
		return true;
	}

	@Override
	public String toString() {
	    String result = (isInverse ? "<" : "") + propertyName;
	    return result;
	    
//		return "Step [propertyName=" + propertyName + ", isInverse="
//				+ isInverse + "]";
				
	}
}