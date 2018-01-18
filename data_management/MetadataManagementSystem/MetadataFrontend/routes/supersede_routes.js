/**
 * Created by snadal on 18/05/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request');

exports.getPlatforms = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "supersede/platforms", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of platforms");
        }
    });
};

