import kotlinx.browser.document
import react.create
import react.dom.client.createRoot

fun main() {
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    val welcome = qmUI.create {
        qmTable = QMtable("0,2,3,6,7,8,10,12,13", "1,4,5,9,11,14,15")
//        qmTable = QMtable("0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15")
        qmUiState = QMuiState()
    }
    createRoot(container).render(welcome)
}