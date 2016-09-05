/**
 * Created by Sergi Nadal on 10/03/2016.
 */
/*
 * sample data to plot over time
 */


var starting_value = 1000;

var syncAttemptMean;
var currentLog;

var data = {"start":(new Date()).getTime(),"end":(((new Date()).getTime())+6000*60*12),"step":1000,"names":["Stats_count2xx"],"values":[[starting_value],[0]]};
for (var i = 0; i < 4000; ++i) {
    data.values[0].push([0]);
}

$(function() {

    data["displayNames"] = ["Distinct Number of Users"];
    data["colors"] = ["green"];
    data["scale"] = "linear";

    var l1 = new LineGraph({containerId: 'graph1', data: data});

    var socket = io('/atos_rt');

    socket.on('/distinct_users', function (fromSocket) {
        data.values = [[parseFloat(Object.keys(fromSocket.message)[0])]];
        data.start = data.start + data.step;
        data.end = data.end + data.step;
        l1.slideData(data);

        $('#valueReq1').text(Math.round((parseFloat(Object.keys(fromSocket.message)[0]) / 1000) * 100 * 100) / 100 + " %");
        if ((Math.round((parseFloat(Object.keys(fromSocket.message)[0]) / 1000) * 100 * 100) / 100) > 85) {
            $("#lightReq1").removeClass('circle_green').addClass('circle_red');
        } else {
            $("#lightReq1").removeClass('circle_red').addClass('circle_green');
        }
    });

    socket.on('/fullMessage', function (fromSocket) {
        var theObj = JSON.parse(Object.keys(fromSocket.message)[0]);
        currentLog = theObj;
        $('#fullLogs').find('tbody')
            .prepend($('<tr>')
                .append($('<td>')
                    .text(theObj.timestamp)
                ).append($('<td>')
                    .text(theObj.deviceID)
                ).append($('<td>')
                    .text(theObj.playbackErrorVideo)
                ).append($('<td>')
                    .text(theObj.rebufferingVideo)
                ).append($('<td>')
                    .text(theObj.syncAttemps)
                )
            );

        $("#fullLogs tr:eq(10)").remove();
    });

    socket.on('/playbackErrorVideo', function (fromSocket) {
        $('#valueReq2').text(Math.round((parseFloat(Object.keys(fromSocket.message)[0]) / 1000) * 100 * 100) / 100 + " %");
        if ((Math.round((parseFloat(Object.keys(fromSocket.message)[0]) / 1000) * 100 * 100) / 100) > 20) {
            $("#lightReq2").removeClass('circle_green').addClass('circle_red');
        } else {
            $("#lightReq2").removeClass('circle_red').addClass('circle_green');
        }
    });

    socket.on('/rebuffering', function (fromSocket) {
        $('#valueReq3').text(Math.round((parseFloat(Object.keys(fromSocket.message)[0]) / 1000) * 100 * 100) / 100 + " %");
        if ((Math.round((parseFloat(Object.keys(fromSocket.message)[0]) / 1000) * 100 * 100) / 100) > 20) {
            $("#lightReq3").removeClass('circle_green').addClass('circle_red');
        } else {
            $("#lightReq3").removeClass('circle_red').addClass('circle_green');
        }
    });

    socket.on('/meanSyncAttempt', function (fromSocket) {
        syncAttemptMean = parseFloat(Object.keys(fromSocket.message)[0]);
    });

    socket.on('/stDevSyncAttempt', function (fromSocket) {
        var mean = Math.round(parseFloat(syncAttemptMean) * 100)/100;
        var stDev = Math.round(parseFloat(Object.keys(fromSocket.message)[0]) * 100)/100;

        $('#valueReq4').text(mean + " ± " + stDev);
        if ((currentLog.syncAttemps < mean - 2*stDev) || (currentLog.syncAttemps > mean + 2*stDev)) {
            $("#lightReq4").removeClass('circle_green').addClass('circle_red');
        } else {
            $("#lightReq4").removeClass('circle_red').addClass('circle_green');
        }


    });
});

