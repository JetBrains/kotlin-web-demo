/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/10/11
 * Time: 12:12 PM
 * To change this template use File | Settings | File Templates.
 */

$(document).ready(function () {
    var isLeftSideShow = true;
    var isRightSideShow = true;

    $("#hideLeft").click(function () {
        $("#left").toggle();
        isLeftSideShow = !isLeftSideShow;
        if (isLeftSideShow) {
            document.getElementById("hideLeft").src = "/icons/togglel.png";
            document.getElementById("hideLeft").title = "Hide left side";
            document.getElementById("left").style = "width: 20%; *width: 230px;";
            if (isRightSideShow) {
                document.getElementById("center").style.width = "60%";
            } else {
                document.getElementById("center").style.width = "79%";
            }
        } else {
            document.getElementById("hideLeft").src = "/icons/toggler.png";
            document.getElementById("hideLeft").title = "Show left side";
            if (isRightSideShow) {
                document.getElementById("center").style.width = "80%";
            } else {
                document.getElementById("center").style.width = "99%";
            }
        }
    });

    $("#hideRight").click(function () {
        $("#right").toggle();
        isRightSideShow = !isRightSideShow;
        if (isRightSideShow) {
            document.getElementById("hideRight").src = "/icons/toggler.png";
            document.getElementById("hideRight").title = "Hide right side";
            document.getElementById("right").style.width = "19%";
            if (isLeftSideShow) {
                document.getElementById("center").style.width = "60%";
            } else {
                document.getElementById("center").style.width = "80%";
            }
        } else {
            document.getElementById("hideRight").src = "/icons/togglel.png";
            document.getElementById("hideRight").title = "Show right side";
            if (isLeftSideShow) {
                document.getElementById("center").style.width = "79%";
            } else {
                document.getElementById("center").style.width = "99%";
            }
        }
    });

    $("#tabs").tabs();

    $('.accordion .head').click(
        function () {
            $(this).next().toggle();
            return false;
        }).next().hide();


});

function loadAccordionContent() {
    var acc = document.createElement("div");
    acc.id = "accordion";
    document.getElementById("examplesaccordion").appendChild(acc);
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

}

function onLoadingExamplesSuccess(data) {
    var acc = document.getElementById("accordion");
    var i = 0;
    while (typeof data[i] != "undefined") {
        var lastHeadName;
        if (data[i].type == "head") {
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
//            contA.id = i + "&head=" + lastHeadName;
            contA.style.cursor = "pointer";
            contA.onclick = function (event) {
                loadExample(this.id, this.innerHTML);
            };
            contA.innerHTML = data[i].text;
            content.appendChild(contA);
            cont.appendChild(content);
        }
        acc.appendChild(cont);
        i++;
    }
    $("#accordion").accordion({
        autoHeight:false,
        navigation:true
    });

    var urlAct = [location.protocol, '//', location.host, "/"].join('');
    var url = document.location.href;
    if (url.indexOf(urlAct) != -1) {
        url = url.substring(url.indexOf(urlAct) + urlAct.length);
        var exampleStr = "?example=";
        if (url.indexOf(exampleStr) == 0) {
            url = url.replace(new RegExp("_", 'g'), " ");
            loadExample(url.substring(exampleStr.length));
        }
    }



}

var loadingExample = false;
var lastSelectedExample = 0;

function loadExample(innerhtml) {
    if ((isContentEditorChanged && confirm(BEFORE_EXIT)) || !isContentEditorChanged) {
        document.getElementById("problems").innerHTML = "";
        setConsoleMessage("");
        removeStyles();
        var el = document.getElementById(lastSelectedExample);
        if (el != null) {
            el.className = "";
        }

        lastSelectedExample = innerhtml;
        document.getElementById(innerhtml).className = "selectedExample";
        document.getElementById("statusbar").innerHTML = "Loading example...";
        loadingExample = true;
        $.ajax({
            url:generateAjaxUrl("loadExample", innerhtml),
            context:document.body,
            success:onLoadingExampleSuccess,
            dataType:"json",
            type:"GET",
//        data:{text: i},
            timeout:10000,
            error:function () {
                loadingExample = false;
                setStatusBarMessage(EXAMPLES_REQUEST_ABORTED);
            }
        });
        loadExamplesHelp(innerhtml);
    }
}

function onLoadingExampleSuccess(data) {
    editor.focus();
    loadingExample = false;
    if (typeof data[0] != "undefined") {
        editor.setValue(data[0].text);
        setStatusBarMessage(LOADING_EXAMPLE_OK);
    }
    isContentEditorChanged = false;
}

$(".applet-enable").click(function () {
    var parent = $(this).parents('.switch');
    $('.applet-disable', parent).removeClass('selected');
    $('.applet-disable', parent).removeClass('rightServer');
    $('.applet-disable', parent).addClass('leftServer');
    $('.applet-nohighlighting', parent).removeClass('selected');
    $(this).addClass('selected');
    $("#appletcheckbox").attr('checked', true);
    $("#nohighlightingcheckbox").attr('checked', false);
    isApplet = true;
    if (!isAppletLoaded) {
        waitLoadingApplet();
        try {
            document.getElementById("myapplet").style.display = "block";
            isAppletLoaded = true;
            var title = document.getElementById("appletclient").title;
            var pos = title.indexOf(GET_FROM_APPLET_FAILED)
            if (pos != -1) {
                title = title.substring(0, pos);
                document.getElementById("appletclient").title = title;
            }
        } catch (e) {
//            document.getElementById("debug").innerHTML = e;
            alert("1");
            hideLoader();
            $(".applet-disable").click();
        }
    }
});

function waitLoadingApplet() {
    var applet = $("#myapplet")[0];
    showLoader();
    function performAppletCode(count) {
//        alert(count);
//        document.getElementById("debug").innerHTML += count + " " + applet.getHighlighting;
        if (!applet.getHighlighting && count > 0) {
            setTimeout(function () {
                performAppletCode(count - 1);
            }, 2000);
        }
        else if (applet.getHighlighting) {
            hideLoader();
        }
        else {
            hideLoader();
        }
    }

    performAppletCode(10);
}

function appletIsLoaded() {
    hideLoader();
}

$(".applet-disable").click(function () {
    var parent = $(this).parents('.switch');
    $('.applet-enable', parent).removeClass('selected');
    $('.applet-nohighlighting', parent).removeClass('selected');
    $(this).addClass('selected');
    $("#appletcheckbox").attr('checked', false);
    $("#nohighlightingcheckbox").attr('checked', false);
    isApplet = false;
});

$(".applet-nohighlighting").click(function () {
    var parent = $(this).parents('.switch');
    $('.applet-disable', parent).removeClass('selected');
    $('.applet-disable', parent).removeClass('leftServer');
    $('.applet-disable', parent).addClass('rightServer');
    $('.applet-enable', parent).removeClass('selected');
    $(this).addClass('selected');
    $("#nohighlightingcheckbox").attr('checked', true);
//    $("#appletcheckbox").attr('checked', false);
//    isApplet = false;
});

var isShortcutsShow = true;
$(".toggleShortcuts").click(function () {
    $("#help3").toggle();
    if (isShortcutsShow) {
        isShortcutsShow = false;
        document.getElementById("toggleShortcutsButton").src = "/images/toogleShortcutsOpen.png";
    } else {
        isShortcutsShow = true;
        document.getElementById("toggleShortcutsButton").src = "/images/toogleShortcuts.png";

    }

});

