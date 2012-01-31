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
        ]
    });

    $("#showInfoAboutLoginDialog").dialog({
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


});

function saveAs() {
    $("#saveDialog").dialog("close");
    var i = editor.getValue();
    var programName = $("#programName").val();
    document.getElementById("programName").value = "";
    var arguments = $("#arguments").val();
//    alert(programName + " " + arguments + " " + i);
    $.ajax({
        url:generateAjaxUrl("saveProgram", programName),
        success:onSaveProgramSuccess,
        dataType:"json",
        type:"POST",
        data:{text:i, consoleArgs:arguments},
        timeout:10000,
        error:function () {
            setStatusBarMessage(SAVE_PROGRAM_REQUEST_ABORTED);
        }
    });
    return false;
}

function save() {
    if (lastSelectedExample == 0) {
        $("#saveAsProgram").click();
        return;
    }

    var pos = lastSelectedExample.indexOf("&head=My_Programs");
//    alert(lastSelectedExample + pos)
    if (pos < 0) {
        $("#saveAsProgram").click();
        return;
    }

    var i = editor.getValue();
    var arguments = $("#arguments").val();
    $.ajax({
        url:generateAjaxUrl("saveProgram", "id=" + lastSelectedExample),
        success:onSaveProgramSuccess,
        dataType:"json",
        type:"POST",
        data:{text:i, consoleArgs:arguments},
        timeout:10000,
        error:function () {
            setStatusBarMessage(SAVE_PROGRAM_REQUEST_ABORTED);
        }
    });
}

function onSaveProgramSuccess(data) {
    if (typeof data != "undefined" && data != null) {
        var i = 0;
        while (typeof data[i] != "undefined") {
            if (data[i].type == "exception") {
                setStatusBarError(data[i].text);
                setTimeout(function () {
                    $("#My_Programs").click();
                }, 500);
            } else if (data[i].type == "programId") {
                isContentEditorChanged = false;
                setStatusBarMessage("Your program was successfully saved.");
            } else {
                isContentEditorChanged = false;
                var pos = data[i].text.indexOf("&id=");
                if (pos > 0) {
                    var name = data[i].text.substring(0, pos);
                    var id = data[i].text.substring(pos + 4);

                    setStatusBarMessage("Saved as: " + name + ".");

                    document.getElementById("myprogramscontent").appendChild(createProgramListElement(id.replace(new RegExp(" ", 'g'), "_") + "&head=My_Programs", name));
                    $("#My_Programs").click();
                    document.getElementById("problems").innerHTML = "";
                    setConsoleMessage("");
                    removeStyles();
                    var el = document.getElementById(lastSelectedExample);
                    if (el != null) {
                        el.className = "";
                    }

                    lastSelectedExample = id + "&head=My_Programs";
                    el = document.getElementById(id + "&head=My_Programs");
                    if (el != null) {
                        el.className = "selectedExample";
                    }

                }

            }
            i++;
        }
        editor.focus();
    }
}

