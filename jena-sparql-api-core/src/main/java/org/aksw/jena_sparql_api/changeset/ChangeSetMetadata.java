package org.aksw.jena_sparql_api.changeset;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ChangeSetMetadata {
    private String creatorName;
    private Calendar createdDate;
    private String changeReason;

    public ChangeSetMetadata() {
        this("anonymous", "unspecified reason");
    }

    public ChangeSetMetadata(String creatorName,
            String changeReason) {
        this(creatorName, changeReason, new GregorianCalendar());
    }

    public ChangeSetMetadata(String creatorName,
            String changeReason, Calendar createdDate) {
        super();
        this.creatorName = creatorName;
        this.createdDate = createdDate;
        this.changeReason = changeReason;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public Calendar getCreatedDate() {
        return createdDate;
    }

    public String getChangeReason() {
        return changeReason;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((changeReason == null) ? 0 : changeReason.hashCode());
        result = prime * result
                + ((createdDate == null) ? 0 : createdDate.hashCode());
        result = prime * result
                + ((creatorName == null) ? 0 : creatorName.hashCode());
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
        ChangeSetMetadata other = (ChangeSetMetadata) obj;
        if (changeReason == null) {
            if (other.changeReason != null)
                return false;
        } else if (!changeReason.equals(other.changeReason))
            return false;
        if (createdDate == null) {
            if (other.createdDate != null)
                return false;
        } else if (!createdDate.equals(other.createdDate))
            return false;
        if (creatorName == null) {
            if (other.creatorName != null)
                return false;
        } else if (!creatorName.equals(other.creatorName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChangeSetMetadata [creatorName=" + creatorName
                + ", createdDate=" + createdDate + ", changeReason="
                + changeReason + "]";
    }

}
