/**
 * Created by snadal on 29/05/16.
 */
/*
function removeDataset(dataset) {
    $.ajax({
        url: 'releases/'+(dataset),
        type:'DELETE'
    }).done(function( data ) {
        location.reload();
    });
}
*/
function getReleases() {
    $.get("/release", function(data) {
        var i = 1;
        $.each((data), function(key, value) {
            var theObj = JSON.parse(value);
            $('#releases').find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(theObj.event)
                    ).append($('<td>')
                        .text(theObj.schemaVersion)
                    ).append($('<td>')
                        .text(theObj.kafkaTopic)
//                    ).append($('<td>').append($('<a href="/view_dataset?datasetID='+(theObj.datasetID)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    //).append($('<td>').append($('<a onClick="notImplemented(\''+((theObj.graph))+'\')" href="#">').append($('<span class="glyphicon glyphicon-edit"></span>')))
//                    ).append($('<td>').append($('<a onClick="removeDataset(\''+((theObj.datasetID))+'\')" href="#">').append($('<span class="glyphicon glyphicon-remove-circle"></span>')))
                    )

                );
            ++i;
        });
    });
}

$(function() {
    getReleases();
});
