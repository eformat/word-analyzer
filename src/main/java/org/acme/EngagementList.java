package org.acme;

import io.smallrye.common.annotation.Blocking;
import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("/")
@Produces("application/json;charset=utf-8")
@Consumes("application/json;charset=utf-8")
public class EngagementList {

    static record Engagement(String uuid, String name) {
    }

    private static ArrayList<Engagement> engagements = new ArrayList<>();

    static {
        // configmap
        String[] practices = ApplicationUtils.readFileFS("/var/tmp/engagements").split("\n");
        if (null == practices) {
            // classpath
            practices = ApplicationUtils.readFile("/engagement-list").split("\n");
        }
        for (String practice : practices) {
            if (null == practice || practice.length() == 0) continue;
            String[] pu = practice.split(":");
            engagements.add(new Engagement(pu[0], pu[1]));
        }
    }

    @GET
    @Blocking
    @Path("/engagements")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "engagements", summary = "all engagement list query", description = "This operation returns a list of engagements", deprecated = false, hidden = false)
    public javax.ws.rs.core.Response engagements() throws Exception {
        return Response.ok().entity(engagements).build();
    }

}
