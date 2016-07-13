$(function() {

    $.get("/senercon/error_statistics", function(data) {
        $.each(JSON.parse(data).error_statistics, function(key, value) {
            $('#tableErrors')
                .append($('<tr>')
                    .append($('<td>')
                        .text(value.date)
                    ).append($('<td>')
                        .text(value.path)
                    ).append($('<td>')
                        .text(value.user_id)
                    ).append($('<td>')
                        .text(value.portal_id)
                    ).append($('<td>')
                        .text(value.error_code)
                    ).append($('<td>')
                        .text(value.position)
                    ).append($('<td>')
                        .text(value.error_type)
                    )
                )
        });
        tf.init();
        $(".helpFooter").remove();
        $("#helpSpan_tableErrors").remove();

        $.each(JSON.parse(data).absolut_sum, function(key, value) {
            $('#tableAbsolutSum')
                .append($('<tr>')
                    .append($('<td>')
                        .text(value.error_type)
                    ).append($('<td>')
                        .text(value.count)
                    )
                )
        });
        tf2.init();
        $(".helpFooter").remove();
        $("#helpSpan_tableErrors").remove();

        barChart();

        $.each(JSON.parse(data).errors_and_users, function(key, value) {
            $('#tableUsersAndErrors')
                .append($('<tr>')
                    .append($('<td>')
                        .text(value.user_id)
                    ).append($('<td>')
                        .text(value.count)
                    )
                )
        });
        tf3.init();
        $(".helpFooter").remove();
        $("#helpSpan_tableErrors").remove();
    });

});

function barChart() {
    var margin = {top: 20, right: 20, bottom: 30, left: 40},
        width = 1060*1.5 - margin.left - margin.right,
        height = 300*1.5 - margin.top - margin.bottom;

    var x = d3.scale.ordinal()
        .rangeRoundBands([0, width], .1);

    var y = d3.scale.linear()
        .rangeRound([height, 0]);

    var color = d3.scale.ordinal();
//    .range(["#98abc5", "#8a89a6", "#7b6888", "#6b486b", "#a05d56", "#d0743c", "#ff8c00"]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left")
        .tickFormat(d3.format(".2s"));

    var svg = d3.select("body").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    d3.csv("/senercon/error_statistics/csv", function(error, data) {
        color.domain(d3.keys(data[0]).filter(function(key) { return key !== "Date"; })).range(colorbrewer.Paired[12]);

        data.forEach(function(d) {
            var y0 = 0;
            d.ages = color.domain().map(function(name) { return {name: name, y0: y0, y1: y0 += +d[name]}; });
            d.total = d.ages[d.ages.length - 1].y1;
        });

        data.sort(function(a, b) { return -b.total; });

        x.domain(data.map(function(d) { return d.Date; }));
        y.domain([0, d3.max(data, function(d) { return d.total; })]);

        svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .attr("font-size", "9px")
            .call(xAxis)
            .append("text")
            .attr("x", width-20)
            .attr("y", "30")
            .attr("font-size", "14px")
            .style("text-anchor", "end")
            .text("Date");

        svg.append("g")
            .attr("class", "y axis")
            .call(yAxis)
            .append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text("Frequency");

        var Iteration = svg.selectAll(".Date")
            .data(data)
            .enter().append("g")
            .attr("class", "g")
            .attr("transform", function(d) { return "translate(" + x(d.Date) + ",0)"; });

        Iteration.selectAll("rect")
            .data(function(d) { return d.ages; })
            .enter().append("rect")
            .attr("width", x.rangeBand())
            .attr("y", function(d) { return y(d.y1); })
            .attr("height", function(d) { return y(d.y0) - y(d.y1); })
            .style("fill", function(d) { return color(d.name); });

        var legend = svg.selectAll(".legend")
            .data(color.domain().slice().reverse())
            .enter().append("g")
            .attr("class", "legend")
            .attr("transform", function(d, i) { return "translate(0," + i * 17 + ")"; });

        legend.append("rect")
            .attr("x", 28)
            .attr("width", 18)
            .attr("height", 18)
            .style("fill", color);

        legend.append("text")
            .attr("x", 54)
            .attr("y", 9)
            .attr("dy", ".35em")
            .style("text-anchor", "start")
            .text(function(d) { return d; });

    });

}