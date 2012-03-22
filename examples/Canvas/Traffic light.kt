import jquery.*
import html5.*
import java.util.ArrayList
import js.setInterval
import js.DomElement
import js.setTimeout

fun main(args : Array<String>) {
  val state = CanvasState(getCanvas())
  //Add buttons
  state.addShape(Button("change", "Click here to change light", Vector(120.0, 50.0), Vector(210.0, 50.0)))
  state.addShape(Button("green", "Only GREEN", Vector(340.0, 50.0), Vector(120.0, 50.0)))
  state.addShape(Button("red", "Only RED", Vector(470.0, 50.0), Vector(100.0, 50.0)))
  //Add Kotlin logo as a car
  state.addShape(Car(Vector(500.0, 100.0)))
  //Add Traffic light
  state.addShape(getTrafficLight())
}

//Colors constants
class Colors() {
  val black : String = "#000000"
  val white = "#FFFFFF"
  val grey = "#C0C0C0"
  val red = "#EF4137"
  val yellow = "#FCE013"
  val green = "#0E9648"
}

val colors = Colors()

fun getColors() : Colors {
  return colors
}

class Button(val name : String, val text : String, override var pos : Vector, val size : Vector) : Shape() {
  override fun draw(val state : CanvasState) {
    val context = state.context
    context.fillStyle = getColors().grey
    context.fillPath {
      rect(pos.x, pos.y, size.x, size.y)
    }
    context.fillStyle = getColors().black
    context.font = "bold 15px Georgia, serif"
    context.fillText(text, pos.x + 10, pos.y + 30);
  }

  fun contains(mousePos : Vector) : Boolean = mousePos.isInRect(pos, size)
}

class TrafficLight() : Shape() {
  val list = ArrayList<TrafficLightItem>()
  override var pos = Vector(70.0, 100.0)
  var isRedColor = true
  var canChangeColor = true
  var size = Vector(70.0, 170.0);

  {
    list.add(TrafficLightItem(Vector(pos.x, pos.y), getColors().red, true))
    list.add(TrafficLightItem(Vector(pos.x, pos.y + 50), getColors().yellow, false))
    list.add(TrafficLightItem(Vector(pos.x, pos.y + 100), getColors().green, false))
  }


  override fun draw(state : CanvasState) {
    var context = state.context
    context.shadowed(Vector(- 2.0, 2.0), 0.7) {
      context.fillStyle = getColors().black
      context.fillRect(pos.x - 35, pos.y - 35, size.x, size.y)
      context.fillStyle = getColors().white
      context.fillRect(pos.x - 35 + 1, pos.y - 35 + 1, size.x - 2, size.y - 2)
    }
    for (item in list) {
      item.draw(state)
    }
  }

  fun setOnlyRed(context : Context) {
    if (!isRedColor) {
      canChangeColor = true
      changeColor(context)
    }
    canChangeColor = false
  }

  fun setOnlyGreen(context : Context) {
    if (isRedColor) {
      canChangeColor = true
      changeColor(context)
    }
    canChangeColor = false
  }

  fun changeColor(context : Context) {
    if (canChangeColor) {
      if (isRedColor) {
        changeColorForward(context)
      } else {
        changeColorBackward(context)
      }
    }
  }

  fun changeColorForce(context : Context) {
    canChangeColor = true;
    changeColor(context)
  }

  fun changeColorForward(context : Context) {
    isRedColor = !isRedColor;
    list.get(1).changeColor(context)
    setTimeout({
      list.get(0).changeColor(context)
      list.get(1).changeColor(context)
      list.get(2).changeColor(context)
    }, 500)
  }

  fun changeColorBackward(context : Context) {
    isRedColor = !isRedColor;
    list.get(1).changeColor(context)
    list.get(2).changeColor(context)
    setTimeout({
      list.get(0).changeColor(context)
      list.get(1).changeColor(context)
    }, 500)
  }

  fun canMove() : Boolean {
    return !isRedColor
  }
}


//One element from Traffic light
class TrafficLightItem(override var pos : Vector, val mainColor : String, val isOn : Boolean) : Shape() {
  var isColored = !isOn
  val radius = 25.0

  val firstColor : String get() = if (isOn) mainColor else getColors().white
  var currentColor = firstColor
  val shadowOffset = v(- 5.0, 5.0)

  override fun draw(state : CanvasState) {
    val context = state.context
    context.shadowed(shadowOffset, 0.7) {
      context.fillStyle = getColors().black
      context.fillPath {
        drawCircle(pos, radius)
      }
      if (isColored) {
        currentColor = getColors().white
      } else {
        currentColor = mainColor
      }
      context.fillStyle = currentColor
      context.fillPath {
        drawCircle(pos, radius - 0.5)
      }
    }
  }

  fun changeColor(context : Context) {
    if (isColored) {
      isColored = false;
      currentColor = getColors().white
      context.fillStyle = currentColor
    } else {
      isColored = true;
      currentColor = mainColor
      context.fillStyle = currentColor
    }
  }
}

