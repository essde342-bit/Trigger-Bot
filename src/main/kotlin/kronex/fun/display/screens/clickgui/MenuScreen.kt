package kronex.`fun`.display.screens.clickgui

import com.essde342.triggerbot.AltManager
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Astra's Kotlin-only click menu.
 *
 * The old Java component tree has been removed. Java is retained only for
 * the client/gameplay model (modules, settings and persistence).
 */
object MenuScreen : Screen(Text.literal("Astra Client")) {
    private enum class Page { HOME, GAME, ACCOUNTS }

    private data class Rect(val x: Int, val y: Int, val w: Int, val h: Int) {
        fun contains(px: Double, py: Double): Boolean =
            px >= x && px <= x + w && py >= y && py <= y + h
    }

    private data class CardHit(val module: Module, val bounds: Rect, val toggle: Rect)
    private data class CategoryHit(val category: Category, val bounds: Rect)
    private data class BooleanHit(val setting: BooleanSetting, val bounds: Rect)
    private data class SliderHit(val setting: NumberSetting, val bounds: Rect, val trackX: Int, val trackWidth: Int)
    private data class AccountHit(val index: Int, val row: Rect, val delete: Rect)

    private var page = Page.HOME
    private var category = Category.COMBAT
    private var selectedModule: Module? = null
    private var waitingForBind: Module? = null
    private var draggingSlider: SliderHit? = null
    private var moduleScroll = 0
    private var accountScroll = 0
    private var selectedAccount = -1
    private var accountStatus = ""

    private val cardHits = mutableListOf<CardHit>()
    private val categoryHits = mutableListOf<CategoryHit>()
    private val booleanHits = mutableListOf<BooleanHit>()
    private val sliderHits = mutableListOf<SliderHit>()
    private val accountHits = mutableListOf<AccountHit>()
    private val navHits = mutableMapOf<Page, Rect>()

    private lateinit var nicknameInput: TextFieldWidget

    private val accent = 0xFF69D5B6.toInt()
    private val accentDim = 0xFF2D806D.toInt()
    private val textMain = 0xFFE7F2EE.toInt()
    private val textMuted = 0xFF91A8A0.toInt()
    private val panel = 0xD90A1413.toInt()
    private val panelSoft = 0xC80D1A18.toInt()
    private val border = 0x663F9C83.toInt()
    private val galahad = Identifier.of("triggerbot", "galahad")

    @JvmStatic
    fun openGui() {
        animationDirectionReset()
        MinecraftClient.getInstance().setScreen(this)
    }

    private fun animationDirectionReset() {
        waitingForBind = null
        draggingSlider = null
    }

    override fun init() {
        super.init()
        nicknameInput = TextFieldWidget(
            textRenderer,
            width / 2 - 132,
            111,
            264,
            22,
            Text.literal("Nickname")
        )
        nicknameInput.setMaxLength(16)
        nicknameInput.setSuggestion("Никнейм: 3–16 символов")
        nicknameInput.setText(AltManager.currentNickname())
        addDrawableChild(nicknameInput)
        updateAccountInputVisibility()
    }

    private fun updateAccountInputVisibility() {
        if (::nicknameInput.isInitialized) {
            nicknameInput.visible = page == Page.ACCOUNTS
            nicknameInput.active = page == Page.ACCOUNTS
        }
    }

