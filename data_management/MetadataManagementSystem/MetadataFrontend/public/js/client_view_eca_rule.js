function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

var tabCount = 0;

$(window).load(function() {
    $.get("/eca_rule/"+getParameterByName("eca_ruleID"), function(data) {
        var eca_ruleObj = (data);

        $("#name").val(eca_ruleObj.name);
        $("#event").val(eca_ruleObj.event.event);
        $.each(eca_ruleObj.conditions, function(key, value) {
            ++tabCount;

            $("#tabPanel").append($('<li role="presentation"><a id="button_tab_'+(tabCount)+'" href="#tab_'+(tabCount)+'" aria-controls="settings" role="tab" data-toggle="tab">'+'Filter '+(tabCount)+'<button type="button" class="close closeTab">&nbsp &times;</button></a></li>'));
            $("#tabContent").append($('<div id="tab_'+(tabCount)+'" role="tabpanel" class="tab-pane fill" style="border:1px solid; padding:5px">'+
                '<div class="form-group"> <label class="col-lg-2 control-label">'+'Attribute '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="attribute'+(tabCount)+'" style="width:100%" readonly=""></div></div>' +
                '<div class="form-group"> <label class="col-lg-2 control-label">'+'Operator '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="operator'+(tabCount)+'" style="width:100%" readonly=""></div></div>' +
                '<div class="form-group"> <label class="col-lg-2 control-label">'+'Predicate '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="predicate'+(tabCount)+'" style="width:100%" readonly=""></div></div>' +
                '<div class="form-group"> <label class="col-lg-2 control-label">'+'Value '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="value'+(tabCount)+'" type="text" required="required" readonly=""></div></div></div>'));

            $("#attribute"+tabCount).val(value.attribute);
            $("#operator"+tabCount).val(value.operator);
            $("#predicate"+tabCount).val(value.predicate);
            $("#value"+tabCount).val(value.value);
        });


        $("#feature").val(eca_ruleObj.feature);
        $("#operator").val(eca_ruleObj.operator);
        $("#predicate").val(eca_ruleObj.predicate);
        $("#value").val(eca_ruleObj.value);
        $("#windowTime").val(eca_ruleObj.windowTime);
        $("#windowSize").val(eca_ruleObj.windowSize);
        $("#action").val(eca_ruleObj.action);
    });

});