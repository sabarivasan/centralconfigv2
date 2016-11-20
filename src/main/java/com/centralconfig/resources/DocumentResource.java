package com.centralconfig.resources;

import com.centralconfig.dw.CentralConfigConfiguration;
import com.centralconfig.model.ConfigChange;
import com.centralconfig.model.Constants;
import com.centralconfig.model.Delta;
import com.centralconfig.model.DocType;
import com.centralconfig.model.Document;
import com.centralconfig.model.YPath;
import com.centralconfig.parse.YamlDiffer;
import com.centralconfig.parse.YamlSerDeser;
import com.centralconfig.persist.DbSerializable;
import com.centralconfig.persist.KVStore;
import com.centralconfig.publish.ConfigChangePublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.StringUtils;
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
import java.util.SortedSet;

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
    private final ConfigChangePublisher configChangePublisher;

    public DocumentResource(CentralConfigConfiguration config, KVStore kvStore, ConfigChangePublisher
            configChangePublisher) {
        this.config = config;
        this.kvStore = kvStore;
        this.configChangePublisher = configChangePublisher;
    }

    /**
     * Create a document by posting a yaml/json document
     */
    @POST
//    @Path("/{namespacePath}")
    @Path("{namespacePath: .+}")
    public Response upsertDoc(@NotNull @PathParam("namespacePath") String nsPath,
                              @NotNull @Body InputStream body,
                              @NotNull @NotEmpty @QueryParam("author") String author,
                              @QueryParam("ensureAbsent") @DefaultValue("false") boolean ensureAbsent,
                              @NotNull @HeaderParam("Content-Type") String contentType) {

        try {
            validateNsPath(nsPath);

            // 1 key-value per document
            // TODO: get sub-document at yPath
            Optional<String> existing = kvStore.getValueAt(nsPath);
            if (ensureAbsent && existing.isPresent()) {
                return Response.status(Response.Status.PRECONDITION_FAILED)
                        .entity(String.format("Document at namespace path '%s' already exists", nsPath)).build();
            }

            String diff;

            Document newDoc = YamlSerDeser.parse(nsPath, body);
            String serNewDoc = newDoc.ser();
            Response.Status status;
            boolean docChanged = true;
            SortedSet<Delta> deltas = null;
            if (existing.isPresent()) {
                Document oldDoc = new Document(existing.get());
                deltas = YamlDiffer.compare(oldDoc, newDoc);
                docChanged = !deltas.isEmpty();
                diff = StringUtils.join(deltas, '\n');
                status = Response.Status.OK;
            } else {
                diff = serNewDoc;
                status = Response.Status.CREATED;
            }

            // 1 key-value per document
            if (docChanged) {
                kvStore.put(nsPath, serNewDoc);

                configChangePublisher.configChanged(nsPath, new ConfigChange(author, System.currentTimeMillis(),
                                                                             deltas));
            }

            return Response.status(status).entity(diff).build();
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
        if (parts.length != config.getNumNamespaceLevels()) {
            throw new IllegalArgumentException(
                    String.format("namespace path '%s' doesn't agree with number of namespace levels expected: %d",
                            nsPath, config.getNumNamespaceLevels()));
        }
    }

}
