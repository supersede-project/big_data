/**
 * Created by snadal on 18/05/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.postClonedLogicalOntology = function (req, res, next) {
    if (!(req.body.hasOwnProperty('logicalOntologyName')) || req.body.logicalOntologyName==null
       || !(req.body.hasOwnProperty('physicalOntology')) || req.body.physicalOntology==null){
        res.status(400).json({msg: "(Bad Request) data format: {logicalOntologyName, physicalOntology}"});
    } else {
        var graphName = randomstring.generate();
        async.parallel([
                function(callback){
                    var logicalOntology = new Object();
                    logicalOntology.name = req.body.logicalOntologyName;
                    logicalOntology.user = req.user.username;
                    logicalOntology.type = "LOGICAL_ONTOLOGY";
                    logicalOntology.clonedFromPhysical = true;
                    logicalOntology.physical_ontology = req.body.physicalOntology;
                    logicalOntology.graph = config.DEFAULT_NAMESPACE+graphName;
                    request.post({
                        url: config.METADATA_DATA_LAYER_URL + "artifacts/"+req.user.username,
                        body: JSON.stringify(logicalOntology)
                    }, function done(err, results) {
                        callback();
                    });
                },
                function(callback){
                    request.get(config.METADATA_DATA_LAYER_URL + "artifacts/PHYSICAL_ONTOLOGY/"+encodeURIComponent(req.body.physicalOntology)+"/"+req.user.username, function (error1, response1, body1) {
                        if (!error1 && response1.statusCode == 200) {
                            request.get(config.METADATA_DATA_LAYER_URL + "datasets/"+(JSON.parse(body1).dataset)+"/"+req.user.username, function (error2, response2, body2) {
                                if (!error2 && response2.statusCode == 200) {
                                    request.post({
                                        url: config.ONTO_MATCH_MERGE_URL + "extraction/"+JSON.parse(body2).type+"/LOGICAL_ONTOLOGY",
                                        body: body2
                                    }, function done(err, results) {
                                        request.post({
                                            url: config.METADATA_DATA_LAYER_URL + "artifacts/"+encodeURIComponent(config.DEFAULT_NAMESPACE+graphName)+"/"+req.user.username,
                                            body: results.body
                                        }, function done(err, results) {
                                            callback();
                                        });
                                    });
                                } else {
                                    callback();
                                }
                            });
                        } else {
                            callback();
                        }

                    });

                }
            ],
            function(err, results){
                res.status(200).json("success");
            });
    }
};
