/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/10/11
 * Time: 12:12 PM
 * To change this template use File | Settings | File Templates.
 */

(function () {
    var isLeftSideShow = true;
    var isRightSideShow = true;

    /*$("#hideLeft").click(function () {
     $("#examplesaccordion").toggle();
     isLeftSideShow = !isLeftSideShow;
     if (isLeftSideShow) {
     document.getElementById("left").style.width = "220px";
     if (isRightSideShow) {
     document.getElementById("center").style.width = "60%";
     document.getElementById("right").style.marginLeft = "60%";
     } else {
     document.getElementById("center").style.width = "80%";
     document.getElementById("right").style.marginLeft = "80%";
     }
     } else {
     document.getElementById("left").style.width = "20px";
     if (isRightSideShow) {
     document.getElementById("center").style.width = "80%";
     document.getElementById("right").style.marginLeft = "80%";
     } else {
     document.getElementById("center").style.width = "95%";
     document.getElementById("right").style.marginLeft = "95%";
     }
     }
     });

     $("#hideRight").click(function () {
     $("#helpcontent").toggle();
     isRightSideShow = !isRightSideShow;
     if (isRightSideShow) {
     document.getElementById("right").style.marginLeft = "60%";
     if (isLeftSideShow) {
     document.getElementById("center").style.width = "60%";

     } else {
     document.getElementById("center").style.width = "80%";
     document.getElementById("right").style.marginLeft = "80%";
     }
     } else {
     //                document.getElementById("right").style.width = "20px";
     if (isLeftSideShow) {
     document.getElementById("center").style.width = "80%";
     document.getElementById("right").style.marginLeft = "80%";
     } else {
     document.getElementById("center").style.width = "95%";
     document.getElementById("right").style.marginLeft = "95%";
     }
     }
     });*/

    /*$("#hideLeft").click(function () {
     $("#examplesaccordion").toggle();
     isLeftSideShow = !isLeftSideShow;
     if (isLeftSideShow) {
     document.getElementById("left").style.width = "19%";
     if (isRightSideShow) {
     document.getElementById("center").style.width = "60%";
     } else {
     document.getElementById("center").style.width = "75%";
     }
     } else {
     document.getElementById("left").style.width = "20px";
     if (isRightSideShow) {
     document.getElementById("center").style.width = "75%";
     } else {
     document.getElementById("center").style.width = "95%";
     }
     }
     });

     $("#hideRight").click(function () {
     $("#helpcontent").toggle();
     isRightSideShow = !isRightSideShow;
     if (isRightSideShow) {
     document.getElementById("right").style.width = "19%";
     if (isLeftSideShow) {
     document.getElementById("center").style.width = "60%";
     } else {
     document.getElementById("center").style.width = "75%";
     }
     } else {
     document.getElementById("right").style.width = "20px";
     if (isLeftSideShow) {
     document.getElementById("center").style.width = "75%";
     } else {
     document.getElementById("center").style.width = "95%";
     }
     }
     });*/

    $("#hideLeft").click(function () {
        $("#examplesaccordion").toggle();
        isLeftSideShow = !isLeftSideShow;
        if (isLeftSideShow) {
            document.getElementById("left").style.width = "220px";
            if (isRightSideShow) {
                document.getElementById("center").style.width = "800px";
            } else {
                document.getElementById("center").style.width = "1000px";
            }
        } else {
            document.getElementById("left").style.width = "20px";
            if (isRightSideShow) {
                document.getElementById("center").style.width = "1000px";
            } else {
                document.getElementById("center").style.width = "1200px";
            }
        }
    });

    $("#hideRight").click(function () {
        $("#helpcontent").toggle();
        isRightSideShow = !isRightSideShow;
        if (isRightSideShow) {
            document.getElementById("right").style.width = "220px";
            if (isLeftSideShow) {
                document.getElementById("center").style.width = "800px";
            } else {
                document.getElementById("center").style.width = "1000px";
            }
        } else {
            document.getElementById("right").style.width = "20px";
            if (isLeftSideShow) {
                document.getElementById("center").style.width = "1000px";
            } else {
                document.getElementById("center").style.width = "1200px";
            }
        }
    });


    $("#tabs").tabs();

    $("#editorinput").resizable({
        handles:"s",
        alsoResize:".CodeMirror-scroll"
    });


    $('.accordion .head').click(
        function () {
            $(this).next().toggle();
            return false;
        }).next().hide();


})();

function loadAccordionContent() {
    var acc = document.createElement("div");
    acc.id = "accordion";
    $.ajax({
        url:document.location.href + "?sessionId=" + sessionId + "&allExamples=true",
        context:document.body,
        success:onLoadingExamplesSuccess,
        dataType:"json",
        type:"GET",
        //data:{text:i},
        //timeout: 30000,
        error:function () {
            document.getElementById("statusbar").innerHTML = "Loading examples failed.";
        }
    });
    /*for (var i = 0; i < 3; ++i) {
        var head = document.createElement("h3");
        var headA = document.createElement("a");
        headA.href = "#";
        headA.innerHTML = "bbb" + i;
        head.appendChild(headA);
        acc.appendChild(head);
        var cont = document.createElement("div");

        for (var j = 0; j < 6; j++) {
            var contA = document.createElement("p");
            contA.id = i + "_" + j;
            contA.style.cursor = "pointer";
            contA.onclick = function (event) {
                loadExample(this.id);
            };
            contA.innerHTML = "aaa" + j;
            cont.appendChild(contA);
        }
        acc.appendChild(cont);
    }*/
    document.getElementById("examplesaccordion").appendChild(acc);
}

function onLoadingExamplesSuccess(data) {
    var acc = document.getElementById("accordion");
    var i = 0;
    var headCounter = 0;
    while (typeof data[i] != "undefined") {
        if (data[i].type == "head") {
            var head = document.createElement("h3");
            var headA = document.createElement("a");
            headA.href = "#";

            headA.innerHTML = data[i].text;
            head.appendChild(headA);
            acc.appendChild(head);
            var cont = document.createElement("div");
            headCounter++;
        }
        if (data[i].type == "content") {
            var contA = document.createElement("p");
            contA.id = i;
            contA.style.cursor = "pointer";
            contA.onclick = function (event) {
                loadExample(this.id);
            };
            contA.innerHTML = data[i].text;
            cont.appendChild(contA);
        }
        acc.appendChild(cont);
        i++;
    }
    $("#accordion").accordion();
}

function loadExample(id) {
//    var i = editor.getValue();
    document.getElementById("statusbar").innerHTML = "Loading example...";
    editor.setValue("Loading Example...");
    $.ajax({
        url:document.location.href + "?sessionId=" + sessionId + "&exampleId=" + id,
        context:document.body,
        success:onLoadingExampleSuccess,
        dataType:"json",
        type:"GET",
//        data:{text: i},
        //timeout: 30000,
        error:function () {
            document.getElementById("statusbar").innerHTML = "Loading example failed.";
        }
    });
}

function onLoadingExampleSuccess(data) {
    if (typeof data[0] != "undefined") {
        editor.setValue(data[0].text);
        document.getElementById("statusbar").innerHTML = "Example is loaded.";
    }
}