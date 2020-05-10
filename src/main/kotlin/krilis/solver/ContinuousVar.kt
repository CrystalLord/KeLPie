package krilis.solver

class ContinuousVar(
    val name: String,
    val lowerBound: Double? = null,
    val upperBound: Double? = null
) : ProgramElement {

    fun toLinearExpr() = LinearExpr(mapOf(this.name to SimpleTerm(1.0, this)), constantSum = 0.0)

    override operator fun plus(other: ContinuousVar) = LinearExpr(
            mapOf(this.name to SimpleTerm(1.0, this), other.name to SimpleTerm(1.0, other)),
            constantSum = 0.0
        )

    override operator fun plus(other: LinearExpr): LinearExpr = other + this

    override operator fun plus(other: Double): LinearExpr {
        return LinearExpr(mapOf(this.name to SimpleTerm(1.0, this)), other)
    }

    override operator fun minus(other: Double): LinearExpr {
        return LinearExpr(mapOf(this.name to SimpleTerm(1.0, this)), -other)
    }

    override operator fun times(other: Double): LinearExpr {
        return LinearExpr(mapOf(this.name to SimpleTerm(other, this)), constantSum = 0.0)
    }

    override operator fun minus(other: ContinuousVar): LinearExpr = LinearExpr(
        mapOf(this.name to SimpleTerm(1.0, this), other.name to SimpleTerm(-1.0, other)),
        constantSum = 0.0
    )

    override operator fun minus(other: LinearExpr): LinearExpr {
        throw RuntimeException()
    }

    override operator fun unaryMinus(): LinearExpr {
        return LinearExpr(mapOf(this.name to SimpleTerm(-1.0, this)), 0.0)
    }
}

operator fun Double.plus(other: ContinuousVar) = other + this
operator fun Double.minus(other: ContinuousVar) = other - this
operator fun Double.times(other: ContinuousVar) = other * this
