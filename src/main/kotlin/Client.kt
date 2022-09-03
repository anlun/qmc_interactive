import kotlinx.browser.document
import react.create
import react.dom.client.createRoot

fun main() {
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    val welcome = QM.create {
        minTermInput = "1,2,4,7"
    }
    createRoot(container).render(welcome)
}