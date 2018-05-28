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

function generateFile(rule) {
    $.get("/cer_rule/" + rule + "/generate_config_file", function (data) {
        var file = new Blob ([data], {type: "text/plain;charset=utf-8"})
        saveAs(file, rule+".ttl");
    });
}

function getRules() {
    $.get("/cer_rule", function(data) {
        var i = 1;
        $.each((data), function(key, value) {
            var theObj = (value);
            console.log(JSON.stringify(value));
            $('#rules').find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(theObj.ruleName)
                    ).append($('<td>').append($('<a href="/view_cer_rule?cer_ruleID='+(theObj.cer_ruleID)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    ).append($('<td>').append($('<btn onclick="generateFile(\'' + encodeURIComponent(theObj.graph) +'\')"> </btn>').append($('<span class="glyphicon glyphicon-plus-sign"></span>'))))
                    ).append($('<td id="'+(theObj.cer_ruleID)+'">').append($('<a class="delete" href="#">').append($('<span class="glyphicon glyphicon-trash"></span>')))

            );
            ++i;
        });
        $(".delete").click(function () {
            var id = $(this).parent().attr("id");
            $.ajax({
                url: '/cer_rule/'+id,
                type: 'DELETE',
                success: function(result) {
                    location.reload();
                }
            });
        });
    });
}

$(function() {
    getRules();
});


