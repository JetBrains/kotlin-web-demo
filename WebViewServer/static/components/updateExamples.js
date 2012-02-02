/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 1/19/12
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */

//$(document).ready(function () {

function update() {
    //kotlinServer?sessionId=-1&type=updateExamples&args=null
    $.get("kotlinServer?sessionId=-1&type=updateExamples&args=null");

    $.ajax({
        url:generateAjaxUrl("loadExample", "all"),
        context:document.body,
        success:onLoadingExamplesSuccess,
        dataType:"json",
        type:"GET",
        //data:{text:i},
        timeout:10000,
        error:function () {
            setStatusBarMessage(EXAMPLES_REQUEST_ABORTED);
        }
    });

    function onLoadingExamplesSuccess(data) {
        var acc = document.createElement("div");
        acc.id = "accordion";
        var i = 0;
        while (typeof data[i] != "undefined") {
            var lastHeadName;
            if (data[i].type == "folder") {
                var head = document.createElement("h3");
                var headA = document.createElement("a");
                headA.href = "#";

                headA.innerHTML = data[i].text;
                lastHeadName = data[i].text
                head.appendChild(headA);
                acc.appendChild(head);
                var cont = document.createElement("div");
            }
            if (data[i].type == "content") {
                var content = document.createElement("p");
                var span = document.createElement("span");
                span.className = "bullet";
                span.innerHTML = "&#8226;";
                content.appendChild(span);
                var contA = document.createElement("a");
                contA.id = data[i].text;
//            contA.id = i + "&folder=" + lastHeadName;
                contA.style.cursor = "pointer";
                contA.onclick = function (event) {
                    beforeLoadExample(this.id, this.innerHTML);
                };
                contA.innerHTML = data[i].text;
                content.appendChild(contA);
                cont.appendChild(content);
            }
            acc.appendChild(cont);
            i++;
        }
        /*$("#accordion").accordion({
         autoHeight:false,
         navigation:true
         });*/


        document.getElementsByTagName("body")[0].innerHTML = acc.innerHTML;
    }

}

//});