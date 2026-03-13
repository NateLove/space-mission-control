package com.demo.mission.health;

import com.demo.mission.model.Mission;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class MissionControlHealthCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        try {
            long missionCount    = Mission.count();
            long activeMissions  = Mission.count("status IN ('TRAINING','LAUNCH_READY','IN_FLIGHT')");

            return HealthCheckResponse.named("mission-control-db")
                    .up()
                    .withData("total_missions", missionCount)
                    .withData("active_missions", activeMissions)
                    .withData("status", "All systems nominal 🚀")
                    .build();
        } catch (Exception e) {
            return HealthCheckResponse.named("mission-control-db")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
