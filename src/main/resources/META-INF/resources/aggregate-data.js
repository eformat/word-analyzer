document.getElementById("getData").onclick = function() {
    $('#chart').empty()
    getData()
};
function getData() {
    console.log("calling...")
    $('#spinner').show()
    $.ajax({
        url: '/aggregate',
        success: function(d) {
            $('#spinner').hide()
            var data = d.data;

            const index = data.findIndex(x => x.key === "cnt_total");
            var total = data.slice(index, 1)[0];
            if (index > -1) {
              data.splice(index, 1);
            }

            var w = 1400;
            var h = 600;
            var margin = {
                top: 50,
                bottom: 100,
                left: 60,
                right: 20
            };
            var width = w - margin.left - margin.right;
            var height = h - margin.top - margin.bottom;

            var x = d3.scaleBand()
                .domain(data.map(function(d) {
                    return d.key;
                }))
                .range([margin.left, width])
                .padding(0.1);

            var y = d3.scaleLinear()
                .domain([0, d3.max(data, function(d) {
                    return d.value;
                })])
                .range([height, margin.top])

            var yAxis = d3.axisLeft().scale(y)

            var svg = d3.select("#chart").append("svg")
                .attr("id", "chart")
                .attr("width", w)
                .attr("height", h);

            svg.append("text")
                .attr("class", "y label")
                .attr("transform", "rotate(-90)")
                .attr("y", 0)
                .attr("x", 0 - (height / 2))
                .attr("dy", "1em")
                .style("text-anchor", "middle")
                .text("[count] if term exists in a weekly report");

            svg.append("text")
                    .attr("x", (width / 2))
                    .attr("y", 20)
                    .attr("text-anchor", "middle")
                    .text("Total Weekly Reports - " + total.value);

            svg.append("g")
                .attr("class", "axis")
                .attr("transform", "translate(" + margin.left + ",0)")
                .call(yAxis);

            svg.selectAll("rect")
                .data(data)
                .enter()
                .append("rect")
                .attr("class", "bar")
                .on("mouseover", function() {
                    d3.select(this)
                        .attr("fill", "red")
                })
                .on("mouseout", function() {
                    d3.select(this)
                        .transition("colorfade")
                        .duration(250)
                        .attr("fill", function(d) {
                            return "rgb(" + Math.round(d.value * 2) + "," +
                                Math.round(d.value * 2) + "," + Math.round(d.value * 2) + ")";
                        })
                })

                .attr("fill", function(d) {
                    return "rgb(" + Math.round(d.value * 2) + "," +
                        Math.round(d.value * 2) + "," + Math.round(d.value * 2) + ")";
                })

                .attr("x", function(d, i) {
                    return x(d.key);
                })
                .attr("width", x.bandwidth())
                .attr("y", height)

                .transition("bars")
                .delay(function(d, i) {
                    return i * 50;
                })
                .duration(1000)

                .attr("y", function(d, i) {
                    return y(d.value);
                })
                .attr("height", function(d, i) {
                    return height - y(d.value);
                })

            svg.selectAll("rect")
                .append("title")
                .text(function(d) {
                    return d.key + ": " + d.value;
                })

            svg.selectAll(".val-label")
                .data(data)
                .enter()
                .append("text")
                .classed("val-label", true)

                .attr("x", function(d, i) {
                    return x(d.key) + x.bandwidth() / 2;
                })
                .attr("y", height)

                .transition("label")
                .delay(function(d, i) {
                    return i * 50; // gives it a smoother effect
                })
                .duration(1000)

                .attr("y", function(d, i) {
                    return y(d.value) - 4;
                })
                .attr("text-anchor", "middle")
                .text(function(d) {
                    return d.value
                });

            svg.selectAll(".bar-label")
                .data(data)
                .enter()
                .append("text")
                .classed("bar-label", true)

                .attr("transform", function(d, i) {
                    return "translate(" + (x(d.key) + x.bandwidth() / 2 - 8) + "," + (height + 15) + ")" +
                        " rotate(45)"
                })

                .attr("text-anchor", "left")
                .text(function(d) {
                    return d.key
                })

            d3.select("#byKey").on("click", function() {
                data.sort(function(a, b) {
                    return d3.ascending(a.key, b.key)
                })
                x.domain(data.map(function(d) {
                    return d.key;
                }));
                svg.selectAll(".bar")
                    .transition()
                    .duration(500)
                    .attr("x", function(d, i) {
                        return x(d.key);
                    })

                svg.selectAll(".val-label")
                    .transition()
                    .duration(500)
                    .attr("x", function(d, i) {
                        return x(d.key) + x.bandwidth() / 2;
                    })

                svg.selectAll(".bar-label")
                    .transition()
                    .duration(500)
                    .attr("transform", function(d, i) {
                        return "translate(" + (x(d.key) + x.bandwidth() / 2 - 8) + "," + (height + 15) + ")" + " rotate(45)"
                    })

            })

            d3.select("#byValue").on("click", function() {
                data.sort(function(a, b) {
                    return d3.descending(a.value, b.value)
                })
                x.domain(data.map(function(d) {
                    return d.key;
                }));
                svg.selectAll(".bar")
                    .transition()
                    .duration(500)
                    .attr("x", function(d, i) {
                        return x(d.key);
                    })

                svg.selectAll(".val-label")
                    .transition()
                    .duration(500)
                    .attr("x", function(d, i) {
                        return x(d.key) + x.bandwidth() / 2;
                    })

                svg.selectAll(".bar-label")
                    .transition()
                    .duration(500)
                    .attr("transform", function(d, i) {
                        return "translate(" + (x(d.key) + x.bandwidth() / 2 - 8) + "," + (height + 15) + ")" + " rotate(45)"
                    })
            });
        }
    });
}