    private fun changePage(next: Page) {
        page = next
        waitingForBind = null
        draggingSlider = null
        updateAccountInputVisibility()
        if (next == Page.GAME) {
            val modules = ModuleManager.getByCategory(category)
            if (selectedModule == null || modules.none { it === selectedModule }) {
                selectedModule = modules.firstOrNull()
            }
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        context.fill(0, 0, width, height, 0xCE040B0A.toInt())

        // A restrained teal ambience, inspired by Arbuz's dark aquatic scene.
        context.fill(width / 2 - 1, 0, width / 2 + 1, height, 0x123D9C83)
        context.fill(0, height - 2, width, height, 0x553B9F83)

        drawHeader(context)
        when (page) {
            Page.HOME -> drawHome(context)
            Page.GAME -> drawGame(context, mouseX, mouseY)
            Page.ACCOUNTS -> drawAccounts(context, mouseX, mouseY)
        }
        drawFooter(context)

        super.render(context, mouseX, mouseY, delta)
    }

    private fun drawHeader(context: DrawContext) {
        val header = Rect(12, 10, width - 24, 36)
        drawPanel(context, header, panel, border)
        drawText(context, "ASTRA", 24, 23, textMain, useGalahad = true)

        val navY = 17
        val navW = 76
        val navH = 22
        val navGap = 5
        val navStart = 112
        navHits.clear()

        val items = listOf(
            Page.HOME to "МЕНЮ",
            Page.GAME to "ИГРА",
            Page.ACCOUNTS to "АККАУНТЫ"
        )
        items.forEachIndexed { index, pair ->
            val rect = Rect(navStart + index * (navW + navGap), navY, navW, navH)
            navHits[pair.first] = rect
            drawNavItem(context, rect, pair.second, page == pair.first)
        }

        val username = runCatching {
            MinecraftClient.getInstance().session.username
        }.getOrDefault("Player")
        val profileLabel = textRenderer.trimToWidth(username, 130)
        val profileW = textRenderer.getWidth(profileLabel)
        val profileX = width - 28 - profileW
        drawDot(context, profileX - 12, 28, accent)
        drawText(context, profileLabel, profileX, 23, textMain)
    }

    private fun drawNavItem(context: DrawContext, rect: Rect, label: String, selected: Boolean) {
        if (selected) {
            context.fill(rect.x, rect.y + rect.h - 2, rect.x + rect.w, rect.y + rect.h, accent)
            context.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h - 2, 0x252F9D7E)
        }
        val labelWidth = textRenderer.getWidth(label)
        drawText(context, label, rect.x + (rect.w - labelWidth) / 2, rect.y + 7,
            if (selected) textMain else textMuted)
    }

    private fun drawHome(context: DrawContext) {
        val centerX = width / 2
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

        drawTextCentered(context, date, centerX, height / 2 - 88, textMuted)
        drawTextCentered(context, time, centerX, height / 2 - 66, textMain, scaleHint = true)

        // Minimal Astra emblem in place of Arbuz branding.
        val emblemSize = 58
        val emblem = Rect(centerX - emblemSize / 2, height / 2 - 26, emblemSize, emblemSize)
        drawPanel(context, emblem, 0x302E9E81, 0x8A68D5B6.toInt())
        drawTextCentered(context, "A", centerX, emblem.y + 19, accent, useGalahad = true)

        drawTextCentered(context, "ASTRA CLIENT", centerX, height / 2 + 43, textMain, useGalahad = true)
        drawTextCentered(
            context,
            "ТВОЙ ИНТЕРФЕЙС. ТВОИ ПРАВИЛА.",
            centerX,
            height / 2 + 61,
            textMuted
        )

        val button = Rect(centerX - 67, height / 2 + 91, 134, 27)
        drawPanel(context, button, 0xD51D4D40.toInt(), 0xAA69D5B6.toInt())
        drawTextCentered(context, "ОТКРЫТЬ ИГРУ", centerX, button.y + 9, textMain)
        homeGameButton = button

        val quick = "Модули • настройки • аккаунты"
        drawTextCentered(context, quick, centerX, height / 2 + 128, textMuted)
    }

    private var homeGameButton = Rect(0, 0, 0, 0)

    private fun drawGame(context: DrawContext, mouseX: Int, mouseY: Int) {
        cardHits.clear()
        categoryHits.clear()
        booleanHits.clear()
        sliderHits.clear()

        drawText(context, "ИГРОВОЙ ИНТЕРФЕЙС", 18, 59, textMain)
        drawText(context, "Модули и параметры клиента", 18, 73, textMuted)

        val contentTop = 88
        val contentBottom = height - 25
        val contentHeight = (contentBottom - contentTop).coerceAtLeast(130)

        val sidebar = Rect(16, contentTop, 92, contentHeight)
        drawPanel(context, sidebar, panel, border)
        drawText(context, "РАЗДЕЛЫ", sidebar.x + 10, sidebar.y + 11, textMuted)

        val categoryLabels = listOf(
            Category.COMBAT to "Combat",
            Category.VISUALS to "Visuals",
            Category.OTHER to "Other"
        )
        categoryLabels.forEachIndexed { index, entry ->
            val rect = Rect(sidebar.x + 7, sidebar.y + 34 + index * 34, sidebar.w - 14, 28)
            categoryHits.add(CategoryHit(entry.first, rect))
            val active = category == entry.first
            if (active) {
                context.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, 0x423C9F84)
                context.fill(rect.x, rect.y, rect.x + 2, rect.y + rect.h, accent)
            }
            drawText(context, entry.second, rect.x + 9, rect.y + 10, if (active) textMain else textMuted)
        }

