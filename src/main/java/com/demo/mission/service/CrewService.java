package com.demo.mission.service;

import com.demo.mission.model.CrewMember;
import com.demo.mission.model.CrewMember.CrewRole;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class CrewService {

    private static final Logger LOG = Logger.getLogger(CrewService.class);

    public List<CrewMember> listAll() {
        return CrewMember.listAll();
    }

    public List<CrewMember> listUnassigned() {
        return CrewMember.findUnassigned();
    }

    public List<CrewMember> listByRole(CrewRole role) {
        return CrewMember.findByRole(role);
    }

    public CrewMember get(Long id) {
        return CrewMember.<CrewMember>findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Crew member " + id + " not found"));
    }

    @Transactional
    public CrewMember create(CrewMember member) {
        LOG.infof("👨‍🚀 Registering new crew member: %s (%s)", member.getFullName(), member.role);
        member.persist();
        return member;
    }

    @Transactional
    public CrewMember update(Long id, CrewMember update) {
        CrewMember existing = get(id);
        existing.firstName   = update.firstName   != null ? update.firstName   : existing.firstName;
        existing.lastName    = update.lastName    != null ? update.lastName    : existing.lastName;
        existing.email       = update.email       != null ? update.email       : existing.email;
        existing.role        = update.role        != null ? update.role        : existing.role;
        existing.callsign    = update.callsign    != null ? update.callsign    : existing.callsign;
        existing.flightHours = update.flightHours != null ? update.flightHours : existing.flightHours;
        return existing;
    }

    @Transactional
    public void delete(Long id) {
        CrewMember member = get(id);
        if (member.mission != null) {
            throw new jakarta.ws.rs.BadRequestException(
                    String.format("Cannot delete crew member '%s' — they are assigned to mission '%s'. Remove them from the mission first.",
                            member.getFullName(), member.mission.name));
        }
        member.delete();
    }
}
