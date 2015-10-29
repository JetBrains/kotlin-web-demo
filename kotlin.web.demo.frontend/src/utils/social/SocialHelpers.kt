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

package utils.social

import kotlinx.html.dom.*
import kotlinx.html.js.*
import org.w3c.dom.HTMLAnchorElement
import utils.encodeURIComponent
import kotlin.browser.document
import kotlin.browser.window


//fun createTweeterShareLink(
//        tweetText: String = "",
//        url: String = window.location.href,
//        width: Int  = 575,
//        height: Int = 400,
//        hashTags: Set<String> = emptySet<String>(),
//        textContent: String = "Tweet",
//        via: String = ""
//) = document.create.a {
//    +textContent
//    classes = setOf("twitter")
//    target = "_blank"
//    href = "https://twitter.com/intent/tweet?" +
//            "text=${encodeURIComponent(tweetText)}&" +
//            "url=${encodeURIComponent(url)}&" +
//            "hashtags=${encodeURIComponent(hashTags.join(separator = ","))}&" +
//            "via=${encodeURIComponent(via)}"
//
//    val left   = (window.innerWidth - width)  / 2
//    val top    = (window.innerHeight - height)  / 2
//    onClickFunction = { e ->
//        e.preventDefault()
//        window.open(this.href, "Tweet", "height=$height,width=$width,left=$left,top=$top")
//    }
//}

fun createFacebookShareLink(
        textContent: String = "FaceBook",
        link: String = window.location.href,
        redirectURL: String = window.location.href,
        pictureURL: String = window.location.href + "/static/images/1level.gif",
        name: String = "",
        caption: String = "",
        description: String = "",
        width: Int  = 575,
        height: Int = 400
): HTMLAnchorElement {
    val a = document.create.a {
        +textContent
        classes = setOf("facebook")
        target = "_blank"
        href = "https://www.facebook.com/dialog/feed?" +
                "app_id=957678017630094&" +
                "display=popup&" +
                "link=${encodeURIComponent(link)}&" +
                "redirect_uri=${encodeURIComponent(redirectURL)}&" +
//                "picture=${encodeURIComponent(pictureURL)}&" +
                "name=$name&" +
                "caption=$caption&" +
                "description=$description"
    }
    val left   = (window.innerWidth - width)  / 2
    val top    = (window.innerHeight - height)  / 2
    a.onclick = { e ->
        e.preventDefault()
        window.open(a.href, "FB", "height=$height,width=$width,left=$left,top=$top")
    }
    return a
}