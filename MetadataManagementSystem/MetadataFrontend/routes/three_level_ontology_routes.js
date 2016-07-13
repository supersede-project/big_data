/**
 * Created by snadal on 18/05/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async'),
    uuid = require('uuid');

exports.postThreeLevelOntology = function (req, res, next) {
    if (!(req.body.hasOwnProperty('threeLevelOntologyName')) || req.body.threeLevelOntologyName==null
       || !(req.body.hasOwnProperty('domainOntology')) || req.body.domainOntology==null
       || !(req.body.hasOwnProperty('logicalOntology')) || req.body.logicalOntology==null
       || !(req.body.hasOwnProperty('physicalOntology')) || req.body.physicalOntology==null){
        res.status(400).json({msg: "(Bad Request) data format: {threeLevelOntologyName, domainOntology, logicalOntology, physicalOntology}"});
    } else {
        var graphName = randomstring.generate();
        async.parallel([
                function(callback){
                    var threeLevelOntology = new Object();
                    threeLevelOntology.threeLevelOntologyID = uuid.v4();
                    threeLevelOntology.name = req.body.threeLevelOntologyName;
                    threeLevelOntology.user = req.user.username;
                    threeLevelOntology.type = "THREE_LEVEL_ONTOLOGY";
                    threeLevelOntology.domainOntology = req.body.domainOntology;
                    threeLevelOntology.logicalOntology = req.body.logicalOntology;
                    threeLevelOntology.physicalOntology = req.body.physicalOntology;
                    request.post({
                        url: config.METADATA_DATA_LAYER_URL + "artifacts/"+req.user.username,
                        body: JSON.stringify(threeLevelOntology)
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
