/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/18/11
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */


function getToken() {
    document.getElementById("help1").innerHTML = editor.getTokenAt(editor.getCursor(true)).start;
    timer = setTimeout(getToken, 500);
}

var timer = setTimeout(getToken, 500);
