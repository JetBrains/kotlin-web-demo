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

    $("#saveDialog").dialog({
        modal:"true",
        width:300,
        height:120,
        autoOpen:false,
        buttons:[
            { text:"Save",
                click:function () {
                    saveAs();
                }
            },
            { text:"Cancel",
                click:function () {
                    $(this).dialog("close");
                }
            }
        ],
        open:function () {
            setTimeout(function () {
                $("#programName").focus();
            }, 200);
        }
    });

    $("#showInfoAboutLoginDialog").dialog({
        modal:"true",
        width:300,
//        height:120,
        autoOpen:false
    });

    $("#popupForCanvas").dialog({
//        modal:"false",
        width:600,
        height:350,
        autoOpen:false,
        close:function () {
            $("#popupForCanvas").html("");
            /*var canvas = document.getElementById("mycanvas");
            var context = canvas.getContext('2d');
            context.clearRect(0, 0, canvas.width, canvas.height);
//          $(this).dialog("close");*/
        }
    });

    $("#dialogAboutRunConfiguration").dialog({
        modal:"true",
        width:300,
//        height:120,
        autoOpen:false
    });

    $("#confirmDialog").dialog({
        modal:"true",
        width:500,
        height:120,
        autoOpen:false,
        buttons:[
            { text:"Save changes",
                click:function () {
                    save();
                    closeConfirmDialog();
                }
            },
            { text:"Discard changes",
                click:function () {
                    closeConfirmDialog();
                }
            },
            { text:"Cancel",
                click:function () {
                    closeConfirmDialog();
                }
            }
        ]
    });

    $("#runConfigurationMode").selectmenu({
            change:function () {
                runConfiguration.mode = $("#runConfigurationMode").val();
                getErrors();
            }
        }
    );


});

function saveAs() {
    Accordion.saveAsProgram();
}

function save() {
    Accordion.saveProgram();
}

function loadAccordionContent() {
    Accordion.loadAllContent();
}

function deleteProgram(name) {
    Accordion.deleteProgram(name);
}

function generatePublicLinkForProgram(name) {
    Accordion.generatePublicLinkForProgram(name);
}

function generatePublicLinkForExample(name) {
    Accordion.generatePublicLinkForExample(name);
}


function beforeLoadExample(name) {
    Accordion.loadExample(name);
}

function beforeLoadProgram(name, isPublicLink) {
    Accordion.loadProgram(name, isPublicLink);
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
    saveModeToCookies("applet");
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
            hideLoader();
            $(".applet-disable").click();
        }
    }
});

function waitLoadingApplet() {
    var applet = $("#myapplet")[0];
    showLoader();
    function performAppletCode(count) {
        if (!applet.checkApplet && count > 0) {
            setTimeout(function () {
                performAppletCode(count - 1);
            }, 1000);
        }
        else if (applet.checkApplet) {
            hideLoader();
        }
        else {
            hideLoader();
        }
    }

    performAppletCode(10);
}

$(".applet-disable").click(function () {
    var parent = $(this).parents('.switch');
    $('.applet-enable', parent).removeClass('selected');
    $('.applet-nohighlighting', parent).removeClass('selected');
    $(this).addClass('selected');
    $("#appletcheckbox").attr('checked', false);
    $("#nohighlightingcheckbox").attr('checked', false);
    saveModeToCookies("server");
    isApplet = false;
    setStatusBarMessage("");
});

$(".applet-nohighlighting").click(function () {
    var parent = $(this).parents('.switch');
    $('.applet-disable', parent).removeClass('selected');
    $('.applet-disable', parent).removeClass('leftServer');
    $('.applet-disable', parent).addClass('rightServer');
    $('.applet-enable', parent).removeClass('selected');
    $(this).addClass('selected');
    $("#nohighlightingcheckbox").attr('checked', true);
    saveModeToCookies("nohighlighting");
//    $("#appletcheckbox").attr('checked', false);
//    isApplet = false;
    setStatusBarMessage("");
});

function saveModeToCookies(mode) {
    $.cookie("typeCheckerMode", mode);
}

function getModeFromCookies() {
    return $.cookie("typeCheckerMode");
}

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
    setStatusBarMessage("");
});

/*$("#userName").click(function (e) {
 var div = document.createElement("div");
 div.id = "logout";
 div.innerHTML = "Logout";
 div.style.position = "absolute";
 div.style.left = e.clientX + 10 + "px";
 div.style.top = e.clientY + 10 + "px";
 div.onclick = function () {
 close();
 logout();
 };
 document.body.appendChild(div);

 function close() {
 div.parentNode.removeChild(div);
 }

 });*/

$("#logout").click(function (e) {
    logout();
});

var isLogoutShown = false;


function userNameClick(e) {
    if (!isLogoutShown) {
        $("#headerlinks").bind("mouseleave", function () {
            var timeout = setTimeout(function () {
                close();
            }, 100);
            $("#logout").bind("mouseover", function () {

                clearTimeout(timeout);
                $("#logout").bind("mouseleave", function () {
                    timeout = setTimeout(function () {
                        close();
                    }, 500);
                });
            });

        });

        isLogoutShown = true;
        var div = document.createElement("div");
        div.id = "logout";
        div.innerHTML = "Logout";
        div.style.position = "absolute";

        var element = document.getElementById("userNameTitle");
        var left = element.offsetLeft;
        var top = element.offsetTop;
        for (var parent = element.offsetParent; parent; parent = parent.offsetParent) {
            left += parent.offsetLeft - parent.scrollLeft;
            top += parent.offsetTop - parent.scrollTop
        }


//        alert(left + " " + top);
//        alert (left + document.getElementById("userNameTitle").width + " " + top + document.getElementById("userNameTitle").height);
        div.style.left = left + 240 - 42 + "px";
        div.style.top = top + 27 - 3 + "px";
        div.onclick = function () {
            close();
            logout();
        };
        document.body.appendChild(div);
    } else {
        close();
    }


    function close() {
        isLogoutShown = false;
        var el = document.getElementById("logout");
        if (el != null) {
            el.parentNode.removeChild(document.getElementById("logout"));
        }
    }

    function selectTextById(element) {
        var doc = document;
        var text = doc.getElementById(element);
        if (doc.body.createTextRange) {
            var range = document.body.createTextRange();
            range.moveToElementText(text);
            range.select();
        } else if (window.getSelection) {
            var selection = window.getSelection();
            var range = document.createRange();
            range.selectNodeContents(text);
            selection.removeAllRanges();
            selection.addRange(range);
        }
    }

    function selectTextByElement(element) {
        var doc = document;
        var text = element;
        if (doc.body.createTextRange) {
            var range = document.body.createTextRange();
            range.moveToElementText(text);
            range.select();
        } else if (window.getSelection) {
            var selection = window.getSelection();
            var range = document.createRange();
            range.selectNodeContents(text);
            selection.removeAllRanges();
            selection.addRange(range);
        }
    }

}




