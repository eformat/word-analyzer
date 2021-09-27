package org.acme;

import io.smallrye.common.annotation.Blocking;
import io.trino.jdbc.$internal.guava.io.BaseEncoding;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Path("/")
public class SearchAggregateByEngagement {

    private static final Logger log = LoggerFactory.getLogger(SearchAggregateByEngagement.class);

    private final DataSource dataSource;

    @Inject
    public SearchAggregateByEngagement(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final Map<Integer, String> PRACTICES;

    static {
        PRACTICES = new HashMap<Integer, String>();
        String[] practices = ApplicationUtils.readFile("/trino-practice-list").split("\n");
        int cnt = 2;
        for (String practice : practices) {
            PRACTICES.put(cnt++, practice);
        }
    }

    @POST
    @Path("/aggregate-for-engagement")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(operationId = "aggregate-for-engagement", summary = "trino query", description = "This operation returns aggregate search for practices in a specific engagement using trino", deprecated = false, hidden = false)
    public javax.ws.rs.core.Response allpractices(@QueryParam(value = "uuid") String uuid, String terms) throws Exception {
        log.debug(">> terms: " + terms);
        List<String> argList = new ArrayList();
        String[] tms = terms.split("\n");
        for (String s : tms) {
            if (!s.isBlank())
                argList.add(s.strip());
        }
        return allpractice(uuid, argList);
    }

    @GET
    @Blocking
    @Path("/aggregate-for-engagement")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(operationId = "aggregate-for-engagement", summary = "trino query", description = "This operation returns aggregate search for practices in a specific engagement using trino", deprecated = false, hidden = false)
    public javax.ws.rs.core.Response allpractice(@QueryParam(value = "uuid") String uuid,  @QueryParam(value = "terms") List<String> terms) throws Exception {
        StringBuffer completeQuery = new StringBuffer();
        completeQuery.append("select count(*) as cnt_total,");
        // terms
        if (null != terms && terms.size() > 0) {
            int cnt = 1;
            for (String term : terms) {
                completeQuery.append(generateSubQuery(cnt, uuid, term));
                if (cnt != terms.size()) {
                    // last element no comma
                    completeQuery.append(",\n");
                }
                cnt++;
            }
        } else {
            for (Integer i : PRACTICES.keySet()) {
                completeQuery.append(generateSubQuery(i, uuid, PRACTICES.get(i)));
                if (i - 1 != PRACTICES.keySet().size()) {
                    // last element no comma
                    completeQuery.append(",\n");
                }
            }
        }
        completeQuery.append(" from elasticsearch.default.\"engagements-read\" e");
        log.debug(">> completeQuery: " + completeQuery);
        List<GeneralResponse> result = new ArrayList<>();
        QueryAndList<GeneralResponse> queryAndList = new QueryAndList<>(completeQuery.toString(), result);
        GeneralResponse response = queryAndList.from(dataSource, completeQuery.toString(), GeneralResponse::from).data.get(0);
        return javax.ws.rs.core.Response.ok().entity(response).build();
    }

    private String generateSubQuery(int i, String uuid, String term) {
        String query = ApplicationUtils.readFile("/trino-query-practices-by-name");
        String colName = "cnt_" + term.replace(" ", "_")
                .replace("\\", "").replace("/", "_")
                .replace("&", "_");
        query = query.replace("<PRACTICE>", term);
        // for now use old method for query all
        if (uuid.equalsIgnoreCase("999")) {
            query = query.replace("<UUID>", "*");
        } else {
            query = query.replace("<UUID>", uuid);
        }
        String b32query = BaseEncoding.base32().encode(query.getBytes());
        StringBuffer subQuery = new StringBuffer();
        subQuery.append("(select json_extract(result, '$.hits.total.value') from elasticsearch.default.\"engagements-read$query:");
        subQuery.append(b32query);
        subQuery.append("\") as " + colName);
        return subQuery.toString();
    }

    record QueryAndList<T>(String query, List<T> data) {
        <T> QueryAndList<T> from(DataSource dataSource, String query, Function<ResultSet, T> mapper) {
            return new QueryAndList<>(
                    query,
                    getResponseList(dataSource, query, mapper)
            );
        }

        private <T> List<T> getResponseList(DataSource dataSource, String query, Function<ResultSet, T> mapper) {
            List<T> result = new ArrayList<>();
            try (var connection = dataSource.getConnection();
                 var statement = connection.prepareStatement(query)) {
                try (var resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(mapper.apply(resultSet));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return result;
        }
    }

    record Count(String key, Integer value) {
    }

    record GeneralResponse(ArrayList<Count> data) {
        static GeneralResponse from(ResultSet resultSet) {
            try {
                ArrayList<Count> counts = new ArrayList<>();
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    counts.add(new Count(resultSet.getMetaData().getColumnName(i), resultSet.getInt(resultSet.getMetaData().getColumnName(i))));
                }
                return new GeneralResponse(counts);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
