package com.centralconfig.resources;

import com.centralconfig.dw.CentralConfigConfiguration;
import com.centralconfig.model.Constants;
import com.centralconfig.model.DocType;
import com.centralconfig.model.Document;
import com.centralconfig.model.YPath;
import com.centralconfig.parse.YamlSerDeser;
import com.centralconfig.persist.DbSerializable;
import com.centralconfig.persist.KVStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.http.Body;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Resource to do CRUD operations on documents
 */
@Path("/doc")
@Consumes({MediaType.APPLICATION_JSON, "text/yaml"})
@Produces({MediaType.APPLICATION_JSON, "text/yaml"})
public class DocumentResource {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentResource.class);

    private final CentralConfigConfiguration config;
    private final KVStore kvStore;

    public DocumentResource(CentralConfigConfiguration config, KVStore kvStore) {
        this.config = config;
        this.kvStore = kvStore;
    }

    /**
     * Create a document by posting a yaml/json document
     */
    @POST
//    @Path("/{namespacePath}")
    @Path("{namespacePath: .+}")
    public Response createDoc(@NotNull @PathParam("namespacePath") String nsPath,
                              @NotNull @Body InputStream body,
                              @NotNull @NotEmpty @QueryParam("author") String author,
                              @NotNull @HeaderParam("Content-Type") String contentType) {

        try {
            validateNsPath(nsPath);
            Document doc = YamlSerDeser.parse(nsPath, body);

            // 1 key-value per document
            String ser = doc.ser();
            kvStore.put(nsPath, ser);

            return Response.status(Response.Status.CREATED).entity(ser).build();
        } catch (IllegalArgumentException iae) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(iae.getMessage()).build();
//                    .entity("format invalid. Accepted Values (case-insensitive): yaml, json, properties").build();
        } catch (JsonProcessingException jpe) {
            LOG.error("Invalid yaml/json", jpe);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid yaml/json in request" + jpe.getMessage()).build();
        } catch (IOException ioe) {
            LOG.error("Exception when processing request", ioe);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Exception occurred while processing request: " + ioe.getMessage()).build();
        }
    }


    /**
     * Get a document or part of a document at yPath
     */
    @GET
//    @Path("/{yPath}")
    @Path("{yPath: .+}")
    public Response getDoc(@NotNull @PathParam("yPath") String yPath,
                           @QueryParam("expandAliases") @DefaultValue("false") boolean expandAliases,
                           @QueryParam("format") @DefaultValue("yaml") String docType) {

        try {
            YPath fqPath = new YPath(yPath, config.getNumNamespaceLevels());

            // 1 key-value per document
            // TODO: get sub-document at yPath
            Optional<String> doc = kvStore.getValueAt(fqPath.getNamespacePath());
            Object entity;
            if (doc.isPresent()) {
                DocType format = parseDocType(docType);
                if (DocType.PROPERTIES == format) {
                    entity = doc.get();
                } else {
                    Document d = new Document(doc.get());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(Constants.LENGTH_1024);
                    YamlSerDeser.write(d, format, baos, expandAliases);
                    entity = baos.toByteArray();
                }
                return Response.ok().entity(entity).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (IllegalArgumentException iae) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(iae.getMessage()).build();
        } catch (IOException ioe) {
            LOG.error("Exception when processing request", ioe);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Exception occurred while processing request: " + ioe.getMessage()).build();
        }
    }

    private DocType parseDocType(String docType) {
        try {
            return DocType.valueOf(docType.toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException(
                    "format invalid. Accepted Values (case-insensitive): yaml, json, properties");
        }
    }

    private void validateNsPath(String nsPath) {
        String[] parts = nsPath.split(DbSerializable.HIER_SEPARATOR);
        if (parts.length < config.getNumNamespaceLevels()) {
            throw new IllegalArgumentException(
                    String.format("namespace path '%s' doesn't agree with number of namespace levels expected: %d",
                            nsPath, config.getNumNamespaceLevels()));
        }
    }

}