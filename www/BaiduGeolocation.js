var cordova = require('cordova');

var SERVICE_NAME = 'BaiduGeolocation';
var ACTION_GET_CURRENT_POSITION = 'getCurrentPosition';
var ACTION_WATCH_POSITION = 'watchPosition';
var ACTION_CLEAR_WATCH = 'clearWatch';

var geolocation = module.exports = {};

geolocation.getCurrentPosition = function (success, error, options) {
  if (typeof success === 'object') {
    options = success;
  }
  if (typeof error === 'object') {
    options = error;
  }

  cordova.exec(function(args) {
    success.apply(null, args);
  }, error, SERVICE_NAME, ACTION_GET_CURRENT_POSITION, [options]);
};

geolocation.watchPosition = function (success, error, options) {
  cordova.exec(function(args) {
    success.apply(null, args);
  }, error, SERVICE_NAME, ACTION_WATCH_POSITION, [options]);
};

geolocation.clearWatch = function () {
  cordova.exec(null, null, SERVICE_NAME, ACTION_CLEAR_WATCH);
};
