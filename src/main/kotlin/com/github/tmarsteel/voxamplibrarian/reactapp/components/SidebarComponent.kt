package com.github.tmarsteel.voxamplibrarian.reactapp.components

import com.github.tmarsteel.voxamplibrarian.BlobBinaryInput
import com.github.tmarsteel.voxamplibrarian.BlobBinaryOutput.Companion.writeToBlob
import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration.toProtocolDataModel
import com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration.toUiDataModel
import com.github.tmarsteel.voxamplibrarian.kibibytes
import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException
import com.github.tmarsteel.voxamplibrarian.reactapp.classes
import com.github.tmarsteel.voxamplibrarian.reactapp.components.sidebar.ProgramSlotComponent
import com.github.tmarsteel.voxamplibrarian.reactapp.components.sidebar.ProgramSlotLocation
import com.github.tmarsteel.voxamplibrarian.reactapp.icon
import com.github.tmarsteel.voxamplibrarian.startDownload
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
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.span
import react.useState
import kotlin.js.Date

external interface SidebarComponentProps : Props {
    var ampConnected: Boolean
    var vtxAmpState: VtxAmpState?
    var onProgramSlotSelected: (ProgramSlot) -> Unit
    var onSaveConfiguration: (ProgramSlot) -> Unit
    var onLoadConfiguration: (ProgramSlot) -> Unit
    var onViewNonAmpConfiguration: (SimulationConfiguration) -> Unit
    var onWriteConfigurationToAmpSlot: (SimulationConfiguration, ProgramSlot) -> Unit
    var onClose: () -> Unit
}

private val logger = LoggerFactory["sidebar"]

private data class LoadedFile(
    val filename: String?,
    val configs: List<SimulationConfiguration>,
    /** for change detection */
    val originalConfigs: List<SimulationConfiguration> = configs,
) {
    fun hasUnsavedChanges(): Boolean {
        return configs.zip(originalConfigs).any { (currentConfig, originalConfig) -> currentConfig != originalConfig }
    }

    fun withChangesSaved(asFilename: String): LoadedFile = copy(asFilename, configs, configs)

    fun withConfigAtIndex(config: SimulationConfiguration, index: Int): LoadedFile {
        val newConfigs = configs.toMutableList()
        if (index > newConfigs.lastIndex) {
            repeat(index - newConfigs.lastIndex) {
                newConfigs.add(SimulationConfiguration.DEFAULT)
            }
        }
        newConfigs[index] = config
        return copy(configs = newConfigs)
    }

    companion object {
        val DEFAULT = LoadedFile(null, SimulationConfiguration.DEFAULT.repeat(11))
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

        div {
            className = classes("sidebar__slots")

            for (programSlot in ProgramSlot.values()) {
                ProgramSlotComponent {
                    programName = localAmpState?.storedUserPrograms?.get(programSlot)?.programName
                    location = ProgramSlotLocation.Amplifier(programSlot)
                    onViewProgram = ({
                        props.onLoadConfiguration(programSlot)
                    }).takeIf { localAmpState != null }
                    onSaveToThisLocation = ({
                        props.onSaveConfiguration(programSlot)
                    }).takeIf { localAmpState != null }
                    isActive = props.ampConnected && localAmpState != null && localAmpState is VtxAmpState.ProgramSlotSelected && localAmpState.slot == programSlot
                    onActivated = ({
                        props.onProgramSlotSelected(programSlot)
                    }).takeIf { localAmpState != null }
                }
            }
        }

        div {
            className = classes("sidebar-tree-entry", "sidebar-tree-entry--level-0")
            icon("file-earmark", "Currently loaded file")
            span {
                className = classes("sidebar-tree-entry__label")
                +(currentFile.filename ?: "<no filename>")
            }
        }

        div {
            css(ClassName("actions")) {
                marginBottom = 1.rem
            }

            button {
                icon("folder2-open", "load a file")
                +"Load file"
                title = "Load a file that contains programs"

                onClick = loadFile@{
                    if (currentFile.hasUnsavedChanges() && !window.confirm("You have unsaved changes, continue?")) {
                        return@loadFile
                    }
                    hiddenFileInputRef.current!!.click()
                }
            }

            button {
                icon("download", "export")
                +"Export programs"
                title = "Export these programs as a file"

                onClick = {
                    val newVtxProgFile = VtxProgFile(currentFile.configs.map { it.toProtocolDataModel() })
                    val blob = writeToBlob { binaryOut ->
                        newVtxProgFile.writeToInVtxProgFormat(binaryOut)
                    }

                    val filename = currentFile.filename ?: run {
                        val now = Date()
                        "unknown-${now.getFullYear()}-${(now.getMonth() + 1).toString().padStart(2, '0')}-${now.getDate().toString().padStart(2, '0')}.vtxporg"
                    }
                    startDownload(blob, filename)
                    currentFile = currentFile.withChangesSaved(filename)
                }
            }

            button {
                icon("arrow-up", "Configure amplifier")
                +"Apply all to Amp"
                title = "Configure amplifier with the first ${ProgramSlot.values().size} programs"

                onClick = {
                    currentFile.configs
                        .take(ProgramSlot.values().size)
                        .zip(ProgramSlot.values())
                        .forEach { (config, slot) ->
                            props.onWriteConfigurationToAmpSlot(config, slot)
                        }

                    props.onProgramSlotSelected(ProgramSlot.A1)
                }
            }
        }

        val ampInteractPossible: Boolean = localAmpState != null && localAmpState is VtxAmpState.ProgramSlotSelected
        div {
            className = classes("sidebar__slots")

            currentFile.configs.forEachIndexed { configIndexInFile, config ->
                ProgramSlotComponent {
                    location = ProgramSlotLocation.File(currentFile.filename, configIndexInFile)
                    programName = config.programName
                    onViewProgram = {
                        props.onViewNonAmpConfiguration(config)
                    }
                    onSaveIntoSelectedAmpSlot = (storeFileProgramToAmp@{
                        // TODO
                    }).takeIf { ampInteractPossible }
                    onSaveToThisLocation = (saveToFileSlot@{
                        // TODO
                    }).takeIf { ampInteractPossible }
                }
            }
        }

        div {
            className = classes("sidebar-tree-entry", "sidebar-tree-entry--level-0")
            icon("bug", "Developer Settings")

            span {
                className = classes("sidebar-tree-entry__label")
                +"Developer Settings"
            }
        }

        div {
            className = classes("sidebar-tree-entry", "sidebar-tree-entry--level-1")

            LogLevelComponent {}
        }
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
            if (file.size.toLong() > 10.kibibytes) {
                window.alert("This file is too big to possibly be a VTXPROG file.")
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