class Car(override var pos: Vector) : Shape() {
  val relSize : Double = 0.20
  val imageSize = v(377.0, 393.0)
  var size : Vector = imageSize * relSize

  override fun draw(state : CanvasState) {
    size = imageSize * (state.size.x / imageSize.x) * relSize
    state.context.drawImage(getKotlinLogo(), 0.0, 0.0, imageSize.x, imageSize.y, pos.x, pos.y, size.x, size.y)
    if (getTrafficLight().canMove()) {
      move(state)
    }
  }

  var isMoveForward = false;
  var isMoveUp = true;
  
  fun move(state : CanvasState) {
    if ((pos.x > (state.width - 150)) && (isMoveForward)) {
      isMoveForward = false
    } else if ((pos.x < 100) && (!isMoveForward)) {
      isMoveForward = true
    }

    if ((pos.y > 150) && (!isMoveUp)) {
      isMoveUp = true
    } else if ((pos.y < 80) && (isMoveUp)) {
      isMoveUp = false
    }
    var x : Double
    var y : Double
    if (isMoveForward) x = pos.x + 2 else x = pos.x - 2
    if (isMoveUp) y = pos.y - 2 else y = pos.y + 2

    pos = Vector(x, y) ;
  }
}

var trafficLight = TrafficLight()

fun getTrafficLight() : TrafficLight {
  return trafficLight
}

class CanvasState(val canvas : Canvas) {
  val trLight : TrafficLight get() = getTrafficLight();
  val context = getContext()
  var shapes = ArrayList<Shape>()

  var width = canvas.width.toDouble()
  var height = canvas.height.toDouble()

  val size : Vector
  get() = v(width, height)

  fun addShape(shape : Shape) {
    shapes.add(shape)
  }

  {
    jq(canvas).click {
      val mousePos = mousePos(it)
      for (shape in shapes) {
        if (shape is Button && mousePos in shape) {
          val name = shape.name
          when (name) {
            "change" -> trLight.changeColorForce(context)
            "red" -> trLight.setOnlyRed(context)
            "green" -> trLight.setOnlyGreen(context)
            else -> continue
          }

        }
      }
    }

    setInterval({
      draw()
    }, 1000 / 30)

    setInterval({
      trLight.changeColor(context)
    }, 5000)
  }

  fun mousePos(e : MouseEvent) : Vector {
    var offset = Vector()
    var element : DomElement? = canvas
    while (element != null) {
      val el : DomElement = element.sure()
      offset += Vector(el.offsetLeft, el.offsetTop)
      element = el.offsetParent
    }
    return Vector(e.pageX, e.pageY) - offset
  }

  fun draw() {
    clear()
    for (shape in shapes) {
      shape.draw(this)
    }
  }

  fun clear() {
    context.fillStyle = "#FFFFFF"
    context.fillRect(0.0, 0.0, width, height)
    context.strokeStyle = "#000000"
    context.lineWidth = 4.0
    context.strokeRect(0.0, 0.0, width, height)
  }
}

abstract class Shape() {
  abstract var pos : Vector
  abstract fun draw(state : CanvasState)

  // a couple of helper extension methods we'll be using in the derived classes
  fun Context.shadowed(shadowOffset : Vector, alpha : Double, render : Context.() -> Unit) {
    save()
    shadowColor = "rgba(100, 100, 100, $alpha)"
    shadowBlur = 5.0
    shadowOffsetX = shadowOffset.x
    shadowOffsetY = shadowOffset.y
    render()
    restore()
  }

  fun Context.fillPath(constructPath : Context.() -> Unit) {
    beginPath()
    constructPath()
    closePath()
    fill()
  }

  fun Context.drawCircle(position : Vector, rad : Double) {
    arc(position.x, position.y, rad, 0.0, 2 * Math.PI, false)
  }

  fun Context.fillCircle(position : Vector, rad : Double) {
    fillPath {
      drawCircle(position, rad)
    }
  }
}

fun v(x : Double, y : Double) = Vector(x, y)

class Vector(val x : Double = 0.0, val y : Double = 0.0) {
  fun plus(v : Vector) = v(x + v.x, y + v.y)
  fun minus() = v(- x, - y)
  fun minus(v : Vector) = v(x - v.x, y - v.y)
  fun times(koef : Double) = v(x * koef, y * koef)
  fun distanceTo(v : Vector) = Math.sqrt((this - v).sqr)
  fun rotatedBy(theta : Double) : Vector {
    val sin = Math.sin(theta)
    val cos = Math.cos(theta)
    return v(x * cos - y * sin, x * sin + y * cos)
  }

  fun isInRect(topLeft : Vector, size : Vector) = (x >= topLeft.x) && (x <= topLeft.x + size.x) &&
  (y >= topLeft.y) && (y <= topLeft.y + size.y)

  val sqr : Double
  get() = x * x + y * y
  val normalized : Vector
  get() = this * (1.0 / Math.sqrt(sqr))
}
