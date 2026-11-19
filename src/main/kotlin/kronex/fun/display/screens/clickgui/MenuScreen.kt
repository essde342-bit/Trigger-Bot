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
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.network.ServerAddress
import net.minecraft.client.network.ServerInfo
import net.minecraft.client.option.ServerList
import net.minecraft.sound.SoundCategory
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Astra Client's Kotlin menu.
 *
 * UI is rendered here; gameplay modules, settings and config persistence remain
 * in the existing Java model. The layout is deliberately responsive because
 * the client is also used on low-resolution Android displays.
 */
object MenuScreen : Screen(Text.literal("Astra Client")) {
    private enum class Page { HOME, GAME, ACCOUNTS }
    private enum class AccountTab { PROFILES, SERVERS }

    private data class Rect(val x: Int, val y: Int, val w: Int, val h: Int) {
        fun contains(px: Double, py: Double): Boolean =
            w > 0 && h > 0 && px >= x && py >= y && px < x + w && py < y + h
    }

    private data class ModuleHit(val module: Module, val row: Rect, val toggle: Rect)
    private data class BooleanHit(val setting: BooleanSetting, val bounds: Rect)
    private data class SliderHit(
        val setting: NumberSetting,
        val bounds: Rect,
        val trackX: Int,
        val trackWidth: Int
    )
    private data class AccountHit(val index: Int, val row: Rect, val delete: Rect)
    private data class ServerHit(val server: ServerInfo, val row: Rect, val favorite: Rect)

    private val pages = Page.values().toList()
    private val categoryTabs = listOf(
        Category.COMBAT to "COMBAT",
        Category.MOVEMENT to "MOVE",
        Category.PLAYER to "PLAYER",
        Category.RENDER to "RENDER",
        Category.MISC to "MISC"
    )

    private var page = Page.HOME
    private var category = Category.COMBAT
    private var accountTab = AccountTab.PROFILES

    private var selectedModule: Module? = null
    private var mobileSettingsOpen = false
    private var desktopSettingsOpen = false
    private var waitingForBind: Module? = null
    private var draggingSlider: SliderHit? = null
    private var altHeld = false

    private var moduleScroll = 0
    private val columnScrolls = mutableMapOf<Category, Int>()
    private val desktopColumnHits = mutableListOf<Pair<Category, Rect>>()
    private var settingsScroll = 0
    private var accountScroll = 0
    private var serverScroll = 0
    private var favoriteServerIndex = 0
    private var accountStatus = ""

    private val navHits = mutableMapOf<Page, Rect>()
    private val categoryHits = mutableListOf<Pair<Category, Rect>>()
    private val moduleHits = mutableListOf<ModuleHit>()
    private val booleanHits = mutableListOf<BooleanHit>()
    private val sliderHits = mutableListOf<SliderHit>()
    private val accountHits = mutableListOf<AccountHit>()
    private val serverHits = mutableListOf<ServerHit>()
    private val favoriteServerHits = mutableListOf<Pair<ServerInfo, Rect>>()

    private lateinit var nicknameInput: TextFieldWidget
    private var accountsBackButton = Rect(0, 0, 0, 0)
    private var serverJoinButton = Rect(0, 0, 0, 0)
    private var accountApplyButton = Rect(0, 0, 0, 0)
    private var accountSaveButton = Rect(0, 0, 0, 0)

    private var detailsModuleToggle = Rect(0, 0, 0, 0)
    private var detailsBindButton = Rect(0, 0, 0, 0)
    private var detailsCloseButton = Rect(0, 0, 0, 0)

    private var volumeButton = Rect(0, 0, 0, 0)
    private var volumeSlider = Rect(0, 0, 0, 0)
    private var volumePopup = Rect(0, 0, 0, 0)
    private var volumeOpen = false
    private var draggingVolume = false
    private var previousVolume = 0.55

    private var serverList: ServerList? = null
    private var savedServers: List<ServerInfo> = emptyList()
    private val favoriteAddresses = linkedSetOf<String>()
    private var favoritesLoaded = false

    private val accent = 0xFF73D9B7.toInt()
    private val accentBright = 0xFFB5FCE3.toInt()
    private val textMain = 0xFFF0F7F3.toInt()
    private val textMuted = 0xFF9AAEA7.toInt()
    private val textDim = 0xFF637970.toInt()
    private val panel = 0xC9081110.toInt()
    private val panelSoft = 0xB80B1714.toInt()
    private val panelSelected = 0xE3122721.toInt()
    private val border = 0x553D9B7B
    private val galahad = Identifier.of("triggerbot", "galahad")

    @JvmStatic
    fun openGui() {
        page = Page.HOME
        waitingForBind = null
        mobileSettingsOpen = false
        desktopSettingsOpen = false
        MinecraftClient.getInstance().setScreen(this)
    }

    @JvmStatic
    fun openAccounts() {
        page = Page.ACCOUNTS
        accountTab = AccountTab.PROFILES
        accountStatus = ""
        loadAccountsData()
        MinecraftClient.getInstance().setScreen(this)
    }

    private fun loadAccountsData() {
        loadFavorites()
        val client = MinecraftClient.getInstance()
        val list = ServerList(client)
        list.loadFile()
        serverList = list
        savedServers = (0 until list.size()).mapNotNull { index ->
            runCatching { list.get(index) }.getOrNull()
        }
        accountScroll = 0
        serverScroll = 0
    }

    private fun loadFavorites() {
        if (favoritesLoaded) return
        favoritesLoaded = true
        favoriteAddresses.clear()
        val file = favoriteFile()
        if (!file.exists()) return
        runCatching {
            file.readLines(Charsets.UTF_8)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { favoriteAddresses.add(it) }
        }
    }

    private fun favoriteFile(): File =
        File(MinecraftClient.getInstance().runDirectory, "config/astra-server-favorites.txt")

    private fun saveFavorites() {
        val file = favoriteFile()
        val parent = file.parentFile
        if (!parent.exists() && !parent.mkdirs()) return
        runCatching { file.writeText(favoriteAddresses.joinToString("\n"), Charsets.UTF_8) }
    }

