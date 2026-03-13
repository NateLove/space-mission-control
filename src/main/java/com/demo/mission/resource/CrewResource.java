package com.demo.mission.resource;

import com.demo.mission.model.CrewMember;
import com.demo.mission.model.CrewMember.CrewRole;
import com.demo.mission.service.CrewService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

@Path("/crew")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Crew", description = "Manage astronaut crew members")
public class CrewResource {

    @Inject
    CrewService crewService;

    @GET
    @Operation(summary = "List all crew members")
    public List<CrewMember> list(
            @QueryParam("role") CrewRole role,
            @QueryParam("unassigned") @DefaultValue("false") boolean unassigned) {
        if (unassigned) return crewService.listUnassigned();
        if (role != null) return crewService.listByRole(role);
        return crewService.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get a crew member by ID")
    public CrewMember get(@PathParam("id") Long id) {
        return crewService.get(id);
    }

    @POST
    @Operation(summary = "Register a new crew member")
    public Response create(@Valid CrewMember member) {
        CrewMember created = crewService.create(member);
        return Response.created(URI.create("/api/crew/" + created.id))
                .entity(created)
                .build();
    }

    @PATCH
    @Path("/{id}")
    @Operation(summary = "Update crew member details")
    public CrewMember update(@PathParam("id") Long id, CrewMember update) {
        return crewService.update(id, update);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a crew member")
    public Response delete(@PathParam("id") Long id) {
        crewService.delete(id);
        return Response.noContent().build();
    }
}
