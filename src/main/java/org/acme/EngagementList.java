package org.acme;

import io.smallrye.common.annotation.Blocking;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import org.acme.rest.client.EngagementClient;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EngagementList {

    @Inject
    @RestClient
    EngagementClient engagementClient;

    static record Engagement(String uuid, String name) {
    }

    @GET
    @Blocking
    @Path("/engagements")
    @Operation(operationId = "engagements", summary = "all engagement list query", description = "This operation returns a list of engagements", deprecated = false, hidden = false)
    public javax.ws.rs.core.Response engagements() throws Exception {
        ArrayList<Engagement> engagements = mapResponse(engagementClient.getAllEngagements());
        return Response.ok().entity(engagements).build();
    }

    private ArrayList<Engagement> mapResponse(String engagements) {
        Object e = parse(Buffer.buffer(engagements));
        final ArrayList<Engagement> eg = new ArrayList<>();
        final Iterator<Object> i = ((JsonArray)e).iterator();
        while (i.hasNext()) {
            final JsonObject element = (JsonObject) i.next();
            eg.add(new Engagement(element.getString("uuid"), element.getString("customer_name").concat(" - ").concat(element.getString("project_name"))));
        }
        return eg;
    }

    private Object parse(Buffer buffer) {
        JsonParser parser = JsonParser.newParser();
        AtomicReference<Object> result = new AtomicReference<>();
        parser.handler(event -> {
            switch (event.type()) {
                case VALUE:
                    Object res = result.get();
                    if (res == null) {
                        result.set(event.value());
                    } else if (res instanceof List) {
                        List list = (List) res;
                        list.add(event.value());
                    } else if (res instanceof Map) {
                        Map map = (Map) res;
                        map.put(event.fieldName(), event.value());
                    }
                    break;
                case START_ARRAY:
                    result.set(new ArrayList());
                    parser.objectValueMode();
                    break;
                case START_OBJECT:
                    result.set(new HashMap());
                    parser.objectValueMode();
                    break;
            }
        });
        parser.handle(buffer);
        parser.end();
        Object res = result.get();
        if (res instanceof List) {
            return new JsonArray((List) res);
        }
        if (res instanceof Map) {
            return new JsonObject((Map<String, Object>) res);
        }
        return res;
    }
}
