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

//tabCount: 1, 2, 3...
//i: 0, 1, 2...

tabCount = 2;
function createNewTab(i, ecaRule) {
    $("#tabPanel").append($('<li role="presentation"><a id="button_tab_'+(tabCount)+'" href="#tab_'+(tabCount)+'" aria-controls="settings" role="tab" data-toggle="tab">'+'SC'+(tabCount)+'</a></li>'));
    $("#tabContent").append($('<div id="tab_'+(tabCount)+'" role="tabpanel" class="tab-pane fill" style="float:right; width:80%; padding-right:5%">'+'<div class="form-group"> <label class="col-lg-2 control-label" for="feature">'+'Feature '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="feature'+(tabCount)+'" type="text" required="required" readonly="""></input></div></div>'+
        '<div class="form-group"> <label class="col-lg-2 control-label" for="operator">'+'Operator '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="operator'+(tabCount)+'" type="text" style="width:100%" readonly="""></select></div></div>' +
        '<div class="form-group"> <label class="col-lg-2 control-label" for="predicate">'+'Predicate '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="predicate'+(tabCount)+'" type="text" style="width:100%" readonly="""></input></div></div>' +
        '<div class="form-group"> <label class="col-lg-2 control-label" for="value">'+'Value '+(tabCount)+'</label><div class="col-lg-10"><input class="value form-control" id="value'+(tabCount)+'" type="text" required="required" readonly=""> </input></div></div>' +
        '<div class="form-group"> <label class="col-lg-2 control-label" for="filter">'+'Filter '+(tabCount)+'</label><div class="col-lg-10"><input class="value form-control" id="filter'+(tabCount)+'" type="text" required="required" readonly=""> </input></div></div></div>'));
    $("#feature"+tabCount).val(ecaRule.simpleClauses[i][0]);
    $("#operator"+tabCount).val(ecaRule.simpleClauses[i][1]);
    $("#predicate"+tabCount).val(ecaRule.simpleClauses[i][2]);
    $("#value"+tabCount).val(ecaRule.simpleClauses[i][3]);
    $("#filter"+tabCount).val(ecaRule.simpleClauses[i][4]);
    ++tabCount;
}

$(window).load(function() {
    $.get("/eca_rule/"+getParameterByName("eca_ruleID"), function(data) {
        $('#tabPanel li:first').tab('show'); // Select first tab
        var eca_ruleObj = (data);

        $("#name").val(eca_ruleObj.name);
        $("#bdiOntology").val(eca_ruleObj.globalLevel);
        $("#feature1").val(eca_ruleObj.simpleClauses[0][0]);
        $("#operator1").val(eca_ruleObj.simpleClauses[0][1]);
        $("#predicate1").val(eca_ruleObj.simpleClauses[0][2]);
        $("#value1").val(eca_ruleObj.simpleClauses[0][3]);
        $("#filter1").val(eca_ruleObj.simpleClauses[0][4 ]);
        $("#action").val(eca_ruleObj.action);
        for(i = 1; i < eca_ruleObj.simpleClauses.length; ++i) {
            createNewTab(i, eca_ruleObj);
        }
       // $("#windowTime").val(eca_ruleObj.windowTime);
       // $("#windowSize").val(eca_ruleObj.windowSize);
    });

});
