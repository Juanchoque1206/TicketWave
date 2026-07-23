package com.insert7team.TicketWave.venue.entity;

import com.insert7team.TicketWave.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
public class Venue extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false)
    private int totalCapacity;

    @Column(nullable = false)
    private boolean hasAssignedSeating;

    @Column(length = 200)
    private String externalSeatMapId;

    @Column(length = 255)
    private String contactEmail;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }
    public boolean isHasAssignedSeating() { return hasAssignedSeating; }
    public void setHasAssignedSeating(boolean hasAssignedSeating) { this.hasAssignedSeating = hasAssignedSeating; }
    public String getExternalSeatMapId() { return externalSeatMapId; }
    public void setExternalSeatMapId(String externalSeatMapId) { this.externalSeatMapId = externalSeatMapId; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public List<Section> getSections() { return sections; }
    public void setSections(List<Section> sections) { this.sections = sections; }
}
