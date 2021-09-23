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
@Produces("application/json;charset=utf-8")
@Consumes("application/json;charset=utf-8")
public class SearchAggregateByEngagement {

    private static final Logger log = LoggerFactory.getLogger(SearchAggregateByEngagement.class);

    private final DataSource dataSource;

    @Inject
    public SearchAggregateByEngagement(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Inject
    SearchAggregate searchAggregate;

    private static final Map<Integer, String> PRACTICES;

    static {
        PRACTICES = new HashMap<Integer, String>();
        String[] practices = ApplicationUtils.readFile("/trino-practice-list").split("\n");
        int cnt = 2;
        for (String practice : practices) {
            PRACTICES.put(cnt++, practice);
        }
    }

    @GET
    @Blocking
    @Path("/aggregate-for-engagement")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "aggregate-for-engagement", summary = "trino query", description = "This operation returns aggregate search for practices in a specific engagement using trino", deprecated = false, hidden = false)
    public javax.ws.rs.core.Response allpractice(@QueryParam(value = "uuid") String uuid) throws Exception {
        // for now use old method for query all
        if (uuid.equalsIgnoreCase("999")) {
            return searchAggregate.allpractice();
        }
        StringBuffer completeQuery = new StringBuffer();
        completeQuery.append("select count(*) as cnt_total,");
        for (Integer i : PRACTICES.keySet()) {
            completeQuery.append(generateSubQuery(i, uuid));
            if (i - 1 != PRACTICES.keySet().size()) {
                // last element no comma
                completeQuery.append(",\n");
            }
        }
        completeQuery.append(" from elasticsearch.default.\"engagements-read\" e");
        log.info(">> completeQuery: " + completeQuery);
        QueryAndList<GeneralResponse> response = QueryAndList.from(dataSource, completeQuery.toString(), GeneralResponse::from);
        return javax.ws.rs.core.Response.ok().entity(response.data.get(0)).build();
    }

    private String generateSubQuery(int i, String uuid) {
        String query = ApplicationUtils.readFile("/trino-query-practices-by-name");
        String colName = "cnt_" + PRACTICES.get(i).replace(" ", "_");
        query = query.replace("<PRACTICE>", PRACTICES.get(i));
        query = query.replace("<UUID>", uuid);
        String b32query = BaseEncoding.base32().encode(query.getBytes());
        StringBuffer subQuery = new StringBuffer();
        subQuery.append("(select json_extract(result, '$.hits.total.value') from elasticsearch.default.\"engagements-read$query:");
        subQuery.append(b32query);
        subQuery.append("\") as " + colName);
        return subQuery.toString();
    }

    static record QueryAndList<T>(String query, List<T> data) {
        static <T> QueryAndList<T> from(DataSource dataSource, String query, Function<ResultSet, T> mapper) {
            return new QueryAndList<>(
                    query,
                    getResponseList(dataSource, query, mapper)
            );
        }

        private static <T> List<T> getResponseList(DataSource dataSource, String query, Function<ResultSet, T> mapper) {
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

    static record Count(String key, Integer value) {
    }

    static record GeneralResponse(Count... data) {
        static GeneralResponse from(ResultSet resultSet) {
            try {
                return new GeneralResponse(
                        new Count(resultSet.getMetaData().getColumnName(1), resultSet.getInt("cnt_total")),
                        new Count(resultSet.getMetaData().getColumnName(2), resultSet.getInt("cnt_" + PRACTICES.get(2).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(3), resultSet.getInt("cnt_" + PRACTICES.get(3).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(4), resultSet.getInt("cnt_" + PRACTICES.get(4).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(5), resultSet.getInt("cnt_" + PRACTICES.get(5).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(6), resultSet.getInt("cnt_" + PRACTICES.get(6).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(7), resultSet.getInt("cnt_" + PRACTICES.get(7).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(8), resultSet.getInt("cnt_" + PRACTICES.get(8).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(9), resultSet.getInt("cnt_" + PRACTICES.get(9).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(10), resultSet.getInt("cnt_" + PRACTICES.get(10).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(11), resultSet.getInt("cnt_" + PRACTICES.get(11).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(12), resultSet.getInt("cnt_" + PRACTICES.get(12).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(13), resultSet.getInt("cnt_" + PRACTICES.get(13).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(14), resultSet.getInt("cnt_" + PRACTICES.get(14).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(15), resultSet.getInt("cnt_" + PRACTICES.get(15).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(16), resultSet.getInt("cnt_" + PRACTICES.get(16).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(17), resultSet.getInt("cnt_" + PRACTICES.get(17).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(18), resultSet.getInt("cnt_" + PRACTICES.get(18).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(19), resultSet.getInt("cnt_" + PRACTICES.get(19).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(20), resultSet.getInt("cnt_" + PRACTICES.get(20).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(21), resultSet.getInt("cnt_" + PRACTICES.get(21).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(22), resultSet.getInt("cnt_" + PRACTICES.get(22).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(23), resultSet.getInt("cnt_" + PRACTICES.get(23).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(24), resultSet.getInt("cnt_" + PRACTICES.get(24).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(25), resultSet.getInt("cnt_" + PRACTICES.get(25).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(26), resultSet.getInt("cnt_" + PRACTICES.get(26).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(27), resultSet.getInt("cnt_" + PRACTICES.get(27).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(28), resultSet.getInt("cnt_" + PRACTICES.get(28).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(29), resultSet.getInt("cnt_" + PRACTICES.get(29).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(30), resultSet.getInt("cnt_" + PRACTICES.get(30).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(31), resultSet.getInt("cnt_" + PRACTICES.get(31).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(32), resultSet.getInt("cnt_" + PRACTICES.get(32).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(33), resultSet.getInt("cnt_" + PRACTICES.get(33).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(34), resultSet.getInt("cnt_" + PRACTICES.get(34).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(35), resultSet.getInt("cnt_" + PRACTICES.get(35).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(36), resultSet.getInt("cnt_" + PRACTICES.get(36).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(37), resultSet.getInt("cnt_" + PRACTICES.get(37).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(38), resultSet.getInt("cnt_" + PRACTICES.get(38).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(39), resultSet.getInt("cnt_" + PRACTICES.get(39).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(40), resultSet.getInt("cnt_" + PRACTICES.get(40).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(41), resultSet.getInt("cnt_" + PRACTICES.get(41).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(42), resultSet.getInt("cnt_" + PRACTICES.get(42).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(43), resultSet.getInt("cnt_" + PRACTICES.get(43).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(44), resultSet.getInt("cnt_" + PRACTICES.get(44).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(45), resultSet.getInt("cnt_" + PRACTICES.get(45).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(46), resultSet.getInt("cnt_" + PRACTICES.get(46).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(47), resultSet.getInt("cnt_" + PRACTICES.get(47).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(48), resultSet.getInt("cnt_" + PRACTICES.get(48).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(49), resultSet.getInt("cnt_" + PRACTICES.get(49).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(50), resultSet.getInt("cnt_" + PRACTICES.get(50).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(51), resultSet.getInt("cnt_" + PRACTICES.get(51).replace(" ", "_"))),
                        new Count(resultSet.getMetaData().getColumnName(52), resultSet.getInt("cnt_" + PRACTICES.get(52).replace(" ", "_")))
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
