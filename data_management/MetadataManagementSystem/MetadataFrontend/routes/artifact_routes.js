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
    request.get(config.METADATA_DATA_LAYER_URL + "artifacts/"+req.params.artifactType+"/"+encodeURIComponent(req.params.artifactID), function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving artifact");
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