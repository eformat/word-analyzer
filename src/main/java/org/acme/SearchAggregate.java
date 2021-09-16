package org.acme;

import io.smallrye.common.annotation.Blocking;
import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

@Path("/")
@Produces("application/json;charset=utf-8")
@Consumes("application/json;charset=utf-8")
public class SearchAggregate {

    private final DataSource dataSource;

    @Inject
    public SearchAggregate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GET
    @Blocking
    @Path("/aggregate")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "aggregate", summary = "trino query", description = "This operation returns aggregate search for all practices using trino", deprecated = false, hidden = false)
    public javax.ws.rs.core.Response allpractice() throws Exception {
        String query = ApplicationUtils.readFile("/trino-query.sql");
        QueryAndList<GeneralResponse> response = QueryAndList.from(dataSource, query, GeneralResponse::from);
        return javax.ws.rs.core.Response.ok().entity(response.data.get(0)).build();
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
                        //new Count(resultSet.getMetaData().getColumnName(1), resultSet.getInt("cnt_total")),
                        new Count(resultSet.getMetaData().getColumnName(2), resultSet.getInt("cnt_event_storming")),
                        new Count(resultSet.getMetaData().getColumnName(3), resultSet.getInt("cnt_impact_mapping")),
                        new Count(resultSet.getMetaData().getColumnName(4), resultSet.getInt("cnt_empathy_mapping")),
                        new Count(resultSet.getMetaData().getColumnName(5), resultSet.getInt("cnt_north_star")),
                        new Count(resultSet.getMetaData().getColumnName(6), resultSet.getInt("cnt_metrics")),
                        new Count(resultSet.getMetaData().getColumnName(7), resultSet.getInt("cnt_target_outcomes")),
                        new Count(resultSet.getMetaData().getColumnName(8), resultSet.getInt("cnt_non_functional")),
                        new Count(resultSet.getMetaData().getColumnName(9), resultSet.getInt("cnt_design_sprint")),
                        new Count(resultSet.getMetaData().getColumnName(10), resultSet.getInt("cnt_user_story_mapping")),
                        new Count(resultSet.getMetaData().getColumnName(11), resultSet.getInt("cnt_value_slicing")),
                        new Count(resultSet.getMetaData().getColumnName(12), resultSet.getInt("cnt_weighted_shortest_job_first")),
                        new Count(resultSet.getMetaData().getColumnName(13), resultSet.getInt("cnt_canary")),
                        new Count(resultSet.getMetaData().getColumnName(14), resultSet.getInt("cnt_example_mapping")),
                        new Count(resultSet.getMetaData().getColumnName(15), resultSet.getInt("cnt_dark_launches")),
                        new Count(resultSet.getMetaData().getColumnName(16), resultSet.getInt("cnt_design_experiments")),
                        new Count(resultSet.getMetaData().getColumnName(17), resultSet.getInt("cnt_backlog_refinement")),
                        new Count(resultSet.getMetaData().getColumnName(18), resultSet.getInt("cnt_prioritzation")),
                        new Count(resultSet.getMetaData().getColumnName(19), resultSet.getInt("cnt_AB_testing")),
                        new Count(resultSet.getMetaData().getColumnName(20), resultSet.getInt("cnt_feature_flag")),
                        new Count(resultSet.getMetaData().getColumnName(21), resultSet.getInt("cnt_how_wow_now")),
                        new Count(resultSet.getMetaData().getColumnName(22), resultSet.getInt("cnt_blue_green")),
                        new Count(resultSet.getMetaData().getColumnName(23), resultSet.getInt("cnt_sprint_planning")),
                        new Count(resultSet.getMetaData().getColumnName(24), resultSet.getInt("cnt_kanban")),
                        new Count(resultSet.getMetaData().getColumnName(25), resultSet.getInt("cnt_stand_up")),
                        new Count(resultSet.getMetaData().getColumnName(26), resultSet.getInt("cnt_usability_testing")),
                        new Count(resultSet.getMetaData().getColumnName(27), resultSet.getInt("cnt_guerrila_testing")),
                        new Count(resultSet.getMetaData().getColumnName(28), resultSet.getInt("cnt_showcase")),
                        new Count(resultSet.getMetaData().getColumnName(29), resultSet.getInt("cnt_retro")),
                        new Count(resultSet.getMetaData().getColumnName(30), resultSet.getInt("cnt_pair_programming")),
                        new Count(resultSet.getMetaData().getColumnName(31), resultSet.getInt("cnt_containers")),
                        new Count(resultSet.getMetaData().getColumnName(32), resultSet.getInt("cnt_everything_as_code")),
                        new Count(resultSet.getMetaData().getColumnName(33), resultSet.getInt("cnt_mobbing_as_cnt_mobbing")),
                        new Count(resultSet.getMetaData().getColumnName(34), resultSet.getInt("cnt_CI")),
                        new Count(resultSet.getMetaData().getColumnName(35), resultSet.getInt("cnt_CD")),
                        new Count(resultSet.getMetaData().getColumnName(36), resultSet.getInt("cnt_big_picture")),
                        new Count(resultSet.getMetaData().getColumnName(37), resultSet.getInt("cnt_testing")),
                        new Count(resultSet.getMetaData().getColumnName(38), resultSet.getInt("cnt_emerging_architecture")),
                        new Count(resultSet.getMetaData().getColumnName(39), resultSet.getInt("cnt_visualisation_of_work")),
                        new Count(resultSet.getMetaData().getColumnName(40), resultSet.getInt("cnt_social_contract")),
                        new Count(resultSet.getMetaData().getColumnName(41), resultSet.getInt("cnt_stop_the_world")),
                        new Count(resultSet.getMetaData().getColumnName(42), resultSet.getInt("cnt_realtime_retro")),
                        new Count(resultSet.getMetaData().getColumnName(43), resultSet.getInt("cnt_priority_sliders")),
                        new Count(resultSet.getMetaData().getColumnName(44), resultSet.getInt("cnt_team_workspaces")),
                        new Count(resultSet.getMetaData().getColumnName(45), resultSet.getInt("cnt_team_sentiment")),
                        new Count(resultSet.getMetaData().getColumnName(46), resultSet.getInt("cnt_network_mapping")),
                        new Count(resultSet.getMetaData().getColumnName(47), resultSet.getInt("cnt_team_identity")),
                        new Count(resultSet.getMetaData().getColumnName(48), resultSet.getInt("cnt_definition_of_ready")),
                        new Count(resultSet.getMetaData().getColumnName(49), resultSet.getInt("cnt_definition_of_done")),
                        new Count(resultSet.getMetaData().getColumnName(50), resultSet.getInt("cnt_accceptance_criteria")),
                        new Count(resultSet.getMetaData().getColumnName(51), resultSet.getInt("cnt_foundation")),
                        new Count(resultSet.getMetaData().getColumnName(52), resultSet.getInt("cnt_options"))
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
