function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(window).load(function() {
    $.get("/event/"+getParameterByName("eventID"), function(data) {
        var eventObj = (data);
        $("#event").val(eventObj.event);
        $("#kafkaTopic").val(eventObj.kafkaTopic);
        if (eventObj.dispatch === "true") $("#dispatch").prop('checked', true);
        $("#dispatchTo").val(eventObj.dispatcherPath ? eventObj.dispatcherPath : '-');
        $("#platform").val(eventObj.platform);
        $("input:checkbox").click(function() { return false; });
        var container = document.getElementById("jsoneditor");
        $("#jsoneditor").css("background-color","white");
        var options = {
            "mode": "view",
            "indentation": 2
        };
        var editor = new JSONEditor(container, options);
        editor.set(JSON.parse(eventObj.jsonInstances));
    });
});
