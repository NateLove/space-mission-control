package com.demo.mission.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "missions")
public class Mission extends PanacheEntity {

    @NotBlank(message = "Mission name is required")
    @Size(max = 100, message = "Mission name must be 100 characters or fewer")
    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    public String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    public MissionStatus status = MissionStatus.PLANNING;

    @NotBlank
    @Column(name = "destination", nullable = false, length = 100)
    public String destination;

    @Column(name = "launch_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime launchDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime updatedAt;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<CrewMember> crew = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Finders ──────────────────────────────────────────────────────────────

    public static List<Mission> findByStatus(MissionStatus status) {
        return list("status", status);
    }

    public static List<Mission> findByDestination(String destination) {
        return list("destination LIKE ?1", "%" + destination + "%");
    }

    public enum MissionStatus {
        PLANNING,
        CREW_SELECTION,
        TRAINING,
        LAUNCH_READY,
        IN_FLIGHT,
        COMPLETED,
        ABORTED
    }
}
