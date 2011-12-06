/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/30/11
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
var sessionId;
var isApplet = false;
var kotlinVersion;

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

function showContent() {
    $('#loader').hide();
    $(".applet-enable").click();
}

function setKotlinVersion(version) {
    kotlinVersion = version;
    document.getElementById("kotlinVersion").innerHTML = kotlinVersion;
    document.getElementById("kotlinVersionTop").innerHTML = "(" + kotlinVersion + ")";
}