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

    $("#hideLeft").click(function () {
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
        });


    $("#tabs").tabs();

    $("#editorinput").resizable({
     handles:"s",
     alsoResize: ".CodeMirror-scroll"
     });

    $('.accordion .head').click(
        function () {
            $(this).next().toggle();
            return false;
        }).next().hide();

    $("#accordion").accordion();

    $("#speedA").selectmenu({
        dropdown: true
    });

    $("#speedA").open();

})();