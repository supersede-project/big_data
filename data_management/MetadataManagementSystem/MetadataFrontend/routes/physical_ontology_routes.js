/**
 * Created by snadal on 18/05/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.postPhysicalOntology = function (req, res, next) {
    if (!(req.body.hasOwnProperty('physicalOntologyName')) || req.body.physicalOntologyName==null
       || !(req.body.hasOwnProperty('dataset')) || req.body.dataset==null){
        res.status(400).json({msg: "(Bad Request) data format: {physicalOntologyName, dataset}"});
    } else {
        var graphName = randomstring.generate();
        async.parallel([
                function(callback){
                    var physicalOntology = new Object();
                    physicalOntology.name = req.body.physicalOntologyName;
                    physicalOntology.user = req.user.username;
                    physicalOntology.type = "PHYSICAL_ONTOLOGY";
                    physicalOntology.dataset = req.body.dataset;
                    physicalOntology.graph = config.DEFAULT_NAMESPACE+graphName;
                    request.post({
                        url: config.METADATA_DATA_LAYER_URL + "artifacts/"+req.user.username,
                        body: JSON.stringify(physicalOntology)
                    }, function done(err, results) {
                        callback();
                    });
                },
                function(callback){
                    request.get(config.METADATA_DATA_LAYER_URL + "datasets/"+(req.body.dataset)+"/"+req.user.username, function (error, response, body) {
                        if (!error && response.statusCode == 200) {
                            request.post({
                                url: config.ONTO_MATCH_MERGE_URL + "extraction/"+JSON.parse(body).type+"/PHYSICAL_ONTOLOGY",
                                body: body
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
                }
            ],
            function(err, results){
                res.status(200).json("success");
            });
    }
};
