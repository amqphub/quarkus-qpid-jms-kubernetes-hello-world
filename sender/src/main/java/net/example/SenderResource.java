package net.example;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api")
public class SenderResource {

    private static Logger LOG = LoggerFactory.getLogger(SenderResource.class);

    @Inject
    SenderService service;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/greeting/{name}")
    public String greeting(@PathParam("name") String name) {
        return service.send(name);
    }

    @POST
    @Path("/send")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String send(String string) {
        return service.send(string);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Sender Serivce, hit /send with something to send to the receiver";
    }

    @GET
    @Path("/ready")
    @Produces("text/plain")
    public String ready() {
        LOG.info("SENDER: I am ready!");

        return "OK\n";
    }
}
