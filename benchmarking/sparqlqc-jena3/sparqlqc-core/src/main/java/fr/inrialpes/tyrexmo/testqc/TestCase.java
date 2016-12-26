package fr.inrialpes.tyrexmo.testqc;

import org.apache.jena.query.Query;

public class TestCase {
    protected Query source;
    protected Query target;
    protected boolean expectedResult;

    public TestCase(Query source, Query target, boolean expectedResult) {
        super();
        this.source = source;
        this.target = target;
        this.expectedResult = expectedResult;
    }

    public Query getSource() {
        return source;
    }

    public Query getTarget() {
        return target;
    }

    public boolean getExpectedResult() {
        return expectedResult;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (expectedResult ? 1231 : 1237);
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
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
        TestCase other = (TestCase) obj;
        if (expectedResult != other.expectedResult)
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }
}