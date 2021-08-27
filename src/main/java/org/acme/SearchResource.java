package org.acme;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@ApplicationScoped
@GraphQLApi
public class SearchResource {

    private static final Logger log = LoggerFactory.getLogger(SearchResource.class);

    @Inject
    RestClient restClient;

    @ConfigProperty(name = "search.max.results", defaultValue = "100")
    int searchMaxResults;

    @Query(value = "query")
    @Description("Search address by search term")
    public Uni<List<SearchData>> query(@Name("search") String search, @Name("size") Optional<Integer> size) {
        String finalSearch = (search == null) ? "" : search.trim().toLowerCase();
        log.debug(">>> Final Search Words: finalSearch(" + finalSearch + ")");
        if (size.isPresent() && size.get() > searchMaxResults) { // set max number of returns
            size = Optional.of(searchMaxResults);
        }
        String eQuery = _fetchQueryString(search, size.orElse(15));
        return _fetchLowLevelClient(eQuery, size.orElse(15));
    }

    protected String _fetchQueryString(String search, Integer size) {
        String queryJson;
        if (search == null || search.isEmpty()) {
            JsonObject matchAll = new JsonObject().put("match_none", new JsonObject()); // match_all the other option
            JsonObject query = new JsonObject().put("query", matchAll);
            queryJson = query.encode();
        } else {
            queryJson = ApplicationUtils.readFile("/query-suggest-match.json");
            JsonObject qJson = new JsonObject(queryJson);
            ApplicationUtils.addJson(qJson, "size", size.toString());
            JsonObject searchContent = qJson
                    .getJsonObject("query")
                    .getJsonObject("match_phrase");
            searchContent.put("attachment.content", search.toLowerCase());
            queryJson = qJson.encode();
        }
        log.info(">>> search request: " + queryJson);
        return queryJson;
    }

    /*
      Optimized approach, uses elastic low level client
      Load query from json file, the same query we can use
      to the elastic rest end point
     */
    protected Uni<List<SearchData>> _fetchLowLevelClient(String queryJson, Integer size) {
        // make low level query request
        Request request = new Request(
                "POST",
                "/engagements/_search");
        request.setJsonEntity(queryJson);
        final String jquery = queryJson.toString();

        Uni<List<SearchData>> result = Uni.createFrom().emitter(emitter -> {
            restClient.performRequestAsync(request, new ResponseListener() {
                @Override
                public void onSuccess(Response response) {
                    String responseBody = null;
                    try {
                        responseBody = EntityUtils.toString(response.getEntity());
                        JsonObject json = new JsonObject(responseBody);
                        emitter.complete(_processSearch(jquery, size, json).await().indefinitely());
                    } catch (IOException e) {
                        log.warn(">>> Caught exception whilst calling ES: " + e);
                        emitter.fail(e);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    log.warn(">>> Caught exception whilst calling ES: " + e);
                    emitter.fail(e);
                }
            });
        });
        return result;
    }

    protected Uni<List<SearchData>> _processSearch(String search, Integer size, JsonObject result) {
        log.info(">>> search returned, elastic took: " + result.getInteger("took") + " [ms]");
        Instant start = Instant.now();
        JsonArray matches = result.getJsonObject("hits").getJsonArray("hits");

        HashSet<SearchData> uniqueList = new HashSet<SearchData>();
        for (int i = 0; i < matches.size(); i++) {
            JsonObject hit = matches.getJsonObject(i);
            SearchData searchData = hit.getJsonObject("_source").mapTo(SearchData.class);
            float score = hit.getFloat("_score");
            searchData.setScore(BigDecimal.valueOf(score));
            JsonObject highlight = hit.getJsonObject("highlight");
            try {
                String a = highlight.getJsonArray("attachment.content").getString(0);
                searchData.setHighlight(a);
            } catch (Exception ex) {
                // ignore
            }
            uniqueList.add(searchData);
        }
        List<SearchData> list = new ArrayList<SearchData>(uniqueList);
        list.sort(Collections.reverseOrder());
        if (list.size() > size)
            list = list.subList(0, size);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        log.debug(">>> processing time: " + timeElapsed + " [ms]");
        log.info(">>> found: " + list.size() + " items");
        return Uni.createFrom().item(list);
    }
}
