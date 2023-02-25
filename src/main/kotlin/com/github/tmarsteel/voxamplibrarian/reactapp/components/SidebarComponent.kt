package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.BlobBinaryInput
import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration.toUiDataModel
import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException
import com.github.tmarsteel.voxamplibrarian.reactapp.classes
import com.github.tmarsteel.voxamplibrarian.reactapp.icon
import com.github.tmarsteel.voxamplibrarian.vtxprog.VtxProgFile
import csstype.ClassName
import csstype.Cursor
import csstype.None
import csstype.rem
import emotion.react.css
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.createRef
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.span
import react.useState

external interface SidebarComponentProps : Props {
    var ampConnected: Boolean
    var vtxAmpState: VtxAmpState?
    var onProgramSlotSelected: (ProgramSlot) -> Unit
    var onSaveConfiguration: (ProgramSlot) -> Unit
    var onLoadConfiguration: (ProgramSlot) -> Unit
    var onClose: () -> Unit
}

private val logger = LoggerFactory["sidebar"]

private data class LoadedFile(
    val filename: String,
    val configs: List<SimulationConfiguration>,
    /** for change detection */
    val originalConfigs: List<SimulationConfiguration> = configs,
) {
    fun hasUnsavedChanges(): Boolean {
        return configs.zip(originalConfigs).any { (currentConfig, originalConfig) -> currentConfig != originalConfig }
    }

    fun withChangesSaved(): LoadedFile = copy(filename, configs, configs)

    companion object {
        val DEFAULT = LoadedFile("empty", SimulationConfiguration.DEFAULT.repeat(11))
    }
}

val SidebarComponent = FC<SidebarComponentProps> { props ->
    val localAmpState = props.vtxAmpState?.takeIf { props.ampConnected }
    var currentFile: LoadedFile by useState(LoadedFile.DEFAULT)
    val hiddenFileInputRef = createRef<HTMLInputElement>()

    icon("x", "close side menu") {
        css(ClassName("sidebar-close")) {
            fontSize = 2.rem
            cursor = Cursor.pointer
        }
        onClick = { props.onClose() }
    }

    div {
        className = classes("sidebar__inner")

        div {
            className = classes("sidebar-tree-entry", "sidebar-tree-entry--level-0")

            ConnectivityIndicatorComponent {
                isActive = props.ampConnected
            }

            span {
                className = classes("sidebar-tree-entry__label")
                +"VT20X/40X/100X Amplifier (${if (!props.ampConnected) "not " else ""}connected)"
            }
        }

        for (programSlot in ProgramSlot.values()) {
            div {
                className = classes(
                    "sidebar-tree-entry",
                    "sidebar-tree-entry--level-1",
                    "sidebar-tree-entry--clickable".takeIf { props.ampConnected },
                    "sidebar-tree-entry--active".takeIf { props.ampConnected && localAmpState != null && localAmpState is VtxAmpState.ProgramSlotSelected && localAmpState.slot == programSlot }
                )

                span {
                    className = ClassName("sidebar-tree-entry__label")
                    +"${programSlot.name}: ${localAmpState?.storedUserPrograms?.get(programSlot)?.programName ?: "<empty>"}"
                    onClick = {
                        if (props.vtxAmpState != null) {
                            props.onProgramSlotSelected(programSlot)
                        }
                    }
                }

                div {
                    className = classes(
                        "sidebar-tree-entry-action",
                        "disabled".takeIf { localAmpState == null },
                    )

                    icon("upload", "Load this program")
                    onClick = {
                        if (localAmpState != null) {
                            props.onLoadConfiguration(programSlot)
                        }
                    }
                }
                div {
                    className = classes(
                        "sidebar-tree-entry-action",
                        "disabled".takeIf { localAmpState == null },
                    )

                    icon("download", "Save the current configuration to this place")
                    onClick = {
                        if (localAmpState != null) {
                            props.onSaveConfiguration(programSlot)
                        }
                    }
                }
            }
        }

        div {
            className = classes("sidebar-tree-entry", "sidebar-tree-entry--level-0")
            icon("file-earmark", "Currently loaded file")
            span {
                className = classes("sidebar-tree-entry__label")
                +currentFile.filename
            }

            div {
                className = classes("sidebar-tree-entry-action")
                icon("folder2-open", "load a file")
                onClick = loadFile@{
                    if (currentFile.hasUnsavedChanges() && !window.confirm("You have unsaved changes, continue?")) {
                        return@loadFile
                    }
                    hiddenFileInputRef.current!!.click()
                }
            }

            div {
                className = classes("sidebar-tree-entry-action")
                icon("download", "export configurations")
                onClick = {

                }
            }

            div {
                className = classes(
                    "sidebar-tree-entry-action",
                    "disabled".takeUnless { localAmpState != null }
                )
                icon("arrow-up", "Configure amplifier with the first 8 programs")
                onClick = {

                }
            }
        }

        for (config in currentFile.configs) {
            div {
                className = classes("sidebar-tree-entry", "sidebar-tree-entry--level-1")

                span {
                    className = ClassName("sidebar-tree-entry__label")
                    +(config.programName ?: "<no name>")
                    onClick = {

                    }
                }

                div {
                    className = classes(
                        "sidebar-tree-entry-action",
                        "disabled".takeUnless { localAmpState != null }
                    )
                    icon("arrow-up", "Write this program to the currently selected slot on the amp")
                    onClick = {

                    }
                }

                div {
                    className = classes(
                        "sidebar-tree-entry-action",
                        "disabled".takeUnless { localAmpState != null }
                    )
                    icon("arrow-down", "Write the current amp configuration to this slot.")
                    onClick = {

                    }
                }
            }
        }
    }

    div {
        className = ClassName("sidebar__bottom")

        LogLevelComponent {}
    }

    input {
        css {
            display = None.none
        }
        type = InputType.file
        ref = hiddenFileInputRef
        accept = ".vtxprog"
        multiple = false
        onChange = fileSelected@{
            val file = it.target.files!!.item(0)!!
            if (file.size.toLong() > 1024L * 10L) {
                window.alert("This file is too big to possible be a VTXPROG file.")
                return@fileSelected
            }

            GlobalScope.launch {
                try {
                    val vtxprogFile = VtxProgFile.readFromInVtxProgFormat(BlobBinaryInput(file))
                    currentFile = LoadedFile(
                        file.name,
                        vtxprogFile.programs.map { it.toUiDataModel() },
                    )
                }
                catch (ex: Throwable) {
                    logger.error("Failed to load VTXPROG file", ex)
                    val errorDetails = when (ex) {
                        is MessageParseException.InvalidMessage -> ": ${ex.message}"
                        is MessageParseException.PrefixNotRecognized -> ": does not appear to be a VTXPROG file."
                        else -> "; see console logs for details."
                    }
                    window.alert("Failed to load the file$errorDetails")
                }
                finally {
                    hiddenFileInputRef.current?.value = ""
                }
            }
        }
    }
}

private fun <T> T.repeat(times: Int): List<T> {
    val list = ArrayList<T>(times)
    repeat(times) {
        list.add(this)
    }
    return list
}