        val detailsWidth = if (width >= 700) 210 else 0
        val gridX = sidebar.x + sidebar.w + 10
        val rightEdge = width - 16
        val gridWidth = (rightEdge - gridX - if (detailsWidth > 0) detailsWidth + 10 else 0).coerceAtLeast(150)
        val columns = if (gridWidth >= 340) 2 else 1
        val gap = 8
        val cardWidth = ((gridWidth - gap * (columns - 1)) / columns).coerceAtLeast(130)
        val cardHeight = 60
        val rowStep = cardHeight + 8
        val availableRows = (contentHeight / rowStep).coerceAtLeast(1)
        val modules = ModuleManager.getByCategory(category)

        if (selectedModule == null || modules.none { it === selectedModule }) {
            selectedModule = modules.firstOrNull()
        }

        val totalRows = ceil(modules.size.toDouble() / columns).toInt()
        val maxScroll = (totalRows - availableRows).coerceAtLeast(0)
        moduleScroll = moduleScroll.coerceIn(0, maxScroll)
        val firstIndex = moduleScroll * columns
        val visibleModules = modules.drop(firstIndex).take(availableRows * columns)

        visibleModules.forEachIndexed { visibleIndex, module ->
            val col = visibleIndex % columns
            val row = visibleIndex / columns
            val rect = Rect(
                gridX + col * (cardWidth + gap),
                contentTop + row * rowStep,
                cardWidth,
                cardHeight
            )
            val toggle = Rect(rect.x + rect.w - 42, rect.y + 22, 28, 13)
            cardHits.add(CardHit(module, rect, toggle))

            val active = module.isEnabled
            drawPanel(
                context,
                rect,
                if (module === selectedModule) 0xE0112521.toInt() else panelSoft,
                if (module === selectedModule) 0xAA69D5B6.toInt() else border
            )
            drawText(context, textRenderer.trimToWidth(module.name, rect.w - 58),
                rect.x + 10, rect.y + 10, if (active) textMain else textMain)
            drawText(context, textRenderer.trimToWidth(module.desc, rect.w - 18),
                rect.x + 10, rect.y + 29, textMuted)
            drawSwitch(context, toggle, active)
            if (module === selectedModule) {
                context.fill(rect.x + 1, rect.y + rect.h - 2, rect.x + rect.w - 1, rect.y + rect.h - 1, accent)
            }
        }

        if (modules.isEmpty()) {
            drawText(context, "В этом разделе пока нет модулей", gridX + 10, contentTop + 18, textMuted)
        }

        if (detailsWidth > 0) {
            val detailsX = gridX + gridWidth + 10
            drawModuleDetails(context, selectedModule, Rect(detailsX, contentTop, detailsWidth, contentHeight))
        } else {
            drawText(context, "Выбери модуль, чтобы увидеть его настройки", gridX + 6, contentBottom - 14, textMuted)
        }

