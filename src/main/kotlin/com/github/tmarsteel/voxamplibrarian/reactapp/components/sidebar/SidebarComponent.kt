package com.github.tmarsteel.voxamplibrarian.reactapp.components.sidebar

import com.github.tmarsteel.voxamplibrarian.*
import com.github.tmarsteel.voxamplibrarian.BlobBinaryOutput.Companion.writeToBlob
import com.github.tmarsteel.voxamplibrarian.appmodel.SimulationConfiguration
import com.github.tmarsteel.voxamplibrarian.appmodel.VtxAmpState
import com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration.toProtocolDataModel
import com.github.tmarsteel.voxamplibrarian.appmodel.hardware_integration.toUiDataModel
import com.github.tmarsteel.voxamplibrarian.logging.LoggerFactory
import com.github.tmarsteel.voxamplibrarian.protocol.Program
import com.github.tmarsteel.voxamplibrarian.protocol.ProgramSlot
import com.github.tmarsteel.voxamplibrarian.protocol.message.MessageParseException
import com.github.tmarsteel.voxamplibrarian.reactapp.StateAndLocalStorageHook.Companion.useStateBackedByLocalStorage
import com.github.tmarsteel.voxamplibrarian.reactapp.classes
import com.github.tmarsteel.voxamplibrarian.reactapp.components.ConnectivityIndicatorComponent
import com.github.tmarsteel.voxamplibrarian.reactapp.components.LogLevelComponent
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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import react.FC
import react.Props
import react.createRef
import react.dom.events.ChangeEvent
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.span
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

@Serializable
private data class PersistedState(
    val configGroups: List<ConfigurationGroup>,
    val selectedGroupUid: String,
) {
    init {
        require(configGroups.any { it.uid == selectedGroupUid })
    }

    val selectedGroup: ConfigurationGroup by lazy {
        configGroups.find { it.uid == selectedGroupUid }!!
    }

    fun withGroup(newGroup: ConfigurationGroup): PersistedState {
        return copy(configGroups = configGroups.filter { it.uid != newGroup.uid } + listOf(newGroup))
    }

    fun withoutSelectedGroup(): PersistedState {
        val newGroups = configGroups.toMutableList()
        newGroups.retainAll { it.uid != selectedGroupUid }
        if (newGroups.isEmpty()) {
            newGroups.add(ConfigurationGroup.createBlank())
        }

        return copy(
            configGroups = newGroups,
            selectedGroupUid = newGroups.first().uid,
        )
    }

    companion object {
        fun createBlank(): PersistedState {
            val singleGroup = ConfigurationGroup.createBlank()
            return PersistedState(listOf(singleGroup), singleGroup.uid)
        }
    }
}

@Serializable
private data class ConfigurationGroup(
    val uid: String,
    val name: String?,
    @Serializable
    val configs: List<@Serializable(SimulationConfigAsHexStreamSerializer::class) SimulationConfiguration>,
) {
    fun withFirstConfigs(configs: List<SimulationConfiguration>): ConfigurationGroup {
        val newConfigs = ArrayList(this.configs)
        newConfigs.ensureCapacity(configs.size)
        configs.forEachIndexed { i, newConfig -> newConfigs[i] = newConfig }
        return copy(configs = newConfigs)
    }

    fun withConfigAtIndex(config: SimulationConfiguration, index: Int): ConfigurationGroup {
        val newConfigs = configs.toMutableList()
        if (index > newConfigs.lastIndex) {
            repeat(index - newConfigs.lastIndex) {
                newConfigs.add(SimulationConfiguration.DEFAULT)
            }
        }
        newConfigs[index] = config
        return copy(configs = newConfigs)
    }

    fun withoutConfigAtIndex(index: Int): ConfigurationGroup {
        val newConfigs = configs.toMutableList()
        newConfigs.removeAt(index)
        return copy(configs = newConfigs)
    }

    fun withAdditionalConfig(config: SimulationConfiguration): ConfigurationGroup {
        return copy(configs = configs + config)
    }

    companion object {
        const val MIN_CONFIGS_IN_GROUP = 11
        fun createBlank(): ConfigurationGroup {
            return ConfigurationGroup(
                window.asDynamic().crypto.randomUUID(),
                null,
                SimulationConfiguration.DEFAULT.repeat(MIN_CONFIGS_IN_GROUP)
            )
        }

        fun fromVtxProgFile(file: VtxProgFile, filename: String): ConfigurationGroup {
            val name = filename.removeSuffix(".vtxprog").removeSuffix(".VTXPROG")
            return createBlank().copy(
                name = name,
                configs = file.programs.map { it.toUiDataModel() },
            )
        }
    }
}

