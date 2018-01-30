/**
 * Created by snadal on 7/06/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.getCerRule = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "cer_rule/"+req.params.cer_ruleID, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving CER Rule");
        }
    });
};

exports.getAllCerRules = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "cer_rule/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of CER rules");
        }
    });
};

exports.postCerRule = function (req, res, next) {
   if (!(req.body.hasOwnProperty('ruleName')) || req.body.ruleName == null
        || !(req.body.hasOwnProperty('pattern')) || req.body.pattern == null
        || !(req.body.hasOwnProperty('condition')) || req.body.condition == null
        || !(req.body.hasOwnProperty('filters')) || req.body.filters == null
        || !(req.body.hasOwnProperty('action')) || req.body.action == null
        || !(req.body.hasOwnProperty('windowTime')) || req.body.windowTime == null
        || !(req.body.hasOwnProperty('windowSize')) || req.body.windowSize == null) {
        res.status(400).json({msg: "(Bad Request) data format: ruleName, pattern, filters, action, windowTime, windowSize}"});
   }
   else {
        var rule = new Object();
        rule.ruleName = req.body.ruleName;
        rule.pattern = req.body.pattern;
        rule.condition = req.body.condition;
        rule.filters = req.body.filters;
        rule.action = req.body.action;
        rule.windowTime = req.body.windowTime;
        rule.windowSize = req.body.windowSize;
        rule.type = "RULES";
        rule.graph = config.DEFAULT_NAMESPACE+"RULE/"+randomstring.generate();

        console.log("Posting "+JSON.stringify(rule));

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "cer_rule/",
            body: JSON.stringify(rule)
        }, function done(error, response, body) {
            console.log("Got response "+error+" - "+response+" - "+body);
            if (!error && response.statusCode == 200) {
                res.status(200).json("ok");
            } else {
                res.status(500).send("Error posting rule");
            }
        });
    }
};

exports.directGeneration = function (req, res, next) {
    if (!(req.body.hasOwnProperty('ruleName')) || req.body.ruleName == null
        || !(req.body.hasOwnProperty('pattern')) || req.body.pattern == null
        //|| !(req.body.hasOwnProperty('condition')) || req.body.condition == null
        || !(req.body.hasOwnProperty('filters')) || req.body.filters == null
        || !(req.body.hasOwnProperty('windowTime')) || req.body.windowTime == null
        || !(req.body.hasOwnProperty('windowSize')) || req.body.windowSize == null) {
        res.status(400).json({msg: "(Bad Request) data format: ruleName, pattern, filters, windowTime, windowSize}"});
    }
    else {
        var rule = new Object();
        rule.ruleName = req.body.ruleName;
        rule.pattern = req.body.pattern;
        //rule.condition = req.body.condition;
        rule.filters = req.body.filters;
        //rule.actionName = req.body.actionName;
        rule.actionType = req.body.actionType;
        rule.actionParameters = req.body.actionParameters;
        rule.windowTime = req.body.windowTime;
        rule.windowSize = req.body.windowSize;
        rule.type = "RULES";
        rule.graph = config.DEFAULT_NAMESPACE+"RULE/"+randomstring.generate();

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "cer_rule/directGeneration",
            body: JSON.stringify(rule)
        }, function done(error, response, body) {
            console.log("Got response "+error+" - "+response+" - "+body);
            if (!error && response.statusCode == 200) {
                res.status(200).json("ok");
            } else {
                res.status(500).send("Error posting rule");
            }
        });
    }
};


exports.generateConfigFile = function (req, res) {
    request.get(config.METADATA_DATA_LAYER_URL + "cer_rule/" + req.params.ruleName + "/generate_config_file/", function(error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving CER Rule configuration file");
        }
    })
}