function createProgramListElement(id, name) {
    var content = document.createElement("p");
    var span = document.createElement("span");
    span.className = "bullet";
    span.innerHTML = "&#8226;";
    content.appendChild(span);
    var contA = document.createElement("a");
    contA.id = id;
    contA.style.cursor = "pointer";
    contA.onclick = function () {
        beforeLoadProgram(this.id);
    };
    contA.innerHTML = name;
    var delImg = document.createElement("img");
    delImg.src = "/icons/delete.png";
    delImg.title = "Delete";
    delImg.onclick = function () {
        deleteProgram(this.parentNode.childNodes[1].id);
    };
    var linkImg = document.createElement("img");
    linkImg.src = "/icons/link1.png";
    linkImg.title = "Public link for this program";
    linkImg.onclick = function () {
        generatePublicLinkForProgram(this.parentNode.childNodes[1].id);
    };
    content.appendChild(contA);
    content.appendChild(delImg);
    content.appendChild(linkImg);

    return content;
}


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
        var ids = [];
        if (data[i].type == "head") {
            var head = document.createElement("h3");
            var headA = document.createElement("a");
            headA.href = "#";
            var id = data[i].text.replace(new RegExp(" ", 'g'), "_");
            headA.id = id;

            headA.innerHTML = data[i].text;
            lastHeadName = data[i].text
            head.appendChild(headA);
            acc.appendChild(head);
            var cont = document.createElement("div");
            var idWoSymbols = id.replace(new RegExp(",", 'g'), "_").replace(new RegExp("!", 'g'), "_");
            cont.innerHTML = "<div class=\"toolbox\" id=\"pld" + idWoSymbols + "\" style=\"display:none\"><div align=\"center\"><div class=\"fixedpage\"><div class=\"publicLinkHref\" id=\"pl" + idWoSymbols + "\"></div><img class=\"closePopup\" id=\"cp" + idWoSymbols + "\" src=\"/icons/close.png\" title=\"Close popup\"></span></div></div></div>";
            ids.push(idWoSymbols);
        }
        if (data[i].type == "content") {
            var content = document.createElement("p");
            var span = document.createElement("span");
            span.className = "bullet";
            span.innerHTML = "&#8226;";
            content.appendChild(span);
            var contA = document.createElement("a");
            contA.id = data[i].text.replace(new RegExp(" ", 'g'), "_") + "&head=" + lastHeadName.replace(new RegExp(" ", 'g'), "_");
//            contA.id = i + "&head=" + lastHeadName;
            contA.style.cursor = "pointer";
            contA.onclick = function (event) {
                beforeLoadExample(this.id);
            };
            contA.innerHTML = data[i].text;
            var linkImg = document.createElement("img");
            linkImg.src = "/icons/link1.png";
            linkImg.title = "Public link for this example";
            linkImg.onclick = function (event) {
                generatePublicLinkForExample(this.parentNode.childNodes[1].id);
            };

            content.appendChild(contA);
            content.appendChild(linkImg);
            cont.appendChild(content);
        }
        acc.appendChild(cont);

        forEach(ids, function (id) {
            $("#cp" + id).click(function () {
                $("div#pld" + id).slideUp('slow');
            });
        });

        i++;
    }

    /*$( "#accordion" ).bind( "accordionchangestart", function(event, ui) {
     alert("a");
     event.preventDefault();
     });*/

    var myProg = document.createElement("h3");
    var innerDiv = document.createElement("div");
    innerDiv.id = "tools";
    var myProgA = document.createElement("a");
    myProgA.href = "#";
    myProgA.id = "My_Programs";

    myProgA.innerHTML = "My Programs";
    innerDiv.appendChild(myProgA);

    if (isLogin) {
        var saveImg = document.createElement("img");
        saveImg.src = "/icons/save1.png";
        saveImg.id = "saveProgram";
        saveImg.title = "Save current program";
        var saveAsImg = document.createElement("img");
        saveAsImg.src = "/icons/saveAs1.png";
        saveAsImg.id = "saveAsProgram";
        saveAsImg.title = "Save current program as ...";
        innerDiv.appendChild(saveAsImg);
        innerDiv.appendChild(saveImg);
    } else {
        var infoImg = document.createElement("img");
        infoImg.src = "/images/whatisthis.png";
//        infoImg.src = "/icons/information.png";
        infoImg.id = "showInfoAboutLogin";
        infoImg.title = "Help";
        innerDiv.appendChild(infoImg);
    }
    myProg.appendChild(innerDiv);
