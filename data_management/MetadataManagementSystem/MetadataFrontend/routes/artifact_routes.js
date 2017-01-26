/**
 * Created by snadal on 7/06/16.
 */
var fs = require('fs'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    randomstring = require("randomstring"),
    async = require('async');

exports.postArtifact = function (req, res, next) {

};

exports.getArtifacts = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "artifacts/"+req.params.artifactType, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving list of artifacts");
        }
    });
};

exports.getArtifact = function (req, res, next) {
    console.log("In GET a single artifact metadata");
    console.log("METADATA_URL : "+config.METADATA_DATA_LAYER_URL);
    console.log("artifactType : "+req.params.artifactType);
    console.log("artifactID : "+req.params.artifactID);
    console.log("encodeURIComponent(req.params.artifactID) : "+encodeURIComponent(req.params.artifactID));
    console.log("Full GET "+config.METADATA_DATA_LAYER_URL + "artifacts/"+req.params.artifactType+"/"+encodeURIComponent(req.params.artifactID));
    request.get(config.METADATA_DATA_LAYER_URL + "artifacts/"+req.params.artifactType+"/"+encodeURIComponent(req.params.artifactID), function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            console.log("Got error");
            console.log(error);
            console.log(response);
            console.log(JSON.stringify(response));
            console.log(body);
            console.log(JSON.stringify(body));
            res.status(500).send("Error retrieving artifact. Error "+error+". Response "+JSON.stringify(response)+". Body "+body);
        }
    });
};

exports.getArtifactContent = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "artifacts/"+req.params.artifactType+"/"+encodeURIComponent(req.params.artifactID)+"/content", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json((body));
        } else {
            res.status(500).send("Error retrieving artifact content");
        }
    });
};

exports.getArtifactGraphical = function (req, res, next) {
    request.get(config.METADATA_DATA_LAYER_URL + "artifacts/"+req.params.artifactType+"/"+encodeURIComponent(req.params.artifactID)+"/graphical", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving graphical representation of artifact");
        }
    });
};

exports.deleteArtifact = function (req, res, next) {
    request.delete(config.METADATA_DATA_LAYER_URL + "artifacts/"+req.params.artifactType+"/"+encodeURIComponent(req.params.artifactID), function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json("ok");
        } else {
            res.status(500).send("Error deleting artifact");
        }
    });
};

exports.postTriple = function (req, res, next) {
    request.post({
        url: config.METADATA_DATA_LAYER_URL + "artifacts/"+encodeURIComponent(req.params.artifactID)+"/triple/"+
            encodeURIComponent(req.body.s) + "/" + encodeURIComponent(req.body.p) + "/" + encodeURIComponent(req.body.o)
    }, function done(err, results) {
        res.status(200).json("ok");
    });
};

exports.postGraphicalGraph = function (req, res, next) {
    request.post({
        url: config.METADATA_DATA_LAYER_URL + "artifacts/"+encodeURIComponent(req.params.artifactID)+"/graphicalGraph",
        body: JSON.stringify(req.body.graphicalGraph)
    }, function done(err, results) {
        res.status(200).json("ok");
    });
};