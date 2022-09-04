sealed class Signal {
    object Dash : Signal()
    object One  : Signal()
    object Zero : Signal()

    companion object {
        fun fromInt(v : Int) : Signal? {
            if (v == 0) return Zero
            if (v == 1) return One
            return null
        }
    }

    fun combine(other : Signal) : Signal =
        if (this == other) { this }
        else               { Dash }

    override fun toString() : String =
        when (this) {
            is Dash -> "-"
            is One  -> "1"
            is Zero -> "0"
        }

    fun toIntRepresentatives() : List<Int> =
        when (this) {
            is Zero -> listOf(0)
            is One  -> listOf(1)
            is Dash -> listOf(0, 1)
        }
}

fun List<Signal>.toIntRepresentatives() : List<Int> {
    if (this.isEmpty()) return emptyList()
    return this.drop(1).fold(this[0].toIntRepresentatives()) { l, s ->
        l.flatMap {
            when (s) {
                is Signal.Zero -> listOf(it * 2)
                is Signal.One  -> listOf(it * 2 + 1)
                is Signal.Dash  -> listOf(it * 2, it * 2 + 1)
            }
        }
    }
    .sorted()
}

class MinTerm4(
    val A : Signal,
    val B : Signal,
    val C : Signal,
    val D : Signal
    )
{
    companion object {
        private const val size  : Int = 16 // 2 ** 4
        val range = 0 until size
        const val argString = "A, B, C, D"

        fun fromInt(v : Int) : MinTerm4? {
            if (v !in range) return null
            val a = Signal.fromInt((v / 8) % 2) ?: return null
            val b = Signal.fromInt((v / 4) % 2) ?: return null
            val c = Signal.fromInt((v / 2) % 2) ?: return null
            val d = Signal.fromInt(      v % 2) ?: return null
            return MinTerm4(a, b, c, d)
        }
    }

    fun distance(other : MinTerm4?) : Int {
        if (other == null) return size
        var result = 0
        if (this.A != other.A) { result += 1 }
        if (this.B != other.B) { result += 1 }
        if (this.C != other.C) { result += 1 }
        if (this.D != other.D) { result += 1 }
        return result
    }
    fun isDistance1(other : MinTerm4?) : Boolean {
        return distance(other) == 1
    }

    fun combine(other : MinTerm4?) : MinTerm4? {
        if (other == null || !this.isDistance1(other)) return null
        return MinTerm4(this.A.combine(other.A)
                      , this.B.combine(other.B)
                      , this.C.combine(other.C)
                      , this.D.combine(other.D)
                      )
    }

    override fun toString(): String {
        return A.toString() + B.toString() + C.toString() + D.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is MinTerm4) return false
        return this.A == other.A && this.B == other.B
            && this.C == other.C && this.D == other.D
    }

    override fun hashCode(): Int {
        val hashSeed = 239
        val hashAB   = hashSeed * A.hashCode() + B.hashCode()
        val hashABC  = hashSeed * hashAB       + C.hashCode()
        val hashABCD = hashSeed * hashABC      + D.hashCode()
        return hashSeed * hashABCD
    }

    fun toIntRepresentatives() : List<Int> =
        listOf(A, B, C, D).toIntRepresentatives()
}
fun Int.toMinTerm4String() : String = MinTerm4.fromInt(this).toString()
fun Int.minTerm4CountOnes() : Int = toMinTerm4String().count { it == '1' }
fun String.toIntInMinTerm4Range() : Int? {
    try {
        val v = this.toInt()
        if (v !in MinTerm4.range) return null
        return v
    } catch (e : NumberFormatException) {
        return null
    }
}
fun Boolean.toSymbol() : String =
    if (this) "1" else "0"

class QMtable(val minTermInput : String) {
    companion object {
        private fun combineListWithItself(l : List<MinTerm4>) : List<MinTerm4> =
            l.flatMap { mt1 ->
                l.mapNotNull { mt2 ->
                    mt1.combine(mt2)
                }
            }
            .distinct()
    }

    val minTermList : List<Int> =
        minTermInput
            .split(",")
            .mapNotNull { it.trim().toIntInMinTerm4Range() }
            .sorted()
            .distinct()

    val combine0List : List<MinTerm4> =
        minTermList.sortedBy { it.minTerm4CountOnes() }
                   .mapNotNull { MinTerm4.fromInt(it) }
    val combine1List : List<MinTerm4> = combineListWithItself(combine0List)
    val combine2List : List<MinTerm4> = combineListWithItself(combine1List)
    val combine3List : List<MinTerm4> = combineListWithItself(combine2List)
    val combine4List : List<MinTerm4> = combineListWithItself(combine3List)
}