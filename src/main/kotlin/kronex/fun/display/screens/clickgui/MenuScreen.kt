package kronex.\`fun\`.display.screens.clickgui

import com.essde342.triggerbot.TriggerBotClient
import com.essde342.triggerbot.ui.imple.BooleanSetting
import com.essde342.triggerbot.ui.imple.NumberSetting
import com.essde342.triggerbot.ui.modules.Category
import com.essde342.triggerbot.ui.modules.Module
import com.essde342.triggerbot.ui.modules.ModuleManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Astra Click GUI.
 * The interface is Kotlin-rendered over the live world; it does not use the
 * vanilla background blur. Existing module objects remain the backend model.
 */
object MenuScreen : Screen(Text.literal("Astra Client")) {
    private data class Rect(val x: Int, val y: Int, val w: Int, val h: Int) {
        fun contains(px: Double, py: Double): Boolean =
            w > 0 && h > 0 && px >= x && py >= y && px < x + w && py < y + h
    }

    private data class WindowLayout(
        val x: Int, val y: Int, val w: Int, val h: Int,
        val sidebar: Int, val contentX: Int, val contentY: Int,
        val contentW: Int, val contentH: Int
    )
    private data class ModuleGroup(val title: String, val modules: List<Module>)
    private data class ModuleHit(val module: Module, val row: Rect, val toggle: Rect, val settings: Rect)
    private data class BooleanHit(val setting: BooleanSetting, val toggle: Rect)
    private data class SliderHit(val setting: NumberSetting, val track: Rect)

    private val categoryItems = listOf(
        Category.COMBAT to "Бой",
        Category.MOVEMENT to "Движение",
        Category.RENDER to "Визуалы",
        Category.PLAYER to "Игрок",
        Category.MISC to "Разное"
    )
    private val galahadFont = Identifier.of("triggerbot", "galahad")

    private val background = 0xF20B0B0D.toInt()
    private val sidebarBackground = 0xFF0D0D0F.toInt()
    private val cardBackground = 0xFF101012.toInt()
    private val rowHover = 0xFF191719.toInt()
    private val border = 0xFF252326.toInt()
    private val accent = 0xFFE47E82.toInt()
    private val primaryText = 0xFFF0EEEE.toInt()
    private val secondaryText = 0xFF929092.toInt()
    private val mutedText = 0xFF626064.toInt()
    private val switchOff = 0xFF343336.toInt()
    private val switchOn = 0xFF9F555B.toInt()
    private val switchKnob = 0xFFE8E5E5.toInt()

    private var selectedCategory = Category.COMBAT
    private var selectedModule: Module? = null
    private var settingsOpen = false
    private var waitingForBind: Module? = null
    private var draggingSlider: SliderHit? = null
    private var contentScroll = 0
    private var settingsScroll = 0
    private var contentMaxScroll = 0
    private var settingsMaxScroll = 0
    private lateinit var searchField: TextFieldWidget

    private val categoryHits = mutableListOf<Pair<Category, Rect>>()
    private val moduleHits = mutableListOf<ModuleHit>()
    private val booleanHits = mutableListOf<BooleanHit>()
    private val sliderHits = mutableListOf<SliderHit>()

    private var settingsPanel = Rect(0, 0, 0, 0)
    private var settingsClose = Rect(0, 0, 0, 0)
    private var settingsModuleSwitch = Rect(0, 0, 0, 0)
    private var settingsBind = Rect(0, 0, 0, 0)

    private val username: String
        get() = runCatching { MinecraftClient.getInstance().session.username }.getOrDefault("Player")

    @JvmStatic
    fun openGui() {
        ModuleManager.moduleRegister()
        selectedCategory = Category.COMBAT
        selectedModule = null
        settingsOpen = false
        waitingForBind = null
        contentScroll = 0
        settingsScroll = 0
        MinecraftClient.getInstance().setScreen(this)
    }

    override fun init() {
        super.init()
        val layout = calculateLayout()
        searchField = TextFieldWidget(
            textRenderer,
            layout.contentX + 54,
            layout.y + 15,
            (layout.contentW - 112).coerceAtLeast(54),
            20,
            Text.literal("Поиск")
        )
        searchField.setMaxLength(48)
        searchField.setSuggestion("Поиск модулей")
        searchField.setDrawsBackground(false)
        addDrawableChild(searchField)
    }

    private fun calculateLayout(): WindowLayout {
        val margin = if (width < 500) 7 else 20
        val panelW = min(1040, max(220, width - margin * 2))
        val panelH = min(690, max(180, height - margin * 2))
        val panelX = (width - panelW) / 2
        val panelY = (height - panelH) / 2
        val sidebarW = when {
            panelW < 360 -> 86
            panelW < 520 -> 112
            else -> 176
        }
        return WindowLayout(
            panelX, panelY, panelW, panelH, sidebarW,
            panelX + sidebarW, panelY + 58,
            panelW - sidebarW, panelH - 72
        )
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        // Keep the 3D world visible. Do not call renderBackground(): it blurs the world.
        context.fill(0, 0, width, height, 0x26000000)
        val layout = calculateLayout()
        drawWindow(context, layout)
        drawSidebar(context, layout, mouseX, mouseY)
        drawHeader(context, layout)
        drawModuleCards(context, layout, mouseX, mouseY)
        if (settingsOpen && selectedModule != null) {
            drawSettings(context, layout, selectedModule!!, mouseX, mouseY)
        }
        super.render(context, mouseX, mouseY, delta)
    }

    private fun drawWindow(context: DrawContext, layout: WindowLayout) {
        drawRounded(context, layout.x, layout.y, layout.w, layout.h, 11, background)
        context.drawBorder(layout.x, layout.y, layout.w, layout.h, border)
        drawRounded(
            context, layout.x + 1, layout.y + 1, layout.sidebar - 1,
            layout.h - 2, 9, sidebarBackground
        )
        context.fill(layout.x + layout.sidebar, layout.y + 1,
            layout.x + layout.sidebar + 1, layout.y + layout.h - 1, border)
        context.fill(layout.contentX, layout.y + 56,
            layout.x + layout.w - 1, layout.y + 57, border)
        context.fill(layout.x + 1, layout.y + layout.h - 43,
            layout.x + layout.sidebar - 1, layout.y + layout.h - 42, border)
    }

    private fun drawSidebar(context: DrawContext, layout: WindowLayout, mouseX: Int, mouseY: Int) {
        categoryHits.clear()
        val logoX = layout.x + if (layout.sidebar < 120) 13 else 24
        val logo = Text.literal("ASTRA").setStyle(Style.EMPTY.withFont(galahadFont))
        context.drawText(textRenderer, logo, logoX, layout.y + 17, primaryText, false)

        val sectionY = layout.y + 78
        drawText(context, "ФУНКЦИИ", layout.x + 17, sectionY, mutedText)
        val itemStart = sectionY + 19
        val itemHeight = if (layout.sidebar < 120) 31 else 36

        categoryItems.forEachIndexed { index, item ->
            val rect = Rect(layout.x + 8, itemStart + index * itemHeight,
                layout.sidebar - 16, itemHeight - 3)
            categoryHits.add(item.first to rect)
            val active = selectedCategory == item.first && searchField.getText().isBlank()
            if (active) {
                drawRounded(context, rect.x, rect.y, rect.w, rect.h, 6, 0xFF211719.toInt())
                context.drawBorder(rect.x, rect.y, rect.w, rect.h, 0xFF5C3438.toInt())
            } else if (rect.contains(mouseX.toDouble(), mouseY.toDouble())) {
                drawRounded(context, rect.x, rect.y, rect.w, rect.h, 6, rowHover)
            }
            drawText(context, categoryMarker(index), rect.x + 9, rect.y + 8,
                if (active) accent else secondaryText)
            val labelX = rect.x + if (layout.sidebar < 120) 25 else 32
            val labelW = (rect.w - (labelX - rect.x) - 4).coerceAtLeast(12)
            drawText(context, textRenderer.trimToWidth(item.second, labelW),
                labelX, rect.y + 8, if (active) accent else primaryText)
        }

        val footerY = layout.y + layout.h - 37
        drawRounded(context, layout.x + 13, footerY - 2, 25, 25, 12, 0xFF242326.toInt())
        drawText(context, "?", layout.x + 22, footerY + 6, secondaryText)
        if (layout.sidebar >= 120) {
            drawText(context, textRenderer.trimToWidth(username, layout.sidebar - 62),
                layout.x + 46, footerY + 2, primaryText)
            drawText(context, "Astra Client", layout.x + 46, footerY + 13, mutedText)
        }
    }

    private fun categoryMarker(index: Int): String = when (index) {
        0 -> "✣"
        1 -> "↕"
        2 -> "◎"
        3 -> "♙"
        else -> "⌘"
    }

    private fun drawHeader(context: DrawContext, layout: WindowLayout) {
        drawText(context, "МОДУЛИ", layout.contentX + 18, layout.y + 20, primaryText)
        val searchX = layout.contentX + 13
        val searchW = (layout.contentW - 26).coerceAtLeast(40)
        drawRounded(context, searchX, layout.y + 13, searchW, 25, 6, 0xFF101012.toInt())
        context.drawBorder(searchX, layout.y + 13, searchW, 25, border)
        drawText(context, "⌕", layout.contentX + 23, layout.y + 19, mutedText)
        val menuX = layout.x + layout.w - 39
        drawRounded(context, menuX, layout.y + 14, 25, 23, 5, 0xFF151517.toInt())
        context.drawBorder(menuX, layout.y + 14, 25, 23, border)
        drawText(context, "≡", menuX + 8, layout.y + 19, secondaryText)
    }

    private fun groupsForView(): List<ModuleGroup> {
        val allModules = ModuleManager.getModules().toList()
        val query = searchField.getText().trim()
        if (query.isNotEmpty()) {
            val matches = allModules.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.desc.contains(query, ignoreCase = true)
            }
            return matches.groupBy { it.category }
                .entries
                .sortedBy { entry -> categoryItems.indexOfFirst { item -> item.first == entry.key } }
                .map { entry -> ModuleGroup(categoryLabel(entry.key), entry.value) }
        }

        val selected = allModules.filter { it.category == selectedCategory }
        return when (selectedCategory) {
            Category.COMBAT -> listOf(ModuleGroup("ДРАКА", selected))
            Category.MOVEMENT -> listOf(ModuleGroup("ДВИЖЕНИЕ", selected))
            Category.PLAYER -> listOf(ModuleGroup("ИГРОК", selected))
            Category.MISC -> listOf(ModuleGroup("ИНСТРУМЕНТЫ", selected))
            Category.RENDER -> {
                val cameraNames = setOf("Fullbright", "No Hurt Cam", "Aspect Ratio")
                val visual = selected.filter { !cameraNames.contains(it.name) }
                val camera = selected.filter { cameraNames.contains(it.name) }
                listOf(
                    ModuleGroup("ВИЗУАЛЫ", visual),
                    ModuleGroup("КАМЕРА", camera)
                ).filter { it.modules.isNotEmpty() }
            }
        }
    }

    private fun categoryLabel(category: Category): String =
        categoryItems.firstOrNull { it.first == category }?.second?.uppercase() ?: "МОДУЛИ"

    private fun drawModuleCards(context: DrawContext, layout: WindowLayout, mouseX: Int, mouseY: Int) {
        moduleHits.clear()
        val groups = groupsForView()
        val x = layout.contentX + 15
        val y = layout.contentY + 10
        val availableW = layout.contentW - 30
        val gap = 12
        val columns = if (availableW >= 470) 2 else 1
        val columnW = ((availableW - gap * (columns - 1)) / columns).coerceAtLeast(70)
        val bottoms = IntArray(columns) { 0 }
        val rowHeight = if (layout.contentW < 300) 38 else 43
        val viewport = Rect(layout.contentX + 4, layout.contentY,
            layout.contentW - 8, layout.contentH - 4)

        val cards = if (groups.isEmpty()) {
            listOf(ModuleGroup(if (searchField.getText().isNotBlank()) "ПОИСК" else categoryLabel(selectedCategory), emptyList()))
        } else groups

        context.enableScissor(viewport.x, viewport.y, viewport.x + viewport.w, viewport.y + viewport.h)
        cards.forEach { group ->
            val targetColumn = bottoms.indices.minByOrNull { bottoms[it] } ?: 0
            val cardX = x + targetColumn * (columnW + gap)
            val cardY = y + bottoms[targetColumn] - contentScroll
            val cardH = 42 + max(1, group.modules.size) * rowHeight
            val card = Rect(cardX, cardY, columnW, cardH)

            drawRounded(context, card.x, card.y, card.w, card.h, 8, cardBackground)
            context.drawBorder(card.x, card.y, card.w, card.h, border)
            drawText(context, textRenderer.trimToWidth(group.title, card.w - 30),
                card.x + 12, card.y + 12, accent)
            drawText(context, "◆", card.x + card.w - 19, card.y + 12, accent)
            context.fill(card.x + 1, card.y + 35, card.x + card.w - 1, card.y + 36, 0xFF1D1B1E.toInt())

            if (group.modules.isEmpty()) {
                val empty = if (searchField.getText().isNotBlank()) "Ничего не найдено" else "Пока нет модулей"
                drawText(context, empty, card.x + 12, card.y + 55, mutedText)
            } else {
                group.modules.forEachIndexed { index, module ->
                    val rowY = card.y + 43 + index * rowHeight
                    val row = Rect(card.x + 5, rowY, card.w - 10, rowHeight - 1)
                    if (row.contains(mouseX.toDouble(), mouseY.toDouble())) {
                        drawRounded(context, row.x, row.y, row.w, row.h, 4, rowHover)
                    }
                    val toggle = Rect(row.x + row.w - 34, row.y + (row.h - 15) / 2, 30, 15)
                    val settings = Rect(row.x + row.w - 67, row.y + 3, 25, row.h - 7)
                    moduleHits.add(ModuleHit(module, row, toggle, settings))
                    val textW = (row.w - 108).coerceAtLeast(16)
                    drawText(context, textRenderer.trimToWidth(module.name, textW),
                        row.x + 8, row.y + (row.h - 8) / 2,
                        if (module.isEnabled) primaryText else secondaryText)
                    drawText(context, "•••", settings.x + 4, settings.y + 7, secondaryText)
                    drawSwitch(context, toggle, module.isEnabled)
                    if (index < group.modules.lastIndex) {
                        context.fill(row.x + 7, row.y + row.h,
                            row.x + row.w - 7, row.y + row.h + 1, 0xFF1E1C1F.toInt())
                    }
                }
            }
            bottoms[targetColumn] += cardH + 12
        }
        context.disableScissor()

        val contentHeight = bottoms.maxOrNull() ?: 0
        contentMaxScroll = (contentHeight - viewport.h).coerceAtLeast(0)
        contentScroll = contentScroll.coerceIn(0, contentMaxScroll)

        if (contentMaxScroll > 0) {
            val trackX = layout.x + layout.w - 8
            val trackY = viewport.y + 4
            val trackH = viewport.h - 8
            context.fill(trackX, trackY, trackX + 2, trackY + trackH, 0xFF242225.toInt())
            val thumbH = max(22, trackH * trackH / (contentHeight + 1))
            val thumbY = trackY + ((trackH - thumbH).toDouble() * contentScroll / contentMaxScroll).roundToInt()
            drawRounded(context, trackX, thumbY, 2, thumbH, 1, accent)
        }
    }

    private fun drawSettings(
        context: DrawContext,
        layout: WindowLayout,
        module: Module,
        mouseX: Int,
        mouseY: Int
    ) {
        val content = Rect(layout.contentX + 4, layout.contentY,
            layout.contentW - 8, layout.contentH - 4)
        context.fill(content.x, content.y, content.x + content.w, content.y + content.h, 0xAA080809.toInt())

        val popupW = min(430, content.w - 18).coerceAtLeast(150)
        val popupH = min(500, content.h - 18).coerceAtLeast(125)
        val popupX = content.x + (content.w - popupW) / 2
        val popupY = content.y + (content.h - popupH) / 2
        settingsPanel = Rect(popupX, popupY, popupW, popupH)

        drawRounded(context, popupX, popupY, popupW, popupH, 9, 0xFF111113.toInt())
        context.drawBorder(popupX, popupY, popupW, popupH, 0xFF343034.toInt())
        drawText(context, "НАСТРОЙКИ МОДУЛЯ", popupX + 14, popupY + 13, mutedText)
        drawText(context, textRenderer.trimToWidth(module.name, popupW - 62),
            popupX + 14, popupY + 29, primaryText)

        settingsClose = Rect(popupX + popupW - 28, popupY + 9, 19, 19)
        drawRounded(context, settingsClose.x, settingsClose.y,
            settingsClose.w, settingsClose.h, 5, rowHover)
        drawText(context, "×", settingsClose.x + 6, settingsClose.y + 5, secondaryText)

        settingsModuleSwitch = Rect(popupX + popupW - 48, popupY + 33, 32, 16)
        drawSwitch(context, settingsModuleSwitch, module.isEnabled)
        drawText(context, textRenderer.trimToWidth(module.desc, popupW - 28),
            popupX + 14, popupY + 48, secondaryText)

        settingsBind = Rect(popupX + 12, popupY + 66, popupW - 24, 24)
        drawRounded(context, settingsBind.x, settingsBind.y,
            settingsBind.w, settingsBind.h, 5, 0xFF19181A.toInt())
        context.drawBorder(settingsBind.x, settingsBind.y, settingsBind.w, settingsBind.h, border)
        val bindText = if (waitingForBind === module) "НАЖМИ КЛАВИШУ..." else "БИНД: " + formatBind(module.bind)
        drawText(context, bindText, settingsBind.x + 9, settingsBind.y + 8, secondaryText)

        val settingTop = popupY + 103
        val settingBottom = popupY + popupH - 10
        val settingViewport = max(0, settingBottom - settingTop)
        val totalSettingsHeight = module.settings.sumOf { if (it is NumberSetting) 48 else 34 }
        settingsMaxScroll = (totalSettingsHeight - settingViewport).coerceAtLeast(0)
        settingsScroll = settingsScroll.coerceIn(0, settingsMaxScroll)
        booleanHits.clear()
        sliderHits.clear()

        context.enableScissor(popupX + 8, settingTop, popupX + popupW - 8, settingBottom)
        var offset = 0
        module.settings.forEach { setting ->
            val rowHeight = if (setting is NumberSetting) 48 else 34
            val rowY = settingTop + offset - settingsScroll
            if (rowY + rowHeight >= settingTop && rowY <= settingBottom) {
                when (setting) {
                    is BooleanSetting -> {
                        drawText(context, textRenderer.trimToWidth(setting.name, popupW - 78),
                            popupX + 14, rowY + 11, primaryText)
                        val toggle = Rect(popupX + popupW - 48, rowY + 7, 32, 16)
                        drawSwitch(context, toggle, setting.isEnabled)
                        booleanHits.add(BooleanHit(setting, toggle))
                    }
                    is NumberSetting -> {
                        drawText(context, textRenderer.trimToWidth(setting.name, popupW - 88),
                            popupX + 14, rowY + 5, primaryText)
                        val value = formatNumber(setting.doubleValue, setting.increment)
                        val valueWidth = textRenderer.getWidth(value)
                        drawText(context, value, popupX + popupW - valueWidth - 14, rowY + 5, accent)
                        val track = Rect(popupX + 14, rowY + 25, popupW - 28, 12)
                        drawSlider(context, track, setting.doubleValue, setting.min, setting.max)
                        sliderHits.add(SliderHit(setting, track))
                    }
                }
            }
            offset += rowHeight
        }
        context.disableScissor()

        if (module.settings.isEmpty()) {
            drawText(context, "Нет дополнительных параметров", popupX + 14, settingTop + 12, mutedText)
        }
    }

    private fun drawSwitch(context: DrawContext, rect: Rect, enabled: Boolean) {
        drawRounded(context, rect.x, rect.y, rect.w, rect.h, rect.h / 2,
            if (enabled) switchOn else switchOff)
        val knob = rect.h - 5
        val knobX = if (enabled) rect.x + rect.w - knob - 2 else rect.x + 2
        drawRounded(context, knobX, rect.y + 2, knob, knob, knob / 2, switchKnob)
    }

    private fun drawSlider(context: DrawContext, rect: Rect, value: Double, minValue: Double, maxValue: Double) {
        val h = 4
        val y = rect.y + rect.h / 2 - h / 2
        drawRounded(context, rect.x, y, rect.w, h, 2, switchOff)
        val range = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0
        val progress = ((value - minValue) / range).coerceIn(0.0, 1.0)
        val filled = (rect.w * progress).roundToInt()
        drawRounded(context, rect.x, y, max(1, filled), h, 2, accent)
        val knobX = (rect.x + filled - 4).coerceIn(rect.x, rect.x + max(0, rect.w - 8))
        drawRounded(context, knobX, y - 3, 8, 10, 4, primaryText)
    }

    private fun drawRounded(
        context: DrawContext,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        radius: Int,
        color: Int
    ) {
        if (w <= 0 || h <= 0) return
        val r = min(radius, min(w / 2, h / 2)).coerceAtLeast(0)
        if (r == 0) {
            context.fill(x, y, x + w, y + h, color)
            return
        }
        context.fill(x + r, y, x + w - r, y + h, color)
        context.fill(x, y + r, x + w, y + h - r, color)
        for (row in 0 until r) {
            val inset = r - 1 - row
            context.fill(x + inset, y + row, x + w - inset, y + row + 1, color)
            context.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, color)
        }
    }

    private fun drawText(context: DrawContext, value: String, x: Int, y: Int, color: Int) {
        context.drawText(textRenderer, value, x, y, color, false)
    }

    private fun formatNumber(value: Double, step: Double): String =
        if (step >= 1.0) value.roundToInt().toString()
        else String.format(java.util.Locale.ROOT, "%.2f", value)

    private fun formatBind(bind: Int): String = when {
        bind < 0 -> "НЕТ"
        bind >= GLFW.GLFW_MOUSE_BUTTON_1 && bind <= GLFW.GLFW_MOUSE_BUTTON_LAST ->
            "МЫШЬ " + (bind - GLFW.GLFW_MOUSE_BUTTON_1 + 1)
        else -> GLFW.glfwGetKeyName(bind, 0)?.uppercase() ?: ("KEY " + bind)
    }

    private fun updateSlider(hit: SliderHit, mouseX: Double) {
        if (hit.track.w <= 0) return
        val percent = ((mouseX - hit.track.x) / hit.track.w.toDouble()).coerceIn(0.0, 1.0)
        val raw = hit.setting.min + (hit.setting.max - hit.setting.min) * percent
        val step = hit.setting.increment.takeIf { it > 0.0 } ?: 0.01
        val value = hit.setting.min + ((raw - hit.setting.min) / step).roundToInt() * step
        hit.setting.setValue(value)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (waitingForBind != null && button in 0..7) {
            val module = waitingForBind
            module?.bind = GLFW.GLFW_MOUSE_BUTTON_1 + button
            waitingForBind = null
            ModuleManager.syncBindsToConfig()
            TriggerBotClient.saveConfig()
            return true
        }
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button)

        if (settingsOpen) {
            if (settingsClose.contains(mouseX, mouseY)) {
                settingsOpen = false
                waitingForBind = null
                return true
            }
            val module = selectedModule
            if (module != null && settingsModuleSwitch.contains(mouseX, mouseY)) {
                module.toggled()
                TriggerBotClient.saveConfig()
                return true
            }
            if (module != null && settingsBind.contains(mouseX, mouseY)) {
                waitingForBind = module
                return true
            }
            booleanHits.firstOrNull { it.toggle.contains(mouseX, mouseY) }?.let {
                it.setting.toggle()
                TriggerBotClient.saveConfig()
                return true
            }
            sliderHits.firstOrNull { it.track.contains(mouseX, mouseY) }?.let {
                draggingSlider = it
                updateSlider(it, mouseX)
                return true
            }
            if (!settingsPanel.contains(mouseX, mouseY)) {
                settingsOpen = false
                waitingForBind = null
            }
            return true
        }

        categoryHits.firstOrNull { it.second.contains(mouseX, mouseY) }?.let {
            selectedCategory = it.first
            contentScroll = 0
            return true
        }
        moduleHits.firstOrNull { it.settings.contains(mouseX, mouseY) }?.let {
            selectedModule = it.module
            settingsScroll = 0
            settingsOpen = true
            waitingForBind = null
            return true
        }
        moduleHits.firstOrNull { it.toggle.contains(mouseX, mouseY) }?.let {
            it.module.toggled()
            TriggerBotClient.saveConfig()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(
        mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double
    ): Boolean {
        draggingSlider?.let {
            updateSlider(it, mouseX)
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (draggingSlider != null) {
            draggingSlider = null
            TriggerBotClient.saveConfig()
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(
        mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double
    ): Boolean {
        val direction = if (verticalAmount < 0) 1 else -1
        if (settingsOpen) {
            settingsScroll = (settingsScroll + direction * 28).coerceIn(0, settingsMaxScroll)
        } else {
            contentScroll = (contentScroll + direction * 34).coerceIn(0, contentMaxScroll)
        }
        return true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        waitingForBind?.let { module ->
            when (keyCode) {
                GLFW.GLFW_KEY_ESCAPE -> {
                    waitingForBind = null
                    return true
                }
                GLFW.GLFW_KEY_DELETE, GLFW.GLFW_KEY_BACKSPACE -> module.bind = -1
                else -> module.bind = keyCode
            }
            waitingForBind = null
            ModuleManager.syncBindsToConfig()
            TriggerBotClient.saveConfig()
            return true
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun shouldPause(): Boolean = false

    override fun close() {
        settingsOpen = false
        waitingForBind = null
        draggingSlider = null
        TriggerBotClient.saveConfig()
        super.close()
    }
}
