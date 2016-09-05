var passport = require('passport'),
    bcrypt = require('bcryptjs'),
    LocalStrategy = require('passport-local').Strategy,
    _ = require('underscore');

var list =
    [
        { user: '$2a$10$C9bRx/1OFqe6.MUzQyFMB.4XmI2Xy5Q8qz.bnqt5RXLh6NpCFIdDq', password: '$2a$10$znfSV3zQvvfuA0EnO9JCfOqBTxYeLTHkqYo5HeJAzuJ4GmrmTRglu', username: 'Admin'},
        { user: '$2a$10$UxI/Bt2s1YqelB6ZbbuTh.BiG5lEYqFPBzeZHExFfx0B9LOkwza/q', password: '$2a$10$7YFnE0DvJwqr06S3dpJ7HOecPpuXUfsoq4fVo9ZAFy5QtsZpNxfCa', username: 'AtoS'},
        { user: '$2a$10$NilMpFc2BskJjCWcm3ULTuIxflwi6nMqdFDXypLESZ.qL2mBNJeWm', password: '$2a$10$ojZapaDcomxz14n1IDvN.uKVDhfd5gO24GfHto5sThgA7xItVA24W', username: 'SEnerCon'},
        { user: '$2a$10$js8b8kfo.Wi8cmZx.AIh6ebd5/mcTJPhAJ4LenZ9z5Ko1hoDC0G1S', password: '$2a$10$24uFBTw.ZqQ9JWd.fSFJluxGdOi7F.8VWc8dZGqLiIjjmk6x6Wtja', username: 'SIEMENS'}
    ];

passport.use(new LocalStrategy(
    function(username, password, done) {
        _.each(list, function(uspw) {
            if (bcrypt.compareSync(username, uspw.user) && bcrypt.compareSync(password, uspw.password))
                return done(null, uspw.username);
        });
        return done(null, false, {message: 'Invalid username or password'});
    }
));

passport.serializeUser(function(user, done) {
    done(null, user);
});

passport.deserializeUser(function(user, done) {
    done(null, user);
});