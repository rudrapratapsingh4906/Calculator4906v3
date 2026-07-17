package com.example.domain.scanner

object MathKnowledgeBase {

    data class SolvingMethod(
        val methodName: String,
        val steps: List<String>,
        val complexity: Int, // 1-5, lower is faster/simpler
        val pros: String,
        val cons: String
    )

    data class ConceptInfo(
        val chapter: MathChapter,
        val conceptName: String,
        val formulas: List<String>,
        val primaryApproach: List<String>,
        val alternateMethods: List<SolvingMethod> = emptyList(),
        val level: MathLevel = MathLevel.CLASS_9_10
    )

    fun getTopicsForChapter(chapter: MathChapter): List<String> {
        return when (chapter) {
            MathChapter.TRIGONOMETRY -> listOf("Ratios and Identities", "Trigonometric Equations", "Inverse Trigonometric Functions", "Heights and Distances")
            MathChapter.COMPLEX_NUMBERS_QUADRATIC_EQUATIONS -> listOf("Complex Basics", "De Moivre's Theorem", "Cube Roots of Unity", "Quadratic Equations", "Nature of Roots")
            MathChapter.MATRICES_DETERMINANTS -> listOf("Matrix Multiplication", "Determinants", "Inverse and Adjoint", "System of Linear Equations", "Cramer's Rule")
            MathChapter.INTEGRAL_CALCULUS -> listOf("Indefinite Integrals", "Definite Integrals", "Substitution Method", "Parts Method", "Area Under Curves")
            MathChapter.LIMIT_CONTINUITY_DIFFERENTIABILITY -> listOf("Limits", "Continuity", "Standard Derivatives", "Chain Rule", "Implicit Differentiation")
            MathChapter.VECTOR_ALGEBRA -> listOf("Vector Addition", "Dot Product", "Cross Product", "Scalar Triple Product")
            MathChapter.THREE_DIMENSIONAL_GEOMETRY -> listOf("Lines in 3D", "Planes in 3D", "Shortest Distance")
            MathChapter.STATISTICS_PROBABILITY -> listOf("Mean and Variance", "Classical Probability", "Bayes' Theorem", "Binomial Distribution")
            MathChapter.SEQUENCE_SERIES -> listOf("Arithmetic Progression", "Geometric Progression", "Special Series")
            MathChapter.PERMUTATIONS_COMBINATIONS -> listOf("Fundamental Principle", "Permutations", "Combinations")
            MathChapter.SETS_RELATIONS_FUNCTIONS -> listOf("Sets and Operations", "Relations", "Types of Functions", "Domain and Range")
            MathChapter.COORDINATE_GEOMETRY -> listOf("Straight Lines", "Circles", "Conic Sections")
            else -> listOf("General Basics", "Problem Solving")
        }
    }

    fun findChapter(text: String): MathChapter {
        val lower = text.lowercase()
        return MathChapter.entries.find { 
            val name = it.name.replace("_", " ").lowercase()
            lower.contains(name) || name.split(" ").any { part -> part.length > 3 && lower.contains(part) }
        } ?: MathChapter.GENERAL
    }

    fun findTopic(text: String): String? {
        val lower = text.lowercase()
        MathChapter.entries.forEach { chapter ->
            getTopicsForChapter(chapter).forEach { topic ->
                if (lower.contains(topic.lowercase())) return topic
            }
        }
        return null
    }