    override fun init() {
        super.init()
        nicknameInput = TextFieldWidget(
            textRenderer,
            16,
            16,
            160,
            19,
            Text.literal("Nickname")
        )
        nicknameInput.setMaxLength(16)
        nicknameInput.setSuggestion("Ник 3–16 символов")
        nicknameInput.setText(AltManager.currentNickname())
        nicknameInput.setDrawsBackground(false)
        nicknameInput.visible = page == Page.ACCOUNTS && accountTab == AccountTab.PROFILES
        nicknameInput.active = nicknameInput.visible
        addDrawableChild(nicknameInput)
    }

    private fun changePage(next: Page) {
        page = next
        waitingForBind = null
        draggingVolume = false
        moduleScroll = 0
        settingsScroll = 0
        if (next == Page.GAME) {
            val modules = modulesForCategory(category)
            if (selectedModule == null || modules.none { it === selectedModule }) {
                selectedModule = modules.firstOrNull()
            }
            mobileSettingsOpen = false
        }
        if (next == Page.ACCOUNTS) loadAccountsData()
        if (::nicknameInput.isInitialized) {
            nicknameInput.visible = next == Page.ACCOUNTS && accountTab == AccountTab.PROFILES
            nicknameInput.active = nicknameInput.visible
        }
    }

    private fun modulesForCategory(value: Category): List<Module> =
        ModuleManager.getByCategory(value)

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        // Minecraft's own panorama remains in the background, like the live
        // rotating scene on Arbuz's main screen. The overlay darkens it evenly.
        renderBackground(context, mouseX, mouseY, delta)
        context.fill(0, 0, width, height, 0x72030A08)

        drawAtmosphere(context)

        drawHeader(context, mouseX, mouseY)
        when (page) {
            Page.HOME -> drawHome(context, mouseX, mouseY)
            Page.GAME -> drawGame(context, mouseX, mouseY)
            Page.ACCOUNTS -> drawAccounts(context, mouseX, mouseY)
        }
        drawFooter(context, mouseX, mouseY)

        if (page == Page.ACCOUNTS && altHeld) {
            drawFavoriteServerWheel(context)
        }

