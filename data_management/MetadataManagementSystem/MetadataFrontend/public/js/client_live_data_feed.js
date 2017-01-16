/**
 * Created by snadal on 16/01/17.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(function() {
    var topic = getParameterByName("topic");
    var socket = io("/raw_data");

    socket.on('/raw_data', function (fromSocket) {
        var socketMsg = JSON.parse(Object.keys(fromSocket.message)[0]);
        if (socketMsg.topic == topic) {
            $('#liveDataFeed').find('tbody')
                .prepend($('<tr>')
                    .append($('<td>')
                        .text(socketMsg.message)
                    )
                );
            $("#liveDataFeed tr:eq(25)").remove();
        }
    });

});
