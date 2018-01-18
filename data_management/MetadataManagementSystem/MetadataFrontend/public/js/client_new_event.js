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

    $(".checkbox").change(function() {
        if(this.checked) {
            $("#dispatcherStrategyForm").show();
        } else {
            $("#dispatcherStrategyForm").hide();
        }
    });

    $('#submitRelease').on("click", function(e){
        e.preventDefault();

        var release = new Object();
        release["event"] = $("#event").val();
        release["jsonInstances"] = JSON.stringify(editor.get());
        release["kafkaTopic"] = $("#kafkaTopic").val();
        release["dispatch"] = $("#dispatch")[0].checked;
        release["dispatcherStrategy"] = $("#dispatcherStrategy").val();
        release["platform"] = $("#platform").val();

        $.ajax({
            url: '/event',
            type: 'POST',
            data: release
        }).done(function() {
            window.location.href = '/manage_events';
        }).fail(function(err) {
            alert("error "+JSON.stringify(err));
        });
    });
});