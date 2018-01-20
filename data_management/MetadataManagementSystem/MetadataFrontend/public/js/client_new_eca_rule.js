/**
 * Created by snadal on 07/06/16.
 */

var tabCount = 0;

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

function getAttributes() {
    var eventID = $("#event").val();
    $.get("/event/"+eventID, function(data) {
        $("#attribute"+tabCount).empty().trigger('change');
        _.each(data.attributes, function(element,index,list) {
            $("#attribute"+tabCount).append($('<option value="'+element.iri+'">').text(element.name +" ("+element.iri+")"));
        });
        $("#attribute"+tabCount).select2({
            theme: "bootstrap"
        });;
    });
}

function getOperators() {
    $.get("/eca_rule_operator_types", function(data) {
        _.each(data, function(element,index,list) {
            $("#operator"+tabCount).append($('<option value="'+element.key+'">').text(element.val));
        });
        $("#operator"+tabCount).select2({
            theme: "bootstrap"
        });
    });
}

function getPredicates() {
    $.get("/eca_rule_predicate_types", function(data) {
        _.each(data, function(element,index,list) {
            $("#predicate"+tabCount).append($('<option value="'+element.key+'">').text(element.key + " (" + element.val + ")"));
        });
        $("#predicate"+tabCount).select2({
            theme: "bootstrap"
        });
    });
}

$(window).load(function() {
    $.get("/event", function(data) {
        $.each((data), function(key, value) {
            var obj = (value);
            $("#event").append($('<option value="'+obj.eventID+'">').text(obj.event + " (" + obj.platform + " platform)"));
        });
        $("#event").select2({
            theme: "bootstrap"
        })
        $("#event").trigger('change');
    });

    $("#event").change(function() {
        getAttributes();
        getOperators();
        getPredicates();
    });

    $("#alertParameters").on("select2:select", function (evt) {
        var element = evt.params.data.element;
        var $element = $(element);
        $element.detach();
        $(this).append($element);
    });

    $("#event").on('change', function() {
        $("#alertParameters").empty().trigger('change');
        $(".closeTab").click();
        var currentEvent = $("#event").select2('data');
        _.each(currentEvent, function(event) {
            $.get("/event/"+event.id, function(eventData) {
                _.each(eventData.attributes, function(element,index,list) {
                    $("#alertParameters").append($('<option value="'+element.iri+'">').text(element.name +" ("+element.iri+")"));
                });
                $("#alertParameters").select2({
                    theme: "bootstrap"
                });
            });
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

    $('#addCondition').on("click", function(e) {
        e.preventDefault();
        ++tabCount;
        $("#tabPanel").append($('<li role="presentation"><a id="button_tab_'+(tabCount)+'" href="#tab_'+(tabCount)+'" aria-controls="settings" role="tab" data-toggle="tab">'+'Filter '+(tabCount)+'<button type="button" class="close closeTab">&nbsp &times;</button></a></li>'));
        $("#tabContent").append($('<div id="tab_'+(tabCount)+'" role="tabpanel" class="tab-pane fill" style="border:1px solid; padding:5px">'+
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Attribute '+(tabCount)+'</label><div class="col-lg-10"><select class="attribute" id="attribute'+(tabCount)+'" style="width:100%"></select></div></div>' +
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Operator '+(tabCount)+'</label><div class="col-lg-10"><select id="operator'+(tabCount)+'" style="width:100%"></select></div></div>' +
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Predicate '+(tabCount)+'</label><div class="col-lg-10"><select id="predicate'+(tabCount)+'" style="width:100%"> </select></div></div>' +
            '<div class="form-group"> <label class="col-lg-2 control-label">'+'Value '+(tabCount)+'</label><div class="col-lg-10"><input class="form-control" id="value'+(tabCount)+'" type="text" required="required"> </input></div></div></div>'));
        getAttributes();
        getOperators();
        getPredicates();

        registerCloseEvent();
    });

    $('#submitEcaRule').on("click", function(e){
        e.preventDefault();

        var Eca_Rule = new Object();
        Eca_Rule.name = $("#name").val();
        Eca_Rule.eventID = $("#event").val();
        Eca_Rule.conditions = new Array();
        for (i = 1; i <= tabCount; ++i) {
            var condition = new Object();
            condition.attribute = $("#attribute"+i).val();
            condition.operator = $("#operator"+i).val();
            condition.predicate = $("#predicate"+i).val();
            condition.value = $("#value"+i).val();

            Eca_Rule.conditions.push(condition);
        }
        Eca_Rule.windowTime = $("#windowTime").val();
        Eca_Rule.windowSize = $("#windowSize").val();
        Eca_Rule.action = $("#action").val();
        Eca_Rule.alertParameters = $("#alertParameters").select2('data').map(function(e) { return e.id });

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