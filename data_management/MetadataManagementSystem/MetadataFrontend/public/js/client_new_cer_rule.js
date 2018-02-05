/**
 * Created by snadal on 07/06/16.
 */

var tabCount = 0;
var actionParameterTabCount = 0;

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function registerCloseEvent() {
    $(".closeTab").click(function () {
        var tabContentId = $(this).parent().attr("href");
        $(this).parent().parent().remove(); //remove li of tab
        $('#tabPanel a:last').tab('show'); // Select first tab
        $(tabContentId).remove(); //remove respective tab content
         --tabCount;
    });
}

function registerActionParameterCloseEvent() {
    $(".closeActionTab").click(function () {
        var tabContentId = $(this).parent().attr("href");
        $(this).parent().parent().remove(); //remove li of tab
        $('#actionTabPanel a:last').tab('show'); // Select first tab
        $(tabContentId).remove(); //remove respective tab content
        --actionParameterTabCount;
    });
}

function getEvents() {
    $("#event" + tabCount).empty().trigger('change');
    var pattern = $("#pattern").select2('data');
    _.each(pattern, function (event) {
        $("#event" + tabCount).append($('<option value="' + event.id + '">').text(event.text));
    });
    $("#event"+tabCount).select2({
        theme: "bootstrap"
    });
}

function getActionEvents() {
    $("#actionEvent" + actionParameterTabCount).empty().trigger('change');
    var pattern = $("#pattern").select2('data');
    _.each(pattern, function (event) {
        $("#actionEvent" + actionParameterTabCount).append($('<option value="' + event.id + '">').text(event.text));
    });
    $("#actionEvent"+actionParameterTabCount).select2({
        theme: "bootstrap"
    });
}

function getComparators() {
    $.get("/eca_rule_predicate_types", function(data) {
        _.each(data, function(element,index,list) {
            $("#comparator"+tabCount).append($('<option value="'+element.key+'">').text(element.key + " (" + element.val + ")"));
        });
        $("#comparator"+tabCount).select2({
            theme: "bootstrap"
        });
    });
}

