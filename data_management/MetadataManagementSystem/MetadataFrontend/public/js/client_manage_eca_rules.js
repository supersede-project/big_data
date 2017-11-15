/**
 * Created by snadal on 29/05/16.
 */
/*
function removeDataset(dataset) {
    $.ajax({
        url: 'releases/'+(dataset),
        type:'DELETE'
    }).done(function( data ) {
        location.reload();
    });
}
*/
function getRules() {
    $.get("/eca_rule", function(data) {
        var i = 1;
        $.each((data), function(key, value) {
            var theObj = (value);
            $('#rules').find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(theObj.ruleName)
                    ).append($('<td>').append($('<a href="/view_eca_rule?eca_ruleID='+(theObj.eca_ruleID)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    ).append($('<td>').append($('<a href="/eca_rule/' + (theObj.ruleName) + '/generate_config_file">').append($('<span class="glyphicon glyphicon-plus-sign"></span>'))))
                );
            ++i;
        });
    });
}

$(function() {
    getRules();
   /* $(".btn").on('click', function(e) {
        e.defaultPrevented();
        alert("button clicked");
        $.get("/eca_rule/" + $(this).getAttribute("id") + "/generate_config_file", function (data) {
            alert(data);
        });
    });*/
});


