var breakdownPerAPI;
var containerIndex;
var plots;
var plotsData;
var starting_value = 250;

$(function() {
    breakdownPerAPI = new Object();
    plots = new Object();
    plotsData = new Object();
    containerIndex = 0;
});

$(function() {
    var socket = io('/siemens_rt');
    socket.on('/breakdown_per_api', function (fromSocket) {
        console.log(JSON.stringify(breakdownPerAPI));
        console.log(JSON.stringify(plots));
        var APIs = Object.keys(fromSocket.message)[0];
        var obj = JSON.parse(APIs);

        var listOfAPIs = Object.keys(obj);
        for (var i = 0; i < listOfAPIs.length; ++i) {
            var API = listOfAPIs[i];
            if (!breakdownPerAPI[API]) {
                var html = '<div id="graph'+containerIndex+'" class="aGraph" style="position:relative;width:100%;height:250px"><h4>'+API+'</h4></div>';
                $("#allPlots").append(html);

                breakdownPerAPI[API] = new Object();
                plots[API] = new Object();
                var newData = getNewDataElement();
                newData["displayNames"] = ["Successful API calls","Unsuccessful API calls"];
                newData["colors"] = ["green","red"];
                newData["scale"] = "linear";
                plotsData[API] = newData;

                plots[API].plot = new LineGraph({containerId: 'graph' + containerIndex, data: plotsData[API]});

                ++containerIndex;


            }
            breakdownPerAPI[API].succesful = obj[listOfAPIs[i]].succesful;
            breakdownPerAPI[API].unsuccesful = obj[listOfAPIs[i]].unsuccesful;

            console.log(API);
            console.log(plots[API].plot);

            plotsData[API].values = [[(breakdownPerAPI[API].succesful)],[(breakdownPerAPI[API].unsuccesful)]];
            plotsData[API].start += plotsData[API].step;
            plotsData[API].end += plotsData[API].step;
            plots[API].plot.slideData(plotsData[API]);
        }

    });

});

function getNewDataElement() {
    return {"start":(new Date()).getTime(),"end":(((new Date()).getTime())+6000*5),"step":6000,"names":["Stats_count2xx"],"values":[[starting_value, starting_value, starting_value, starting_value],[starting_value, starting_value, starting_value, starting_value]]};
}