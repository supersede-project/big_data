/**
 * Created by snadal on 29/05/16.
 */

function getEvents() {
    $.get("/event", function(data) {
        var i = 1;
        $.each((data), function(key, value) {
            var theObj = (value);
            $('#events').find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(theObj.event)
                    ).append($('<td>')
                        .text(theObj.kafkaTopic)
                    ).append($('<td>')
                        .text(theObj.dispatcherPath ? theObj.dispatcherPath : '-')
                    ).append($('<td>')
                        .text(theObj.platform)
                    ).append($('<td>').append($('<a href="/view_event?eventID='+(theObj.eventID)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    ).append($('<td>').append($('<a href="/view_event_ontology?graph='+(theObj.graph)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    )

                );
            ++i;
        });
    });
}

$(function() {
    getEvents();
});
