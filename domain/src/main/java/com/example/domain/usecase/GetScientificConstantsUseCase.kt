package com.example.domain.usecase

import com.example.domain.model.ConstantCategory
import com.example.domain.model.ScientificConstant

class GetScientificConstantsUseCase {
    operator fun invoke(): List<ScientificConstant> {
        return listOf(
            // Mathematical Constants
            ScientificConstant(
                id = "PI",
                name = "Pi",
                symbol = "π",
                value = "3.141592653589793",
                unit = "dimensionless",
                description = "The mathematical constant representing the ratio of any circle's circumference to its diameter in Euclidean space.",
                category = ConstantCategory.MATHEMATICAL,
                field = "Mathematics"
            ),
            ScientificConstant(
                id = "E",
                name = "Euler's Number",
                symbol = "e",
                value = "2.718281828459045",
                unit = "dimensionless",
                description = "The unique mathematical constant that is the base of natural logarithms. It has unique properties under calculus.",
                category = ConstantCategory.MATHEMATICAL,
                field = "Mathematics"
            ),
            ScientificConstant(
                id = "GOLDEN_RATIO",
                name = "Golden Ratio",
                symbol = "φ",
                value = "1.618033988749895",
                unit = "dimensionless",
                description = "A special number found by dividing a line into two parts so that the longer part divided by the smaller part is also equal to the whole length divided by the longer part.",
                category = ConstantCategory.MATHEMATICAL,
                field = "Mathematics"
            ),

            // Universal Constants
            ScientificConstant(
                id = "SPEED_OF_LIGHT",
                name = "Speed of Light in Vacuum",
                symbol = "c",
                value = "299792458",
                unit = "m / s",
                description = "The fundamental physical constant defining the speed at which all electromagnetic radiation propagates in a vacuum. It represents the cosmic speed limit.",
                category = ConstantCategory.UNIVERSAL,
                field = "Physics"
            ),
            ScientificConstant(
                id = "GRAVITATIONAL_CONSTANT",
                name = "Gravitational Constant",
                symbol = "G",
                value = "6.67430e-11",
                unit = "m³ / (kg · s²)",
                description = "The empirical key physical constant used in the calculation of gravitational forces in Newton's Law of Universal Gravitation and Einstein's Theory of General Relativity.",
                category = ConstantCategory.UNIVERSAL,
                field = "Physics"
            ),
            ScientificConstant(
                id = "PLANCK_CONSTANT",
                name = "Planck Constant",
                symbol = "h",
                value = "6.62607015e-34",
                unit = "J · s",
                description = "The quantum of electromagnetic action that relates a photon's energy to its electromagnetic frequency.",
                category = ConstantCategory.UNIVERSAL,
                field = "Physics"
            ),
            ScientificConstant(
                id = "REDUCED_PLANCK_CONSTANT",
                name = "Reduced Planck Constant",
                symbol = "ħ",
                value = "1.054571817e-34",
                unit = "J · s",
                description = "Planck's constant divided by 2π, also known as the Dirac constant. It is the fundamental unit of angular momentum in quantum mechanics.",
                category = ConstantCategory.UNIVERSAL,
                field = "Physics"
            ),
            ScientificConstant(
                id = "ELECTRON_VOLT",
                name = "Electron Volt",
                symbol = "eV",
                value = "1.602176634e-19",
                unit = "J",
                description = "The amount of kinetic energy gained by a single electron accelerating from rest through an electric potential difference of one volt.",
                category = ConstantCategory.UNIVERSAL,
                field = "Physics"
            ),

            // Electromagnetic Constants
            ScientificConstant(
                id = "ELEMENTARY_CHARGE",
                name = "Elementary Charge",
                symbol = "e",
                value = "1.602176634e-19",
                unit = "C",
                description = "The magnitude of electrical charge carried by a single electron or proton. It is a fundamental physical constant of electrodynamics.",
                category = ConstantCategory.ELECTROMAGNETIC,
                field = "Physics"
            ),
            ScientificConstant(
                id = "VACUUM_PERMITTIVITY",
                name = "Vacuum Permittivity",
                symbol = "ε₀",
                value = "8.8541878128e-12",
                unit = "F / m",
                description = "The capability of a vacuum to permit electric field lines. It also appears in Coulomb's law of electrostatic force.",
                category = ConstantCategory.ELECTROMAGNETIC,
                field = "Physics"
            ),
            ScientificConstant(
                id = "VACUUM_PERMEABILITY",
                name = "Vacuum Permeability",
                symbol = "μ₀",
                value = "1.25663706212e-6",
                unit = "H / m",
                description = "The magnetic permeability of classical vacuum, representing the capability of a vacuum to support magnetic field lines.",
                category = ConstantCategory.ELECTROMAGNETIC,
                field = "Physics"
            ),
            ScientificConstant(
                id = "FINE_STRUCTURE_CONSTANT",
                name = "Fine-Structure Constant",
                symbol = "α",
                value = "7.2973525693e-3",
                unit = "dimensionless",
                description = "A dimensionless physical constant characterizing the strength of the electromagnetic interaction between elementary charged particles.",
                category = ConstantCategory.ELECTROMAGNETIC,
                field = "Physics"
            ),
            ScientificConstant(
                id = "JOSEPHSON_CONSTANT",
                name = "Josephson Constant",
                symbol = "K_J",
                value = "483597.8484e9",
                unit = "Hz / V",
                description = "A constant relating the electric potential difference to the frequency of magnetic flux oscillations in a Josephson junction.",
                category = ConstantCategory.ELECTROMAGNETIC,
                field = "Physics"
            ),
            ScientificConstant(
                id = "VON_KLITZING_CONSTANT",
                name = "Von Klitzing Constant",
                symbol = "R_K",
                value = "25812.80745",
                unit = "Ω",
                description = "The quantum unit of electrical resistance, essential for measurement and calibration in the quantum Hall effect.",
                category = ConstantCategory.ELECTROMAGNETIC,
                field = "Physics"
            ),

            // Atomic & Nuclear Constants
            ScientificConstant(
                id = "ELECTRON_MASS",
                name = "Electron Mass",
                symbol = "mₑ",
                value = "9.1093837015e-31",
                unit = "kg",
                description = "The rest mass of a free, stationary electron.",
                category = ConstantCategory.ATOMIC_NUCLEAR,
                field = "Physics"
            ),
            ScientificConstant(
                id = "PROTON_MASS",
                name = "Proton Mass",
                symbol = "m_p",
                value = "1.67262192369e-27",
                unit = "kg",
                description = "The rest mass of a free proton, one of the primary constituents of atomic nuclei.",
                category = ConstantCategory.ATOMIC_NUCLEAR,
                field = "Physics"
            ),
            ScientificConstant(
                id = "NEUTRON_MASS",
                name = "Neutron Mass",
                symbol = "m_n",
                value = "1.67492749804e-27",
                unit = "kg",
                description = "The rest mass of a free, unbound neutron, crucial in nuclear physics and cosmology.",
                category = ConstantCategory.ATOMIC_NUCLEAR,
                field = "Physics"
            ),
            ScientificConstant(
                id = "RYDBERG_CONSTANT",
                name = "Rydberg Constant",
                symbol = "R_inf",
                value = "10973731.56816",
                unit = "m⁻¹",
                description = "A fundamental constant expressing the limiting value of the highest wavenumber of any electromagnetic radiation emitted from a hydrogen atom.",
                category = ConstantCategory.ATOMIC_NUCLEAR,
                field = "Physics"
            ),
            ScientificConstant(
                id = "BOHR_RADIUS",
                name = "Bohr Radius",
                symbol = "a₀",
                value = "5.29177210903e-11",
                unit = "m",
                description = "The approximate, most probable distance between the atomic nucleus and the electron of a hydrogen atom in its ground state.",
                category = ConstantCategory.ATOMIC_NUCLEAR,
                field = "Physics"
            ),
            ScientificConstant(
                id = "ATOMIC_MASS_UNIT",
                name = "Unified Atomic Mass Unit",
                symbol = "u",
                value = "1.66053906660e-27",
                unit = "kg",
                description = "Exactly one-twelfth of the mass of an unbound neutral atom of carbon-12 at rest and in its ground state.",
                category = ConstantCategory.ATOMIC_NUCLEAR,
                field = "Physics / Chemistry"
            ),

            // Physico-Chemical Constants
            ScientificConstant(
                id = "BOLTZMANN_CONSTANT",
                name = "Boltzmann Constant",
                symbol = "k",
                value = "1.380649e-23",
                unit = "J / K",
                description = "The physical constant relating thermodynamic temperature to the average kinetic energy of individual gas particles.",
                category = ConstantCategory.PHYSICO_CHEMICAL,
                field = "Physics / Thermodynamics"
            ),
            ScientificConstant(
                id = "AVOGADRO_CONSTANT",
                name = "Avogadro Constant",
                symbol = "Nₐ",
                value = "6.02214076e23",
                unit = "mol⁻¹",
                description = "The proportionality factor that relates the number of constituent particles in a sample with the amount of substance in moles.",
                category = ConstantCategory.PHYSICO_CHEMICAL,
                field = "Chemistry"
            ),
            ScientificConstant(
                id = "UNIVERSAL_GAS_CONSTANT",
                name = "Universal Gas Constant",
                symbol = "R",
                value = "8.314462618",
                unit = "J / (mol · K)",
                description = "The physical constant of proportionality in the Ideal Gas Law equation.",
                category = ConstantCategory.PHYSICO_CHEMICAL,
                field = "Chemistry / Physics"
            ),
            ScientificConstant(
                id = "STEFAN_BOLTZMANN_CONSTANT",
                name = "Stefan-Boltzmann Constant",
                symbol = "σ",
                value = "5.670374419e-8",
                unit = "W / (m² · K⁴)",
                description = "The physical constant of proportionality in the Stefan–Boltzmann law, relating total thermal radiation energy from a black body to its thermodynamic temperature.",
                category = ConstantCategory.PHYSICO_CHEMICAL,
                field = "Physics"
            ),
            ScientificConstant(
                id = "FARADAY_CONSTANT",
                name = "Faraday Constant",
                symbol = "F",
                value = "96485.33212",
                unit = "C / mol",
                description = "The magnitude of electrical charge per mole of electrons, fundamental to electrochemical calculations.",
                category = ConstantCategory.PHYSICO_CHEMICAL,
                field = "Chemistry"
            ),

            // Astronomical Constants
            ScientificConstant(
                id = "STANDARD_GRAVITY",
                name = "Standard Gravity",
                symbol = "g",
                value = "9.80665",
                unit = "m / s²",
                description = "The standard acceleration of gravity near Earth's surface, used in physics, mechanics, and weight calculations.",
                category = ConstantCategory.ASTRONOMICAL,
                field = "Geophysics / Physics"
            ),
            ScientificConstant(
                id = "ASTRONOMICAL_UNIT",
                name = "Astronomical Unit",
                symbol = "au",
                value = "149597870700",
                unit = "m",
                description = "A unit of length, roughly the average distance from the Earth to the Sun.",
                category = ConstantCategory.ASTRONOMICAL,
                field = "Astronomy"
            ),
            ScientificConstant(
                id = "PARSEC",
                name = "Parsec",
                symbol = "pc",
                value = "3.08567758149e16",
                unit = "m",
                description = "An astronomical unit of distance, equal to the distance at which a length of one astronomical unit subtends an angle of one arcsecond.",
                category = ConstantCategory.ASTRONOMICAL,
                field = "Astronomy"
            ),
            ScientificConstant(
                id = "LIGHT_YEAR",
                name = "Light Year",
                symbol = "ly",
                value = "9.46073047258e15",
                unit = "m",
                description = "The distance that light travels in a vacuum in one standard Julian year of 365.25 days.",
                category = ConstantCategory.ASTRONOMICAL,
                field = "Astronomy"
            ),
            ScientificConstant(
                id = "SOLAR_MASS",
                name = "Solar Mass",
                symbol = "M☉",
                value = "1.98847e30",
                unit = "kg",
                description = "A standard unit of mass in astronomy, equal to the mass of the Sun, used to describe the masses of other stars, galaxies, and massive objects.",
                category = ConstantCategory.ASTRONOMICAL,
                field = "Astronomy"
            )
        )
    }
}
