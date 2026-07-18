package com.example.data.repository

import com.example.data.local.dao.VoiceCommandDao
import com.example.data.local.entity.VoiceCommandEntity
import com.example.domain.model.VoiceCommand
import com.example.domain.repository.VoiceCommandRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VoiceCommandRepositoryImpl(
    private val dao: VoiceCommandDao
) : VoiceCommandRepository {

    private val defaultCommands = listOf(
        VoiceCommand(
            "wake_phrase", "Wake Phrase",
            listOf("hey calculator", "hello calculator", "hey tutor", "hello tutor"),
            listOf("hey calculator", "hello calculator", "hey tutor", "hello tutor")
        ),
        VoiceCommand(
            "open_graph", "Open Graph Plotter",
            listOf("open graph", "plot graph", "graph plotter", "graph kholo", "graph banao", "graph dikhao"),
            listOf("open graph", "plot graph", "graph plotter", "graph kholo", "graph banao", "graph dikhao")
        ),
        VoiceCommand(
            "open_matrix", "Open Matrix Calculator",
            listOf("open matrix", "matrix calculator", "matrix kholo", "matrix calculator kholo"),
            listOf("open matrix", "matrix calculator", "matrix kholo", "matrix calculator kholo")
        ),
        VoiceCommand(
            "open_scanner", "Open Vision Scanner",
            listOf("open ocr scanner", "open vision scanner", "ocr scanner", "math scanner", "scanner kholo", "scan karo"),
            listOf("open ocr scanner", "open vision scanner", "ocr scanner", "math scanner", "scanner kholo", "scan karo")
        ),
        VoiceCommand(
            "open_practice", "Open Practice Mode",
            listOf("open practice mode", "practice mode", "start practice", "practice", "practice shuru karo", "abhyas shuru karo"),
            listOf("open practice mode", "practice mode", "start practice", "practice", "practice shuru karo", "abhyas shuru karo")
        ),
        VoiceCommand(
            "open_history", "Open History",
            listOf("open history", "show history", "history kholo", "history dikhao"),
            listOf("open history", "show history", "history kholo", "history dikhao")
        ),
        VoiceCommand(
            "open_settings", "Open Settings",
            listOf("open settings", "show settings", "settings kholo", "setting kholo"),
            listOf("open settings", "show settings", "settings kholo", "setting kholo")
        ),
        VoiceCommand(
            "unit_converter", "Open Unit Converter",
            listOf("open unit converter", "unit converter", "unit converter kholo", "unit convert karo"),
            listOf("open unit converter", "unit converter", "unit converter kholo", "unit convert karo")
        ),
        VoiceCommand(
            "open_stats", "Open Statistics",
            listOf("open statistics", "statistics", "stats", "statistics kholo", "stats kholo"),
            listOf("open statistics", "statistics", "stats", "statistics kholo", "stats kholo")
        ),
        VoiceCommand(
            "open_complex", "Open Complex Calculator",
            listOf("open complex numbers", "complex numbers", "complex calculator", "complex number kholo", "complex calculator kholo"),
            listOf("open complex numbers", "complex numbers", "complex calculator", "complex number kholo", "complex calculator kholo")
        ),
        VoiceCommand(
            "open_calculus", "Open Calculus",
            listOf("open calculus", "calculus", "calculus kholo"),
            listOf("open calculus", "calculus", "calculus kholo")
        ),
        VoiceCommand(
            "next_step", "Next Step",
            listOf("next step", "agla step", "next step batao", "agla charan"),
            listOf("next step", "agla step", "next step batao", "agla charan")
        ),
        VoiceCommand(
            "previous_step", "Previous Step",
            listOf("previous step", "go back", "back", "piche jao", "pichla step"),
            listOf("previous step", "go back", "back", "piche jao", "pichla step")
        ),
        VoiceCommand(
            "show_hint", "Show Hint",
            listOf("show hint", "give hint", "hint", "hint do", "hint batao", "ishara do"),
            listOf("show hint", "give hint", "hint", "hint do", "hint batao", "ishara do")
        ),
        VoiceCommand(
            "explain_why", "Explain Why",
            listOf("explain why", "why", "kyun", "explain karo", "samjhao kyun"),
            listOf("explain why", "why", "kyun", "explain karo", "samjhao kyun")
        ),
        VoiceCommand(
            "show_formula", "Show Formula",
            listOf("formula first", "show formula", "formula", "formula dikhao", "formula batao", "sutra batao"),
            listOf("formula first", "show formula", "formula", "formula dikhao", "formula batao", "sutra batao")
        ),
        VoiceCommand(
            "another_method", "Another Method",
            listOf("another method", "alternative method", "other method", "doosra tarika", "dusra method"),
            listOf("another method", "alternative method", "other method", "doosra tarika", "dusra method")
        ),
        VoiceCommand(
            "answer_only", "Answer Only",
            listOf("answer only", "show answer", "get answer", "sirf answer", "answer dikhao"),
            listOf("answer only", "show answer", "get answer", "sirf answer", "answer dikhao")
        ),
        VoiceCommand(
            "similar_question", "Similar Question",
            listOf("give similar question", "similar question", "vaisa hi question do", "similar question dikhao"),
            listOf("give similar question", "similar question", "vaisa hi question do", "similar question dikhao")
        ),
        VoiceCommand(
            "stop", "Stop",
            listOf("stop speaking", "stop", "chup ho jao", "chup karo"),
            listOf("stop speaking", "stop", "chup ho jao", "chup karo")
        ),
        VoiceCommand(
            "repeat", "Repeat",
            listOf("repeat answer", "repeat", "fir se bolo", "repeat karo", "dohrao"),
            listOf("repeat answer", "repeat", "fir se bolo", "repeat karo", "dohrao")
        ),
        VoiceCommand(
            "slower", "Slower",
            listOf("speak slower", "slower", "dhire bolo", "dheere bolo"),
            listOf("speak slower", "slower", "dhire bolo", "dheere bolo")
        ),
        VoiceCommand(
            "zoom_in", "Zoom In Graph",
            listOf("zoom in", "zoom in graph", "bada karo", "zoom in karo"),
            listOf("zoom in", "zoom in graph", "bada karo", "zoom in karo")
        ),
        VoiceCommand(
            "zoom_out", "Zoom Out Graph",
            listOf("zoom out", "zoom out graph", "chota karo", "zoom out karo"),
            listOf("zoom out", "zoom out graph", "chota karo", "zoom out karo")
        ),
        VoiceCommand(
            "reset_graph", "Reset Graph Viewport",
            listOf("reset graph", "reset viewport", "graph reset karo", "normal view"),
            listOf("reset graph", "reset viewport", "graph reset karo", "normal view")
        ),
        VoiceCommand(
            "plot_expression", "Plot Function",
            listOf("plot ", "graph banao ", "plot function ", "draw "),
            listOf("plot ", "graph banao ", "plot function ", "draw ")
        ),
        VoiceCommand(
            "solve_matrix", "Solve Matrix",
            listOf("solve matrix", "matrix solve karo", "compute matrix"),
            listOf("solve matrix", "matrix solve karo", "compute matrix")
        ),
        VoiceCommand(
            "find_determinant", "Find Determinant",
            listOf("find determinant", "determinant", "determinant nikalo"),
            listOf("find determinant", "determinant", "determinant nikalo")
        ),
        VoiceCommand(
            "find_inverse", "Find Inverse",
            listOf("find inverse", "matrix inverse", "inverse nikalo"),
            listOf("find inverse", "matrix inverse", "inverse nikalo")
        ),
        VoiceCommand(
            "transpose_matrix", "Transpose Matrix",
            listOf("transpose matrix", "transpose", "transpose nikalo"),
            listOf("transpose matrix", "transpose", "transpose nikalo")
        )
    )

    private fun VoiceCommandEntity.toDomain(): VoiceCommand {
        return VoiceCommand(
            commandId = commandId,
            commandName = commandName,
            aliases = aliases.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            defaultAliases = defaultAliases.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            isCustom = isCustom
        )
    }

    private fun VoiceCommand.toEntity(): VoiceCommandEntity {
        return VoiceCommandEntity(
            commandId = commandId,
            commandName = commandName,
            aliases = aliases.joinToString(","),
            defaultAliases = defaultAliases.joinToString(","),
            isCustom = isCustom
        )
    }

    override fun getVoiceCommandsFlow(): Flow<List<VoiceCommand>> {
        return dao.getAllVoiceCommandsFlow().map { entities ->
            if (entities.isEmpty()) {
                val prepopulated = defaultCommands.map { it.toEntity() }
                dao.insertVoiceCommands(prepopulated)
                defaultCommands
            } else {
                entities.map { it.toDomain() }
            }
        }
    }

    override suspend fun getVoiceCommands(): List<VoiceCommand> {
        val entities = dao.getAllVoiceCommands()
        return if (entities.isEmpty()) {
            val prepopulated = defaultCommands.map { it.toEntity() }
            dao.insertVoiceCommands(prepopulated)
            defaultCommands
        } else {
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveVoiceCommand(command: VoiceCommand) {
        dao.insertVoiceCommand(command.toEntity())
    }

    override suspend fun resetToDefault(commandId: String) {
        val existing = dao.getVoiceCommandById(commandId)
        if (existing != null) {
            dao.insertVoiceCommand(existing.copy(aliases = existing.defaultAliases))
        }
    }

    override suspend fun resetAllToDefault() {
        val entities = dao.getAllVoiceCommands()
        val updated = entities.map { it.copy(aliases = it.defaultAliases) }
        dao.insertVoiceCommands(updated)
    }
}