        super.render(context, mouseX, mouseY, delta)
    }

    private fun drawAtmosphere(context: DrawContext) {
        // Subtle water-like tint and soft horizon. It uses only DrawContext
        // primitives, so it works on GL4ES without custom shaders.
        context.fill(0, height / 2, width, height, 0x160A6D58)
        context.fill(0, height - 3, width, height - 2, 0x553D9678)
        for (index in 0 until 30) {
            val x = ((index * 71 + 19) % width.coerceAtLeast(1))
            val y = ((index * 37 + 11) % height.coerceAtLeast(1))
            context.fill(x, y, x + 2, y + 2, 0x5537A98A)
        }
    }

    private fun drawHeader(context: DrawContext, mouseX: Int, mouseY: Int) {
        navHits.clear()
        val headerY = 8
        drawText(context, "ASTRA", 16, headerY + 7, textMain, useGalahad = true)

        val labels = listOf(
            Page.HOME to "МЕНЮ",
            Page.GAME to "ИГРА",
            Page.ACCOUNTS to "АККАУНТЫ"
        )
        val widths = listOf(48, 42, 66)
        val gap = 5
        val groupWidth = widths.sum() + gap * (widths.size - 1)
        val navStart = ((width - groupWidth) / 2).coerceAtLeast(72)

        labels.forEachIndexed { index, item ->
            val rect = Rect(
                navStart + widths.take(index).sum() + index * gap,
                headerY,
                widths[index],
                24
            )
            navHits[item.first] = rect
            drawNavItem(context, rect, item.second, page == item.first)
        }

        val username = runCatching {
            MinecraftClient.getInstance().session.username
        }.getOrDefault("Player")
        val safeWidth = (width - navStart - groupWidth - 24).coerceAtLeast(42)
        val profileText = textRenderer.trimToWidth(username, safeWidth.coerceAtMost(84))
        val profileWidth = textRenderer.getWidth(profileText)
        val profileX = (width - profileWidth - 18).coerceAtLeast(navStart + groupWidth + 8)
        context.fill(profileX - 10, headerY + 9, profileX - 4, headerY + 15, accent)
        drawText(context, profileText, profileX, headerY + 7, textMuted)
    }

    private fun drawNavItem(context: DrawContext, rect: Rect, label: String, selected: Boolean) {
        if (selected) {
            context.fill(rect.x + 3, rect.y + rect.h - 2, rect.x + rect.w - 3, rect.y + rect.h, accent)
            context.fill(rect.x, rect.y + 2, rect.x + rect.w, rect.y + rect.h - 3, 0x1E65C7A4)
        }
        val labelWidth = textRenderer.getWidth(label)
        drawText(
            context,
            label,
            rect.x + (rect.w - labelWidth) / 2,
            rect.y + 8,
            if (selected) textMain else textMuted
        )
    }

    private fun drawHome(context: DrawContext, mouseX: Int, mouseY: Int) {
        val centerX = width / 2
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

        val dateY = (height * 0.19f).roundToInt().coerceAtLeast(37)
        drawTextCentered(context, date, centerX, dateY, textMuted)
        drawTextCentered(context, time, centerX, dateY + 15, textMain, scaleHint = true)

        val compact = height < 280
        val logoY = if (compact) {
            (height * 0.40f).roundToInt().coerceIn(52, (height - 100).coerceAtLeast(52))
        } else {
            (height * 0.48f).roundToInt().coerceIn(90, (height - 120).coerceAtLeast(90))
        }
        drawAstraEmblem(context, centerX, logoY)

        drawTextCentered(context, "ASTRA CLIENT", centerX, logoY + 35, textMain, useGalahad = true)
        drawTextCentered(
            context,
            "Клиент, собранный вокруг твоего игрового стиля",
            centerX,
            logoY + 48,
            textMuted
        )

        val buttonWidth = 106
        val buttonHeight = 22
        val buttonY = if (compact) (height - 42).coerceAtLeast(logoY + 57)
        else (height * 0.70f).roundToInt()
        val button = Rect(
            centerX - buttonWidth / 2,
            buttonY.coerceAtMost((height - 30).coerceAtLeast(0)),
            buttonWidth,
            buttonHeight
        )
        drawButton(context, button, "ВОЙТИ", true, button.contains(mouseX.toDouble(), mouseY.toDouble()))
        homeLoginButton = button
    }

    private var homeLoginButton = Rect(0, 0, 0, 0)

    private fun drawAstraEmblem(context: DrawContext, centerX: Int, centerY: Int) {
        // Original Astra crescent-and-stars mark; every star/dot is the same size.
        val radius = 24.0
        for (index in 0..46) {
            val angle = Math.toRadians(52.0 + index * (256.0 / 46.0))
            val px = centerX + (cos(angle) * radius).roundToInt()
            val py = centerY + (sin(angle) * radius).roundToInt()
            context.fill(px, py, px + 2, py + 2, 0xFFFFFFFF.toInt())
        }

        // Letter C is a quiet watermark inside the crescent.
        val watermark = Text.literal("C").setStyle(Style.EMPTY.withFont(galahad))
        context.drawCenteredTextWithShadow(textRenderer, watermark, centerX + 1, centerY - 15, 0xCCFFFFFF.toInt())

        for (index in 0 until 116) {
            val px = centerX + ((index * 29 + 7) % 78) - 39
            val py = centerY + ((index * 17 + 3) % 54) - 27
            if (kotlin.math.abs(px - centerX) > 30 || kotlin.math.abs(py - centerY) > 22) {
                context.fill(px, py, px + 2, py + 2, 0xFFFFFFFF.toInt())
            }
        }

        for (line in 0 until 5) {
            val spread = 10 + line * 7
            context.fill(
                centerX - spread,
                centerY + 29 + line * 3,
                centerX + spread,
                centerY + 30 + line * 3,
                0x1F73D9B7
            )
        }
    }

    private fun drawGame(context: DrawContext, mouseX: Int, mouseY: Int) {
        categoryHits.clear()
        moduleHits.clear()
        booleanHits.clear()
        sliderHits.clear()
        desktopColumnHits.clear()

        if (width >= 760) {
            drawDesktopGame(context, mouseX, mouseY)
        } else {
            drawCompactGame(context, mouseX, mouseY)
        }
    }

    /**
     * Arbuz-style desktop layout: all five module groups are visible at once.
     * A selected module opens its settings as a centered overlay, leaving the
     * five-column overview visible behind it.
     */
    private fun drawDesktopGame(context: DrawContext, mouseX: Int, mouseY: Int) {
        drawText(context, "МОДУЛИ", 16, 42, textMain)
        drawText(context, "Выбери функцию, чтобы открыть её параметры", 16, 55, textMuted)

        val contentTop = 70
        val contentBottom = height - 23
        val contentHeight = (contentBottom - contentTop).coerceAtLeast(80)
        val outerMargin = 16
        val gap = 7
        val columnWidth = ((width - outerMargin * 2 - gap * 4) / 5).coerceAtLeast(100)
        val rowHeight = 28
        val headerHeight = 25

        categoryTabs.forEachIndexed { columnIndex, entry ->
            val x = outerMargin + columnIndex * (columnWidth + gap)
            val column = Rect(x, contentTop, columnWidth, contentHeight)
            desktopColumnHits.add(entry.first to column)

            drawPanel(context, column, panel, border)
            context.fill(column.x + 1, column.y + 1, column.x + column.w - 1, column.y + 3,
                if (entry.first == category) accent else 0x443D9B7B)
            drawText(
                context,
                entry.second,
                column.x + 9,
                column.y + 9,
                if (entry.first == category) accentBright else textMain
            )

            val modules = modulesForCategory(entry.first)
            val listTop = column.y + headerHeight
            val listBottom = column.y + column.h - 5
            val viewport = (listBottom - listTop).coerceAtLeast(0)
            val totalHeight = modules.size * rowHeight
            val maxScroll = (totalHeight - viewport).coerceAtLeast(0)
            val scroll = (columnScrolls[entry.first] ?: 0).coerceIn(0, maxScroll)
            columnScrolls[entry.first] = scroll

            context.enableScissor(column.x + 3, listTop, column.x + column.w - 3, listBottom)
            modules.forEachIndexed { index, module ->
                val rowY = listTop + index * rowHeight - scroll
                val row = Rect(column.x + 5, rowY, column.w - 10, rowHeight - 2)
                if (row.y + row.h < listTop || row.y > listBottom) return@forEachIndexed

                val toggle = Rect(row.x + row.w - 27, row.y + 7, 22, 11)
                moduleHits.add(ModuleHit(module, row, toggle))
                val selected = module === selectedModule
                drawPanel(
                    context,
                    row,
                    if (selected) panelSelected else panelSoft,
                    if (selected) 0x8873D9B7.toInt() else 0x223D9B7B
                )

                val textWidth = (row.w - 42).coerceAtLeast(28)
                drawText(
                    context,
                    textRenderer.trimToWidth(module.name, textWidth),
                    row.x + 6,
                    row.y + 8,
                    if (module.isEnabled) textMain else textMuted
                )
                drawSwitch(context, toggle, module.isEnabled)
            }
            context.disableScissor()

            if (modules.isEmpty()) {
                drawText(context, "Пусто", column.x + 9, listTop + 9, textDim)
            }
            if (maxScroll > 0) {
                val trackX = column.x + column.w - 4
                context.fill(trackX, listTop, trackX + 1, listBottom, 0x554A7063)
                val thumbHeight = (viewport.toDouble() * viewport / totalHeight).roundToInt().coerceAtLeast(12)
                val thumbY = listTop + ((viewport - thumbHeight).toDouble() * scroll / maxScroll).roundToInt()
                context.fill(trackX, thumbY, trackX + 1, thumbY + thumbHeight, accent)
            }
        }

        if (desktopSettingsOpen && selectedModule != null) {
            context.fill(0, 0, width, height, 0x78010908)
            val popupWidth = 330.coerceAtMost(width - 36)
            val popupHeight = (height - 76).coerceAtMost(330).coerceAtLeast(150)
            val popup = Rect((width - popupWidth) / 2, (height - popupHeight) / 2, popupWidth, popupHeight)
            drawModuleDetails(context, selectedModule!!, popup, mouseX, mouseY, compact = false)
        }
    }

    /**
     * Phone layout: keep the category strip compact, then show either the list
     * or the selected module's settings as a full-width page.
     */
    private fun drawCompactGame(context: DrawContext, mouseX: Int, mouseY: Int) {
        val tabsY = 42
        val gap = 3
        val left = 10
        val tabsWidth = (width - left * 2 - gap * 4).coerceAtLeast(120)
        val tabWidth = tabsWidth / 5

        categoryTabs.forEachIndexed { index, entry ->
            val rect = Rect(left + index * (tabWidth + gap), tabsY, tabWidth, 21)
            categoryHits.add(entry.first to rect)
            drawButton(
                context,
                rect,
                entry.second,
                category == entry.first,
                rect.contains(mouseX.toDouble(), mouseY.toDouble())
            )
        }

        val contentTop = tabsY + 28
        val footerTop = height - 21
        val contentHeight = (footerTop - contentTop).coerceAtLeast(36)
        val modules = modulesForCategory(category)

        if (selectedModule == null || modules.none { it === selectedModule }) {
            selectedModule = modules.firstOrNull()
        }

        if (mobileSettingsOpen && selectedModule != null) {
            val detailRect = Rect(12, contentTop, width - 24, contentHeight)
            drawModuleDetails(context, selectedModule!!, detailRect, mouseX, mouseY, compact = true)
            return
        }

        val modulePanel = Rect(12, contentTop, width - 24, contentHeight)
        drawPanel(context, modulePanel, panel, border)
        drawText(context, categoryTabs.first { it.first == category }.second, modulePanel.x + 9, modulePanel.y + 8, textMuted)

        val rowHeight = 30
        val listTop = modulePanel.y + 25
        val listBottom = modulePanel.y + modulePanel.h - 4
        val listHeight = (listBottom - listTop).coerceAtLeast(0)
        val totalHeight = modules.size * rowHeight
        val maxScroll = (totalHeight - listHeight).coerceAtLeast(0)
        moduleScroll = moduleScroll.coerceIn(0, maxScroll)

        context.enableScissor(modulePanel.x + 2, listTop, modulePanel.x + modulePanel.w - 2, listBottom)
        modules.forEachIndexed { index, module ->
            val rowY = listTop + index * rowHeight - moduleScroll
            val row = Rect(modulePanel.x + 6, rowY, modulePanel.w - 12, rowHeight - 2)
            if (row.y + row.h < listTop || row.y > listBottom) return@forEachIndexed

            val toggle = Rect(row.x + row.w - 31, row.y + 8, 24, 12)
            moduleHits.add(ModuleHit(module, row, toggle))
            drawPanel(context, row, if (module === selectedModule) panelSelected else panelSoft,
                if (module === selectedModule) 0x8873D9B7.toInt() else 0x223D9B7B)
            drawText(context, textRenderer.trimToWidth(module.name, row.w - 44), row.x + 8, row.y + 8, textMain)
            drawSwitch(context, toggle, module.isEnabled)
        }
        context.disableScissor()

        if (modules.isEmpty()) {
            drawText(context, "В этой категории пока нет модулей.", modulePanel.x + 10, listTop + 10, textMuted)
        }
        drawTextCentered(context, "Нажми на модуль для настроек", width / 2, footerTop - 11, textDim)
    }

    private fun drawModuleDetails(
        context: DrawContext,
        module: Module,
        rect: Rect,
        mouseX: Int,
        mouseY: Int,
        compact: Boolean
    ) {
        drawPanel(context, rect, panel, border)

        if (!compact) {
            detailsCloseButton = Rect(rect.x + rect.w - 21, rect.y + 7, 14, 14)
            drawText(context, "×", detailsCloseButton.x + 4, detailsCloseButton.y + 3, textMuted)
        }

        if (compact) {
            accountsBackButton = Rect(rect.x + 8, rect.y + 7, 62, 19)
            drawButton(context, accountsBackButton, "← МОДУЛИ", false,
                accountsBackButton.contains(mouseX.toDouble(), mouseY.toDouble()))
        }

        val titleY = if (compact) rect.y + 5 else rect.y + 12
        if (compact) {
            drawText(context, textRenderer.trimToWidth(module.name, rect.w - 124),
                rect.x + 78, titleY + 5, textMain)
        } else {
            drawText(context, "НАСТРОЙКИ МОДУЛЯ", rect.x + 12, titleY, textMuted)
            drawText(context, textRenderer.trimToWidth(module.name, rect.w - 92), rect.x + 12, titleY + 16, textMain)
            drawText(
                context,
                textRenderer.trimToWidth(module.desc, (rect.w - 24).coerceAtLeast(40)),
                rect.x + 12,
                titleY + 31,
                textMuted
            )
        }

        detailsModuleToggle = if (compact) {
            Rect(rect.x + rect.w - 42, titleY + 5, 30, 15)
        } else {
            Rect(rect.x + rect.w - 48, titleY + 12, 34, 16)
        }
        drawSwitch(context, detailsModuleToggle, module.isEnabled)

        detailsBindButton = Rect(rect.x + 12, if (compact) titleY + 26 else titleY + 47,
            rect.w - 24, if (compact) 17 else 20)
        val bindText = if (waitingForBind === module) "НАЖМИ КЛАВИШУ..." else "БИНД: ${formatBind(module.bind)}"
        drawButton(
            context,
            detailsBindButton,
            bindText,
            false,
            detailsBindButton.contains(mouseX.toDouble(), mouseY.toDouble())
        )

        val settingsTop = if (compact) titleY + 48 else titleY + 76
        val visibleHeight = (rect.y + rect.h - settingsTop - 7).coerceAtLeast(0)
        val settings = module.settings
        val totalHeight = settings.fold(0) { total, setting -> total + if (setting is NumberSetting) 42 else 28 }
        val maxScroll = (totalHeight - visibleHeight).coerceAtLeast(0)
        settingsScroll = settingsScroll.coerceIn(0, maxScroll)

        booleanHits.removeAll { it.setting !in settings }
        sliderHits.removeAll { it.setting !in settings }

        context.enableScissor(rect.x + 6, settingsTop, rect.x + rect.w - 6, rect.y + rect.h - 5)
        var offset = 0
        settings.forEach { setting ->
            val rowHeight = if (setting is NumberSetting) 42 else 28
            val rowY = settingsTop + offset - settingsScroll
            val row = Rect(rect.x + 10, rowY, rect.w - 20, rowHeight)
            if (row.y + row.h >= settingsTop && row.y <= rect.y + rect.h) {
                when (setting) {
                    is BooleanSetting -> {
                        drawText(context, textRenderer.trimToWidth(setting.name, row.w - 52), row.x, row.y + 6, textMain)
                        val toggle = Rect(row.x + row.w - 31, row.y + 5, 25, 12)
                        drawSwitch(context, toggle, setting.isEnabled)
                        booleanHits.add(BooleanHit(setting, toggle))
                    }
                    is NumberSetting -> {
                        drawText(context, textRenderer.trimToWidth(setting.name, row.w - 54), row.x, row.y + 3, textMain)
                        val valueText = formatNumber(setting.doubleValue, setting.increment)
                        val valueWidth = textRenderer.getWidth(valueText)
                        drawText(context, valueText, row.x + row.w - valueWidth, row.y + 3, accent)
                        val track = Rect(row.x, row.y + 21, row.w, 13)
                        drawSlider(context, track, setting.doubleValue, setting.min, setting.max)
                        sliderHits.add(SliderHit(setting, track, track.x, track.w))
                    }
                }
            }
            offset += rowHeight
        }
        context.disableScissor()

        if (settings.isEmpty()) {
            drawText(context, "Для этого модуля нет дополнительных параметров.", rect.x + 12, settingsTop + 8, textMuted)
        }
        if (maxScroll > 0) {
            drawText(context, "ПРОКРУТКА НАСТРОЕК", rect.x + 12, rect.y + rect.h - 13, textDim)
        }
    }

    private fun drawAccounts(context: DrawContext, mouseX: Int, mouseY: Int) {
        accountHits.clear()
        serverHits.clear()
        val compact = width < 760
        if (!compact) {
            drawText(context, "АККАУНТЫ", 16, 43, textMain)
            drawText(context, "Профили и быстрый выбор сохранённых серверов", 16, 56, textMuted)
        }

        val tabsY = if (compact) 42 else 70
        val tabWidth = 92
        val profileTab = Rect(16, tabsY, tabWidth, 21)
        val serversTab = Rect(16 + tabWidth + 5, tabsY, tabWidth, 21)
        accountTabs[AccountTab.PROFILES] = profileTab
        accountTabs[AccountTab.SERVERS] = serversTab

        drawButton(context, profileTab, "ПРОФИЛИ", accountTab == AccountTab.PROFILES,
            profileTab.contains(mouseX.toDouble(), mouseY.toDouble()))
        drawButton(context, serversTab, "СЕРВЕРА", accountTab == AccountTab.SERVERS,
            serversTab.contains(mouseX.toDouble(), mouseY.toDouble()))

        val contentTop = tabsY + 29
        val contentBottom = height - 21
        val panelRect = Rect(16, contentTop, width - 32, (contentBottom - contentTop).coerceAtLeast(20))
        drawPanel(context, panelRect, panel, border)

        if (accountTab == AccountTab.PROFILES) {
            drawProfiles(context, panelRect, mouseX, mouseY)
        } else {
            drawServers(context, panelRect, mouseX, mouseY)
        }

        if (accountStatus.isNotEmpty()) {
            drawText(
                context,
                textRenderer.trimToWidth(accountStatus, width - 36),
                18,
                height - 29,
                accent
            )
        }
    }

    private val accountTabs = mutableMapOf<AccountTab, Rect>()

    private fun drawProfiles(context: DrawContext, rect: Rect, mouseX: Int, mouseY: Int) {
        val currentName = AltManager.currentNickname()
        drawText(context, "АККАУНТ: " + textRenderer.trimToWidth(currentName, rect.w - 82),
            rect.x + 11, rect.y + 6, textMuted)

        val inputX = rect.x + 10
        val inputY = rect.y + 17
        val inputWidth = (rect.w - 20).coerceIn(90, 280)
        if (::nicknameInput.isInitialized) {
            nicknameInput.setPosition(inputX, inputY)
            nicknameInput.width = inputWidth
            nicknameInput.height = 19
            nicknameInput.visible = true
            nicknameInput.active = true
        }

        val buttonsY = inputY + 19
        val buttonGap = 5
        val buttonWidth = ((rect.w - 25) / 2).coerceAtLeast(60)
        accountApplyButton = Rect(rect.x + 10, buttonsY, buttonWidth, 19)
        accountSaveButton = Rect(rect.x + 15 + buttonWidth, buttonsY, buttonWidth, 19)
        drawButton(context, accountApplyButton, "ПРИМЕНИТЬ", true,
            accountApplyButton.contains(mouseX.toDouble(), mouseY.toDouble()))
        drawButton(context, accountSaveButton, "СОХРАНИТЬ", false,
            accountSaveButton.contains(mouseX.toDouble(), mouseY.toDouble()))

        val listTop = buttonsY + 18
        drawText(context, "СОХРАНЁННЫЕ ОФЛАЙН-НИКИ", rect.x + 11, listTop, textMuted)
        val rowsTop = listTop + 10
        val rowHeight = if (width < 760) 17 else 21
        val visibleHeight = (rect.y + rect.h - rowsTop - 5).coerceAtLeast(0)
        val visibleRows = (visibleHeight / rowHeight).coerceAtLeast(0)
        val names = AltManager.getAll()
        val maxScroll = (names.size - visibleRows).coerceAtLeast(0)
        accountScroll = accountScroll.coerceIn(0, maxScroll)

        context.enableScissor(rect.x + 5, rowsTop, rect.x + rect.w - 5, rect.y + rect.h - 5)
        names.drop(accountScroll).take(visibleRows).forEachIndexed { offset, name ->
            val index = accountScroll + offset
            val row = Rect(rect.x + 9, rowsTop + offset * rowHeight, rect.w - 18, rowHeight - 2)
            val delete = Rect(row.x + row.w - 57, row.y, 57, row.h)
            accountHits.add(AccountHit(index, row, delete))
            drawPanel(context, row, if (index == selectedAccount) panelSelected else panelSoft,
                if (index == selectedAccount) 0x8873D9B7.toInt() else 0x223D9B7B)
            drawText(context, textRenderer.trimToWidth(name, row.w - 74), row.x + 7, row.y + 5,
                if (index == selectedAccount) textMain else textMuted)
            drawButton(context, delete, "УДАЛИТЬ", false,
                delete.contains(mouseX.toDouble(), mouseY.toDouble()))
        }
        context.disableScissor()

        if (names.isEmpty()) {
            drawText(context, "Пока нет сохранённых ников.", rect.x + 11, rowsTop + 5, textDim)
        }

        if (height > 280) {
            drawText(context, "Microsoft-профиль выбирается через авторизованный лаунчер Minecraft.",
                rect.x + 11, rect.y + rect.h - 14, textDim)
        }
    }

    private var selectedAccount = -1

    private fun drawServers(context: DrawContext, rect: Rect, mouseX: Int, mouseY: Int) {
        val servers = savedServers
        drawText(context, "СОХРАНЁННЫЕ СЕРВЕРЫ", rect.x + 11, rect.y + 8, textMuted)
        drawText(context, "Нажми ★, чтобы добавить сервер в быстрое колесо Alt.", rect.x + 11, rect.y + 21, textDim)

        val rowsTop = rect.y + 26
        val rowHeight = if (width < 760) 20 else 26
        val footerSpace = 23
        val visibleHeight = (rect.y + rect.h - rowsTop - footerSpace - 4).coerceAtLeast(0)
        val visibleRows = (visibleHeight / rowHeight).coerceAtLeast(0)
        val maxScroll = (servers.size - visibleRows).coerceAtLeast(0)
        serverScroll = serverScroll.coerceIn(0, maxScroll)

        context.enableScissor(rect.x + 5, rowsTop, rect.x + rect.w - 5, rect.y + rect.h - footerSpace)
        servers.drop(serverScroll).take(visibleRows).forEachIndexed { offset, server ->
            val rowIndex = serverScroll + offset
            val row = Rect(rect.x + 9, rowsTop + offset * rowHeight, rect.w - 18, rowHeight - 2)
            val star = Rect(row.x + row.w - 27, row.y + 3, 20, 17)
            serverHits.add(ServerHit(server, row, star))
            val selected = selectedServerAddress == server.address
            drawPanel(context, row, if (selected) panelSelected else panelSoft,
                if (selected) 0x8873D9B7.toInt() else 0x223D9B7B)
            val labelWidth = (row.w - 56).coerceAtLeast(35)
            drawText(context, textRenderer.trimToWidth(server.name, labelWidth), row.x + 7, row.y + 4, textMain)
            drawText(context, textRenderer.trimToWidth(server.address, labelWidth), row.x + 7, row.y + 14, textDim)
            drawText(context, if (favoriteAddresses.contains(server.address)) "★" else "☆",
                star.x + 5, star.y + 4, if (favoriteAddresses.contains(server.address)) accentBright else textMuted)
        }
        context.disableScissor()

        if (servers.isEmpty()) {
            drawText(context, "Нет сохранённых серверов. Добавь их через обычный список серверов Minecraft.",
                rect.x + 11, rowsTop + 8, textMuted)
        }

        serverJoinButton = Rect(rect.x + 10, rect.y + rect.h - 24, (rect.w - 20).coerceAtMost(180), 18)
        drawButton(context, serverJoinButton, "ПОДКЛЮЧИТЬ", true,
            serverJoinButton.contains(mouseX.toDouble(), mouseY.toDouble()))
    }

    private var selectedServerAddress = ""

    private fun drawFavoriteServerWheel(context: DrawContext) {
        favoriteServerHits.clear()
        val favorites = savedServers.filter { favoriteAddresses.contains(it.address) }
        val boxWidth = (width - 36).coerceIn(160, 230)
        val rowHeight = 20
        val visibleCount = favorites.size.coerceAtMost(5)
        val boxHeight = 34 + (visibleCount.coerceAtLeast(1) * rowHeight)
        val x = width - boxWidth - 14
        val y = 40
        val rect = Rect(x.coerceAtLeast(8), y, boxWidth, boxHeight.coerceAtMost(height - 50))
        drawPanel(context, rect, 0xF006100D.toInt(), 0xAA73D9B7.toInt())
        drawText(context, "БЫСТРЫЙ ВХОД  •  ALT + КОЛЕСО", rect.x + 9, rect.y + 8, accentBright)

        if (favorites.isEmpty()) {
            drawText(context, "Добавь ★ на вкладке «Сервера».", rect.x + 9, rect.y + 23, textMuted)
            return
        }

        favoriteServerIndex = favoriteServerIndex.coerceIn(0, minOf(favorites.lastIndex, 4))
        favorites.take(5).forEachIndexed { index, server ->
            val row = Rect(rect.x + 7, rect.y + 23 + index * rowHeight, rect.w - 14, rowHeight - 1)
            favoriteServerHits.add(server to row)
            drawPanel(context, row,
                if (index == favoriteServerIndex) panelSelected else panelSoft,
                if (index == favoriteServerIndex) 0x8873D9B7.toInt() else 0x223D9B7B)
            drawText(context, textRenderer.trimToWidth(server.name, row.w - 12), row.x + 6, row.y + 5,
                if (index == favoriteServerIndex) textMain else textMuted)
        }
    }

    private fun connectToServer(server: ServerInfo) {
        if (!ServerAddress.isValid(server.address)) {
            accountStatus = "Некорректный адрес сервера"
            return
        }
        val client = MinecraftClient.getInstance()
        ConnectScreen.connect(this, client, ServerAddress.parse(server.address), server, false, null)
    }

    private fun toggleFavorite(address: String) {
        loadFavorites()
        if (!favoriteAddresses.add(address)) favoriteAddresses.remove(address)
        saveFavorites()
    }

    private fun drawFooter(context: DrawContext, mouseX: Int, mouseY: Int) {
        drawText(context, "MINECRAFT 1.21.4  •  ASTRA CLIENT", 14, height - 12, textDim)

        volumeButton = Rect(width - 30, height - 23, 18, 18)
        val volume = MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MASTER)
        drawText(context, if (volume <= 0.001f) "×" else "♫",
            volumeButton.x + 4, volumeButton.y + 4, textMuted)

        val hoveredButton = volumeButton.contains(mouseX.toDouble(), mouseY.toDouble())
        val hoveredSlider = volumeSlider.contains(mouseX.toDouble(), mouseY.toDouble())
        val hoveredPopup = volumePopup.contains(mouseX.toDouble(), mouseY.toDouble())
        volumeOpen = hoveredButton || hoveredSlider || hoveredPopup || draggingVolume

        if (volumeOpen) {
            volumePopup = Rect((width - 184).coerceAtLeast(8), height - 53, 148, 27)
            drawPanel(context, volumePopup, 0xF006100D.toInt(), border)
            drawText(context, "ЗВУК", volumePopup.x + 8, volumePopup.y + 9, textMuted)
            volumeSlider = Rect(volumePopup.x + 44, volumePopup.y + 5, 96, 16)
            drawSliderBar(context, volumeSlider, volume.toDouble(), 0.0, 1.0)
        }
    }

    private fun drawPanel(context: DrawContext, rect: Rect, fill: Int, outline: Int) {
        if (rect.w <= 0 || rect.h <= 0) return
        if (rect.w > 4 && rect.h > 4) {
            context.fill(rect.x + 1, rect.y, rect.x + rect.w - 1, rect.y + rect.h, fill)
            context.fill(rect.x, rect.y + 2, rect.x + rect.w, rect.y + rect.h - 2, fill)
        } else {
            context.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, fill)
        }
        context.drawBorder(rect.x, rect.y, rect.w, rect.h, outline)
    }

    private fun drawButton(context: DrawContext, rect: Rect, label: String, primary: Boolean, hovered: Boolean) {
        val fill = when {
            primary && hovered -> 0xE8429679.toInt()
            primary -> 0xD52A7059.toInt()
            hovered -> 0xA5264036.toInt()
            else -> 0x85101E19.toInt()
        }
        drawPanel(context, rect, fill, if (primary) 0xAA73D9B7.toInt() else 0x553D9B7B)
        val available = (rect.w - 8).coerceAtLeast(10)
        val clipped = textRenderer.trimToWidth(label, available)
        val textWidth = textRenderer.getWidth(clipped)
        drawText(context, clipped, rect.x + (rect.w - textWidth) / 2, rect.y + (rect.h - 8) / 2,
            if (primary || hovered) textMain else textMuted)
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

    private fun drawSwitch(context: DrawContext, rect: Rect, enabled: Boolean) {
        context.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h,
            if (enabled) 0xFF347F6B.toInt() else 0xFF26322F.toInt())
        val knobX = if (enabled) rect.x + rect.w - 10 else rect.x + 2
        context.fill(knobX, rect.y + 2, knobX + 8, rect.y + rect.h - 2,
            if (enabled) 0xFFE8FFF6.toInt() else 0xFF8CA099.toInt())
    }

    private fun drawSlider(context: DrawContext, rect: Rect, value: Double, min: Double, max: Double) {
        drawSliderBar(context, rect, value, min, max)
    }

    private fun drawSliderBar(context: DrawContext, rect: Rect, value: Double, min: Double, max: Double) {
        val trackY = rect.y + rect.h / 2 - 1
        context.fill(rect.x, trackY, rect.x + rect.w, trackY + 3, 0xFF273632.toInt())
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val progress = ((value - min) / range).coerceIn(0.0, 1.0)
        val filled = (rect.w * progress).roundToInt()
        context.fill(rect.x, trackY, rect.x + filled, trackY + 3, accent)
        val knobX = (rect.x + filled - 3).coerceIn(rect.x, rect.x + (rect.w - 6).coerceAtLeast(0))
        context.fill(knobX, trackY - 3, knobX + 6, trackY + 6, textMain)
    }

    private fun updateSlider(hit: SliderHit, mouseX: Double) {
        if (hit.trackWidth <= 0) return
        val percent = ((mouseX - hit.trackX).toDouble() / hit.trackWidth.toDouble()).coerceIn(0.0, 1.0)
        val raw = hit.setting.min + (hit.setting.max - hit.setting.min) * percent
        val step = hit.setting.increment.takeIf { it > 0.0 } ?: 0.01
        val stepped = hit.setting.min + ((raw - hit.setting.min) / step).roundToInt() * step
        hit.setting.setValue(stepped)
    }

    private fun updateVolume(mouseX: Double) {
        val client = MinecraftClient.getInstance()
        if (volumeSlider.w <= 0) return
        val percent = ((mouseX - volumeSlider.x).toDouble() / volumeSlider.w.toDouble()).coerceIn(0.0, 1.0)
        client.options.getSoundVolumeOption(SoundCategory.MASTER).setValue(percent)
        if (percent > 0.001) previousVolume = percent
    }

    private fun formatNumber(value: Double, step: Double): String =
        if (step >= 1.0) value.roundToInt().toString()
        else String.format(java.util.Locale.ROOT, "%.2f", value)

    private fun formatBind(bind: Int): String = when {
        bind < 0 -> "НЕТ"
        bind >= GLFW.GLFW_MOUSE_BUTTON_1 && bind <= GLFW.GLFW_MOUSE_BUTTON_LAST -> "МЫШЬ ${bind + 1}"
        else -> GLFW.glfwGetKeyName(bind, 0)?.uppercase() ?: "KEY $bind"
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button)

        navHits.entries.firstOrNull { it.value.contains(mouseX, mouseY) }?.let {
            changePage(it.key)
            return true
        }

        if (volumeButton.contains(mouseX, mouseY)) {
            val client = MinecraftClient.getInstance()
            val current = client.options.getSoundVolume(SoundCategory.MASTER)
            if (current > 0.001f) {
                previousVolume = current.toDouble()
                client.options.getSoundVolumeOption(SoundCategory.MASTER).setValue(0.0)
            } else {
                client.options.getSoundVolumeOption(SoundCategory.MASTER).setValue(previousVolume)
            }
            return true
        }
        if (volumeOpen && volumeSlider.contains(mouseX, mouseY)) {
            draggingVolume = true
            updateVolume(mouseX)
            return true
        }

        when (page) {
            Page.HOME -> {
                if (homeLoginButton.contains(mouseX, mouseY)) {
                    changePage(Page.ACCOUNTS)
                    return true
                }
            }

            Page.GAME -> {
                if (width >= 760 && desktopSettingsOpen && detailsCloseButton.contains(mouseX, mouseY)) {
                    desktopSettingsOpen = false
                    return true
                }

                categoryHits.firstOrNull { it.second.contains(mouseX, mouseY) }?.let {
                    category = it.first
                    selectedModule = modulesForCategory(category).firstOrNull()
                    moduleScroll = 0
                    settingsScroll = 0
                    mobileSettingsOpen = false
                    return true
                }

                if (width < 760 && mobileSettingsOpen && accountsBackButton.contains(mouseX, mouseY)) {
                    mobileSettingsOpen = false
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

                if (selectedModule != null && detailsModuleToggle.contains(mouseX, mouseY)) {
                    selectedModule!!.toggled()
                    TriggerBotClient.saveConfig()
                    return true
                }
                if (selectedModule != null && detailsBindButton.contains(mouseX, mouseY)) {
                    waitingForBind = selectedModule
                    return true
                }

                moduleHits.firstOrNull { it.toggle.contains(mouseX, mouseY) }?.let {
                    it.module.toggled()
                    selectedModule = it.module
                    TriggerBotClient.saveConfig()
                    if (width < 760) mobileSettingsOpen = true
                    return true
                }
                moduleHits.firstOrNull { it.row.contains(mouseX, mouseY) }?.let {
                    if (it.module.name == "Alt Manager") {
                        changePage(Page.ACCOUNTS)
                        return true
                    }
                    selectedModule = it.module
                    settingsScroll = 0
                    if (width < 760) mobileSettingsOpen = true
                    else desktopSettingsOpen = true
                    return true
                }
            }

            Page.ACCOUNTS -> {
                if (accountTabs.entries.firstOrNull { it.value.contains(mouseX, mouseY) }?.key
                    ?.let { accountTab = it; true } == true) {
                    if (::nicknameInput.isInitialized) {
                        nicknameInput.visible = accountTab == AccountTab.PROFILES
                        nicknameInput.active = nicknameInput.visible
                    }
                    accountScroll = 0
                    serverScroll = 0
                    accountStatus = ""
                    return true
                }

                if (accountTab == AccountTab.PROFILES) {
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
                } else {
                    serverHits.firstOrNull { it.favorite.contains(mouseX, mouseY) }?.let {
                        toggleFavorite(it.server.address)
                        return true
                    }
                    serverHits.firstOrNull { it.row.contains(mouseX, mouseY) }?.let {
                        selectedServerAddress = it.server.address
                        return true
                    }
                    if (serverJoinButton.contains(mouseX, mouseY)) {
                        val server = savedServers.firstOrNull { it.address == selectedServerAddress }
                        if (server != null) connectToServer(server)
                        else accountStatus = "Сначала выбери сервер"
                        return true
                    }
                }
            }
        }

        if (page == Page.ACCOUNTS && altHeld) {
            favoriteServerHits.firstOrNull { it.second.contains(mouseX, mouseY) }?.let {
                connectToServer(it.first)
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    private fun saveAccount() {
        val name = nicknameInput.getText().trim()
        if (!AltManager.isValidName(name)) {
            accountStatus = "Ник: 3–16 латинских букв, цифр или _"
            return
        }
        accountStatus = if (AltManager.add(name)) "Ник сохранён" else "Такой ник уже есть"
        selectedAccount = -1
    }

    private fun applyAccount() {
        val name = nicknameInput.getText().trim()
        if (!AltManager.isValidName(name)) {
            accountStatus = "Некорректный ник"
            return
        }
        accountStatus = if (AltManager.apply(name)) {
            "Офлайн-профиль выбран: $name"
        } else {
            "Нельзя сменить профиль во время игры"
        }
    }

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        deltaX: Double,
        deltaY: Double
    ): Boolean {
        if (draggingVolume) {
            updateVolume(mouseX)
            return true
        }
        draggingSlider?.let {
            updateSlider(it, mouseX)
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (draggingVolume) {
            draggingVolume = false
            return true
        }
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
        val direction = if (verticalAmount < 0) 1 else -1
        when (page) {
            Page.HOME -> {
                val index = pages.indexOf(page)
                changePage(pages[(index + direction + pages.size) % pages.size])
                return true
            }

            Page.GAME -> {
                if (width >= 760 && desktopSettingsOpen && selectedModule != null) {
                    val total = selectedModule!!.settings.fold(0) { sum, setting ->
                        sum + if (setting is NumberSetting) 42 else 28
                    }
                    val viewport = (height - 160).coerceAtLeast(30)
                    settingsScroll = (settingsScroll + direction * 24).coerceIn(0, (total - viewport).coerceAtLeast(0))
                    return true
                }
                if (width >= 760) {
                    val selectedColumn = desktopColumnHits.firstOrNull { it.second.contains(mouseX, mouseY) }?.first
                        ?: Category.COMBAT
                    val modulesInColumn = modulesForCategory(selectedColumn)
                    val rowHeight = 28
                    val viewport = (height - 120).coerceAtLeast(30)
                    val total = modulesInColumn.size * rowHeight
                    val maxScroll = (total - viewport).coerceAtLeast(0)
                    columnScrolls[selectedColumn] = ((columnScrolls[selectedColumn] ?: 0) + direction * 24)
                        .coerceIn(0, maxScroll)
                    return true
                }
                val modules = modulesForCategory(category)
                if (width < 760 && mobileSettingsOpen) {
                    val module = selectedModule
                    if (module != null) {
                        val total = module.settings.fold(0) { total, setting -> total + if (setting is NumberSetting) 42 else 28 }
                        val viewport = (height - 142).coerceAtLeast(24)
                        settingsScroll = (settingsScroll + direction * 24).coerceIn(0, (total - viewport).coerceAtLeast(0))
                    }
                } else {
                    val rowHeight = if (width < 760) 29 else 35
                    val viewport = (height - 132).coerceAtLeast(24)
                    val total = modules.size * rowHeight
                    moduleScroll = (moduleScroll + direction * 24).coerceIn(0, (total - viewport).coerceAtLeast(0))
                }
                return true
            }

            Page.ACCOUNTS -> {
                if (altHeld) {
                    val favorites = savedServers.filter { favoriteAddresses.contains(it.address) }
                    if (favorites.isNotEmpty()) {
                        favoriteServerIndex = (favoriteServerIndex + direction).coerceIn(0, favorites.lastIndex)
                    }
                    return true
                }
                if (accountTab == AccountTab.PROFILES) {
                    accountScroll = (accountScroll + direction).coerceAtLeast(0)
                } else {
                    serverScroll = (serverScroll + direction).coerceAtLeast(0)
                }
                return true
            }
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
            altHeld = true
            return true
        }

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
        if (page == Page.ACCOUNTS && accountTab == AccountTab.PROFILES && keyCode == GLFW.GLFW_KEY_ENTER) {
            applyAccount()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
            altHeld = false
            return true
        }
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun shouldPause(): Boolean = false

    override fun close() {
        AltManager.save()
        super.close()
    }
}
