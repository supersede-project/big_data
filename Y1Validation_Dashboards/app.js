var port = 80;
var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var methodOverride = require('method-override');
var session = require('express-session');
var app = express();
var io = require('socket.io').listen(app.listen(port));
var flash = require('express-flash');
var multer = require('multer');
var passport = require('passport');
var request = require('request');


//var atos = require('./routes/atos');


/*****************************************************************************************/
/*****************************************************************************************/
/*          Server configuration                                                         */
/*****************************************************************************************/
/*****************************************************************************************/
// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');
app.use(logger('dev'));
app.use(methodOverride());
app.use(session({ secret: 'uwotm8', proxy: true, resave: true, saveUninitialized: true }));
app.use(bodyParser.urlencoded({extended: true}));
app.use(cookieParser());
app.use(bodyParser.json());                          // parse application/json
app.use(bodyParser.urlencoded({ extended: true }));  // parse application/x-www-form-urlencoded
app.use(express.static(path.join(__dirname, 'public')));

app.use(flash());
app.use(passport.initialize());
app.use(passport.session());

require('./auth');

/*****************************************************************************************/
/*****************************************************************************************/
/*          Generic                                                                      */
/*****************************************************************************************/
/*****************************************************************************************/

app.get('/', checkAuthenticated, function(req,res) {
    res.render('index', {username:req.session.passport.user});
});

app.get('/login', function (req, res) {
    res.setHeader('Last-Modified', (new Date()).toUTCString());
    res.render('login', {message: req.flash('error')});
});

app.post('/login', passport.authenticate('local', {
    successRedirect: '/',
    failureRedirect: '/login',
    failureFlash: true
}));

app.get('/logout', function(req, res){
    req.logout();
    res.redirect('/');
});

/*****************************************************************************************/
/*****************************************************************************************/
/*          AtoS Messages                                                                */
/*****************************************************************************************/
/*****************************************************************************************/

app.get('/atos', checkAuthenticated, function(req,res) {
    res.render('atos', {username:req.session.passport.user});
});

app.post('/atos/distinctDevices', function(req, res){
    io.of('/atos_rt').emit('/distinct_users',{message:req.body});
    res.json(true);
});

app.post('/atos/fullMessage', function(req, res){
    io.of('/atos_rt').emit('/fullMessage',{message:req.body});
    res.json(true);
});

app.post('/atos/playbackErrorVideo', function(req, res){
    io.of('/atos_rt').emit('/playbackErrorVideo',{message:req.body});
    res.json(true);
});

app.post('/atos/rebuffering', function(req, res){
    io.of('/atos_rt').emit('/rebuffering',{message:req.body});
    res.json(true);
});

app.post('/atos/meanSyncAttempt', function(req, res){
    io.of('/atos_rt').emit('/meanSyncAttempt',{message:req.body});
    res.json(true);
});

app.post('/atos/stDevSyncAttempt', function(req, res){
    io.of('/atos_rt').emit('/stDevSyncAttempt',{message:req.body});
    res.json(true);
});

/*****************************************************************************************/
/*****************************************************************************************/
/*          SIEMENS Messages                                                             */
/*****************************************************************************************/
/*****************************************************************************************/

app.get('/siemens/summary', checkAuthenticated, function(req,res) {
    res.render('siemens', {username:req.session.passport.user});
});

app.get('/siemens/plots/allAPIs', checkAuthenticated, function(req,res) {
    res.render('siemensPlotsAllAPIs', {username:req.session.passport.user});
});

app.get('/siemens/plots/breakdownAPIs', checkAuthenticated, function(req,res) {
    res.render('siemensPlotsBreakdownAPIs', {username:req.session.passport.user});
});

app.post('/siemens/full_message', function(req, res){
    io.of('/siemens_rt').emit('/full_message',{message:req.body});
    res.json(true);
});

app.post('/siemens/total_api_calls', function(req, res){
    io.of('/siemens_rt').emit('/total_api_calls',{message:req.body});
    res.json(true);
});

app.post('/siemens/total_successful_api_calls', function(req, res){
    io.of('/siemens_rt').emit('/total_successful_api_calls',{message:req.body});
    res.json(true);
});

app.post('/siemens/total_unsuccessful_api_calls', function(req, res){
    io.of('/siemens_rt').emit('/total_unsuccessful_api_calls',{message:req.body});
    res.json(true);
});

app.post('/siemens/breakdown_per_api', function(req, res){
    io.of('/siemens_rt').emit('/breakdown_per_api',{message:req.body});
    res.json(true);
});

app.post('/siemens/plot_all_APIs', function(req, res){
    io.of('/siemens_rt').emit('/plot_all_APIs',{message:req.body});
    res.json(true);
});

/*****************************************************************************************/
/*****************************************************************************************/
/*          SENERCON Messages                                                            */
/*****************************************************************************************/
/*****************************************************************************************/

app.get('/senercon', checkAuthenticated, function(req,res) {
    res.render('senercon', {username:req.session.passport.user});
});

app.get('/senercon/error_statistics', checkAuthenticated, function(req,res) {
    request.get("http://localhost:8081/senercon/error_statistics", function (error, response, body) {
        res.json(body);
    })
});

app.get('/senercon/error_statistics/csv', checkAuthenticated, function(req,res) {
    request.get("http://localhost:8081/senercon/error_statistics/csv", function (error, response, body) {
        res.end(body);
    })
});


function checkAuthenticated(req, res, next) {
    if (req.isAuthenticated()) { return next(); }
    res.redirect('/login');
}

module.exports = app;