//        myProg.appendChild(myProgA);
    acc.appendChild(myProg);

    var myProgCont = document.createElement("div");
    myProgCont.id = "myprogramscontent";
    myProgCont.innerHTML = "<div id=\"toolbox\" style=\"display:none\"><div align=\"center\"><div class=\"fixedpage\"><div id=\"publicLinkHref\"></div><img id=\"closePopup\" src=\"/icons/close.png\" title=\"Close popup\">    </span></div></div></div>";
    acc.appendChild(myProgCont);

    $("#closePopup").click(function () {
        $('div#toolbox').slideUp('slow');
    });

    loadListOfPrograms();


    $("#accordion").accordion({
        autoHeight:false,
        navigation:true
    }).find('#tools img').click(function (ev) {
            ev.preventDefault();
            if (this.id == "saveProgram") {
                save();
            } else if (this.id == "saveAsProgram") {
                $("#saveDialog").dialog("open");
                $("#programName").focus();
            } else if (this.id == "showInfoAboutLogin") {
                $("#showInfoAboutLoginDialog").dialog("open");
            }
        });

    var urlAct = [location.protocol, '//', location.host, "/"].join('');
    var url = document.location.href;
    if (url.indexOf(urlAct) != -1) {
        url = url.substring(url.indexOf(urlAct) + urlAct.length);
        var exampleStr = "?example=";
        if (url.indexOf(exampleStr) == 0) {
//            url = url.replace(new RegExp("_", 'g'), " ");
            $("#" + url.substring(url.indexOf("&head=") + 6)).click();
            beforeLoadExample(url.substring(exampleStr.length));
        }
        var publicLink = "?publicLink=";
        if (url.indexOf(publicLink) == 0) {
//            url = url.replace(new RegExp("_", 'g'), " ");
            //$("#" + url.substring(url.indexOf("&head=") + 6)).click();
            beforeLoadProgram(url.substring(publicLink.length) + "&head=My_Programs", true);
        }
    }


}

function loadListOfPrograms() {
    $.ajax({
        url:generateAjaxUrl("loadProgram", "all"),
        context:document.body,
        success:onLoadingProgramsSuccess,
        dataType:"json",
        type:"GET",
        //data:{text:i},
        timeout:10000,
        error:function () {
            setStatusBarMessage(SAVE_PROGRAM_REQUEST_ABORTED);
        }
    });
}

function onLoadingProgramsSuccess(data) {
    var i = 0;
    var cont = document.getElementById("myprogramscontent");
    while (typeof data[i] != "undefined") {
        cont.appendChild(createProgramListElement(data[i].id.replace(new RegExp(" ", 'g'), "_") + "&head=My_Programs", data[i].name));
        i++;
    }
}

function beforeLoadProgram(name, isPublicLink) {
    if (isContentEditorChanged) {
        confirmAction(function (name, isPublicLink) {
            return function () {
                loadProgram(name, isPublicLink);
            }
        }(name, isPublicLink));
    } else {
        loadProgram(name, isPublicLink);
    }
}

function loadProgram(name, isPublicLink) {
    if (lastSelectedExample == name) {
        return;
    }
    document.getElementById("problems").innerHTML = "";
    setConsoleMessage("");
    removeStyles();
    var el = document.getElementById(lastSelectedExample);
    if (el != null) {
        el.className = "";
    }

    lastSelectedExample = name;
    el = document.getElementById(name);
    if (el != null) {
        el.className = "selectedExample";
    }
    var el1 = $("#My_Programs");
    if (el1 != null) {
        el1.click();
    }
    document.getElementById("statusbar").innerHTML = "Loading program...";
    loadingExample = true;
    name = name.replace(new RegExp("_", 'g'), " ");
    if (isPublicLink) {
        name += "publicLink";
    }
    $.ajax({
        url:generateAjaxUrl("loadProgram", name),
        context:document.body,
        success:onLoadingProgramSuccess,
        dataType:"json",
        type:"GET",
        //data:{text:i},
        timeout:10000,
        error:function () {
            setStatusBarMessage(SAVE_PROGRAM_REQUEST_ABORTED);
        }
    });

}


function deleteProgram(name) {
    if (confirm(BEFORE_DELETE_PROGRAM)) {
        if (lastSelectedExample == name) {
            lastSelectedExample = "";
        }

        name = name.replace(new RegExp("_", 'g'), " ");
        $.ajax({
            url:generateAjaxUrl("deleteProgram", name),
            context:document.body,
            success:onDeletingProgramSuccess,
            dataType:"json",
            type:"GET",
            //data:{text:i},
            timeout:10000,
            error:function () {
                setStatusBarMessage(DELETE_PROGRAM_REQUEST_ABORTED);
            }
        });
    }

}

function generatePublicLinkForProgram(name) {
    name = name.replace(new RegExp("_", 'g'), " ");
    $.ajax({
        url:generateAjaxUrl("generatePublicLink", name),
        context:document.body,
        success:onGeneratePublicLinkSuccess,
        dataType:"json",
        type:"GET",
        //data:{text:i},
        timeout:10000,
        error:function () {
            setStatusBarMessage(PUBLIC_LINK_REQUEST_ABORTED);
        }
    });
}

