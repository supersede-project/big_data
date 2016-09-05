var starting_value = 500;

var data = {"start":(new Date()).getTime(),"end":(((new Date()).getTime())+6000*5),"step":6000,"names":["Stats_count2xx"],"values":[[starting_value, starting_value, starting_value, starting_value],[starting_value, starting_value, starting_value, starting_value]]};

//var data = {"start":1336594920000,"end":1336680960000,"step":6000000,"names":["Stats_count2xx","Stats_count3xx","Stats_count4xx","Stats_count5xx"],"values":[[starting_value,starting_value,starting_value,starting_value,starting_value,starting_value,starting_value,starting_value,starting_value,starting_value,starting_value,starting_value,starting_value]]};


$(function() {

    data["displayNames"] = ["Successful API calls","Unsuccessful API calls"];
    data["colors"] = ["green","red"];
    data["scale"] = "linear";

    var l1 = new LineGraph({containerId: 'graph1', data: data});

    var socket = io('/siemens_rt');

    socket.on('/plot_all_APIs', function (fromSocket) {
        var suc_unsuc = JSON.parse(Object.keys(fromSocket.message)[0]);
        data.values = [[(suc_unsuc.total_suc)],[(suc_unsuc.total_unsuc)]];
        data.start += data.step;
        data.end += data.step;
        l1.slideData(data);
    });


});
