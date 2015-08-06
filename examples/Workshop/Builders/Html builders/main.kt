import kotlin.browser.document

fun main(args: Array<String>){
    document.body!!.style.overflowY = ""
    document.body!!.innerHTML = renderProductTable()
}