package com.demo.mission.service;

import com.demo.mission.model.CrewMember;
import com.demo.mission.model.Mission;
import com.demo.mission.model.Mission.MissionStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class MissionService {

    private static final Logger LOG = Logger.getLogger(MissionService.class);

    @ConfigProperty(name = "mission.max-crew-size", defaultValue = "7")
    int maxCrewSize;

    // ── Missions ──────────────────────────────────────────────────────────────

    public List<Mission> listAllMissions() {
        return Mission.listAll();
    }

    public List<Mission> listMissionsByStatus(MissionStatus status) {
        return Mission.findByStatus(status);
    }

    public Mission getMission(Long id) {
        return Mission.<Mission>findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Mission " + id + " not found"));
    }

    @Transactional
    public Mission createMission(Mission mission) {
        LOG.infof("🚀 Creating new mission: %s → %s", mission.name, mission.destination);
        mission.status = MissionStatus.PLANNING;
        mission.persist();
        return mission;
    }

    @Transactional
    public Mission updateMission(Long id, Mission update) {
        Mission existing = getMission(id);
        existing.name        = update.name != null ? update.name : existing.name;
        existing.description = update.description != null ? update.description : existing.description;
        existing.destination = update.destination != null ? update.destination : existing.destination;
        existing.launchDate  = update.launchDate != null ? update.launchDate : existing.launchDate;
        return existing;
    }

    @Transactional
    public Mission advanceMissionStatus(Long id) {
        Mission mission = getMission(id);
        MissionStatus next = nextStatus(mission.status);
        if (next == null) {
            throw new BadRequestException("Mission is already in terminal state: " + mission.status);
        }
        LOG.infof("🛸 Advancing mission '%s': %s → %s", mission.name, mission.status, next);
        mission.status = next;
        return mission;
    }

    @Transactional
    public Mission abortMission(Long id, String reason) {
        Mission mission = getMission(id);
        if (mission.status == MissionStatus.COMPLETED || mission.status == MissionStatus.ABORTED) {
            throw new BadRequestException("Cannot abort a mission that is already " + mission.status);
        }
        LOG.warnf("💥 Aborting mission '%s'. Reason: %s", mission.name, reason);
        mission.status = MissionStatus.ABORTED;
        return mission;
    }

    @Transactional
    public void deleteMission(Long id) {
        Mission mission = getMission(id);
        if (mission.status == MissionStatus.IN_FLIGHT) {
            throw new BadRequestException("Cannot delete a mission that is currently IN_FLIGHT. Abort it first.");
        }
        mission.delete();
    }

    // ── Crew ─────────────────────────────────────────────────────────────────

    public List<CrewMember> getCrewForMission(Long missionId) {
        getMission(missionId); // ensures mission exists
        return CrewMember.list("mission.id", missionId);
    }

    @Transactional
    public CrewMember assignCrewMember(Long missionId, Long crewMemberId) {
        Mission mission = getMission(missionId);

        if (mission.status == MissionStatus.IN_FLIGHT
                || mission.status == MissionStatus.COMPLETED
                || mission.status == MissionStatus.ABORTED) {
            throw new BadRequestException("Cannot assign crew to a mission in status: " + mission.status);
        }

        CrewMember member = CrewMember.<CrewMember>findByIdOptional(crewMemberId)
                .orElseThrow(() -> new NotFoundException("Crew member " + crewMemberId + " not found"));

        if (member.mission != null) {
            throw new BadRequestException(
                    String.format("Crew member '%s' is already assigned to mission '%s'",
                            member.getFullName(), member.mission.name));
        }

        long currentCrew = CrewMember.count("mission.id", missionId);
        if (currentCrew >= maxCrewSize) {
            throw new BadRequestException(
                    String.format("Mission '%s' already has the maximum crew of %d", mission.name, maxCrewSize));
        }

        member.mission    = mission;
        member.assignedAt = LocalDateTime.now();

        // Auto-advance to CREW_SELECTION if still PLANNING
        if (mission.status == MissionStatus.PLANNING) {
            mission.status = MissionStatus.CREW_SELECTION;
        }

        LOG.infof("👨‍🚀 Assigned %s (%s) to mission '%s'", member.getFullName(), member.role, mission.name);
        return member;
    }

    @Transactional
    public void removeCrewMember(Long missionId, Long crewMemberId) {
        getMission(missionId);
        CrewMember member = CrewMember.<CrewMember>findByIdOptional(crewMemberId)
                .orElseThrow(() -> new NotFoundException("Crew member " + crewMemberId + " not found"));

        if (member.mission == null || !member.mission.id.equals(missionId)) {
            throw new BadRequestException("Crew member is not assigned to this mission");
        }

        member.mission    = null;
        member.assignedAt = null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MissionStatus nextStatus(MissionStatus current) {
        return switch (current) {
            case PLANNING       -> MissionStatus.CREW_SELECTION;
            case CREW_SELECTION -> MissionStatus.TRAINING;
            case TRAINING       -> MissionStatus.LAUNCH_READY;
            case LAUNCH_READY   -> MissionStatus.IN_FLIGHT;
            case IN_FLIGHT      -> MissionStatus.COMPLETED;
            default             -> null; // COMPLETED and ABORTED are terminal
        };
    }
}
