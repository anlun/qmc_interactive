import kotlinx.browser.document
import react.create
import react.dom.client.createRoot

fun main() {
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    val welcome = QM.create {
        minItemInput = "1,2,3,4"
    }
    createRoot(container).render(welcome)
}