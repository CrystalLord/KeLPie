package krilis.solver

sealed class ProblemSolution

object InfeasibleSolution : ProblemSolution()

object UnboundedSolution : ProblemSolution()

data class OptimalSolution(private val valueMappings: Map<String, Double>,
                           val objectiveValue: Double): ProblemSolution() {
    fun valueOf(element: ContinuousVar): Double = valueMappings[element.name] ?:
            throw IllegalArgumentException("Variable ${element.name} not found in solution.")
    fun valueOf(elementName: String): Double = valueMappings[elementName] ?:
            throw IllegalArgumentException("Variable $elementName not found in solution.")
    fun toMap(): Map<String, Double> = valueMappings
}
