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

tabCount = 1;
function createNewTab(i, ecaRule) {
    $("#tabPanel").append($('<li role="presentation"><a id="button_tab_'+(tabCount)+'" href="#tab_'+(tabCount)+'" aria-controls="settings" role="tab" data-toggle="tab">'+'Filter '+(tabCount)+'</a></li>'));
    $("#tabContent").append($('<div id="tab_' + (tabCount) + '" role="tabpanel" class="tab-pane fill" style="float:right; width:80%; padding-right:5%" xmlns="http://www.w3.org/1999/html">'+'<div class="form-group"> <label class="col-lg-2 control-label">'+'Name '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="name'+(tabCount)+'" type="text" required="required" readonly="""></input></div></div>'+
        '<div class="form-group"> <label class="col-lg-2 control-label">'+'Event '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="event'+(tabCount)+'" type="text" required="required" readonly="""></input ></div></div>' +
        '<div class="form-group"> <label class="col-lg-2 control-label">'+'Left Operator '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="leftOperator'+(tabCount)+'" ttype="text" required="required" readonly="""></input></div></div>' +
        '<div class="form-group"> <label class="col-lg-2 control-label">'+'Comparator '+(tabCount)+'</label><div class="col-lg-10"><input class="value form-control" id="comparator'+(tabCount)+'" type="text" required="required" readonly=""> </input></div></div>' +
        '<div class="form-group"> <label class="col-lg-2 control-label">'+'Right Operator '+(tabCount)+'</label><div class="col-lg-10"><input class="value form-control" id="rightOperator'+(tabCount)+'" type="text" required="required" readonly=""> </input></div></div></div>'));
    $("#name"+tabCount).val(ecaRule.filters[i][0]);
    $("#event"+tabCount).val(ecaRule.filters[i][1]);
    $("#leftOperator"+tabCount).val(ecaRule.filters[i][2]);
    $("#comparator"+tabCount).val(ecaRule.filters[i][3]);
    $("#rightOperator"+tabCount).val(ecaRule.filters[i][4]);
    ++tabCount;
}

$(window).load(function() {
    $.get("/eca_rule/"+getParameterByName("eca_ruleID"), function(data) {
        var eca_ruleObj = (data);
        $("#name").val(eca_ruleObj.ruleName);
        var pat = eca_ruleObj.pattern;
        for (i = 0; i < pat.length; ++i) {
            var gl;
            $.get("/bdi_ontology/" + pat[i], function (ontology) {
                pattern += ' ' + ontology.globalLevel;
            });
        }
        $("#bdiOntology").val(eca_ruleObj.pattern);
        for(i = 0; i < eca_ruleObj.filters.length; ++i) {
            createNewTab(i, eca_ruleObj);
        }
        $("#actionName").val(eca_ruleObj.action[0]);
        $("#actionType").val(eca_ruleObj.action[1]);
        $("#actionParameters").val(eca_ruleObj.action[2]);

        $("#windowTime").val(eca_ruleObj.windowTime);
        $("#windowSize").val(eca_ruleObj.windowSize);
    });
});
