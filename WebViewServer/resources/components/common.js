/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/30/11
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
var sessionId;
var isApplet = true;

function setMode(mode) {
    if (mode == "APPLET") {
        isApplet = true;
    }
}

function setSessionId(id) {
    sessionId = id;
    var data = "browser: " + navigator.appName + " " + navigator.appVersion;
    data += " " + "system: " + navigator.platform;
    $.ajax({
        url:document.location.href + "?sessionId=" + id + "&userData=true",
        context:document.body,
        type:"POST",
        data:{text:data},
        timeout:5000
    });
}