var io = require('socket.io-client');
var EventEmitter = require('events').EventEmitter;
var inherits = require('inherits');
var when = require('when');

var DISCONNECT_TIMEOUT = 5000;

var noop = function(){};

var WebIO = function(opts){
    var that = this;
    EventEmitter.call(this);
    opts = opts || {};
    if(!opts.url){
        throw new Error('url must not be empty');
    }
    this.reconnectionAttempts = opts.reconnectionAttempts || 5;
    this.reconnectionDelay = opts.reconnectionDelay || 2000;
    this.connected = false;
    this.socket = io(opts.url, {
        reconnection: true,
        reconnectionAttempts: this.reconnectionAttempts,
        reconnectionDelay: this.reconnectionDelay,
        autoConnect: false
    });
    this.socket.on('connect', function(){
        that.connected = true;
        that.emit('open');
    });
    this.socket.on('disconnect', function(){
        that.connected = false;
        that.emit('close');
    });
    this.socket.on('data', function(data){
        that.emit('msg', data);
    });

};

inherits(WebIO, EventEmitter);

WebIO.prototype.connect = function(opts){
    var that = this;
    opts = opts || {};
    var promise = when.promise(function(resolve, reject) {
        if (!that.connected) {
            var connectCb = function () {
                resolve();
            };
            that.socket.once('connect', connectCb);
            try {
                that.socket.open();
            } catch (e) {
                reject(e);
            }
        } else {
            resolve();
        }
    });
    promise = promise.timeout(that.reconnectionAttempts * that.reconnectionDelay, 'connect timeout').then(undefined, function(e){
        that.disconnect().done(noop, noop);
    });
    if(opts.success || opts.error){
        opts.success = opts.success || noop;
        opts.error = opts.error || noop;
        promise.done(opts.success, opts.error);
    } else {
        return promise;
    }
};

WebIO.prototype.disconnect = function(opts){
    var that = this;
    opts = opts || {};
    var promise = when.promise(function(resolve, reject) {
        if(that.connected){
            var disconnectCb = function(){
                resolve();
            };
            that.socket.once('disconnect', disconnectCb);
            try {
                that.socket.close();
            } catch (e){
                reject(e);
            }
        } else {
            resolve();
        }
    });
    promise = promise.timeout(DISCONNECT_TIMEOUT, 'disconnect timeout');
    if(opts.success || opts.error){
        opts.success = opts.success || noop;
        opts.error = opts.error || noop;
        promise.done(opts.success, opts.error);
    } else {
        return promise;
    }
};

WebIO.prototype.send = function(opts){
    var that = this;
    opts = opts || {};
    opts.msg = opts.msg || '';
    var promise = when.promise(function(resolve, reject) {
        if(that.connected){
            try {
                that.socket.emit('data', opts.msg);
                resolve();
            } catch (e){
                reject(e);
            }
        } else {
            reject(new Error('not connected'));
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

WebIO.prototype.isConnected = function(){
    return this.connected;
};

module.exports = WebIO;