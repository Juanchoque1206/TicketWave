package com.insert7team.TicketWave.venue.entity;

import com.insert7team.TicketWave.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"section_id", "seat_row", "number"})
})
public class Seat extends BaseEntity {

    @Column(name = "seat_row", nullable = false, length = 10)
    private String row;

    @Column(nullable = false)
    private int number;

    @Column(length = 20)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    public String getRow() { return row; }
    public void setRow(String row) { this.row = row; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }
}