        if (maxScroll > 0) {
            drawText(context, "↑  ↓  прокрутка", gridX + 2, contentBottom - 12, textMuted)
        }
    }

    private var bindButton = Rect(0, 0, 0, 0)
    private var moduleSwitchButton = Rect(0, 0, 0, 0)

    private fun drawModuleDetails(context: DrawContext, module: Module?, rect: Rect) {
        drawPanel(context, rect, panel, border)
        if (module == null) {
            drawText(context, "Нет выбранного модуля", rect.x + 12, rect.y + 16, textMuted)
            return
        }

        drawText(context, "НАСТРОЙКИ", rect.x + 12, rect.y + 11, textMuted)
        drawText(context, textRenderer.trimToWidth(module.name, rect.w - 24), rect.x + 12, rect.y + 30, textMain)
        drawText(context, textRenderer.trimToWidth(module.desc, rect.w - 24), rect.x + 12, rect.y + 46, textMuted)

        moduleSwitchButton = Rect(rect.x + rect.w - 52, rect.y + 66, 40, 18)
        drawSwitch(context, moduleSwitchButton, module.isEnabled)
        drawText(context, if (module.isEnabled) "ВКЛ" else "ВЫКЛ", rect.x + 12, rect.y + 72,
            if (module.isEnabled) accent else textMuted)

        bindButton = Rect(rect.x + 12, rect.y + 96, rect.w - 24, 22)
        drawPanel(context, bindButton, 0x9020342E.toInt(), border)
        val bindLabel = if (waitingForBind === module) "НАЖМИ КЛАВИШУ..." else "БИНД: ${formatBind(module.bind)}"
        drawTextCentered(context, bindLabel, bindButton.x + bindButton.w / 2, bindButton.y + 8, textMain)

        val settings = module.settings
        var settingY = rect.y + 130
        settings.forEach { setting ->
            when (setting) {
                is BooleanSetting -> {
                    val row = Rect(rect.x + 10, settingY, rect.w - 20, 32)
                    drawText(context, setting.name, row.x, row.y + 3, textMain)
                    val toggle = Rect(row.x + row.w - 35, row.y + 2, 28, 13)
                    drawSwitch(context, toggle, setting.isEnabled)
                    booleanHits.add(BooleanHit(setting, toggle))
                    settingY += 38
                }
                is NumberSetting -> {
                    val row = Rect(rect.x + 10, settingY, rect.w - 20, 43)
                    drawText(context, setting.name, row.x, row.y + 2, textMain)
                    val valueText = formatNumber(setting.doubleValue, setting.increment)
                    val valueWidth = textRenderer.getWidth(valueText)
                    drawText(context, valueText, row.x + row.w - valueWidth, row.y + 2, accent)
                    val trackX = row.x
                    val trackWidth = row.w
                    val trackRect = Rect(trackX, row.y + 23, trackWidth, 15)
                    drawSlider(context, trackRect, trackX, trackWidth, setting.doubleValue, setting.min, setting.max)
                    sliderHits.add(SliderHit(setting, trackRect, trackX, trackWidth))
                    settingY += 48
                }
            }
        }

        if (settings.isEmpty()) {
            drawText(context, "Нет дополнительных параметров", rect.x + 12, rect.y + 137, textMuted)
        }
    }

    private fun drawAccounts(context: DrawContext, mouseX: Int, mouseY: Int) {
        accountHits.clear()
        drawText(context, "АККАУНТЫ", 18, 59, textMain)
        drawText(context, "Офлайн-профили на этом устройстве", 18, 73, textMuted)

        val panelRect = Rect(width / 2 - 178, 88, 356, height - 116)
        drawPanel(context, panelRect, panel, border)

        drawText(context, "ТЕКУЩИЙ ПРОФИЛЬ", panelRect.x + 14, panelRect.y + 12, textMuted)
        drawText(context, AltManager.currentNickname(), panelRect.x + 14, panelRect.y + 29, textMain)
        drawText(context, "НОВЫЙ ИЛИ СОХРАНЁННЫЙ НИК", panelRect.x + 14, panelRect.y + 54, textMuted)

        if (::nicknameInput.isInitialized) {
            nicknameInput.setPosition(width / 2 - 132, panelRect.y + 70)
            nicknameInput.visible = true
            nicknameInput.active = true
        }

        val buttonY = panelRect.y + 99
        val applyRect = Rect(panelRect.x + 14, buttonY, 154, 23)
        val saveRect = Rect(panelRect.x + 176, buttonY, 166, 23)
        drawPanel(context, applyRect, 0xD51D4D40.toInt(), 0xAA69D5B6.toInt())
        drawPanel(context, saveRect, 0x9020342E.toInt(), border)
        drawTextCentered(context, "ПРИМЕНИТЬ", applyRect.x + applyRect.w / 2, applyRect.y + 8, textMain)
        drawTextCentered(context, "СОХРАНИТЬ НИК", saveRect.x + saveRect.w / 2, saveRect.y + 8, textMain)
        accountApplyButton = applyRect
        accountSaveButton = saveRect

        drawText(context, "СОХРАНЁННЫЕ НИКИ", panelRect.x + 14, buttonY + 34, textMuted)
        val names = AltManager.getAll()
        val listStart = buttonY + 51
        val rowHeight = 23
        val visibleRows = ((panelRect.y + panelRect.h - listStart - 10) / rowHeight).coerceAtLeast(1)
        val maxScroll = (names.size - visibleRows).coerceAtLeast(0)
        accountScroll = accountScroll.coerceIn(0, maxScroll)

        names.drop(accountScroll).take(visibleRows).forEachIndexed { offset, name ->
            val index = accountScroll + offset
            val row = Rect(panelRect.x + 14, listStart + offset * rowHeight, panelRect.w - 28, 20)
            val delete = Rect(row.x + row.w - 58, row.y, 58, row.h)
            accountHits.add(AccountHit(index, row, delete))
            context.fill(row.x, row.y, row.x + row.w, row.y + row.h,
                if (index == selectedAccount) 0x4A3D9F84 else 0x35101E1B)
            drawText(context, textRenderer.trimToWidth(name, row.w - 75), row.x + 8, row.y + 6,
                if (index == selectedAccount) textMain else textMuted)
            context.fill(delete.x, delete.y, delete.x + delete.w, delete.y + delete.h, 0x593B2727)
            drawTextCentered(context, "УДАЛИТЬ", delete.x + delete.w / 2, delete.y + 6, 0xFFE0B9B9.toInt())
        }

        if (names.isEmpty()) {
            drawText(context, "Список пуст — введи ник и нажми «Сохранить ник».",
                panelRect.x + 14, listStart + 8, textMuted)
        }

        if (accountStatus.isNotEmpty()) {
            drawText(context, textRenderer.trimToWidth(accountStatus, panelRect.w - 28),
                panelRect.x + 14, panelRect.y + panelRect.h - 16, accent)
        }
    }

    private var accountApplyButton = Rect(0, 0, 0, 0)
    private var accountSaveButton = Rect(0, 0, 0, 0)

    private fun drawFooter(context: DrawContext) {
        drawText(context, "ASTRA  •  FABRIC 1.21.4", 16, height - 13, 0xFF81958E.toInt())
        val status = if (waitingForBind != null) "BIND MODE" else "GL4ES READY"
        val labelWidth = textRenderer.getWidth(status)
        drawText(context, status, width - labelWidth - 16, height - 13, 0xFF6D9E8D.toInt())
    }

    private fun drawPanel(context: DrawContext, rect: Rect, fill: Int, outline: Int) {
        context.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, fill)
        context.drawBorder(rect.x, rect.y, rect.w, rect.h, outline)
    }

    private fun drawText(
        context: DrawContext,
        value: String,
        x: Int,
        y: Int,
        color: Int = textMain,
        useGalahad: Boolean = false
    ) {
        val message = if (useGalahad) {
            Text.literal(value).setStyle(Style.EMPTY.withFont(galahad))
        } else {
            Text.literal(value)
        }
        context.drawText(textRenderer, message, x, y, color, false)
    }

    private fun drawTextCentered(
        context: DrawContext,
        value: String,
        centerX: Int,
        y: Int,
        color: Int,
        useGalahad: Boolean = false,
        scaleHint: Boolean = false
    ) {
        val message = if (useGalahad) {
            Text.literal(value).setStyle(Style.EMPTY.withFont(galahad))
        } else {
            Text.literal(value)
        }
        context.drawCenteredTextWithShadow(textRenderer, message, centerX, y, color)
    }

    private fun drawDot(context: DrawContext, x: Int, y: Int, color: Int) {
        context.fill(x - 2, y - 2, x + 3, y + 3, color)
    }

    private fun drawSwitch(context: DrawContext, rect: Rect, enabled: Boolean) {
        context.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h,
            if (enabled) 0xFF347F6B.toInt() else 0xFF26322F.toInt())
        val knobX = if (enabled) rect.x + rect.w - 10 else rect.x + 2
        context.fill(knobX, rect.y + 2, knobX + 8, rect.y + rect.h - 2,
            if (enabled) 0xFFE8FFF6.toInt() else 0xFF8CA099.toInt())
    }

    private fun drawSlider(
        context: DrawContext,
        rect: Rect,
        trackX: Int,
        trackWidth: Int,
        value: Double,
        min: Double,
        max: Double
    ) {
        context.fill(trackX, rect.y + 5, trackX + trackWidth, rect.y + 8, 0xFF273632.toInt())
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val progress = ((value - min) / range).coerceIn(0.0, 1.0)
        val filled = (trackWidth * progress).roundToInt()
        context.fill(trackX, rect.y + 5, trackX + filled, rect.y + 8, accent)
        val knobX = (trackX + filled - 3).coerceIn(trackX, trackX + trackWidth - 6)
        context.fill(knobX, rect.y + 2, knobX + 6, rect.y + 11, textMain)
    }

    private fun updateSlider(hit: SliderHit, mouseX: Double) {
        val percent = ((mouseX - hit.trackX).toDouble() / hit.trackWidth.toDouble()).coerceIn(0.0, 1.0)
        val raw = hit.setting.min + (hit.setting.max - hit.setting.min) * percent
        val step = hit.setting.increment.takeIf { it > 0.0 } ?: 0.01
        val stepped = hit.setting.min + ((raw - hit.setting.min) / step).roundToInt() * step
        hit.setting.setValue(stepped)
    }

    private fun formatNumber(value: Double, step: Double): String =
        if (step >= 1.0) value.roundToInt().toString() else String.format(java.util.Locale.ROOT, "%.2f", value)

    private fun formatBind(bind: Int): String = when {
        bind < 0 -> "NONE"
        bind >= GLFW.GLFW_MOUSE_BUTTON_1 && bind <= GLFW.GLFW_MOUSE_BUTTON_LAST -> "MOUSE ${bind + 1}"
        else -> "KEY $bind"
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for ((targetPage, rect) in navHits) {
            if (rect.contains(mouseX, mouseY) && button == 0) {
                changePage(targetPage)
                return true
            }
        }

        when (page) {
            Page.HOME -> {
                if (button == 0 && homeGameButton.contains(mouseX, mouseY)) {
                    changePage(Page.GAME)
                    return true
                }
            }
            Page.GAME -> {
                if (button == 0) {
                    categoryHits.firstOrNull { it.bounds.contains(mouseX, mouseY) }?.let {
                        category = it.category
                        moduleScroll = 0
                        selectedModule = ModuleManager.getByCategory(category).firstOrNull()
                        return true
                    }

                    sliderHits.firstOrNull { it.bounds.contains(mouseX, mouseY) }?.let {
                        draggingSlider = it
                        updateSlider(it, mouseX)
                        return true
                    }
                    booleanHits.firstOrNull { it.bounds.contains(mouseX, mouseY) }?.let {
                        it.setting.toggle()
                        TriggerBotClient.saveConfig()
                        return true
                    }

                    val module = selectedModule
                    if (module != null && moduleSwitchButton.contains(mouseX, mouseY)) {
                        module.toggled()
                        TriggerBotClient.saveConfig()
                        return true
                    }
                    if (module != null && bindButton.contains(mouseX, mouseY)) {
                        waitingForBind = module
                        return true
                    }

                    cardHits.firstOrNull { it.toggle.contains(mouseX, mouseY) }?.let {
                        it.module.toggled()
                        selectedModule = it.module
                        TriggerBotClient.saveConfig()
                        return true
                    }
                    cardHits.firstOrNull { it.bounds.contains(mouseX, mouseY) }?.let {
                        selectedModule = it.module
                        if (it.module.name == "Alt Manager") changePage(Page.ACCOUNTS)
                        return true
                    }
                }
            }
            Page.ACCOUNTS -> {
                if (button == 0) {
                    if (accountApplyButton.contains(mouseX, mouseY)) {
                        applyAccount()
                        return true
                    }
                    if (accountSaveButton.contains(mouseX, mouseY)) {
                        saveAccount()
                        return true
                    }
                    accountHits.firstOrNull { it.delete.contains(mouseX, mouseY) }?.let {
                        AltManager.remove(it.index)
                        if (selectedAccount == it.index) selectedAccount = -1
                        accountStatus = "Ник удалён"
                        return true
                    }
                    accountHits.firstOrNull { it.row.contains(mouseX, mouseY) }?.let {
                        selectedAccount = it.index
                        nicknameInput.setText(AltManager.get(it.index))
                        return true
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    private fun saveAccount() {
        val name = nicknameInput.getText().trim()
        if (!AltManager.isValidName(name)) {
            accountStatus = "Ник должен содержать 3–16 латинских букв, цифр или _"
            return
        }
        accountStatus = if (AltManager.add(name)) "Ник сохранён" else "Такой ник уже есть"
        selectedAccount = -1
    }

    private fun applyAccount() {
        val name = nicknameInput.text.trim()
        if (!AltManager.isValidName(name)) {
            accountStatus = "Некорректный ник"
            return
        }
        accountStatus = if (AltManager.apply(name)) {
            "Офлайн-профиль выбран: $name"
        } else {
            "Нельзя применить профиль во время игры"
        }
    }

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        deltaX: Double,
        deltaY: Double
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
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        if (page == Page.GAME) {
            val direction = if (verticalAmount < 0) 1 else -1
            moduleScroll = (moduleScroll + direction).coerceAtLeast(0)
            return true
        }
        if (page == Page.ACCOUNTS) {
            val direction = if (verticalAmount < 0) 1 else -1
            accountScroll = (accountScroll + direction).coerceAtLeast(0)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        waitingForBind?.let { module ->
            when (keyCode) {
                GLFW.GLFW_KEY_ESCAPE -> waitingForBind = null
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
        if (page == Page.ACCOUNTS && keyCode == GLFW.GLFW_KEY_ENTER) {
            applyAccount()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun shouldPause(): Boolean = false

    override fun close() {
        AltManager.save()
        super.close()
    }
}
