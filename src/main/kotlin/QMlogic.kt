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
}

class MinTerm3(
    val A : Signal,
    val B : Signal,
    val C : Signal
    )
{
    companion object {
        private val size : Int = 8 // 2 ** 3

        fun fromInt(v : Int) : MinTerm3? {
            if (v < 0 || v >= size) return null
            val a = Signal.fromInt((v / 4) % 2) ?: return null
            val b = Signal.fromInt((v / 2) % 2) ?: return null
            val c = Signal.fromInt(      v % 2) ?: return null
            return MinTerm3(a, b, c)
        }
    }

    fun distance(other : MinTerm3?) : Int {
        if (other == null) return size
        var result = 0
        if (this.A != other.A) { result += 1 }
        if (this.B != other.B) { result += 1 }
        if (this.C != other.C) { result += 1 }
        return result
    }

    fun combine(other : MinTerm3?) : MinTerm3? {
        if (other == null || this.distance(other) != 1) return null
        return MinTerm3(this.A.combine(other.A)
                      , this.B.combine(other.B)
                      , this.C.combine(other.C)
                      )
    }

    override fun toString(): String {
        return A.toString() + B.toString() + C.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is MinTerm3) return false
        return this.A == other.A && this.B == other.B && this.C == other.C
    }
}

class State3 ( val oneInts      : List<Int>
             , val minTermList  : List<MinTerm3>? = null
             , val curMinTermToMerge  : Int? = null
             , val nextMinTermToMerge : Int? = null
             , val mergedMinTerms1 : List<MinTerm3>? = null
             )
{
    companion object {
        fun intToMinTerm3list(l : List<Int>) : List<MinTerm3> =
            l.mapNotNull { MinTerm3.fromInt(it) }
    }

    private fun checkState() {
        if (minTermList != null && minTermList.size != oneInts.size) {
            throw Exception("minTermList and oneInts sizes do not match!")
        }
    }

    private fun nextUnchecked() : State3? {
        if (minTermList == null) {
            return State3(oneInts, intToMinTerm3list(oneInts))
        }
        if (curMinTermToMerge == null || nextMinTermToMerge == null || mergedMinTerms1 == null) {
            return State3(oneInts, intToMinTerm3list(oneInts),
            0, 1, listOf())
        }
        return null
    }

    fun next() : State3? {
        val newState = nextUnchecked()
        newState?.checkState()
        return newState
    }
}