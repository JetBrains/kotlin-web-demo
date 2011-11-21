# jQuery-Tie BETA

This plugin allows you to match the css property of one DOM element to another.
It is persistent, which means the match is applied every time the matched element 
changes its css, attributes, or DOM content. It also introduces new events, 
"cssupdate" and "domupdate", which could be useful under other circumstances.

Please note that this plugin can be tricky and needs to be refined further before 
it is considered non-beta.

# Demo

You can see some examples [here](http://dl.dropbox.com/u/124192/websites/jquerytie/index.html).

## Features

  * Tie one element's appearance to another's, even when JS modifies the page
  * Lets you set the listener scope to help limit trigger pollution
  * Introduces domupdate/cssupdate events, which bubble up from the target element
  
## Compatibility

jQuery-Tie has been tested in the following browsers:
  
  * Firefox 3.6.12
  * Google Chrome 7.0.517.44
  * IE7 (via IE9 beta)
  * IE8 (via IE9 beta)
  * IE9 beta
  
It requires [jQuery version 1.3.x](http://jquery.com) and up.
  
## Caution

Be forewarned that this plugin wraps core jQuery functions so that they 
can fire the necessary events. These functions are:

  * css
  * attr
  * append
  * prepend
  * before
  * after
  * text
  * html
  * empty
  * remove
  * jQuery.fx.prototype.update (for animate)

You'll notice functions like insertBefore and replace are not included
here. These functions will still fire the events because they use one of 
the functions listed above internally.

NON-JQUERY FUNCTIONS WILL NOT TRIGGER THE NEW EVENTS.

This means things calls like document.getElementById("div1").appendChild("text"); 
or "this.selected = true;" are not recognized by jquery.tie. Make sure 
you consistently use jQuery's DOM manipulation functions if you want jquery.tie 
to work correctly.

## Usage

Requires [jQuery](http://jquery.com) and this plugin.

    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.3/jquery.min.js"></script>
    <script type="text/javascript" src="jquery.tie.js"></script>
    
First, imagine we're working with this markup:

    <div id="element_container">
      <div class="group">
        <div id="a">Element A</div>
        <div id="b">Element B</div>
      </div>
    </div>

To match element A's width to element B's:

    $(document).ready(function() {
      $("#a").tie("width", "#a");
      $("#b").width(100);
    });
    
To match element A's width to element B's height:

    $(document).ready(function() {
      $("#a").tie("width", "#b", "height");
      $("#b").height(100);
    });
    
To match element A's width to twice element B's height:

    $(document).ready(function() {
      $("#a").tie("width", "#b", function() {
        return $(this).height() * 2;
      });
      $("#b").height(100);
    });
    
## Advanced Usage

### Global Listeners

One issue with matching CSS is something I call "passive resizing".
Basically, a block level element's width is controlled by one of its 
parents' widths instead of its children.

To completely avoid the issue, you could set up a global listener:

    $(document).ready(function() {
      $("#a").tie("width", "#b", "width", { globalListener: true });
      $("#elements").width(300);
    });

### Proxy Listeners

Global listeners introduce a lot of extra function calls that we 
may want to avoid. Any time there is ANY css/dom update on the page, 
your tie function will be called.

For advanced users, there is a way to reduce this event pollution. 
You can set a proxy listener.

To match element A's width to element B's width using passive resizing
and a proxy listener:

    $(document).ready(function() {
      $("#a").tie("width", "#b", "width", { proxyListener: "#elements" });
      $("#elements").width(300);
    });

Normally, the proxy listener would default to element B's parent. But in the 
case above, no cssupdate event would be triggered on element B or its parent.

To deal with this, we set a proxy listener on #elements, which is what we 
really want to track. So the tie function would be invoked whenever #elements 
or any of its descendants fire a cssupdate/domupdate event.

### onScroll and onResize

You also can trigger the tie function on scroll or resize:

    $(document).ready(function() {
      $("#a").tie("top", "#b", function() {
        return $(window).scrollTop();
      }, { 
        onScroll: true 
      });
    });

    $(document).ready(function() {
      $("#a").tie("height", "body", function() {
          return Math.max($(window).height(), $(this).height()); 
        }, { 
          onResize: true 
        });
    });
    
### Events

To set up a domupdate event listener:

    $(document).ready(function() {
      $("#a").domupdate(function() {
        alert("the dom of my descendants has been updated!");
      });
      $("#a").domupdate();
    });

To set up a cssupdate event listener:

    $(document).ready(function() {
      $("#a").cssupdate(function() {
        alert("the css of my descendants has been updated!");
      });
      $("#a").cssupdate();
    });

## Options

  View defaults and short descriptions for options in jquery.tie.js. This list 
  is meant to be more informative than the js comments.
  
  **globalListener** (boolean)
    
    Default is false. If true, sets the proxyListener to "body". The tie function 
    will be triggered any time there is a cssupdate/domupdate event in the document.
    
  **onResize** (boolean)
  
    Default is false. If true, triggers the tie function when the window is 
    resized.
    
  **onScroll** (boolean)
  
    Default is false. If true, triggers the tie function when the window is 
    scrolled.
    
  **proxyListener** (selector / DOM Element / function that returns a selector)
  
    Default is the parent of element B. Lets you specify the point in the DOM where 
    the tie function should listen for cssupdate/domupdate events.