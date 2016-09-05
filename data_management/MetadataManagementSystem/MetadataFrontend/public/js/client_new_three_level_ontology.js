/**
 * Created by snadal on 07/06/16.
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function getDomainOntologies() {
    $.get("/artifacts/DOMAIN_ONTOLOGY", function(data) {
        _.each(data, function(element,index,list) {
            var theObj = JSON.parse(element);
            $("#domainOntology").append($('<option value="'+theObj.graph+'">').text(theObj.name));
        });
        $("#domainOntology").select2();
    });
}

function getLogicalOntologies() {
    $.get("/artifacts/LOGICAL_ONTOLOGY", function(data) {
        _.each(data, function(element,index,list) {
            var theObj = JSON.parse(element);
            $("#logicalOntology").append($('<option value="'+theObj.graph+'">').text(theObj.name));
        });
        $("#logicalOntology").select2();
    });
}

function getPhysicalOntologies() {
    $.get("/artifacts/PHYSICAL_ONTOLOGY", function(data) {
        _.each(data, function(element,index,list) {
            var theObj = JSON.parse(element);
            $("#physicalOntology").append($('<option value="'+theObj.graph+'">').text(theObj.name));
        });
        $("#physicalOntology").select2();
    });
}

$(window).load(function() {


    $('#save').on("click", function(e){
        e.preventDefault();

        var formData = new FormData($('#threeLevelOntologyForm')[0]);
        formData.append('domainOntology',$('#domainOntology option:selected').val());
        formData.append('logicalOntology',$('#logicalOntology option:selected').val());
        formData.append('physicalOntology',$('#physicalOntology option:selected').val());

        $.ajax({
            url: '/three_level_ontology',
            type: 'POST',
            data:formData,
            contentType: false,
            processData: false
        }).done(function() {
            window.location.href = '/manage_three_level_ontologies';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });
});

$(function() {
    getDomainOntologies();
    getLogicalOntologies();
    getPhysicalOntologies();
});