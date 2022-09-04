import kotlinx.browser.document
import react.create
import react.dom.client.createRoot

fun main() {
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    val welcome = qmUI.create {
        qmTable = QMtable("0,2,3,6,7,8,10,12,13")
        qmUiState = QMuiState.FINAL
    }
    createRoot(container).render(welcome)
}