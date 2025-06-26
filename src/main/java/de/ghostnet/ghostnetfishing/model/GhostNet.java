package de.ghostnet.ghostnetfishing.model;

import jakarta.persistence.*;

@Entity
public class GhostNet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String latitude;
    private String longitude;
    private double estimatedSize;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    private Person reportingPerson;


    @ManyToOne
    private Person recoveringPerson;

    public enum Status {
        GEMELDET,
        BERGUNG_BEVORSTEHEND,
        GEBORGEN,
        VERSCHOLLEN
    }

    public GhostNet() {
    }

    public GhostNet(String latitude, String longitude, double estimatedSize, Status status, Person reportingPerson, Person recoveringPerson) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.estimatedSize = estimatedSize;
        this.status = status;
        this.reportingPerson = reportingPerson;
        this.recoveringPerson = recoveringPerson;
    }

    public Long getId() {
        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public double getEstimatedSize() {
        return estimatedSize;
    }

    public Status getStatus() {
        return status;
    }

    public Person getReportingPerson() {
        return reportingPerson;
    }

    public Person getRecoveringPerson() {
        return recoveringPerson;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setEstimatedSize(double estimatedSize) {
        this.estimatedSize = estimatedSize;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setReportingPerson(Person reportingPerson) {
        this.reportingPerson = reportingPerson;
    }

    public void setRecoveringPerson(Person recoveringPerson) {
        this.recoveringPerson = recoveringPerson;
    }
}