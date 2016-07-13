/**
 * Created by Sergi Nadal on 10/03/2016.
 */
var breakdownPerAPI;

$(function() {
    breakdownPerAPI = new Object();
});

$(function() {
    var socket = io('/siemens_rt');

    socket.on('/full_message', function (fromSocket) {
        $('#fullLogs').find('tbody')
            .prepend($('<tr>')
                .append($('<td>')
                    .text(Object.keys(fromSocket.message)[0])
                )

        );
        $("#fullLogs tr:eq(7)").remove();
    });

    socket.on('/total_api_calls', function (fromSocket) {
        $('#valueReq1').text(Object.keys(fromSocket.message));
    });

    socket.on('/total_successful_api_calls', function (fromSocket) {
        $('#valueReq2').text(Object.keys(fromSocket.message));
    });

    socket.on('/total_unsuccessful_api_calls', function (fromSocket) {
        $('#valueReq3').text(Object.keys(fromSocket.message));
    });

    socket.on('/breakdown_per_api', function (fromSocket) {
        var APIs = Object.keys(fromSocket.message)[0];
        var obj = JSON.parse(APIs);

        var listOfAPIs = Object.keys(obj);
        for (var i = 0; i < listOfAPIs.length; ++i) {
            var API = listOfAPIs[i];
            if (!breakdownPerAPI[API]) breakdownPerAPI[API] = new Object();
            breakdownPerAPI[API].succesful = obj[listOfAPIs[i]].succesful;
            breakdownPerAPI[API].unsuccesful = obj[listOfAPIs[i]].unsuccesful;
        }

        var currentAPIs = Object.keys(breakdownPerAPI);
        for (var i = 0; i < currentAPIs.length; ++i) {
            var contains = $("#tableAPIBreakdown > tbody:contains("+currentAPIs[i]+")");
            if (contains.length == 0) {
                $('#tableAPIBreakdown').find('tbody')
                    .append($('<tr>')
                        .append($('<td>')
                            .text(currentAPIs[i])
                        ).append($('<td>')
                            .text(breakdownPerAPI[currentAPIs[i]].succesful)
                        ).append($('<td>')
                            .text(breakdownPerAPI[currentAPIs[i]].unsuccesful)
                        ).append($('<td>')
                            .text(breakdownPerAPI[currentAPIs[i]].succesful+breakdownPerAPI[currentAPIs[i]].unsuccesful)
                        ).append($('<td>')
                            .text(Math.round((
                                    breakdownPerAPI[currentAPIs[i]].succesful /
                                    ( breakdownPerAPI[currentAPIs[i]].succesful+breakdownPerAPI[currentAPIs[i]].unsuccesful )
                                ) * 100 * 100) / 100 + " %")
                        )
                    )
            } else {
                var rate = Math.round(((
                    breakdownPerAPI[currentAPIs[i]].succesful /
                    ( breakdownPerAPI[currentAPIs[i]].succesful+breakdownPerAPI[currentAPIs[i]].unsuccesful )
                )));
                //if (rate == NaN) rate = 100;

                contains.html('<tr>' +
                        '<td>' +
                            currentAPIs[i] +
                        '</td>' +
                        '<td>' +
                            breakdownPerAPI[currentAPIs[i]].succesful +
                        '</td>' +
                        '<td>' +
                            breakdownPerAPI[currentAPIs[i]].unsuccesful +
                        '</td>' +
                        '<td>' +
                            (breakdownPerAPI[currentAPIs[i]].succesful+breakdownPerAPI[currentAPIs[i]].unsuccesful) +
                        '</td>' +
                        '<td>' +
                            rate + " %" +
                        '</td>' +

                    '</tr>');
            }

            }
        //}
    });




});