val SidebarComponent = FC<SidebarComponentProps> { props ->
    val localAmpState = props.vtxAmpState?.takeIf { props.ampConnected }
    var persistedState by useStateBackedByLocalStorage(
        "sidebar-state",
        PersistedState.createBlank(),
        { Json.encodeToString(it) },
        { Json.decodeFromString(it) },
    )
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
            className = classes("sidebar__section-heading")

            ConnectivityIndicatorComponent {
                isActive = props.ampConnected
            }

            span {
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
            className = classes("sidebar__section-heading")
            icon("file-earmark-binary", "Current saved preset")
            select {
                value = persistedState.selectedGroupUid
                onChange = { e: ChangeEvent<HTMLSelectElement> ->
                    persistedState = persistedState.copy(selectedGroupUid = e.target.value)
                }
                for (group in persistedState.configGroups.sortedBy { it.name }) {
                    option {
                        value = group.uid
                        +(group.name ?: "<no name>")
                    }
                }
            }

            button {
                icon("pencil-fill")
                title = "Rename"
                onClick = {
                    val newName = window.prompt("New name for the saved preset", persistedState.selectedGroup.name ?: "")
                    persistedState = persistedState.withGroup(persistedState.selectedGroup.copy(name = newName))
                }
            }

            button {
                icon("trash")
                title = "Delete this saved preset"
                onClick = deleteGroup@{
                    if (!window.confirm("Remove the saved preset ${persistedState.selectedGroup.name ?: "<no name>"}")) {
                        return@deleteGroup
                    }

                    persistedState = persistedState.withoutSelectedGroup()
                }
            }

            button {
                icon("plus")
                title = "Add new saved preset"
                onClick = {
                    val newGroup = ConfigurationGroup.createBlank()
                    persistedState = persistedState.withGroup(newGroup).copy(selectedGroupUid = newGroup.uid)
                }
            }
        }

        div {
            css(ClassName("actions")) {
                marginBottom = 1.rem
            }

            button {
                icon("folder")
                span {
                    +"Import File"
                }
                title = "Import a .VTXPROG file from VOX TomeRoom"

                onClick = loadFile@{
                    hiddenFileInputRef.current!!.click()
                }
            }

            button {
                icon("save2")
                span {
                    +"Export as File"
                }
                title = "Export these programs as a .VTXPROG file, compatible with VOX ToneRoom"

                onClick = {
                    val newVtxProgFile = VtxProgFile(persistedState.selectedGroup.configs.map { it.toProtocolDataModel() })
                    val blob = writeToBlob { binaryOut ->
                        newVtxProgFile.writeToInVtxProgFormat(binaryOut)
                    }

                    val filename = (persistedState.selectedGroup.name ?: run {
                        val now = Date()
                        "unknown-${now.getFullYear()}-${(now.getMonth() + 1).toString().padStart(2, '0')}-${now.getDate().toString().padStart(2, '0')}"
                    }) + ".vtxporg"
                    startDownload(blob, filename)
                }
            }

            button {
                icon("arrow-up", "Configure amplifier")
                span {
                    +"Apply all to Amp"
                }
                title = "Configure amplifier with the first ${ProgramSlot.values().size} programs"

                disabled = !props.ampConnected || props.vtxAmpState == null
                onClick = {
                    persistedState.selectedGroup.configs
                        .take(ProgramSlot.values().size)
                        .zip(ProgramSlot.values())
                        .forEach { (config, slot) ->
                            props.onWriteConfigurationToAmpSlot(config, slot)
                        }

                    props.onProgramSlotSelected(ProgramSlot.A1)
                }
            }

            button {
                icon("arrow-down", "Save amplifier config")
                span {
                    +"Save all from Amp"
                }
                title = "Copies all the programs from the amplifier to this file"

                disabled = !props.ampConnected || props.vtxAmpState == null
                onClick = {
                    val ampConfigs = props.vtxAmpState!!.storedUserPrograms.entries
                        .sortedBy { (slot, _) -> slot }
                        .map { (_, config) -> config }
                    persistedState = persistedState.withGroup(
                        persistedState.selectedGroup.withFirstConfigs(ampConfigs)
                    )
                }
            }
        }

        val ampInteractPossible: Boolean = localAmpState != null && localAmpState is VtxAmpState.ProgramSlotSelected
        div {
            className = classes("sidebar__slots")

            persistedState.selectedGroup.configs.forEachIndexed { configIndexInGroup, config ->
                ProgramSlotComponent {
                    location = ProgramSlotLocation.File(persistedState.selectedGroup.name, configIndexInGroup)
                    programName = config.programName
                    onViewProgram = {
                        props.onViewNonAmpConfiguration(config)
                    }
                    onSaveIntoSelectedAmpSlot = (storeFileProgramToAmp@{
                        val selectedSlot = (localAmpState as VtxAmpState.ProgramSlotSelected).slot
                        props.onWriteConfigurationToAmpSlot(config, selectedSlot)
                    }).takeIf { ampInteractPossible }
                    onSaveToThisLocation = (saveToFileSlot@{
                        val localConfig = localAmpState?.activeConfiguration ?: return@saveToFileSlot
                        persistedState = persistedState.withGroup(
                            persistedState.selectedGroup.withConfigAtIndex(localConfig, configIndexInGroup)
                        )
                    }).takeIf { ampInteractPossible }
                    onDelete = ({
                        persistedState = persistedState.withGroup(
                            persistedState.selectedGroup.withoutConfigAtIndex(configIndexInGroup)
                        )
                    }).takeIf { persistedState.selectedGroup.configs.size > 1 }
                }
            }

            AddProgramSlotComponent {
                onAddSlot = {
                    persistedState = persistedState.withGroup(
                        persistedState.selectedGroup.withAdditionalConfig(SimulationConfiguration.DEFAULT)
                    )
                }
            }
        }

        div {
            className = classes("sidebar__section-heading")
            icon("bug", "Developer Settings")

            span {
                +"Developer Settings"
            }
        }

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
            if (file.size.toLong() > 1.mibibytes) {
                window.alert("This file is too big, max 1 MiB")
                return@fileSelected
            }

            GlobalScope.launch {
                try {
                    val vtxprogFile = VtxProgFile.readFromInVtxProgFormat(BlobBinaryInput(file))
                    val newGroup = ConfigurationGroup.fromVtxProgFile(vtxprogFile, file.name)
                    persistedState = persistedState.withGroup(newGroup).copy(selectedGroupUid = newGroup.uid)
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

private class SimulationConfigAsHexStreamSerializer : KSerializer<SimulationConfiguration> {
    override fun deserialize(decoder: Decoder): SimulationConfiguration {
        val input = ByteArrayBinaryInput(decoder.decodeString().parseHexStream())
        return Program.readFrom(input).toUiDataModel()
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        SimulationConfiguration::class.simpleName ?: "kotlin.String",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: SimulationConfiguration) {
        val buffer = BufferedBinaryOutput()
        value.toProtocolDataModel().writeTo(buffer)
        val hexStream = buffer.getAsHexStream()
        encoder.encodeString(hexStream)
    }
}