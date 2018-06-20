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
                        .text(theObj.name)
                    ).append($('<td>').append($('<a href="/view_eca_rule?eca_ruleID='+(theObj.eca_ruleID)+'">').append($('<span class="glyphicon glyphicon-search"></span>')))
                    ).append($('<td id="'+(theObj.eca_ruleID)+'">').append($('<a class="delete" href="#">').append($('<span class="glyphicon glyphicon-trash"></span>')))

                    )

                );
            ++i;
        });

        $(".delete").click(function () {
            var id = $(this).parent().attr("id");
            $.ajax({
                url: '/eca_rule/'+id,
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