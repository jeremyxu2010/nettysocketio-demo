var webAPI = require('./WebAPI');
var $ = require('jquery');

var sendPingTimeoutId = null;
webAPI.on('close', function(){
    if(sendPingTimeoutId){
        clearInterval(sendPingTimeoutId);
    }
});
webAPI.on('pong', function(msg){
    $('#output1').append('<span>pong : ' + msg + '</span><br/>');
});
sendPingTimeoutId = setInterval(function(){
    var msg = '' + new Date();
    webAPI.ping({'msg' : msg}).done(function(){
        $('#output1').append('<span>ping : ' + msg + '</span><br/>');
    });
}, 1000);



$('#output2').append('<span>request : /action/path1 {xxxx: 1111, yyyy: 2222}</span><br/>');
webAPI.request({
    path: '/action/path1',
    data : {xxxx: 1111, yyyy: 2222}
}).done(function(resp){
    $('#output2').append('<span> response : ' + JSON.stringify(resp) + '</span><br/>');
}, function(error){
    $('#output2').append('<span> request error </span><br/>');
});

var btn = $('<button>fire user changed event</button><br/>');
btn.on('click', function(){
    webAPI.operate({
        path: '/action/fireGlobalEvent',
        data: {xxxx: 1111, yyyy: 2222}
    }).done(function(){
        $('#output3').append('<span> operate success </span><br/>')
    }, function(error){
        $('#output3').append('<span> operate error </span><br/>');
    });
});
$('#output3').append(btn);
webAPI.addListener('globalEvent', function(eventParams){
    $('#output3').append('<span>globalEvent event occurs, event params : ' + JSON.stringify(eventParams) + '</span><br/>');
});









