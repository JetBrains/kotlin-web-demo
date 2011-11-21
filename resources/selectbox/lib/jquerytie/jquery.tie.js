/*
Copyright (c) 2010 RevSystems, Inc

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

The end-user documentation included with the redistribution, if any, must 
include the following acknowledgment: "This product includes software developed 
by RevSystems, Inc (http://www.revsystems.com/) and its contributors", in the 
same place and form as other third-party acknowledgments. Alternately, this 
acknowledgment may appear in the software itself, in the same form and location 
as other such third-party acknowledgments.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
(function($) {

$.fx.prototype.oldUpdate = $.fx.prototype.update;
$.fx.prototype.update = function() {
  result = this.oldUpdate.apply(this, arguments);
  $(this.elem).trigger("cssupdate");
  return result;
}

function displayChangeWrap(funcName, trigger, reqArgs, reqFunc) {
  $.fn["old_" + funcName] = $.fn[funcName];
  $.fn[funcName] = function() {
    var $this = $(this);
    var $par = $this.parent();
    result = $.fn["old_" + funcName].apply(this, arguments);
    var args = arguments;
    if(arguments.length >= reqArgs && (!reqFunc || reqFunc.call(this, args))) {
      if(funcName == "remove") {
        $par.trigger(trigger);
      }
      else if($this[0].nodeName.toLowerCase() != "body") {
        $this.trigger(trigger);
      }
      return $this;
    }
    return result;
  }
}

displayChangeWrap("css", "cssupdate", 1, function(args) { return args.length >= 2 || typeof args[0] === "object"; });
displayChangeWrap("attr", "domupdate", 2, function(args) { return args.length == 2; });
displayChangeWrap("append", "domupdate", 1);
displayChangeWrap("prepend", "domupdate", 1);
displayChangeWrap("before", "domupdate", 1);
displayChangeWrap("after", "domupdate", 1);
displayChangeWrap("text", "domupdate", 1);
displayChangeWrap("html", "domupdate", 1);
displayChangeWrap("empty", "domupdate", 0);
displayChangeWrap("remove", "domupdate", 0);
displayChangeWrap("removeAttr", "domupdate", 1);

$.fn.tie = function(lhsProp, $rhs, rhsProp, options) {

  options = $.extend({
    globalListener: false,
    onImgLoad: false,
    onResize: false,
    onScroll: false,
    proxyListener: false
  }, options);
  
  if(!rhsProp) rhsProp = lhsProp;
  $rhs = $($rhs);
  var proxyListener = $(options.proxyListener);
  if(options.globalListener) {
    proxyListener = $("body");
  }
  else if(!proxyListener) {
    proxyListener = $rhs.parent();
  }
  
  return $(this).each(function() {
    var $lhs = $(this);
    var rhsIsAncestor = $lhs[0] == $rhs[0];
    $lhs.parents().each(function() { if(this == $rhs[0]) rhsIsAncestor = true; });
    function updateProp(e) {
      $lhs.old_css(lhsProp, $.isFunction(rhsProp) ? rhsProp.call($rhs) : $rhs.old_css(rhsProp));
      if(!rhsIsAncestor && !options.globalListener && !options.proxyListener) {
        $lhs.trigger("cssupdate");
      }
    }
    function triggerProxy() { $(this).trigger("cssupdate"); }
    function destroy() {
      proxyListener.unbind("cssupdate", updateProp);
      proxyListener.unbind("domupdate", updateProp);
      $rhs.unbind("cssupdate", updateProp);
      $rhs.unbind("domupdate", updateProp);
      $lhs.unbind("destroy.tie", destroy);
      $(window).unbind("resize", updateProp);
      $(window).unbind("scroll", updateProp);
      $("img").unbind("load", triggerProxy);
    }
    updateProp();
    if(rhsIsAncestor || options.globalListener || options.proxyListener) {
      proxyListener.bind("cssupdate", updateProp);
      proxyListener.bind("domupdate", updateProp);
    }
    else {
      $rhs.bind("cssupdate", updateProp);
      $rhs.bind("domupdate", updateProp);
    }
    if(options.globalListener && options.onImgLoad) {
      $("img").bind("load", triggerProxy);
    }
    else if(options.onImgLoad) {
      $rhs.find("img").bind("load", triggerProxy);
    }
    $lhs.bind("destroy.tie", destroy);
    if(options.onResize) $(window).bind("resize", updateProp);
    if(options.onScroll) $(window).bind("scroll", updateProp);
  });
}

$.fn.untie = function() { return $(this).trigger("destroy"); }

$.fn.cssupdate = function(handler) {
  if(!handler) $(this).trigger("cssupdate");
  else $(this).bind("cssupdate", handler);
}

$.fn.domupdate = function(handler) {
  if(!handler) $(this).trigger("domupdate");
  else $(this).bind("domupdate", handler);
}

})($);