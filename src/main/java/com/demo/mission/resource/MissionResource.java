package com.demo.mission.resource;

import com.demo.mission.model.CrewMember;
import com.demo.mission.model.Mission;
import com.demo.mission.model.Mission.MissionStatus;
import com.demo.mission.service.MissionService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Path("/missions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Missions", description = "Manage space missions")
public class MissionResource {

    @Inject
    MissionService missionService;

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @GET
    @Operation(summary = "List all missions", description = "Returns all missions, optionally filtered by status")
    public List<Mission> list(
            @Parameter(description = "Filter by mission status")
            @QueryParam("status") MissionStatus status) {
        return status != null
                ? missionService.listMissionsByStatus(status)
                : missionService.listAllMissions();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get a mission by ID")
    public Mission get(@PathParam("id") Long id) {
        return missionService.getMission(id);
    }

    @POST
    @Operation(summary = "Create a new mission")
    public Response create(@Valid Mission mission) {
        Mission created = missionService.createMission(mission);
        return Response.created(URI.create("/api/missions/" + created.id))
                .entity(created)
                .build();
    }

    @PATCH
    @Path("/{id}")
    @Operation(summary = "Update mission details")
    public Mission update(@PathParam("id") Long id, Mission update) {
        return missionService.updateMission(id, update);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a mission")
    public Response delete(@PathParam("id") Long id) {
        missionService.deleteMission(id);
        return Response.noContent().build();
    }

    // ── Status Transitions ────────────────────────────────────────────────────

    @POST
    @Path("/{id}/advance")
    @Operation(
        summary = "Advance mission status",
        description = "Moves the mission to the next stage: PLANNING → CREW_SELECTION → TRAINING → LAUNCH_READY → IN_FLIGHT → COMPLETED"
    )
    public Mission advance(@PathParam("id") Long id) {
        return missionService.advanceMissionStatus(id);
    }

    @POST
    @Path("/{id}/abort")
    @Operation(summary = "Abort a mission")
    public Mission abort(
            @PathParam("id") Long id,
            Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        return missionService.abortMission(id, reason);
    }

    // ── Crew Management ───────────────────────────────────────────────────────

    @GET
    @Path("/{id}/crew")
    @Operation(summary = "List crew assigned to a mission")
    public List<CrewMember> getCrew(@PathParam("id") Long missionId) {
        return missionService.getCrewForMission(missionId);
    }

    @POST
    @Path("/{id}/crew/{crewId}")
    @Operation(summary = "Assign a crew member to this mission")
    public Response assignCrew(
            @PathParam("id") Long missionId,
            @PathParam("crewId") Long crewId) {
        CrewMember assigned = missionService.assignCrewMember(missionId, crewId);
        return Response.ok(assigned).build();
    }

    @DELETE
    @Path("/{id}/crew/{crewId}")
    @Operation(summary = "Remove a crew member from this mission")
    public Response removeCrew(
            @PathParam("id") Long missionId,
            @PathParam("crewId") Long crewId) {
        missionService.removeCrewMember(missionId, crewId);
        return Response.noContent().build();
    }
}
