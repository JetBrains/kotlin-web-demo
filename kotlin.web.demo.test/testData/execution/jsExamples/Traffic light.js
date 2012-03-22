var classes = function(){
  var tmp$0 = Kotlin.Class.create({initialize:function(x, y){
    this.$x = x;
    this.$y = y;
  }
  , get_x:function(){
    return this.$x;
  }
  , get_y:function(){
    return this.$y;
  }
  , plus:function(v){
    {
      return Anonymous.v(this.get_x() + v.get_x(), this.get_y() + v.get_y());
    }
  }
  , minus:function(){
    {
      return Anonymous.v(-this.get_x(), -this.get_y());
    }
  }
  , minus$0:function(v){
    {
      return Anonymous.v(this.get_x() - v.get_x(), this.get_y() - v.get_y());
    }
  }
  , times:function(koef){
    {
      return Anonymous.v(this.get_x() * koef, this.get_y() * koef);
    }
  }
  , distanceTo:function(v){
    {
      return Math.sqrt(this.minus$0(v).get_sqr());
    }
  }
  , rotatedBy:function(theta){
    {
      var sin = Math.sin(theta);
      var cos = Math.cos(theta);
      return Anonymous.v(this.get_x() * cos - this.get_y() * sin, this.get_x() * sin + this.get_y() * cos);
    }
  }
  , isInRect:function(topLeft, size){
    {
      return this.get_x() >= topLeft.get_x() && this.get_x() <= topLeft.get_x() + size.get_x() && this.get_y() >= topLeft.get_y() && this.get_y() <= topLeft.get_y() + size.get_y();
    }
  }
  , get_sqr:function(){
    {
      return this.get_x() * this.get_x() + this.get_y() * this.get_y();
    }
  }
  , get_normalized:function(){
    {
      return this.times(1 / Math.sqrt(this.get_sqr()));
    }
  }
  });
  var tmp$1 = Kotlin.Class.create({initialize:function(){
  }
  , get_pos:function(){
    return this.$pos_0;
  }
  , set_pos:function(tmp$0){
    this.$pos_0 = tmp$0;
  }
  , draw:function(state){
  }
  , shadowed:function(receiver, shadowOffset, alpha, render){
    {
      receiver.save();
      receiver.shadowColor = 'rgba(100, 100, 100, ' + alpha + ')';
      receiver.shadowBlur = 5;
      receiver.shadowOffsetX = shadowOffset.get_x();
      receiver.shadowOffsetY = shadowOffset.get_y();
      render.call(receiver);
      receiver.restore();
    }
  }
  , fillPath:function(receiver, constructPath){
    {
      receiver.beginPath();
      constructPath.call(receiver);
      receiver.closePath();
      receiver.fill();
    }
  }
  , drawCircle:function(receiver, position, rad){
    {
      receiver.arc(position.get_x(), position.get_y(), rad, 0, 2 * Math.PI, false);
    }
  }
  , fillCircle:function(receiver, position, rad){
    {
      var tmp$0;
      this.fillPath(receiver, (tmp$0 = this , function(){
        {
          tmp$0.drawCircle(receiver, position, rad);
        }
      }
      ));
    }
  }
  });
  var tmp$2 = Kotlin.Class.create(tmp$1, {initialize:function(pos){
    this.$pos = pos;
    this.super_init();
    this.$relSize = 0.2;
    this.$imageSize = Anonymous.v(377, 393);
    this.$size = this.get_imageSize().times(this.get_relSize());
    this.$isMoveForward = false;
    this.$isMoveUp = true;
  }
  , get_pos:function(){
    return this.$pos;
  }
  , set_pos:function(tmp$0){
    this.$pos = tmp$0;
  }
  , get_relSize:function(){
    return this.$relSize;
  }
  , get_imageSize:function(){
    return this.$imageSize;
  }
  , get_size:function(){
    return this.$size;
  }
  , set_size:function(tmp$0){
    this.$size = tmp$0;
  }
  , draw:function(state){
    {
      this.set_size(this.get_imageSize().times(state.get_size().get_x() / this.get_imageSize().get_x()).times(this.get_relSize()));
      state.get_context().drawImage(getKotlinLogo(), 0, 0, this.get_imageSize().get_x(), this.get_imageSize().get_y(), this.get_pos().get_x(), this.get_pos().get_y(), this.get_size().get_x(), this.get_size().get_y());
      if (Anonymous.getTrafficLight().canMove()) {
        this.move(state);
      }
    }
  }
  , get_isMoveForward:function(){
    return this.$isMoveForward;
  }
  , set_isMoveForward:function(tmp$0){
    this.$isMoveForward = tmp$0;
  }
  , get_isMoveUp:function(){
    return this.$isMoveUp;
  }
  , set_isMoveUp:function(tmp$0){
    this.$isMoveUp = tmp$0;
  }
  , move:function(state){
    {
      if (this.get_pos().get_x() > state.get_width() - 150 && this.get_isMoveForward()) {
        this.set_isMoveForward(false);
      }
       else if (this.get_pos().get_x() < 100 && !this.get_isMoveForward()) {
        this.set_isMoveForward(true);
      }
      if (this.get_pos().get_y() > 150 && !this.get_isMoveUp()) {
        this.set_isMoveUp(true);
      }
       else if (this.get_pos().get_y() < 80 && this.get_isMoveUp()) {
        this.set_isMoveUp(false);
      }
      var x;
      var y;
      if (this.get_isMoveForward())
        x = this.get_pos().get_x() + 2;
      else 
        x = this.get_pos().get_x() - 2;
      if (this.get_isMoveUp())
        y = this.get_pos().get_y() - 2;
      else 
        y = this.get_pos().get_y() + 2;
      this.set_pos(new Anonymous.Vector(x, y));
    }
  }
  });
  var tmp$3 = Kotlin.Class.create(tmp$1, {initialize:function(pos, mainColor, isOn){
    this.$pos = pos;
    this.$mainColor = mainColor;
    this.$isOn = isOn;
    this.super_init();
    this.$isColored = !this.get_isOn();
    this.$radius = 25;
    this.$currentColor = this.get_firstColor();
    this.$shadowOffset = Anonymous.v(-5, 5);
  }
  , get_pos:function(){
    return this.$pos;
  }
  , set_pos:function(tmp$0){
    this.$pos = tmp$0;
  }
  , get_mainColor:function(){
    return this.$mainColor;
  }
  , get_isOn:function(){
    return this.$isOn;
  }
  , get_isColored:function(){
    return this.$isColored;
  }
  , set_isColored:function(tmp$0){
    this.$isColored = tmp$0;
  }
  , get_radius:function(){
    return this.$radius;
  }
  , get_firstColor:function(){
    var tmp$0;
    if (this.get_isOn())
      tmp$0 = this.get_mainColor();
    else 
      tmp$0 = Anonymous.getColors().get_white();
    {
      return tmp$0;
    }
  }
  , get_currentColor:function(){
    return this.$currentColor;
  }
  , set_currentColor:function(tmp$0){
    this.$currentColor = tmp$0;
  }
  , get_shadowOffset:function(){
    return this.$shadowOffset;
  }
  , draw:function(state){
    {
      var context = state.get_context();
      var tmp$0_0;
      this.shadowed(context, this.get_shadowOffset(), 0.7, (tmp$0_0 = this , function(){
        {
          context.fillStyle = Anonymous.getColors().get_black();
          var tmp$0;
          tmp$0_0.fillPath(context, (tmp$0 = tmp$0_0 , function(){
            {
              tmp$0.drawCircle(this, tmp$0.get_pos(), tmp$0.get_radius());
            }
          }
          ));
          if (tmp$0_0.get_isColored()) {
            tmp$0_0.set_currentColor(Anonymous.getColors().get_white());
          }
           else {
            tmp$0_0.set_currentColor(tmp$0_0.get_mainColor());
          }
          context.fillStyle = tmp$0_0.get_currentColor();
          var tmp$1;
          tmp$0_0.fillPath(context, (tmp$1 = tmp$0_0 , function(){
            {
              tmp$1.drawCircle(this, tmp$1.get_pos(), tmp$1.get_radius() - 0.5);
            }
          }
          ));
        }
      }
      ));
    }
  }
  , changeColor:function(context){
    {
      if (this.get_isColored()) {
        this.set_isColored(false);
        this.set_currentColor(Anonymous.getColors().get_white());
        context.fillStyle = this.get_currentColor();
      }
       else {
        this.set_isColored(true);
        this.set_currentColor(this.get_mainColor());
        context.fillStyle = this.get_currentColor();
      }
    }
  }
  });
  var tmp$4 = Kotlin.Class.create(tmp$1, {initialize:function(){
    this.super_init();
    this.$list = new Kotlin.ArrayList;
    this.$pos = new Anonymous.Vector(70, 100);
    this.$isRedColor = true;
    this.$canChangeColor = true;
    this.$size = new Anonymous.Vector(70, 170);
    {
      this.get_list().add(new Anonymous.TrafficLightItem(new Anonymous.Vector(this.get_pos().get_x(), this.get_pos().get_y()), Anonymous.getColors().get_red(), true));
      this.get_list().add(new Anonymous.TrafficLightItem(new Anonymous.Vector(this.get_pos().get_x(), this.get_pos().get_y() + 50), Anonymous.getColors().get_yellow(), false));
      this.get_list().add(new Anonymous.TrafficLightItem(new Anonymous.Vector(this.get_pos().get_x(), this.get_pos().get_y() + 100), Anonymous.getColors().get_green(), false));
    }
  }
  , get_list:function(){
    return this.$list;
  }
  , get_pos:function(){
    return this.$pos;
  }
  , set_pos:function(tmp$0){
    this.$pos = tmp$0;
  }
  , get_isRedColor:function(){
    return this.$isRedColor;
  }
  , set_isRedColor:function(tmp$0){
    this.$isRedColor = tmp$0;
  }
  , get_canChangeColor:function(){
    return this.$canChangeColor;
  }
  , set_canChangeColor:function(tmp$0){
    this.$canChangeColor = tmp$0;
  }
  , get_size:function(){
    return this.$size;
  }
  , set_size:function(tmp$0){
    this.$size = tmp$0;
  }
  , draw:function(state){
    {
      var context = state.get_context();
      var tmp$0;
      this.shadowed(context, new Anonymous.Vector(-2, 2), 0.7, (tmp$0 = this , function(){
        {
          context.fillStyle = Anonymous.getColors().get_black();
          context.fillRect(tmp$0.get_pos().get_x() - 35, tmp$0.get_pos().get_y() - 35, tmp$0.get_size().get_x(), tmp$0.get_size().get_y());
          context.fillStyle = Anonymous.getColors().get_white();
          context.fillRect(tmp$0.get_pos().get_x() - 35 + 1, tmp$0.get_pos().get_y() - 35 + 1, tmp$0.get_size().get_x() - 2, tmp$0.get_size().get_y() - 2);
        }
      }
      ));
      var tmp$1;
      {
        tmp$1 = this.get_list().iterator();
        while (tmp$1.hasNext()) {
          var item = tmp$1.next();
          {
            item.draw(state);
          }
        }
      }
    }
  }
  , setOnlyRed:function(context){
    {
      if (!this.get_isRedColor()) {
        this.set_canChangeColor(true);
        this.changeColor(context);
      }
      this.set_canChangeColor(false);
    }
  }
  , setOnlyGreen:function(context){
    {
      if (this.get_isRedColor()) {
        this.set_canChangeColor(true);
        this.changeColor(context);
      }
      this.set_canChangeColor(false);
    }
  }
  , changeColor:function(context){
    {
      if (this.get_canChangeColor()) {
        if (this.get_isRedColor()) {
          this.changeColorForward(context);
        }
         else {
          this.changeColorBackward(context);
        }
      }
    }
  }
  , changeColorForce:function(context){
    {
      this.set_canChangeColor(true);
      this.changeColor(context);
    }
  }
  , changeColorForward:function(context){
    {
      this.set_isRedColor(!this.get_isRedColor());
      this.get_list().get(1).changeColor(context);
      var tmp$0;
      setTimeout((tmp$0 = this , function(){
        {
          tmp$0.get_list().get(0).changeColor(context);
          tmp$0.get_list().get(1).changeColor(context);
          tmp$0.get_list().get(2).changeColor(context);
        }
      }
      ), 500);
    }
  }
  , changeColorBackward:function(context){
    {
      this.set_isRedColor(!this.get_isRedColor());
      this.get_list().get(1).changeColor(context);
      this.get_list().get(2).changeColor(context);
      var tmp$0;
      setTimeout((tmp$0 = this , function(){
        {
          tmp$0.get_list().get(0).changeColor(context);
          tmp$0.get_list().get(1).changeColor(context);
        }
      }
      ), 500);
    }
  }
  , canMove:function(){
    {
      return !this.get_isRedColor();
    }
  }
  });
  var tmp$5 = Kotlin.Class.create(tmp$1, {initialize:function(name_0, text, pos, size){
    this.$name = name_0;
    this.$text = text;
    this.$pos = pos;
    this.$size = size;
    this.super_init();
  }
  , get_name:function(){
    return this.$name;
  }
  , get_text:function(){
    return this.$text;
  }
  , get_pos:function(){
    return this.$pos;
  }
  , set_pos:function(tmp$0){
    this.$pos = tmp$0;
  }
  , get_size:function(){
    return this.$size;
  }
  , draw:function(state){
    {
      var context = state.get_context();
      context.fillStyle = Anonymous.getColors().get_grey();
      var tmp$0;
      this.fillPath(context, (tmp$0 = this , function(){
        {
          this.rect(tmp$0.get_pos().get_x(), tmp$0.get_pos().get_y(), tmp$0.get_size().get_x(), tmp$0.get_size().get_y());
        }
      }
      ));
      context.fillStyle = Anonymous.getColors().get_black();
      context.font = 'bold 15px Georgia, serif';
      context.fillText(this.get_text(), this.get_pos().get_x() + 10, this.get_pos().get_y() + 30);
    }
  }
  , contains:function(mousePos){
    {
      return mousePos.isInRect(this.get_pos(), this.get_size());
    }
  }
  });
  var tmp$6 = Kotlin.Class.create({initialize:function(canvas){
    this.$canvas = canvas;
    this.$context = getContext();
    this.$shapes = new Kotlin.ArrayList;
    this.$width = this.get_canvas().width;
    this.$height = this.get_canvas().height;
    {
      var tmp$0_0;
      $(this.get_canvas()).click((tmp$0_0 = this , function(it){
        {
          var mousePos = tmp$0_0.mousePos_0(it);
          var tmp$0;
          {
            tmp$0 = tmp$0_0.get_shapes().iterator();
            while (tmp$0.hasNext()) {
              var shape = tmp$0.next();
              {
                if (Kotlin.isType(shape, Anonymous.Button) && shape.contains(mousePos)) {
                  var name_0 = shape.get_name();
                  var tmp$1;
                  var tmp$2;
                  for (tmp$1 = 0; tmp$1 < 4; ++tmp$1) {
                    if (tmp$1 == 0)
                      if (name_0 == 'change') {
                        tmp$2 = tmp$0_0.get_trLight().changeColorForce(tmp$0_0.get_context());
                        break;
                      }
                    if (tmp$1 == 1)
                      if (name_0 == 'red') {
                        tmp$2 = tmp$0_0.get_trLight().setOnlyRed(tmp$0_0.get_context());
                        break;
                      }
                    if (tmp$1 == 2)
                      if (name_0 == 'green') {
                        tmp$2 = tmp$0_0.get_trLight().setOnlyGreen(tmp$0_0.get_context());
                        break;
                      }
                    if (tmp$1 == 3)
                      continue;
                  }
                  tmp$2;
                }
              }
            }
          }
        }
      }
      ));
      var tmp$1_0;
      setInterval((tmp$1_0 = this , function(){
        {
          tmp$1_0.draw();
        }
      }
      ), 1000 / 30);
      var tmp$2_0;
      setInterval((tmp$2_0 = this , function(){
        {
          tmp$2_0.get_trLight().changeColor(tmp$2_0.get_context());
        }
      }
      ), 5000);
    }
  }
  , get_canvas:function(){
    return this.$canvas;
  }
  , get_trLight:function(){
    {
      return Anonymous.getTrafficLight();
    }
  }
  , get_context:function(){
    return this.$context;
  }
  , get_shapes:function(){
    return this.$shapes;
  }
  , set_shapes:function(tmp$0){
    this.$shapes = tmp$0;
  }
  , get_width:function(){
    return this.$width;
  }
  , set_width:function(tmp$0){
    this.$width = tmp$0;
  }
  , get_height:function(){
    return this.$height;
  }
  , set_height:function(tmp$0){
    this.$height = tmp$0;
  }
  , get_size:function(){
    {
      return Anonymous.v(this.get_width(), this.get_height());
    }
  }
  , addShape:function(shape){
    {
      this.get_shapes().add(shape);
    }
  }
  , mousePos_0:function(e){
    {
      var offset = new Anonymous.Vector(0, 0);
      var element = this.get_canvas();
      while (element != null) {
        var el = Kotlin.sure(element);
        offset = offset.plus(new Anonymous.Vector(el.offsetLeft, el.offsetTop));
        element = el.offsetParent;
      }
      return (new Anonymous.Vector(e.pageX, e.pageY)).minus$0(offset);
    }
  }
  , draw:function(){
    {
      this.clear();
      var tmp$0;
      {
        tmp$0 = this.get_shapes().iterator();
        while (tmp$0.hasNext()) {
          var shape = tmp$0.next();
          {
            shape.draw(this);
          }
        }
      }
    }
  }
  , clear:function(){
    {
      this.get_context().fillStyle = '#FFFFFF';
      this.get_context().fillRect(0, 0, this.get_width(), this.get_height());
      this.get_context().strokeStyle = '#000000';
      this.get_context().lineWidth = 4;
      this.get_context().strokeRect(0, 0, this.get_width(), this.get_height());
    }
  }
  });
  var tmp$7 = Kotlin.Class.create({initialize:function(){
    this.$black = '#000000';
    this.$white = '#FFFFFF';
    this.$grey = '#C0C0C0';
    this.$red = '#EF4137';
    this.$yellow = '#FCE013';
    this.$green = '#0E9648';
  }
  , get_black:function(){
    return this.$black;
  }
  , get_white:function(){
    return this.$white;
  }
  , get_grey:function(){
    return this.$grey;
  }
  , get_red:function(){
    return this.$red;
  }
  , get_yellow:function(){
    return this.$yellow;
  }
  , get_green:function(){
    return this.$green;
  }
  });
  return {Button:tmp$5, CanvasState:tmp$6, Colors:tmp$7, Shape:tmp$1, Car:tmp$2, TrafficLightItem:tmp$3, TrafficLight:tmp$4, Vector:tmp$0};
}
();
var Anonymous = Kotlin.Namespace.create({initialize:function(){
  this.$colors = new Anonymous.Colors;
  this.$trafficLight = new Anonymous.TrafficLight;
}
, main:function(args){
  {
    var state = new Anonymous.CanvasState(getCanvas());
    state.addShape(new Anonymous.Button('change', 'Click here to change light', new Anonymous.Vector(120, 50), new Anonymous.Vector(210, 50)));
    state.addShape(new Anonymous.Button('green', 'Only GREEN', new Anonymous.Vector(340, 50), new Anonymous.Vector(120, 50)));
    state.addShape(new Anonymous.Button('red', 'Only RED', new Anonymous.Vector(470, 50), new Anonymous.Vector(100, 50)));
    state.addShape(new Anonymous.Car(new Anonymous.Vector(500, 100)));
    state.addShape(Anonymous.getTrafficLight());
  }
}
, get_colors:function(){
  return this.$colors;
}
, getColors:function(){
  {
    return Anonymous.get_colors();
  }
}
, get_trafficLight:function(){
  return this.$trafficLight;
}
, set_trafficLight:function(tmp$0){
  this.$trafficLight = tmp$0;
}
, getTrafficLight:function(){
  {
    return Anonymous.get_trafficLight();
  }
}
, v:function(x, y){
  {
    return new Anonymous.Vector(x, y);
  }
}
}, {Colors:classes.Colors, Button:classes.Button, TrafficLight:classes.TrafficLight, TrafficLightItem:classes.TrafficLightItem, Car:classes.Car, CanvasState:classes.CanvasState, Shape:classes.Shape, Vector:classes.Vector});
Anonymous.initialize();

Kotlin.System.flush();
var args = [];
Anonymous.main(args);
Kotlin.System.output();
