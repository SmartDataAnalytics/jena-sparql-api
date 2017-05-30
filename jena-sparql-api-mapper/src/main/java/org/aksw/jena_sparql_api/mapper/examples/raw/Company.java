package org.aksw.jena_sparql_api.mapper.examples.raw;

public class Company {
    private String label;
    private int foundingYear;
    private int numberOfLocations;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getFoundingYear() {
        return foundingYear;
    }

    public void setFoundingYear(int foundingYear) {
        this.foundingYear = foundingYear;
    }

    public int getNumberOfLocations() {
        return numberOfLocations;
    }

    public void setNumberOfLocations(int numberOfLocations) {
        this.numberOfLocations = numberOfLocations;
    }

    @Override
    public String toString() {
        return "Company [label=" + label + ", foundingYear=" + foundingYear + ", numberOfLocations="
                + numberOfLocations + "]";
    }
}