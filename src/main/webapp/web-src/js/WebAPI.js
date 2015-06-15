var WebIO = require('./WebIO');
var when = require('when');
var EventEmitter = require('events').EventEmitter;
var inherits = require('inherits');
var queryString = require('query-string');

var webIO = new WebIO({
    url: window.location.hostname + ':8888'
});

//TODO webtoken should come form cookie or localStorage
var getWebToken = function(){
    var search = window.location.search;
    if(search && search.length > 0 && search.indexOf('?') > -1){
        var params = queryString.parse(search.substring(search.indexOf('?')));
        return params.userId;
    } else {
        return null;
    }
};

var webToken = getWebToken();
var webTokenHasSent = false;

var msgIdCounter = 0;

var REQ_TIMEOUT = 1000 * 60 * 5;

var reqCb = {};

var noop = function(){};

var nextMsgId = function(){
    msgIdCounter = (msgIdCounter + 1) % (Number.MAX_VALUE - 1);
    return msgIdCounter;

};

var MSG_TYPE = {
    REQ_MSG : 0,
    RES_MSG : 1,
    EVENT_MSG :2,
    PING_MSG : 3,
    PONG_MSG : 4,
    OP_MSG: 5
};

var WebAPI = function(){
    EventEmitter.call(this);
};

inherits(WebAPI, EventEmitter);

WebAPI.prototype.request = function(opts){
    opts = opts || {};
    if(!opts.path){
        throw new Error('path must not be empty');
    }
    opts.data = opts.data || {};
    var reqId = nextMsgId();
    var promise = when.promise(function(resolve, reject) {
        webIO.connect().done(function(){
            var sendWebTokenMsg = function(){
                var msg = {
                    type: MSG_TYPE.OP_MSG,
                    body: {
                        opPath: '/auth/sendWebToken',
                        opParams: {
                            webToken: webToken
                        }
                    }
                };
                return webIO.send({
                    msg: JSON.stringify(msg)
                });
            };
            var sendReqMsg = function(){
                var msg = {
                    type: MSG_TYPE.REQ_MSG,
                    reqId: reqId,
                    body: {
                        reqPath: opts.path,
                        reqParams: opts.data
                    }
                };
                return webIO.send({
                    msg: JSON.stringify(msg)
                });
            };
            if((!webTokenHasSent && webToken)) {
                sendWebTokenMsg().done(function () {
                    webTokenHasSent = true;
                    reqCb['' + reqId] = {'resovle': resolve, 'reject': reject};
                    sendReqMsg().done(noop, reject);
                }, reject);
            } else {
                reqCb['' + reqId] = {'resovle': resolve, 'reject': reject};
                sendReqMsg().done(noop, reject);
            }
        }, reject);
    });
    promise = promise.timeout(REQ_TIMEOUT, 'request timeout').then(undefined, function(e){
        if (reqCb['' + reqId]) {
            delete reqCb['' + reqId];
        }
    });
    if(opts.success || opts.error){
        opts.success = opts.success || noop;
        opts.error = opts.error || noop;
        promise.done(opts.success, opts.error);
    } else {
        return promise;
    }
};

WebAPI.prototype.operate = function(opts){
    opts = opts || {};
    if(!opts.path){
        throw new Error('path must not be empty');
    }
    opts.data = opts.data || {};
    var promise = when.promise(function(resolve, reject) {
        webIO.connect().done(function(){
            var sendWebTokenMsg = function(){
                var msg = {
                    type: MSG_TYPE.OP_MSG,
                    body: {
                        opPath: '/auth/sendWebToken',
                        opParams: {
                            webToken: webToken
                        }
                    }
                };
                return webIO.send({
                    msg: JSON.stringify(msg)
                });
            };
            var sendOpMsg = function(){
                var msg = {
                    type: MSG_TYPE.OP_MSG,
                    body: {
                        opPath: opts.path,
                        opParams: opts.data
                    }
                };
                return webIO.send({
                    msg: JSON.stringify(msg)
                });
            };
            if((!webTokenHasSent && webToken)) {
                sendWebTokenMsg().done(function () {
                    webTokenHasSent = true;
                    sendOpMsg().done(resolve, reject);
                }, reject);
            } else {
                sendOpMsg().done(resolve, reject);
            }
        }, reject);
    });
    if(opts.success || opts.error){
        opts.success = opts.success || noop;
        opts.error = opts.error || noop;
        promise.done(opts.success, opts.error);
    } else {
        return promise;
    }
};

WebAPI.prototype.ping = function(opts){
    opts = opts || {};
    opts.msg = opts.msg || '';
    var promise = when.promise(function(resolve, reject) {
        webIO.connect().done(function(){
            var sendWebTokenMsg = function(){
                var msg = {
                    type: MSG_TYPE.OP_MSG,
                    body: {
                        opPath: '/auth/sendWebToken',
                        opParams: {
                            webToken: webToken
                        }
                    }
                };
                return webIO.send({
                    msg: JSON.stringify(msg)
                });
            };
            var sendPingMsg = function(){
                var msg = {
                    type: MSG_TYPE.PING_MSG,
                    body: opts.msg
                };
                return webIO.send({
                    msg: JSON.stringify(msg)
                });
            };
            if((!webTokenHasSent && webToken)){
                sendWebTokenMsg().done(function(){
                    webTokenHasSent = true;
                    sendPingMsg().done(resolve, reject);
                },reject);
            } else {
                sendPingMsg().done(resolve, reject);
            }
        }, reject);
    });
    if(opts.success || opts.error){
        opts.success = opts.success || noop;
        opts.error = opts.error || noop;
        promise.done(opts.success, opts.error);
    } else {
        return promise;
    }
};

var webAPI = new WebAPI();

webIO.on('open', function(){
    webAPI.emit('open');
});

webIO.on('close', function(){
    webAPI.emit('close');
});

webIO.on('msg', function(data){
    var msg = JSON.parse(data);
    var msgType = msg.type;
    if(msgType === MSG_TYPE.RES_MSG){
        var reqId = msg.reqId;
        var resp = msg.body;
        if(reqCb['' + reqId]){
            reqCb['' + reqId].resovle(resp);
            delete reqCb['' + reqId];
        }
    } else if(msgType === MSG_TYPE.EVENT_MSG){
        var event = msg.body;
        var eventName = event.eventName;
        var eventParams = event.eventParams;
        webAPI.emit(eventName, eventParams)
    } else if(msgType === MSG_TYPE.PONG_MSG){
        var msg = msg.body;
        webAPI.emit('pong', msg);
    }
});

module.exports = webAPI;