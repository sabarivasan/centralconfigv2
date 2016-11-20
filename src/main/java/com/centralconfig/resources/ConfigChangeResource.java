package com.centralconfig.resources;

import com.centralconfig.model.ConfigChangeSubscription;
import com.centralconfig.publish.ConfigChangePublisher;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by sabarivasan on 11/20/16.
 */
@Path("/subscribe")
@Consumes({MediaType.APPLICATION_JSON, "text/yaml"})
public class ConfigChangeResource {
    private final ConfigChangePublisher publisher;

    public ConfigChangeResource(ConfigChangePublisher publisher) {
        this.publisher = publisher;
    }

    @POST
    public Response subscribe(ConfigChangeSubscription subscription) {
        publisher.subscribe(subscription);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{subscriberName}")
    public Response subscribe(@PathParam("subscriberName") String subscriberName) {
        ConfigChangeSubscription subscription = publisher.deleteSubscription(subscriberName);

        if (subscription != null) {
            return Response.ok().entity(subscription).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
