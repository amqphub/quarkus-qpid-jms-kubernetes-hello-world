package net.example;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api")
public class ReceiverResource {

    private static Logger LOG = LoggerFactory.getLogger(ReceiverResource.class);

    @Inject
    ReceiverService service;

    @GET
    @Path("/receive")
    @Produces("text/plain")
    public String receive() {
        LOG.info("RECEIVER: Receive endpoint invoked");
        return Optional.ofNullable(service.poll()).orElse("");
    }

    @GET
    @Path("/ready")
    @Produces("text/plain")
    public String ready() {
        LOG.info("RECEIVER: I am ready!");
        return "OK\n";
    }
}
