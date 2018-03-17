/**
 * Created by snadal on 7/06/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.getEventAttributes = function(req, res) {
    request.get(config.METADATA_DATA_LAYER_URL + "event/"+encodeURIComponent(req.params.graph)+"/attributes", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json((body));
        } else {
            res.status(500).send("Error retrieving event's attributes");
        }
    });
};

exports.getEvent = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "event/"+req.params.eventID, function (error, response, body) {
        console.log(body);
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving event");
        }
    });
};

exports.getAllEvents = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "event/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of events");
        }
    });
};


exports.postEvent = function (req, res, next) {
    if (!(req.body.hasOwnProperty('event')) || req.body.event == null
        || !(req.body.hasOwnProperty('type')) || req.body.type == null
        || !(req.body.hasOwnProperty('jsonInstances')) || req.body.jsonInstances == null
        || !(req.body.hasOwnProperty('platform')) || req.body.platform == null
        || !(req.body.hasOwnProperty('tenant')) || req.body.tenant == null) {
        res.status(400).json({msg: "(Bad Request) data format: {event, jsonInstances, platform, tenant}"});
    } else {
        var event = new Object();
        event.event = req.body.event;
        event.type = req.body.type;
        event.jsonInstances = req.body.jsonInstances;
        event.platform = req.body.platform;
        event.tenant = req.body.tenant;

        if (!req.body.kafkaTopic) event.kafkaTopic = "";
        else event.kafkaTopic = req.body.kafkaTopic;

        event.dispatch = !(req.body.hasOwnProperty('event')) || req.body.event == null ? false : req.body.dispatch;
        var graphName = config.DEFAULT_NAMESPACE+/*"Event/"+*/randomstring.generate();
        event.graph = graphName;
        event.dispatcherStrategy = req.body.dispatcherStrategy;

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "event/",
            body: JSON.stringify(event)
        }, function done(error, response, body) {
            if (!error && response.statusCode == 200) {
                async.parallel([
                    function(callback){
                        var EventOntology = new Object();
                        EventOntology.name = event.event;
                        //sourceLevel.user = req.user.username;
                        EventOntology.type = "Event";
                        EventOntology.dataset = event.jsonInstances;
                        EventOntology.graph = graphName;
                        request.post({
                            url: config.METADATA_DATA_LAYER_URL + "artifacts/",
                            body: JSON.stringify(EventOntology)
                        }, function done(err, results) {
                            callback();
                        });
                    },
                    function(callback) {
                        request.post({
                            url: config.METADATA_DATA_LAYER_URL + "artifacts/"+encodeURIComponent(graphName),
                            body: JSON.parse(body).rdf
                        }, function done(err, results) {
                            callback();
                        });
                    }
                ],
                function(err, results){
                    var event_response = new Object();
                    event_response.kafkaTopic = JSON.parse(body).kafkaTopic;
                    res.status(200).json(event_response);
                });
            } else {
                res.status(500).send("Error retrieving list of artifacts");
            }
        });
    }
};