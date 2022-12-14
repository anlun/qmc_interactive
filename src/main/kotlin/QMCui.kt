import csstype.*
import csstype.Display.Companion.inlineBlock
import emotion.react.css
import org.w3c.dom.HTMLOptGroupElement
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.events.MouseEvent
import react.ChildrenBuilder
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.OptionHTMLAttributes
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.hr
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import react.dom.html.TableHTMLAttributes
import react.useState

fun List<String>.concatBySeparator(separator : String) : String {
    if (this.isEmpty()) return ""
    val sb = StringBuilder(this[0])
    this.drop(1).forEach { s ->
        if (s != "") {
            sb.append(separator, s)
        }
    }
    return sb.toString()
}

class QMuiState(paramShows : Array<Boolean> = Array(SHOWS_SIZE) {false})
{
    private var shows: Array<Boolean> =
        if (paramShows.size == SHOWS_SIZE) {
            paramShows
        } else {
            Array(SHOWS_SIZE) { true }
        }
    companion object {
        const val SHOWS_SIZE = 7

        const val DONT_CARE = 0
        const val TRUTH_TABLE = 1
        const val MINTERMS = 2
        const val COMBINED_MINTERMS = 3
        const val PRIME_IMPL_CHART = 4
        const val NON_ESS_PRIME_IMPL_CHART = 5
        const val FINAL_SOLUTION = 6
    }

    fun getFirstUnset() : Int? {
        (TRUTH_TABLE..FINAL_SOLUTION).forEach {
            if (!get(it)) return it
        }
        return null
    }

    fun get(i : Int) : Boolean = shows[i]
    fun set(i : Int, new_value : Boolean) : QMuiState {
        val newShows = shows.copyOf()
        newShows[i] = new_value
        return QMuiState(newShows)
    }
}

