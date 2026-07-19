package org.example.model;

public class Status {
    private StatusType type;
    private String description;

    public Status(StatusType type, String description) {
        this.type = type;
        this.description = description;
    }

    public StatusType getType() {
        return type;
    }

    public void setType(StatusType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
