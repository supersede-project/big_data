/**
 * Created by snadal on 30/04/16.
 */


$(document).ready( function() {
    $("#dispatcherStrategyForm").hide();

    var container = document.getElementById("jsoneditor");
    var options = {
        "mode": "code",
        "indentation": 2
    };
    var editor = new JSONEditor(container, options);

    editor.set(new Object());


    $.get("/dispatcher_strategies_types", function(data) {
        _.each(data, function(element,index,list) {
            var obj = (element);
            $("#dispatcherStrategy").append($('<option value="'+obj.key+'">').text(obj.val));
        });
        $("#dispatcherStrategy").select2({
            theme: "bootstrap"
        });
    });

    $.get("/supersede/platforms", function(data) {
        _.each(data, function(element,index,list) {
            $("#platform").append($('<option value="'+element+'">').text(element));
        });
        $("#platform").select2({
            theme: "bootstrap"
        });
    });

    $.get("/supersede/tenants", function(data) {
        _.each(data, function(element,index,list) {
            $("#tenant").append($('<option value="'+element+'">').text(element));
        });
        $("#tenant").select2({
            theme: "bootstrap"
        });
    });

    $(".checkbox").change(function() {
        if(this.checked) {
            $("#dispatcherStrategyForm").show();
        } else {
            $("#dispatcherStrategyForm").hide();
        }
    });

    $('#submitEvent').on("click", function(e){
        e.preventDefault();

        var event = new Object();
        event.event = $("#event").val();
        event.jsonInstances = JSON.stringify(editor.get());
        event.kafkaTopic = $("#kafkaTopic").val();
        event.dispatch = $("#dispatch")[0].checked;
        event.dispatcherStrategy = $("#dispatcherStrategy").val();
        event.platform = $("#platform").val();
        event.tenant = $("#tenant").val();

        $.ajax({
            url: '/event',
            type: 'POST',
            data: event
        }).done(function() {
            window.location.href = '/manage_events';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });
});