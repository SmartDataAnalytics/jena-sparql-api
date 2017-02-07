package org.aksw.jena_sparql_api.mapper.context;

public class Occurrence {
    protected Object parentEntity;
    protected String propertyName;

    public Occurrence(Object parentEntity, String propertyName) {
        super();
        this.parentEntity = parentEntity;
        this.propertyName = propertyName;
    }

    public Object getParentEntity() {
        return parentEntity;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((parentEntity == null) ? 0 : parentEntity.hashCode());
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
        Occurrence other = (Occurrence) obj;
        if (parentEntity == null) {
            if (other.parentEntity != null)
                return false;
        } else if (!parentEntity.equals(other.parentEntity))
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
        return "Occurrence [parentEntity=" + parentEntity + ", propertyName="
                + propertyName + "]";
    }
}
