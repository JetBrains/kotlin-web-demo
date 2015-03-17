/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
  This shows simple text floating around.
*/
package hello

import kotlin.js.dom.html.window
import kotlin.js.dom.html5.*
import jquery.*

val canvas: HTMLCanvasElement
    get() {
        return window.document.getElementsByTagName("canvas").item(0) as HTMLCanvasElement
    }

val context: CanvasContext
    get() {
        return canvas.getContext("2d")!!
    }


val width: Double
    get() {
        return canvas.width
    }

val height: Double
    get() {
        return canvas.height
    }


// class representing a floating text
class HelloKotlin() {
    var relX = 0.2 + 0.2 * Math.random()
    var relY = 0.4 + 0.2 * Math.random()

    val absX: Double
        get() = (relX * width)
    val absY: Double
        get() = (relY * height)

    var relXVelocity = randomVelocity()
    var relYVelocity = randomVelocity()


    val message = "Hello, Kotlin!"
    val textHeightInPixels = 20
    init {
        context.font = "bold ${textHeightInPixels}px Georgia, serif"
    }
    val textWidthInPixels = context.measureText(message)!!.width

    fun draw() {
        context.save()
        move()
        // if you using chrome chances are good you wont see the shadow
        context.shadowColor = "#000000"
        context.shadowBlur = 5.0
        context.shadowOffsetX = -4.0
        context.shadowOffsetY = 4.0
        context.fillStyle = "rgb(242,160,110)"
        context.fillText(message, absX.toInt(), absY.toInt())
        context.restore()
    }

    fun move() {
        val relTextWidth = textWidthInPixels / width
        if (relX > (1.0 - relTextWidth - relXVelocity.abs) || relX < relXVelocity.abs) {
            relXVelocity *= -1
        }
        val relTextHeight = textHeightInPixels / height
        if (relY > (1.0 - relYVelocity.abs) || relY < relYVelocity.abs + relTextHeight) {
            relYVelocity *= -1
        }
        relX += relXVelocity
        relY += relYVelocity
    }

    fun randomVelocity() = 0.03 * Math.random() * (if (Math.random() < 0.5) 1 else -1)


    val Double.abs: Double
        get() = if (this > 0) this else -this
}

fun renderBackground() {
    context.save()
    context.fillStyle = "#5C7EED"
    context.fillRect(0, 0, width, height)
    context.restore()
}

fun main(args: Array<String>) {
    jq {
        val interval = 50
        // we pass a literal that constructs a new HelloKotlin object
        val logos = Array(3) {
            HelloKotlin()
        }

        window.setInterval({
                               renderBackground()
                               for (logo in logos) {
                                   logo.draw()
                               }
                           }, interval)
    }
}