function generatePublicLinkForExample(name) {
    var head = name.substring(name.indexOf("&head=") + 6).replace(new RegExp(",", 'g'), "_").replace(new RegExp("!", 'g'), "_");
    var href = "http://kotlin-demo.jetbrains.com/?example=" + name;
    setStatusBarMessage("Public link was generated.");
    $("#pl" + head).html(href);
    $("div#pld" + head).slideDown('slow');
    //toolboxHello,_world!
//    selectTextById("pl" + head);
    setStatusBarMessage(href);
}
//var timerForPopup;

function onGeneratePublicLinkSuccess(data) {
    if (data != null && typeof data != "undefined") {
        if (data[0] != null && typeof data[0] != "undefined") {
            if (data[0].type == "exception") {
                setStatusBarError(data[0].text);
            } else {
//                window.prompt ("Copy to clipboard: Ctrl+C, Enter", data[0].text);
                setStatusBarMessage("Public link was generated.");
                $("#publicLinkHref").html(data[0].text);
//                $("#toolbox").height($("#publicLinkHref").height);
                $('div#toolbox').slideDown('slow');
//                selectTextById("publicLinkHref");
                /*if (timerForPopup != null) {
                 clearTimeout(timerForPopup);
                 }
                 timerForPopup = setTimeout(function () {
                 $('div#toolbox').slideUp('slow');
                 }, 5000);*/

            }
        }
    }
}

/*$("#toolbox").mouseover(function () {
 clearTimeout(timerForPopup);
 timerForPopup = null;
 });

 $("#toolbox").mouseleave(function () {
 timerForPopup = setTimeout(function () {
 $('div#toolbox').slideUp('slow');
 }, 5000);
 });*/

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

function onDeletingProgramSuccess(data) {
    if (data != null && typeof data != "undefined") {
        if (data[0] != null && typeof data[0] != "undefined") {
            if (data[0].type == "exception") {
                setStatusBarError(data[0].text);
            } else {
                setStatusBarMessage(data[0].text);
                document.getElementById(data[0].args + "&head=My_Programs").parentNode.innerHTML = "";

            }
        }
    }
}

function onLoadingProgramSuccess(data) {
    editor.focus();
    loadingExample = false;

    if (data != null && typeof data != "undefined") {
        if (data[0] != null && typeof data[0] != "undefined") {
            if (data[0].type == "exception") {
                setStatusBarError(data[0].text);
            } else {
                editor.setValue(data[0].text);
                document.getElementById("arguments").value = data[0].args;
                setStatusBarMessage(LOADING_PROGRAM_OK);
                var el = document.getElementById(lastSelectedExample);
                if (el != null) {
                    el.className = "selectedExample";
                }
            }
        }
    }
    isContentEditorChanged = false;
}

var loadingExample = false;
var lastSelectedExample = 0;

function beforeLoadExample(name) {
    document.getElementById("debug").innerHTML = name;
    if (isContentEditorChanged) {
        confirmAction(function (name) {
            return function () {
                loadExample(name);
            }
        }(name));
    } else {
        loadExample(name);
    }
}

function loadExample(name) {
//    if ((isContentEditorChanged && confirm(BEFORE_EXIT)) || !isContentEditorChanged) {
    if (lastSelectedExample == name) {
        return;
    }
    document.getElementById("problems").innerHTML = "";
    setConsoleMessage("");
    removeStyles();
    var el = document.getElementById(lastSelectedExample);
    if (el != null) {
        el.className = "";
    }

    lastSelectedExample = name;
    document.getElementById(name).className = "selectedExample";
    document.getElementById("statusbar").innerHTML = "Loading example...";
    loadingExample = true;
    name = name.replace(new RegExp("_", 'g'), " ");
    $.ajax({
        url:generateAjaxUrl("loadExample", name),
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
    loadExamplesHelp(name.substring(0, name.indexOf("&head=")));
//    }
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
            hideLoader();
            $(".applet-disable").click();
        }
    }
});

function waitLoadingApplet() {
    var applet = $("#myapplet")[0];
    showLoader();
    function performAppletCode(count) {
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
//    $("#appletcheckbox").attr('checked', false);
//    isApplet = false;
    setStatusBarMessage("");
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

}




