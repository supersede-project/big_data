function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
/*
$(window).load(function() {
    $.get("/artifacts/RULES/"+encodeURIComponent(getParameterByName("graph")), function(data) {
        $("#theTitle").text(data.name);
        $("#theURL").text(data.graph);
    });

    $.get("/artifacts/RULES/"+encodeURIComponent(getParameterByName("graph"))+"/content", function(data) {
        $("#xml").text((data));
        $('pre code').each(function(i, block) {
            hljs.highlightBlock(block);
        });
    });

});
*/

$(window).load(function() {
    $.get("/eca_rule/"+getParameterByName("eca_ruleID"), function(data) {
        var eca_ruleObj = (data);

        $("#name").val(eca_ruleObj.name);
        $("#bdiOntology").val(eca_ruleObj.globalLevel);
        $("#feature").val(eca_ruleObj.feature);
        $("#operator").val(eca_ruleObj.operator);
        $("#predicate").val(eca_ruleObj.predicate);
        $("#value").val(eca_ruleObj.value);
        $("#windowTime").val(eca_ruleObj.windowTime);
        $("#windowSize").val(eca_ruleObj.windowSize);
        $("#action").val(eca_ruleObj.action);
    });

});