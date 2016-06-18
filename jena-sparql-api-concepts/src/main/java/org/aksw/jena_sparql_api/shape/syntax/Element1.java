package org.aksw.jena_sparql_api.shape.syntax;

public abstract class Element1
    implements Element
{
    protected Element subElement;

    public Element1(Element subElement) {
        super();
        this.subElement = subElement;
    }


    public Element getSubElement() {
        return subElement;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((subElement == null) ? 0 : subElement.hashCode());
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
        Element1 other = (Element1) obj;
        if (subElement == null) {
            if (other.subElement != null)
                return false;
        } else if (!subElement.equals(other.subElement))
            return false;
        return true;
    }


}
