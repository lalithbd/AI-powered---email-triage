package org.galactic.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.galactic.dto.view.FeedbackCategory;
import org.galactic.service.FeedbackService;
import org.jboss.resteasy.reactive.RestPath;

@Path("/feed-back")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Path("/{email-id}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addFeedback(@RestPath(value = "email-id") Long id, FeedbackCategory feedbackCategory){
        feedbackService.addFeedbackByEmailId(id, feedbackCategory);
        return Response.ok().build();
    }
}
