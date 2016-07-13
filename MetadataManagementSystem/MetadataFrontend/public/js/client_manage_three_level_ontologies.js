/**
 * Created by snadal on 29/05/16.
 */

function removeOntology(graph) {
    $.ajax({
        url: 'artifacts/PHYSICAL_ONTOLOGY/'+encodeURIComponent(graph),
        type:'DELETE'
    }).done(function( data ) {
        location.reload();
    });
}

function notImplemented(graph) {
    alert("Not implemented - How should we handle changes in the graph name?");
}

function getThreeLevelOntologies() {
    $.get("/artifacts/THREE_LEVEL_ONTOLOGY", function(data) {
        var i = 1;
        $.each(data, function(key, value) {
            var theObj = JSON.parse(value);
            $('#threeLevelOntologies').find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(theObj.name)
                    ).append($('<td>')
                        .append($('<a href="/view_domain_ontology?graph="'+encodeURIComponent(theObj.domainOntology)+'">')).text(theObj.domainOntology)
                    ).append($('<td>')
                        .append($('<a href="/view_logical_ontology?graph="'+encodeURIComponent(theObj.logicalOntology)+'">')).text(theObj.logicalOntology)
                    ).append($('<td>')
                        .append($('<a href="/view_physical_ontology?graph="'+encodeURIComponent(theObj.physicalOntology)+'">')).text(theObj.physicalOntology)
                    ).append($('<td>')
                        .text(theObj.user)
                    ).append($('<td>').append($('<a href="/view_three_level_ontology?threeLevelOntologyID='+theObj.threeLevelOntologyID+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    //).append($('<td>').append($('<span class="glyphicon glyphicon-edit"></span>'))
                    ).append($('<td>').append($('<a onClick="notImplemented(\''+((theObj.threeLevelOntologyID))+'\')" href="#">').append($('<span class="glyphicon glyphicon-edit"></span>')))
                    ).append($('<td>').append($('<a onClick="removeOntology(\''+((theObj.threeLevelOntologyID))+'\')" href="#">').append($('<span class="glyphicon glyphicon-remove-circle"></span>')))
                    )

                );
            ++i;
        });
    });
}

$(function() {
    getThreeLevelOntologies();
});
