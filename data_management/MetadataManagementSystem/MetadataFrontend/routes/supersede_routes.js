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

exports.getTenants = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "supersede/tenants", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of tenants");
        }
    });
};

exports.feedbackReconfiguration = function (req, res, next) {
    if (!(req.body.hasOwnProperty('applicationId')) || req.body.applicationId == null
        || !(req.body.hasOwnProperty('configurationId')) || req.body.configurationId == null
        || !(req.body.hasOwnProperty('tenant')) || req.body.tenant == null) {
        res.status(400).json({msg: "(Bad Request) data format: applicationId, configurationId, tenant}"});
    }
    var reconf = new Object();
    reconf.applicationId = req.body.applicationId;
    reconf.configurationId = req.body.configurationId;
    reconf.tenant = req.body.tenant;
    $.ajax({
        url: '/supersede/feedbackReconfiguration',
        type: 'POST',
        data: reconf
    }).done(function() {
        res.status(200).json("Success");
    }).fail(function(err) {
        res.status(500).json(err);
    });
};