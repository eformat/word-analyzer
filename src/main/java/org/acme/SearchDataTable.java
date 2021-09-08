package org.acme;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import j2html.tags.ContainerTag;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static j2html.TagCreator.*;

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
        DataTable result = new DataTable();
        _datatable(draw, start, length, searchVal, result);
        return result;
    }


    @GET
    @Blocking
    @Path("/datatablehtml")
    @Produces(MediaType.TEXT_HTML)
    @Operation(operationId = "datatable", summary = "datatable search query", description = "This operation returns a datatable search data - https://www.datatables.net/", deprecated = false, hidden = false)
    public String datatablehtml(
            @QueryParam(value = "draw") @DefaultValue("1") int draw,
            @QueryParam(value = "start") @DefaultValue("0") int start,
            @QueryParam(value = "length") @DefaultValue("10") int length,
            @QueryParam(value = "search[value]") String searchVal) throws Exception {
        DataTable result = new DataTable();
        try {
            _datatable(draw, start, length, searchVal, result);
        } catch (Exception ex) {
            log.warn("caught exception calling ES " + ex);
            return h1("No Data Found").render();
        }
        List<SearchData> subList = result.getData();
        AtomicInteger cnt = new AtomicInteger(1);
        ContainerTag html = head().with(
                link().withRel("stylesheet").withHref("https://cdn.datatables.net/1.11.0/css/jquery.dataTables.min.css"),
                link().withRel("stylesheet").withHref("report-styles.css"),
                body(
                        p("Selected Practice - Matching Weekly Reports"),
                        div().withClasses("container").with(
                                div().withClasses("table dataTables_wrapper").with(
                                        table(thead(tr(
                                                th("filename"),
                                                th("score"),
                                                th("highlight")
                                                )).withClass("thead-dark"),
                                                tbody(
                                                        each(subList, row -> tr().with(
                                                                td(a(row.getFilename()).withTarget("_blank").withHref(row.getUrl())),
                                                                td(String.format("%.3f", row.getScore())),
                                                                td(row.getHighlight())
                                                                ).withClass((cnt.getAndIncrement() % 2 == 0) ? "odd" : "even")
                                                        )
                                                )
                                        ).withClasses("display compact dataTable no-footer")
                                )
                        )
                )
        );
        String render = html.render();
        return StringEscapeUtils.unescapeHtml4(render);
    }

    private void _datatable(int draw, int start, int length, String searchVal, DataTable result) throws Exception {
        // Begin result
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
                                field("highlight"),
                                field("fileId")
                        )
                ));
        Response response = searchClient.executeSync(query);
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
    }

}
