package org.aksw.jena_sparql_api.mapper.test.domain;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;

//@DefaultIri("o:#{firstName}-#{lastName}-#{birthPlace}-#{birthDate.toString()}")
@DefaultIri("o:#{firstName}-#{lastName}-#{birthPlace}")
public class Person {
    @Iri("foaf:firstName")
    private String firstName;

    @Iri("foaf:lastName")
    private String lastName;

    @Iri("foaf:birthPlace")
    private String birthPlace;

    @Iri("foaf:birthDate")
    private Calendar birthDate;

    @Iri("foaf:tags")
    private Map<String, Person> tags = new HashMap<>();
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public Calendar getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Calendar birthDate) {
        this.birthDate = birthDate;
    }

    public Map<String, Person> getTags() {
		return tags;
	}

	public void setTags(Map<String, Person> tags) {
		this.tags = tags;
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((birthDate == null) ? 0 : birthDate.hashCode());
        result = prime * result
                + ((birthPlace == null) ? 0 : birthPlace.hashCode());
        result = prime * result
                + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result
                + ((lastName == null) ? 0 : lastName.hashCode());
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
        Person other = (Person) obj;
        if (birthDate == null) {
            if (other.birthDate != null)
                return false;
        } else if (!birthDate.equals(other.birthDate))
            return false;
        if (birthPlace == null) {
            if (other.birthPlace != null)
                return false;
        } else if (!birthPlace.equals(other.birthPlace))
            return false;
        if (firstName == null) {
            if (other.firstName != null)
                return false;
        } else if (!firstName.equals(other.firstName))
            return false;
        if (lastName == null) {
            if (other.lastName != null)
                return false;
        } else if (!lastName.equals(other.lastName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Person [firstName=" + firstName + ", lastName=" + lastName
                + ", birthPlace=" + birthPlace + ", birthDate=" + birthDate
                + "]";
    }
}
