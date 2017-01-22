/**
 * Created by snadal on 07/06/16.
 */
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(window).load(function() {
    $.get("/bdi_ontology", function(data) {
        _.each(data, function(element,index,list) {
            var obj = JSON.parse(element);
            $("#bdiOntology").append($('<option value="'+obj.bdi_ontologyID+'">').text(obj.name));
        });
        $("#bdiOntology").select2({
            theme: "bootstrap"
        })
        $("#bdiOntology").trigger('change');
    });

    $("#bdiOntology").change(function(o) {
        $.get("/bdi_ontology/"+$("#bdiOntology").val(), function(ontology) {
            $.get("/global_level/"+encodeURIComponent(ontology.globalLevel)+"/features", function(data) {
                $("#feature").val(null);
                _.each(JSON.parse(data), function(element,index,list) {
                    $("#feature").append($('<option value="'+element.iri+'">').text(element.name +" ("+element.iri+")"));
                });
                $("#feature").select2({
                    theme: "bootstrap"
                });
            });
        });


    });

    $.get("/eca_rule_predicate_types", function(data) {
        _.each(data, function(element,index,list) {
            $("#predicate").append($('<option value="'+element.key+'">').text(element.key + " (" + element.val + ")"));
        });
        $("#predicate").select2({
            theme: "bootstrap"
        });
    });

    $.get("/eca_rule_action_types", function(data) {
        _.each(data, function(element,index,list) {
            $("#action").append($('<option value="'+element.key+'">').text(element.val));
        });
        $("#action").select2({
            action: "bootstrap"
        });
    });

    $('#submitEcaRule').on("click", function(e){
        e.preventDefault();

        $.get("/bdi_ontology/"+$("#bdiOntology").val(), function(ontology) {
            var Eca_Rule = new Object();
            Eca_Rule.name = $("#name").val();
            Eca_Rule.globalLevel = ontology.globalLevel;
            Eca_Rule.graph = ontology.rules;
            Eca_Rule.feature = $("#feature").val();
            Eca_Rule.predicate = $("#predicate").val();
            Eca_Rule.value = $("#value").val();
            Eca_Rule.action = $("#action").val();

            $.ajax({
                url: '/eca_rule',
                type: 'POST',
                data: Eca_Rule
            }).done(function() {
                window.location.href = '/manage_eca_rules';
            }).fail(function(err) {
                alert("error "+JSON.stringify(err));
            });

        });

    });

});