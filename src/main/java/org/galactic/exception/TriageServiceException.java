package org.galactic.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TriageServiceException extends WebApplicationException {

    private final Status status;

    public TriageServiceException(Status status, String message) {
        super(message);
        this.status = status;
    }
}
