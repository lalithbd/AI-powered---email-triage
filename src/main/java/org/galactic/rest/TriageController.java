package org.galactic.rest;

import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.galactic.dto.view.*;
import org.galactic.service.ClassificationService;
import org.galactic.service.EmailService;
import org.galactic.service.FeedbackService;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

@Path("/triage")
@RequiredArgsConstructor
public class TriageController {

    private final ClassificationService classificationService;

    @Path("/today")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTodayUpdate(){
        Summary dailySummary = classificationService.getDailySummary();
        return Response.ok(dailySummary).build();
    }

    @Path("/today-stats")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTodayUpdateStats(){
        List<StatChip> dailyStats = classificationService.getDailyStats();
        return Response.ok(dailyStats).build();
    }

    @Path("/weekly")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWeeklyUpdate(@RestQuery(value = "pageNo") @DefaultValue("0") @Min(0) int pageNo,
                                    @RestQuery(value = "pageSize") @DefaultValue("20") @Min(1) int pageSize,
                                    @RestQuery(value = "type") String type){
        List<CategoryGroup> categoryGroups = classificationService.getWeeklyMails(pageNo, pageSize, type);
        return Response.ok(categoryGroups).build();
    }

    @Path("/weekly-stats")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWeeklyUpdateStats(){
        List<StatChip> dailyStats = classificationService.getWeeklyStats();
        return Response.ok(dailyStats).build();
    }
}
