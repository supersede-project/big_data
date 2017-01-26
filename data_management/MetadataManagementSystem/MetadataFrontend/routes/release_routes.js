/**
 * Created by snadal on 7/06/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.getRelease = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "release/"+req.params.releaseID, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving release");
        }
    });
};


    exports.getAllReleases = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "release/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of releases");
        }
    });
};


exports.postRelease = function (req, res, next) {
    if (!(req.body.hasOwnProperty('event')) || req.body.event == null
        || !(req.body.hasOwnProperty('schemaVersion')) || req.body.schemaVersion == null
        || !(req.body.hasOwnProperty('jsonInstances')) || req.body.jsonInstances == null) {
        res.status(400).json({msg: "(Bad Request) data format: {event, schemaVersion, jsonInstances}"});
    } else {
        var release = new Object();
        release.event = req.body.event;
        release.schemaVersion = req.body.schemaVersion;
        release.jsonInstances = req.body.jsonInstances;
        release.dispatch = !(req.body.hasOwnProperty('event')) || req.body.event == null ? false : req.body.dispatch;
        var graphName = config.DEFAULT_NAMESPACE+"SOURCE/"+randomstring.generate();//randomstring.generate();
        release.graph = graphName;
        release.dispatcherStrategy = req.body.dispatcherStrategy;

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "release/",
            body: JSON.stringify(release)
        }, function done(error, response, body) {
            if (!error && response.statusCode == 200) {
                console.log("First POST went fine");

                async.parallel([
                    function(callback){
                        console.log("First callback");
                        var sourceLevel = new Object();
                        sourceLevel.name = release.event + " - " + release.schemaVersion;
                        //sourceLevel.user = req.user.username;
                        sourceLevel.type = "SOURCE";
                        sourceLevel.dataset = release.jsonInstances;
                        sourceLevel.graph = graphName;
                        console.log("Posting object "+JSON.stringify(sourceLevel));
                        request.post({
                            url: config.METADATA_DATA_LAYER_URL + "artifacts/",
                            body: JSON.stringify(sourceLevel)
                        }, function done(err, results) {
                            console.log("First finished "+err+" - "+results);
                            callback();
                        });
                    },
                    function(callback) {
                        console.log("Second callback");
                        console.log("Posting "+JSON.parse(body).rdf);
                        request.post({
                            url: config.METADATA_DATA_LAYER_URL + "artifacts/"+encodeURIComponent(graphName),
                            body: JSON.parse(body).rdf
                        }, function done(err, results) {
                            console.log("Second finished "+err+" - "+results);
                            callback();
                        });
                    }
                ],
                function(err, results){
                    var release_response = new Object();
                    release_response.kafkaTopic = JSON.parse(body).kafkaTopic;
                    res.status(200).json(release_response);
                });
            } else {
                console.log("Error in POST release "+error + " - "+response + " - "+body);
                res.status(500).send("Error retrieving list of artifacts");
            }
        });
    }
};