/**
 * Created by snadal on 18/05/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.getBDIOntology = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "bdi_ontology/"+req.params.bdi_ontologyID, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving BDI Ontology");
        }
    });
};

exports.getAllBDIOntologies = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "bdi_ontology/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of BDI Ontologies");
        }
    });
};

exports.postBDIOntology = function (req, res, next) {
    if (!(req.body.hasOwnProperty('name')) || req.body.name==null
       || !(req.body.hasOwnProperty('releases')) || req.body.releases==null){
        res.status(400).json({msg: "(Bad Request) data format: {name, releases}"});
    } else {
        async.parallel([
                function(callback){
                    request.post({
                        url: config.METADATA_DATA_LAYER_URL + "bdi_ontology/",
                        body: JSON.stringify(req.body)
                    }, function done(err, results) {
                        callback();
                    });
                }
            ],
            function(err, results){
                res.status(200).json("success");
            });
    }
};