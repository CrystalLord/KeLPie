package krilis.solver

sealed class ProblemSolution

object InfeasibleSolution : ProblemSolution()

object UnboundedSolution : ProblemSolution()

data class OptimalSolution(val valueMappings: Map<String, Double>,
                           val optimizationValue: Double): ProblemSolution() {
    fun valueOf(element: ContinuousVar): Double = valueMappings[element.name] ?:
        throw IllegalArgumentException("Variable ${element.name} not found in solution.")
}