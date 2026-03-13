package com.demo.mission.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "crew_members")
public class CrewMember extends PanacheEntity {

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false, length = 50)
    public String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false, length = 50)
    public String lastName;

    @Email(message = "Must be a valid email")
    @Column(name = "email", unique = true, length = 100)
    public String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    public CrewRole role;

    @Column(name = "callsign", length = 30)
    public String callsign;

    @Column(name = "flight_hours")
    public Integer flightHours = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    @JsonIgnore
    public Mission mission;

    @Column(name = "assigned_at")
    public LocalDateTime assignedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public Long getMissionId() {
        return mission != null ? mission.id : null;
    }

    // ── Finders ──────────────────────────────────────────────────────────────

    public static List<CrewMember> findByRole(CrewRole role) {
        return list("role", role);
    }

    public static List<CrewMember> findUnassigned() {
        return list("mission IS NULL");
    }

    public enum CrewRole {
        COMMANDER,
        PILOT,
        MISSION_SPECIALIST,
        FLIGHT_ENGINEER,
        PAYLOAD_SPECIALIST,
        MEDICAL_OFFICER,
        SCIENCE_OFFICER
    }
}