    fun classify(text: String, category: MathCategory): ConceptInfo {
        val lower = text.lowercase()
        
        var level = MathLevel.CLASS_9_10
        if (lower.contains("jee advanced") || lower.contains("advanced")) {
            level = MathLevel.JEE_ADVANCED
        } else if (lower.contains("jee main") || lower.contains("mains")) {
            level = MathLevel.JEE_MAIN
        } else if (lower.contains("olympiad") || lower.contains("proof")) {
            level = MathLevel.OLYMPIAD
        } else if (lower.contains("class 11") || lower.contains("class 12")) {
            level = MathLevel.CLASS_11_12
        } else if (lower.contains("class 6") || lower.contains("class 7") || lower.contains("class 8")) {
            level = MathLevel.CLASS_6_8
        } else if (lower.contains("class 1") || lower.contains("class 2") || lower.contains("class 3") || lower.contains("class 4") || lower.contains("class 5")) {
            level = MathLevel.CLASS_1_5
        }
        
        // Trigonometry
        if (lower.contains("sin") || lower.contains("cos") || lower.contains("tan") || lower.contains("trig")) {
            val concept = when {
                lower.contains("inverse") -> "Inverse Trigonometric Functions"
                lower.contains("equation") -> "Trigonometric Equations"
                lower.contains("height") || lower.contains("distance") -> "Heights and Distances"
                else -> "Trigonometric Ratios and Identities"
            }
            
            val formulas = mutableListOf("sin²θ + cos²θ = 1", "sin(A+B) = sinA cosB + cosA sinB", "cos(2θ) = cos²θ - sin²θ")
            if (concept == "Inverse Trigonometric Functions") {
                formulas.addAll(listOf("sin⁻¹x + cos⁻¹x = π/2", "tan⁻¹x + tan⁻¹y = tan⁻¹((x+y)/(1-xy))"))
            }

            return ConceptInfo(
                chapter = MathChapter.TRIGONOMETRY,
                conceptName = concept,
                formulas = formulas,
                primaryApproach = listOf(
                    "Step 1: Use trigonometric identities to simplify the expression.",
                    "Step 2: Solve the simplified trigonometric equation or evaluate the value.",
                    "Step 3: Verify the final answer within the given domain/range."
                ),
                alternateMethods = listOf(
                    SolvingMethod(
                        methodName = "Substitution Method",
                        steps = listOf("Substitute t = tan(θ/2) or other variables.", "Convert to algebraic form.", "Solve and back-substitute."),
                        complexity = 3,
                        pros = "Reliable for complex rational trig functions.",
                        cons = "Can lead to high-degree polynomials."
                    )
                ),
                level = level
            )
        }
        
        // Complex Numbers
        if (category == MathCategory.COMPLEX || lower.contains("imaginary") || lower.matches(Regex(".*\\d+i.*")) || lower.contains("complex")) {
            val concept = when {
                lower.contains("de moivre") -> "De Moivre's Theorem"
                lower.contains("cube root") -> "Cube Roots of Unity"
                lower.contains("locus") -> "Locus of Complex Numbers"
                else -> "Basics of Complex Numbers"
            }
            
            return ConceptInfo(
                chapter = MathChapter.COMPLEX_NUMBERS_QUADRATIC_EQUATIONS,
                conceptName = concept,
                formulas = listOf("i² = -1", "z = a + ib", "|z| = √(a² + b²)", "e^(iθ) = cosθ + i sinθ", "De Moivre: (r(cosθ+isinθ))^n = r^n(cosnθ+isinnθ)"),
                primaryApproach = listOf(
                    "Step 1: Separate the real and imaginary parts of the complex expression.",
                    "Step 2: Apply complex arithmetic rules (addition, multiplication, conjugation).",
                    "Step 3: If required, convert to polar or Euler form for powers/roots.",
                    "Step 4: Analyze geometry in the Argand plane."
                ),
                alternateMethods = listOf(
                    SolvingMethod(
                        methodName = "Polar/Euler Transformation",
                        steps = listOf("Convert z to r(cosθ + i sinθ).", "Apply De Moivre's theorem for powers.", "Convert back to Cartesian form if needed."),
                        complexity = 2,
                        pros = "Simplifies multiplication and powers.",
                        cons = "Angle calculation can be tricky."
                    ),
                    SolvingMethod(
                        methodName = "Argand Plane Visualization",
                        steps = listOf("Plot points z1, z2 on the plane.", "Interpret addition as vector addition.", "Interpret multiplication as rotation and scaling."),
                        complexity = 3,
                        pros = "Intuitive for complex geometry problems.",
                        cons = "Hard to draw accurately for small values."
                    )
                ),
                level = level
            )
        }

        // Matrices & Determinants
        if (category == MathCategory.MATRIX || lower.contains("matrix") || lower.contains("determinant")) {
            val concept = when {
                lower.contains("cramer") -> "Cramer's Rule"
                lower.contains("adjoint") || lower.contains("inverse") -> "Inverse and Adjoint"
                lower.contains("rank") -> "Rank of Matrix"
                else -> "Matrices and Determinants"
            }

            return ConceptInfo(
                chapter = MathChapter.MATRICES_DETERMINANTS,
                conceptName = concept,
                formulas = listOf("|A| = ad - bc (for 2x2)", "A⁻¹ = adj(A) / |A|", "|AB| = |A||B|", "Rank(A) = Number of pivot columns"),
                primaryApproach = listOf(
                    "Step 1: Identify matrix dimensions and elements.",
                    "Step 2: Apply row/column operations or determinant expansion rules.",
                    "Step 3: Solve for unknown variables or calculate the inverse/determinant.",
                    "Step 4: (Advanced) Check for linear independence and calculate Rank."
                ),
                alternateMethods = listOf(
                    SolvingMethod(
                        methodName = "Cramer's Rule",
                        steps = listOf("Calculate ∆ (determinant of coefficients).", "Calculate ∆x, ∆y, ∆z by replacing columns.", "Variables = ∆i / ∆."),
                        complexity = 2,
                        pros = "Direct calculation for systems of equations.",
                        cons = "Computationally heavy for matrices > 3x3."
                    ),
                    SolvingMethod(
                        methodName = "Row Reduction (Gaussian Elimination)",
                        steps = listOf("Form the augmented matrix.", "Perform EROs to reach RREF.", "Back-substitute to find unknowns."),
                        complexity = 1,
                        pros = "Most efficient for large systems.",
                        cons = "High chance of arithmetic errors."
                    ),
                    SolvingMethod(
                        methodName = "Olympiad Proof (Linearity)",
                        steps = listOf("Define the linear transformation T.", "Verify Kernel and Image properties.", "Apply Rank-Nullity Theorem."),
                        complexity = 5,
                        pros = "Provides deep theoretical insight.",
                        cons = "Requires high level of abstraction."
                    )
                ),
                level = level
            )
        }

        // Calculus - Integration
        if (category == MathCategory.CALCULUS && (lower.contains("integrate") || lower.contains("integral") || lower.contains("∫"))) {
            val concept = when {
                lower.contains("definite") || (lower.contains("lower") && lower.contains("upper")) -> "Definite Integration"
                lower.contains("area") -> "Area Under Curves"
                lower.contains("substitution") -> "Integration by Substitution"
                lower.contains("parts") -> "Integration by Parts"
                else -> "Indefinite Integration"
            }

            return ConceptInfo(
                chapter = MathChapter.INTEGRAL_CALCULUS,
                conceptName = concept,
                formulas = listOf("∫ x^n dx = x^(n+1)/(n+1) + C", "∫ e^x dx = e^x + C", "∫ (1/x) dx = ln|x| + C"),
                primaryApproach = listOf(
                    "Step 1: Identify the function to integrate.",
                    "Step 2: Check for standard forms, substitution, or integration by parts.",
                    "Step 3: Apply the integration rule and add constant C (if indefinite).",
                    "Step 4: Evaluate limits if it is a definite integral and verify the final answer."
                ),
                alternateMethods = listOf(
                    SolvingMethod(
                        methodName = "Integration by Parts",
                        steps = listOf("Use ILATE rule to pick u and dv.", "Apply ∫ u dv = uv - ∫ v du."),
                        complexity = 3,
                        pros = "Solves products of different functions.",
                        cons = "Can lead to recursive integrals."
                    ),
                    SolvingMethod(
                        methodName = "Integration by Substitution",
                        steps = listOf("Identify internal function g(x).", "Let u = g(x), find du.", "Substitute and integrate in terms of u."),
                        complexity = 2,
                        pros = "Simplifies many complex looking integrals.",
                        cons = "Requires identifying the right u."
                    )
                ),
                level = level
            )
        }

        // Calculus - Differentiation/Limits/Differential Equations
        if (category == MathCategory.CALCULUS) {
            val concept: String
            val chapter: MathChapter
            if (lower.contains("limit") || lower.contains("continuous") || lower.contains("differentiable")) {
                chapter = MathChapter.LIMIT_CONTINUITY_DIFFERENTIABILITY
                concept = when {
                    lower.contains("limit") -> "Limits"
                    lower.contains("continuous") -> "Continuity"
                    else -> "Differentiability"
                }
            } else if (lower.contains("differential equation") || lower.contains("dy/dx =")) {
                chapter = MathChapter.DIFFERENTIAL_EQUATIONS
                concept = "Differential Equations"
            } else {
                chapter = MathChapter.LIMIT_CONTINUITY_DIFFERENTIABILITY
                concept = "Differentiation"
            }
            
            return ConceptInfo(
                chapter = chapter,
                conceptName = concept,
                formulas = listOf("d(x^n)/dx = n x^(n-1)", "d(sin x)/dx = cos x", "Product Rule: (uv)' = u'v + uv'", "Quotient Rule: (u/v)' = (u'v - uv')/v²"),
                primaryApproach = listOf(
                    "Step 1: Identify the function to differentiate or evaluate limit for.",
                    "Step 2: Apply basic derivative rules, chain rule, product rule, or quotient rule.",
                    "Step 3: Simplify the resulting expression.",
                    "Step 4: Verify the final answer."
                ),
                alternateMethods = listOf(
                    SolvingMethod(
                        methodName = "Logarithmic Differentiation",
                        steps = listOf("Take ln on both sides.", "Differentiate implicitly.", "Solve for dy/dx."),
                        complexity = 3,
                        pros = "Handles functions like f(x)^g(x) easily.",
                        cons = "More steps than standard chain rule."
                    ),
                    SolvingMethod(
                        methodName = "L'Hopital's Rule (for limits)",
                        steps = listOf("Verify 0/0 or ∞/∞ form.", "Differentiate numerator and denominator separately.", "Re-evaluate limit."),
                        complexity = 2,
                        pros = "Fast for indeterminate forms.",
                        cons = "Not always simpler if derivatives are complex."
                    )
                ),
                level = level
            )
        }

        // Vectors
        if (lower.contains("vector") || lower.contains("dot product") || lower.contains("cross product")) {
            val concept = when {
                lower.contains("scalar triple") || lower.contains("stp") -> "Scalar Triple Product"
                lower.contains("cross product") -> "Vector Product"
                else -> "Dot Product and Basics"
            }

            return ConceptInfo(
                chapter = MathChapter.VECTOR_ALGEBRA,
                conceptName = concept,
                formulas = listOf("a·b = |a||b|cosθ", "a×b = |a||b|sinθ n̂", "|a| = √(x² + y² + z²)", "Projection of a on b = (a·b/|b|²)b"),
                primaryApproach = listOf(
                    "Step 1: Identify vector components (i, j, k).",
                    "Step 2: Apply vector addition, dot product, or cross product formulas.",
                    "Step 3: Verify the final answer."
                ),
                alternateMethods = listOf(
                    SolvingMethod(
                        methodName = "Geometrical Construction",
                        steps = listOf("Draw the vectors in 3D space.", "Apply triangle/parallelogram law visually.", "Measure magnitude and angle."),
                        complexity = 4,
                        pros = "Excellent for visual understanding.",
                        cons = "Less precise than analytical methods."
                    )
                ),
                level = level
            )
        }
        
        // Coordinate Geometry / 3D Geometry
        if (category == MathCategory.GEOMETRY || lower.contains("line") || lower.contains("circle") || lower.contains("parabola") || lower.contains("ellipse") || lower.contains("plane")) {
            val chapter = if (lower.contains("plane") || lower.contains("3d")) MathChapter.THREE_DIMENSIONAL_GEOMETRY else MathChapter.COORDINATE_GEOMETRY
            val concept = when {
                lower.contains("plane") -> "Planes in 3D"
                lower.contains("circle") -> "Circles"
                lower.contains("parabola") -> "Parabola"
                lower.contains("ellipse") -> "Ellipse"
                lower.contains("hyperbola") -> "Hyperbola"
                else -> "Straight Lines"
            }

            return ConceptInfo(
                chapter = chapter,
                conceptName = concept,
                formulas = listOf("Distance = √((x₂-x₁)² + (y₂-y₁)²)", "y - y₁ = m(x - x₁)", "(x-h)² + (y-k)² = r²"),
                primaryApproach = listOf(
                    "Step 1: Identify the geometric shape and its standard equation.",
                    "Step 2: Substitute given points, slopes, or conditions into the equation.",
                    "Step 3: Solve for the required locus, distance, intersection, or parameters.",
                    "Step 4: Verify the final answer geometrically."
                ),
                level = level
            )
        }

        // Probability & Statistics
        if (category == MathCategory.STATISTICS || lower.contains("probability") || lower.contains("variance") || lower.contains("mean") || lower.contains("distribution")) {
             val concept = when {
                 lower.contains("bayes") -> "Bayes' Theorem"
                 lower.contains("binomial") && lower.contains("distribution") -> "Binomial Distribution"
                 lower.contains("mean") || lower.contains("variance") -> "Statistical Measures"
                 else -> "Classical Probability"
             }

             return ConceptInfo(
                chapter = MathChapter.STATISTICS_PROBABILITY,
                conceptName = concept,
                formulas = listOf("P(A) = Favorable / Total", "Mean = Σx/n", "Variance = Σ(x-μ)²/n", "P(A∪B) = P(A) + P(B) - P(A∩B)"),
                primaryApproach = listOf(
                    "Step 1: Identify the dataset or the sample space events.",
                    "Step 2: Apply probability combinations, Bayes' theorem, or statistical formulas (mean, variance).",
                    "Step 3: Verify the final answer (probability must be between 0 and 1)."
                ),
                level = level
            )
        }
        
        // Sequence & Series
        if (lower.contains("progression") || lower.contains("sequence") || lower.contains("series") || lower.contains("ap") || lower.contains("gp") || lower.contains("hp")) {
            val concept = when {
                lower.contains("ap") -> "Arithmetic Progression"
                lower.contains("gp") -> "Geometric Progression"
                lower.contains("sum to infinity") -> "Infinite GP"
                else -> "Sequence and Series"
            }

            return ConceptInfo(
                chapter = MathChapter.SEQUENCE_SERIES,
                conceptName = concept,
                formulas = listOf("T_n = a + (n-1)d", "S_n = n/2(2a + (n-1)d)", "T_n = ar^(n-1)", "S_n = a(1-r^n)/(1-r)"),
                primaryApproach = listOf(
                    "Step 1: Identify if the sequence is AP, GP, HP, or mixed.",
                    "Step 2: Use the nth term or sum formulas appropriately.",
                    "Step 3: Simplify and solve for the required term or sum.",
                    "Step 4: Verify the final answer."
                ),
                level = level
            )
        }
        
        // Permutations and Combinations
        if (lower.contains("permutation") || lower.contains("combination") || lower.contains("arrange") || lower.contains("select")) {
            val concept = if (lower.contains("select")) "Combinations" else "Permutations"

            return ConceptInfo(
                chapter = MathChapter.PERMUTATIONS_COMBINATIONS,
                conceptName = concept,
                formulas = listOf("nPr = n! / (n-r)!", "nCr = n! / (r! (n-r)!)", "n! = n(n-1)...1"),
                primaryApproach = listOf(
                    "Step 1: Determine whether the problem requires selection (combinations) or arrangement (permutations).",
                    "Step 2: Break down the problem into disjoint cases if necessary.",
                    "Step 3: Calculate using factorial formulas and principles of counting.",
                    "Step 4: Verify the final answer."
                ),
                level = level
            )
        }
        
        // Sets, Relations, Functions
        if (lower.contains("set") || lower.contains("relation") || lower.contains("function") || lower.contains("domain") || lower.contains("range")) {
            val concept = when {
                lower.contains("domain") || lower.contains("range") -> "Domain and Range"
                lower.contains("composition") -> "Composition of Functions"
                else -> "Sets and Relations"
            }

            return ConceptInfo(
                chapter = MathChapter.SETS_RELATIONS_FUNCTIONS,
                conceptName = concept,
                formulas = listOf("n(A∪B) = n(A) + n(B) - n(A∩B)", "f(g(x)) = (f ∘ g)(x)"),
                primaryApproach = listOf(
                    "Step 1: Clearly define the sets, relations, or functions given.",
                    "Step 2: Apply set operations, Venn diagrams, or functional properties (injectivity, surjectivity).",
                    "Step 3: Evaluate domains, ranges, or inverse functions as needed.",
                    "Step 4: Verify the final answer."
                ),
                level = level
            )
        }

        // Default to General Algebra
        return ConceptInfo(
            chapter = MathChapter.GENERAL,
            conceptName = "Basic Algebra",
            formulas = listOf("(a+b)² = a² + 2ab + b²", "x = (-b ± √(b² - 4ac)) / 2a", "a² - b² = (a-b)(a+b)"),
            primaryApproach = listOf(
                "Step 1: Simplify the algebraic expression or equation.",
                "Step 2: Isolate the variable.",
                "Step 3: Solve for the required variable.",
                "Step 4: Verify the final answer."
            ),
            level = level
        )
    }
}
