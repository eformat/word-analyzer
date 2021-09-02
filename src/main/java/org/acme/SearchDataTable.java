package org.acme;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;

@Path("/")
@Produces("application/json;charset=utf-8")
@Consumes("application/json;charset=utf-8")
public class SearchDataTable {

    private static final Logger log = LoggerFactory.getLogger(SearchDataTable.class);

    @Inject
    @GraphQLClient("search-data")
    DynamicGraphQLClient searchClient;

    @GET
    @Blocking
    @Path("/datatable")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "datatable", summary = "datatable search query", description = "This operation returns a datatable search data - https://www.datatables.net/", deprecated = false, hidden = false)
    public DataTable datatable(
            @QueryParam(value = "draw") @DefaultValue("1") int draw,
            @QueryParam(value = "start") @DefaultValue("0") int start,
            @QueryParam(value = "length") @DefaultValue("10") int length,
            @QueryParam(value = "search[value]") String searchVal) throws Exception {
        // Begin result
        DataTable result = new DataTable();
        result.setDraw(draw);
        log.info(">> datatable {draw:" + draw + ", start:" + start + ", length: " + length + ", search: " + searchVal + "}");

        // Filter based on search
        Document query = document(
                operation(
                        field("query",
                                args(arg("search", searchVal), arg("size", 100)),
                                field("filename"),
                                field("score"),
                                field("url"),
                                field("highlight")
                        )
                ));
        Response response;
        try {
            response = searchClient.executeSync(query);
        } catch (Exception ex) {
            log.warn("caught exception calling ES " + ex);
            return result;
        }
        List<SearchData> searchData = response.getList(SearchData.class, "query");
        int end = (length <= searchData.size() ? length : searchData.size());
        if (start > 0) {
            end = start + (length <= searchData.size() ? length : searchData.size());
            if (end > searchData.size())
                end = searchData.size();
        }
        log.info(">> start: " + start + " end: " + end + " searchData.zize(): " + searchData.size());
        List<SearchData> subList = searchData.subList(start, end);
        result.setData(subList);
        result.setRecordsFiltered(searchData.size());
        result.setRecordsTotal(searchData.size());
        return result;
    }


}
