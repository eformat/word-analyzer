package org.acme.rest.client;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@ApplicationScoped
@RegisterRestClient(configKey = "lodestar.backend.api")
@RegisterClientHeaders
@Produces("application/json")
@Consumes("application/json")
@ClientHeaderParam(name = "version", value = "v2")
@ClientHeaderParam(name = "Origin", value = "https://lodestar.rht-labs.com")
@ClientHeaderParam(name = "accept", value = "*/*")
public interface EngagementClient {

    @GET
    @Path("/engagements?exclude=commits")
    String getAllEngagements();

}