external interface QMprops : Props {
    var qmTable   : QMtable
    var lastDontCareInput : String
    var qmUiState : QMuiState
}
val qmUI = FC<QMprops> { props ->
    var qmTable by useState(props.qmTable)
    var lastDontCareInput by useState(props.lastDontCareInput)
    var qmUiState by useState(props.qmUiState)
    fun ChildrenBuilder.createExampleButton(text: String, qmTable_new: QMtable) {
        button {
            +text
            onClick = { _ ->
                if (qmTable_new.dontCareInput != "") {
                    qmUiState = qmUiState.set(QMuiState.DONT_CARE, true)
                }
                qmTable = qmTable_new
            }
        }
    }

    fun ChildrenBuilder.createStateCheckbox(text: String, stateComponent: Int) {
        input {
            type = InputType.checkbox
            checked = qmUiState.get(stateComponent)
            onClick = { event ->
                qmUiState = qmUiState.set(stateComponent, event.currentTarget.checked)
            }
        }
        +text
        br {}
    }

    fun ChildrenBuilder.createInputBlock() {
        +"f("
        b { +MinTerm4.argString }
        +") = ?? m("
        input {
            type = InputType.text
            onChange = { event ->
                qmTable = QMtable(event.target.value, qmTable.dontCareInput)
            }
            value = qmTable.minTermInput
        }
        +")"
        if (qmUiState.get(QMuiState.DONT_CARE)) {
            +" + ?? d("
            input {
                type = InputType.text
                onChange = { event ->
                    qmTable = QMtable(qmTable.minTermInput, event.target.value)
                }
                value = qmTable.dontCareInput
            }
            +") "
        }
    }

    fun TableHTMLAttributes<HTMLTableElement>.tableCss() {
        css {
            display = Display.inlineTable
            margin = 20.px
//                font - size: 18pt;
            lineHeight = 22.px
            padding = 0.px
//                border - collapse: collapse;
//                border - spacing: 0;
//                font - family: "Times New Roman", Georgia, Serif;
        }
    }

    fun ChildrenBuilder.createTable(header: List<String>, columns: List<List<String>>) {
        val emptyCellString = ""
        fun longestColumnSize(): Int {
            var maxSize = 0
            columns.forEach { column ->
                maxSize = kotlin.math.max(maxSize, column.size)
            }
            return maxSize
        }
        table {
            tableCss()
            thead {
                tr {
                    header.forEach {
                        th {
                            +it
                        }
                    }
                }
            }
            tbody {
                (0 until longestColumnSize()).forEach { currentRow ->
                    tr {
                        columns.forEach { column ->
                            td {
//                                css {
//                                    padding = 5.px
//                                    lineHeight = 15.px
//                                    overflow = Overflow.hidden
//                                }
                                +(column.getOrNull(currentRow) ?: emptyCellString)
                            }
                        }
                    }
                }
            }
        }
    }

    fun ChildrenBuilder.createTruthTableBlock() {
        div {
            css {
                display = inlineBlock
            }
            createStateCheckbox("Truth table", QMuiState.TRUTH_TABLE)
            if (qmUiState.get(QMuiState.TRUTH_TABLE)) {
                createTable(listOf("", "abcd", "f"),
                    listOf(MinTerm4.range.map { "$it: " },
                        MinTerm4.range.map { it.toMinTerm4String() },
                        MinTerm4.range.map {
                            if (qmTable.minTermList.contains(it)) {
                                "1"
                            } else if (qmTable.dontCareList.contains(it)) {
                                "-"
                            } else {
                                "0"
                            }
                        }
                    )
                )
            }
        }
    }

    fun ChildrenBuilder.createMinTermsBlock_old() {
        val header = listOf("", "abcd") +
                if (qmUiState.get(QMuiState.COMBINED_MINTERMS)) {
                    (1..4).flatMap {
                        listOf("Repr. $it", "Combine $it")
                    }
                } else listOf()

        fun reprCombineColumns(l: List<MinTerm4>): List<List<String>> {
            return listOf(l.map { it.toIntRepresentatives().toString() }, l.map { it.toString() })
        }

        val columns: List<List<String>> =
            listOf(qmTable.combine0List.map { it.toIntRepresentatives().toString() },
                qmTable.combine0List.map { it.toString() }) +
                    if (qmUiState.get(QMuiState.COMBINED_MINTERMS)) {
                        reprCombineColumns(qmTable.combine1List) +
                                reprCombineColumns(qmTable.combine2List) +
                                reprCombineColumns(qmTable.combine3List) +
                                reprCombineColumns(qmTable.combine4List)
                    } else {
                        listOf()
                    }
//        +"MinTerms"
        createTable(header, columns)
    }

    fun MinTerm4.implicantIntReprsString() : String {
        return toIntRepresentatives().map { it.toString() }.concatBySeparator(",") + ": "
    }
    fun ChildrenBuilder.createMinTermsBlock() {
        fun createCombineTable(isControl : Boolean, flag: Int, level: Int, l: List<MinTerm4>) {
            if (l.isEmpty()) return
            fun reprCombineColumns(l: List<MinTerm4>): List<List<String>> {
                val isPrimeImplicant: List<String> = l.map { if (qmTable.primeImplicants.contains(it)) "???" else "???" }
                val implicantReprs: List<String> = l.map { it.implicantIntReprsString() }
                val condImplicantReprs: List<List<String>> = listOf(implicantReprs)
                return condImplicantReprs + listOf(l.map { it.toString() }, isPrimeImplicant)
            }

            val header = listOf("", "abcd", "")
            val columns = reprCombineColumns(l)
            div {
                css {
                    display = inlineBlock
                    margin = 20.px
                }
                val msg = "Implicants $level-level"
                if (isControl) {
                    createStateCheckbox(msg, flag)
                } else {
                    +msg
                    br {}
                }
                if (qmUiState.get(flag)) {
                    createTable(header, columns)
                }
            }
        }
        createCombineTable(true, QMuiState.MINTERMS, 0, qmTable.combine0List)
        createCombineTable(true, QMuiState.COMBINED_MINTERMS, 1, qmTable.combine1List)
        createCombineTable(false, QMuiState.COMBINED_MINTERMS, 2, qmTable.combine2List)
        createCombineTable(false, QMuiState.COMBINED_MINTERMS, 3, qmTable.combine3List)
        createCombineTable(false, QMuiState.COMBINED_MINTERMS, 4, qmTable.combine4List)
    }

    fun ChildrenBuilder.createListBlock(title: String, l: List<String>) {
        +title
        +l.concatBySeparator(", ")
    }

    fun ChildrenBuilder.createImplChartBlock(
        flag : Int,
        header: String,
        header_col1: String,
        header_col2: String,
        xl: List<Int>, yl: List<MinTerm4>,
        implChart: Set<Pair<Int, MinTerm4>>
    ) {
        val headerList: List<String> =
            listOf(header_col1, header_col2) + xl.map { "m${it}" } + listOf("")
        val columns: List<List<String>> =
            listOf(yl.map { it.implicantIntReprsString() },
                yl.map { it.toString() },
            ) +
                    xl.map { i ->
                        yl.map { mt ->
                            if (implChart.contains(Pair(i, mt))) {
                                "x"
                            } else {
                                ""
                            }
                        }
                    } +
            listOf(yl.map { it.toABCD() })
        createStateCheckbox(header, flag)
        if (qmUiState.get(flag)) {
            createTable(headerList, columns)
        }
    }

    fun ChildrenBuilder.createPrimeImplChartBlock() {
        val xl = qmTable.minTermList
        val yl = qmTable.primeImplicants
        createImplChartBlock(
            QMuiState.PRIME_IMPL_CHART,
            "Prime implicants' chart",
            "", "",
            xl, yl,
            qmTable.primeImplicantChart
        )
    }

    fun ChildrenBuilder.createNonEssentialPrimeImplChartBlock() {
        val xl = qmTable.nonEssentialPrimeImplicantMinTerms
        val yl = qmTable.nonEssentialPrimeImplicants
        createImplChartBlock(
            QMuiState.NON_ESS_PRIME_IMPL_CHART,
            "Non-essential prime implicants' chart",
            "", "",
            xl, yl,
            qmTable.nonEssentialPrimeImplicantChart
        )
    }

    fun ChildrenBuilder.createDontCareCheckbox() {
        input {
            type = InputType.checkbox
            checked = qmUiState.get(QMuiState.DONT_CARE)
            onClick = { event ->
                qmUiState = qmUiState.set(QMuiState.DONT_CARE, event.currentTarget.checked)
                if (!event.currentTarget.checked) {
                    lastDontCareInput = qmTable.dontCareInput
                    qmTable = QMtable(qmTable.minTermInput, "")
                } else if (qmTable.dontCareInput == "") {
                    qmTable = QMtable(qmTable.minTermInput, lastDontCareInput)
                }
            }
        }
        +"Use 'Don't care' values"
    }

    fun ChildrenBuilder.createStateControlBlock() {
        table {
            tableCss()
//            tr {
//                createDontCareCheckbox()
//            }
//            tr {
//                createStateCheckbox("Step 1. Show the truth table of f", QMuiState.TRUTH_TABLE)
//            }
//            tr {
//                createStateCheckbox("Step 2. Show minterms", QMuiState.MINTERMS)
//            }
//            tr {
//                createStateCheckbox("Step 3. Show combined minterms", QMuiState.COMBINED_MINTERMS)
//            }
//            tr {
//                createStateCheckbox("Step 4. Show minterms representatives", QMuiState.MINTERMS_REPR)
//            }
//            tr {
//                createStateCheckbox("Step 4. Show prime implicants' chart", QMuiState.PRIME_IMPL_CHART)
//            }
//            tr {
//                createStateCheckbox("Step 5. Show non-essential prime implicants' chart", QMuiState.NON_ESS_PRIME_IMPL_CHART)
//            }
            tr {
                createStateCheckbox("Step 6. Show final solution", QMuiState.FINAL_SOLUTION)
            }
        }
    }
    fun ChildrenBuilder.createExampleSelectionBlock_new() {
        fun createOnClick(qmTable_new: QMtable) {
            if (qmTable_new.dontCareInput != "") {
                qmUiState = qmUiState.set(QMuiState.DONT_CARE, true)
            }
            qmTable = qmTable_new
        }
        +"Example inputs:"
        select {
            option {
                +"USER INPUT"
            }
            option {
                +"(1) 7-led A"
                onClick = {
                    createOnClick(QMtable("0,2,3,5,6,7,8,9", ""))
                }
                onSelect = {
                    createOnClick(QMtable("0,2,3,5,6,7,8,9", ""))
                }
            }
            option {
                +"test 2"
            }
        }
    }

    fun ChildrenBuilder.createExampleSelectionBlock() {
        +"Example inputs: "
        createExampleButton("(1) 7-led A", QMtable("0,2,3,5,6,7,8,9", ""))
        + " "
        createExampleButton("(2) 7-led A w/ Don't care", QMtable("0,2,3,5,6,7,8,9", "10-15"))
        + " "
        createExampleButton("(3) Non-Essential Prime implicant chart", QMtable("0,1,2,5,6,7", ""))
    }
    fun ChildrenBuilder.createResultBlock() {
        createStateCheckbox("Result", QMuiState.FINAL_SOLUTION)
        if (qmUiState.get(QMuiState.FINAL_SOLUTION)) {
            table {
                tableCss()
                tr {
                    td { +"A minimal full solution: " }
                    td {
                        +qmTable.minimalFullSolution.map { it.toABCD() }.concatBySeparator(" + ")
                    }
                }
                tr {
                    td { +"Initial representation: " }
                    td {
                        +(qmTable.initialMinTerms.map { it.toABCD() }.concatBySeparator(" + "))
                    }
                }
            }
        }
    }

    val qmcInterpreterLink = "podkopaev.net/qmc"
    h2 { +"Quine???McCluskey (QMC) algorithm" }
    div {
        css {
            fontSize = FontSize.small
        }
        +"An interactive QMC interpreter written by "
        a {
            +"Anton Podkopaev"
            href = "https://podkopaev.net"
        }
        +" and hosted at "
        a { +qmcInterpreterLink
            href = "https://$qmcInterpreterLink"
        }
        +"."
        br{}
        +"Source code of this webpage could be found  "
        a {
            +"here"
            href = "https://github.com/anlun/qmc_interactive"
        }
        +"."
    }
    br {}
    div {
        css {
            display = inlineBlock
        }
        createExampleSelectionBlock()
//        createStateControlBlock()
    }
    br {}
    createDontCareCheckbox()
    br {}
    br {}
    createInputBlock()
    br {}
    div {
        css {
            display = inlineBlock
        }
        createTruthTableBlock()
        createMinTermsBlock()
    }
//    if (qmUiState.get(QMuiState.PRIME_IMPL)) {
//        br {}
//        hr {}
//        createListBlock("Prime Implicants: ", qmTable.primeImplicants.map { it.toString() })
//    }
    br {}
    div {
        css {
            display = inlineBlock
        }
        div {
            css {
                display = inlineBlock
            }
            createPrimeImplChartBlock()
        }
        if (qmTable.nonEssentialSolutions.isNotEmpty()) {
            + " "
            div {
                css {
                    display = inlineBlock
                }
                createNonEssentialPrimeImplChartBlock()
            }
        }
    }
    if (qmUiState.get(QMuiState.PRIME_IMPL_CHART)) {
        br {}
        hr {}
        val epiText = "Essential Prime Implicants: "
        if (qmTable.essentialPrimeImplicants.isNotEmpty()) {
            createListBlock(epiText, qmTable.essentialPrimeImplicants.map { it.toABCD() })
        } else {
            +"$epiText EMPTY"
        }
    }
    if (qmTable.nonEssentialSolutions.isNotEmpty() && qmUiState.get(QMuiState.NON_ESS_PRIME_IMPL_CHART)) {
        br {}
        hr {}
        +"Solutions restricted to non-essential prime implicants:"
        br {}
        br {}
        qmTable.nonEssentialSolutions.forEachIndexed { i, mtl ->
            createListBlock("${i + 1}) ",
                mtl.map { it.toABCD() }
            )
            br {}
        }
    }
    br {}
    br {}
    createResultBlock()

    val unsetState = qmUiState.getFirstUnset()
    if (unsetState != null) {
        br {}
        button {
            +"Next step"
            onClick = { _ ->
                qmUiState = qmUiState.set(unsetState, true)
            }
        }
    }
}