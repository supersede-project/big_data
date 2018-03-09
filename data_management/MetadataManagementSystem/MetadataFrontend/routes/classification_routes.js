/**
 * Created by snadal on 17/05/16.
 */

var request = require('request'),
    config = require(__dirname+'/../config'),
    request = require('request'),
    fs = require('fs');


exports.classifyFeedback = function (req, res, next) {
    if (!(req.body.hasOwnProperty('feedback')) || req.body.feedback==null){
        res.status(400).json({msg: "(Bad Request) data format: {feedback}"});
    } else {
        var feedbackObj = new Object();
        feedbackObj.feedback = req.body.feedback;

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "classification/feedback/",
            body: JSON.stringify(feedbackObj)
        }, function (error, response, body) {
            if (!error && response.statusCode == 200) {
                res.status(200).json(JSON.parse(body));
            } else {
                res.status(500).send("Error classifying feedback");
            }
        });
    }
};

exports.clusterFeedback = function (req, res, next) {
    if (!(req.body.hasOwnProperty('feedback')) || req.body.feedback==null
        || !(req.body.hasOwnProperty('tenant')) || req.body.tenant == null
        || !(req.body.hasOwnProperty('N')) || req.body.N == null){
        res.status(400).json({msg: "(Bad Request) data format: {feedback, tenant, N}"});
    } else {
        var feedbackObj = new Object();
        feedbackObj.feedback = req.body.feedback;
        feedbackObj.tenant = req.body.tenant;
        feedbackObj.N = req.body.N;

        request.post({
            url: config.METADATA_DATA_LAYER_URL + "clustering/feedback/",
            body: JSON.stringify(feedbackObj)
        }, function (error, response, body) {
            if (!error && response.statusCode == 200) {
                res.status(200).json(JSON.parse(body));
            } else {
                res.status(500).send("Error clustering feedback");
            }
        });
    }
};