$(window).load(function() {
    $('#tabPanel li:first').tab('show'); // Select first tab
    $('#actionTabPanel li:first').tab('show'); // Select first tab

    $("#pattern").on("select2:select", function (evt) {
        var element = evt.params.data.element;
        var $element = $(element);

        $element.detach();
        $(this).append($element);
    });

    $.get("/event", function(data) {
        $.each(data, function(key, value) {
            var obj = (value);
            $("#pattern").append($('<option value="'+obj.eventID+'">').text(obj.event));
        });
        $("#pattern").select2({
            theme: "bootstrap"
        })
        $("#pattern").trigger('change');
    });

    $("#pattern").on('change', function() {
        //$("#actionParameters").empty().trigger('change');
        $(".closeTab").click();
        $(".closeActionTab").click();
        tabCount = 0;
        actionParameterTabCount = 0;
        /*var pattern = $("#pattern").select2('data');
        _.each(pattern, function(event) {
            $.get("/event/"+event.id, function(eventData) {
                _.each(eventData.attributes, function(element,index,list) {
                    $("#actionParameters").append($('<option value="'+element.iri+'">').text(eventData.event + '.' + element.name +" ("+element.iri+")"));
                });
                $("#actionParameters").select2({
                    theme: "bootstrap"
                });
            });
        });*/
    });

    $.get("/eca_rule_action_types", function(data) {
        _.each(data, function(element,index,list) {
            $("#actionType").append($('<option value="'+element.key+'">').text(element.val));
        });
        $("#actionType").select2({
            theme: "bootstrap"
        });
    });

    $('#addSimpleClause').on("click", function(e) {
        e.preventDefault();
        ++tabCount;
        $("#tabPanel").append($('<li role="presentation"><a id="button_tab_'+(tabCount)+'" href="#tab_'+(tabCount)+'" aria-controls="settings" role="tab" data-toggle="tab">'+'Filter '+(tabCount)+'<button type="button" class="close closeTab">&nbsp &times;</button></a></li>'));
        $("#tabContent").append($('<div id="tab_'+(tabCount)+'" role="tabpanel" class="tab-pane fill" style="border:1px solid; padding:5px">'+
            //'<div class="form-group"> <label class="col-lg-2 control-label">'+'Name '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="name'+(tabCount)+'" type="text" required="required"> </input></div></div>'+
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Event '+(tabCount)+'</label><div class="col-lg-10"><select class="event" id="event'+(tabCount)+'" style="width:100%"></select></div></div>' +
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Left Operand '+(tabCount)+'</label><div class="col-lg-10"><select id="leftOperator'+(tabCount)+'" style="width:100%"></select></div></div>' +
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Comparator '+(tabCount)+'</label><div class="col-lg-10"><select id="comparator'+(tabCount)+'" style="width:100%"> </select></div></div>' +
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Right Operand '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="rightOperator'+(tabCount)+'" type="text" required="required"> </input></div></div></div>'));

        getEvents();
        $("#event"+tabCount).change(function(o) {
            $.get("/event/"+$("#event"+tabCount).val(), function(eventData) {
                $("#leftOperator"+tabCount).empty().trigger('change');
                _.each(eventData.attributes, function(element,index,list) {
                    $("#leftOperator"+tabCount).append($('<option value="'+element.name+'">').text(element.name +" ("+element.iri+")"));
                });
                $("#leftOperator"+tabCount).select2({
                    theme: "bootstrap"
                });
            });
        });
        registerCloseEvent();
        getComparators();

        $("#event"+tabCount).trigger("change");
    });

    $('#addActionParameter').on("click", function(e) {
        e.preventDefault();
        ++actionParameterTabCount;
        $("#actionTabPanel").append($('<li role="presentation"><a id="action_button_tab_'+(actionParameterTabCount)+'" href="#actionTab_'+(actionParameterTabCount)+'" aria-controls="settings" role="tab" data-toggle="tab">'+'Parameter '+(actionParameterTabCount)+'<button type="button" class="close closeActionTab">&nbsp &times;</button></a></li>'));
        $("#actionTabContent").append($('<div id="actionTab_'+(actionParameterTabCount)+'" role="tabpanel" class="tab-pane fill" style="border:1px solid; padding:5px">'+
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Event '+(actionParameterTabCount)+'</label><div class="col-lg-10"><select class="event" id="actionEvent'+(actionParameterTabCount)+'" style="width:100%"></select></div></div>' +
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Attribute '+(actionParameterTabCount)+'</label><div class="col-lg-10"><select id="attribute'+(actionParameterTabCount)+'" style="width:100%"></select></div></div>' +
            //'<div class="form-group"> <label class="col-lg-2 control-label">'+'Function '+(actionParameterTabCount)+'</label><div class="col-lg-10"><select id="function'+(actionParameterTabCount)+'" style="width:100%"> </select></div></div>' +
            '</div>'));

        getActionEvents();
        $("#actionEvent"+actionParameterTabCount).change(function(o) {
            $.get("/event/"+$("#actionEvent"+actionParameterTabCount).val(), function(eventData) {
                $("#attribute"+actionParameterTabCount).empty().trigger('change');
                _.each(eventData.attributes, function(element,index,list) {
                    $("#attribute"+actionParameterTabCount).append($('<option value="'+element.name+'">').text(element.name +" ("+element.iri+")"));
                });
                $("#attribute"+actionParameterTabCount).select2({
                    theme: "bootstrap"
                });
            });
        });
        registerActionParameterCloseEvent();

        $("#actionEvent"+actionParameterTabCount).trigger("change");

        //getComparators();
    });

    $('#submitEcaRule').on("click", function(e){
        e.preventDefault();

        var CER_rule = new Object();
        CER_rule.ruleName = $("#name").val();

        CER_rule.pattern = $("#pattern").select2('data').map(function(e) { return e.id });
        //CER_rule.condition = $("#condition").val();

        CER_rule.filters = new Array();
        for (i = 1; i <= tabCount; ++i) {
            var sc = new Object();
            //sc.name = $("#name"+i).val();
            sc.event = $("#event"+i).val();
            sc.leftOperand = $("#leftOperator"+i).val();
            sc.comparator = $("#comparator"+i).val();
            sc.rightOperand = $("#rightOperator"+i).val();
            CER_rule.filters.push(sc);
        }

        //CER_rule.actionName = $("#actionName").val();
        CER_rule.actionType = $("#actionType").val();
        CER_rule.actionParameters = new Array();
        for (i = 1; i <= actionParameterTabCount; ++i) {
            var ap = new Object();
            ap.event = $("#actionEvent"+i).val();
            ap.attribute = $("#attribute"+i).val();
            CER_rule.actionParameters.push(ap);
        }

        CER_rule.windowTime = $("#windowTime").val();
        CER_rule.windowSize = $("#windowSize").val();

        $.ajax({
            url: '/cer_rule',
            type: 'POST',
            data: CER_rule
        }).done(function() {
            window.location.href = '/manage_cer_rules';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });

    $('#directGeneration').on("click", function(e){
        e.preventDefault();

        var CER_rule = new Object();
        CER_rule.ruleName = $("#name").val();

        CER_rule.pattern = $("#pattern").select2('data').map(function(e) { return e.id });
        //CER_rule.condition = $("#condition").val();

        CER_rule.filters = new Array();
        for (i = 1; i <= tabCount; ++i) {
            var sc = new Object();
            //sc.name = $("#name"+i).val();
            sc.event = $("#event"+i).val();
            sc.leftOperand = $("#leftOperator"+i).val();
            sc.comparator = $("#comparator"+i).val();
            sc.rightOperand = $("#rightOperator"+i).val();
            CER_rule.filters.push(sc);
        }

        //CER_rule.actionName = $("#actionName").val();
        CER_rule.actionType = $("#actionType").val();
        CER_rule.actionParameters = new Array();
        for (i = 1; i <= actionParameterTabCount; ++i) {
            var ap = new Object();
            ap.event = $("#actionEvent"+i).val();
            ap.attribute = $("#attribute"+i).val();
            CER_rule.actionParameters.push(ap);
        }

        CER_rule.windowTime = $("#windowTime").val();
        CER_rule.windowSize = $("#windowSize").val();

        $.ajax({
            url: '/cer_rule/directGeneration',
            type: 'POST',
            data: CER_rule
        }).done(function() {
            window.location.href = '/manage_cer_rules';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });
});