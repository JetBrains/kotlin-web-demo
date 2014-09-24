/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
This example is based on example from html5 canvas2D docs:
  http://www.w3.org/TR/2dcontext/
Note that only a subset of the api is supported for now.
*/

package fancylines

import kotlin.js.dom.html.window
import kotlin.js.dom.html5.*
import jquery.*

fun main(args: Array<String>) {
  jq {
    FancyLines().run()
  }
}

val canvas: HTMLCanvasElement
  get() {
    return window.document.getElementsByTagName("canvas").item(0) as HTMLCanvasElement
  }

class FancyLines() {
  val context = canvas.getContext("2d")!!
  val height = canvas.height
  val width = canvas.width
  var x = width * Math.random()
  var y = height * Math.random()
  var hue = 0;

  fun line() {
    context.save();

    context.beginPath();

    context.lineWidth = 20.0 * Math.random();
    context.moveTo(x.toInt(), y.toInt());

    x = width * Math.random();
    y = height * Math.random();

    context.bezierCurveTo(width * Math.random(), height * Math.random(),
                width * Math.random(), height * Math.random(), x, y);

    hue += (Math.random() * 10).toInt();

    context.strokeStyle = "hsl($hue, 50%, 50%)";

    context.shadowColor = "white";
    context.shadowBlur = 10.0;

    context.stroke();

    context.restore();
  }

  fun blank() {
    context.fillStyle = "rgba(255,255,1,0.1)";
    context.fillRect(0, 0, width, height);
  }

  fun run() {
    window.setInterval({ line() }, 40);
    window.setInterval({ blank() }, 100);
  }
}
