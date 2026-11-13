package org.galactic.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.galactic.dto.view.TriageEmail;
import org.galactic.service.EmailService;
import org.jboss.resteasy.reactive.RestPath;

@Path("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getEmailData(@RestPath(value = "id") Long id){
        TriageEmail triageEmail = emailService.getEmailDataById(id);
        return Response.ok(triageEmail).build();
    }
}
