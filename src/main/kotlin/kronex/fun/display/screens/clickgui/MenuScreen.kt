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
        val contentW: Int, val contentH: Int, val compact: Boolean
    )
    private data class ModuleGroup(val title: String, val modules: List<Module>)
    private data class ModuleHit(val module: Module, val row: Rect, val toggle: Rect, val settings: Rect)
    private data class BooleanHit(val setting: BooleanSetting, val toggle: Rect)
    private data class SliderHit(val setting: NumberSetting, val track: Rect)
    private data class AccountHit(val index: Int, val name: String, val row: Rect, val delete: Rect)
    private data class CardPlacement(val group: ModuleGroup, val column: Int, val top: Int, val height: Int)

    private val categoryItems = listOf(
        Category.COMBAT to "Бой",
        Category.MOVEMENT to "Движение",
        Category.RENDER to "Визуалы",
        Category.PLAYER to "Игрок",
        Category.MISC to "Разное"
    )
    private val galahadFont = Identifier.of("triggerbot", "galahad")
    private val menuFont = Identifier.of("triggerbot", "pt_sans")

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
    private var accountsPage = false
    private var accountsScroll = 0
    private var accountsStatus = ""
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
    private val accountHits = mutableListOf<AccountHit>()
    private var accountsNavHit = Rect(0, 0, 0, 0)
    private var accountsSaveHit = Rect(0, 0, 0, 0)
    private var accountsApplyHit = Rect(0, 0, 0, 0)
    private var accountsListViewport = Rect(0, 0, 0, 0)

    private var settingsPanel = Rect(0, 0, 0, 0)
    private var settingsClose = Rect(0, 0, 0, 0)
    private var settingsModuleSwitch = Rect(0, 0, 0, 0)
    private var settingsBind = Rect(0, 0, 0, 0)

    private val username: String
        get() = runCatching { MinecraftClient.getInstance().session.username }.getOrDefault("Player")

    @JvmStatic
    fun openAccounts() {
        ModuleManager.moduleRegister()
        AltManager.load()
        accountsPage = true
        accountsStatus = ""
        accountsScroll = 0
        settingsOpen = false
        waitingForBind = null
        if (::searchField.isInitialized) {
            searchField.setText(AltManager.currentNickname())
            searchField.setSuggestion("Ник 3–16 символов")
        }
        MinecraftClient.getInstance().setScreen(this)
    }

    @JvmStatic
    fun openGui() {
        ModuleManager.moduleRegister()
        selectedCategory = Category.COMBAT
        accountsPage = false
        accountsStatus = ""
        if (::searchField.isInitialized) {
            searchField.setText("")
            searchField.setSuggestion("Поиск модулей")
        }
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
        val fieldX = if (layout.compact) layout.x + 42 else layout.contentX + 54
        val fieldY = if (layout.compact) layout.y + 40 else layout.y + 15
        val fieldWidth = if (layout.compact) {
            (layout.w - 60).coerceAtLeast(48)
        } else {
            (layout.contentW - 112).coerceAtLeast(54)
        }

        searchField = TextFieldWidget(
            textRenderer, fieldX, fieldY, fieldWidth,
            if (layout.compact) 18 else 20, Text.literal("Поиск")
        )
        searchField.setMaxLength(48)
        searchField.setSuggestion("")
        searchField.setEditableColor(primaryText)
        searchField.setDrawsBackground(false)
        searchField.setRenderTextProvider { value, firstCharacterIndex ->
            val start = firstCharacterIndex.coerceIn(0, value.length)
            Text.literal(value.substring(start))
                .setStyle(Style.EMPTY.withFont(menuFont))
                .asOrderedText()
        }
        searchField.setChangedListener {
            contentScroll = 0
            if (accountsPage) accountsScroll = 0
        }
        if (accountsPage) searchField.setText(AltManager.currentNickname())
        addDrawableChild(searchField)
    }

    private fun calculateLayout(): WindowLayout {
        // Zalith/Pojav can expose a short scaled GUI height. Switch to top tabs
        // before a vertical sidebar can overflow the visible panel.
        val compact = width < 620 || height < 420
        val margin = if (compact) 6 else 20
        val panelW = min(860, max(220, width - margin * 2))
        val panelH = min(550, max(if (compact) 150 else 180, height - margin * 2))
        val panelX = (width - panelW) / 2
        val panelY = (height - panelH) / 2
        val sidebarW = if (compact) 0 else when {
            panelW < 360 -> 86
            panelW < 520 -> 112
            else -> 176
        }
        val headerHeight = if (compact) 94 else 58
        val bottomReserve = if (compact) 12 else 72
        return WindowLayout(
            panelX, panelY, panelW, panelH, sidebarW,
            panelX + sidebarW, panelY + headerHeight,
            panelW - sidebarW,
            (panelH - headerHeight - bottomReserve).coerceAtLeast(0),
            compact
        )
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        // Keep the 3D world visible. Do not call renderBackground(): it blurs the world.
        context.fill(0, 0, width, height, 0x26000000)
        val layout = calculateLayout()
        drawWindow(context, layout)
        drawSidebar(context, layout, mouseX, mouseY)
        drawHeader(context, layout)
        if (accountsPage) {
            drawAccountsPage(context, layout, mouseX, mouseY)
        } else {
            drawModuleCards(context, layout, mouseX, mouseY)
        }
        if (!accountsPage && settingsOpen && selectedModule != null) {
            drawSettings(context, layout, selectedModule!!, mouseX, mouseY)
        }
        super.render(context, mouseX, mouseY, delta)
        drawSearchOverlay(context)
    }

    private fun drawSearchOverlay(context: DrawContext) {
        // Typed text, cursor and selection are rendered by TextFieldWidget
        // through its PT Sans render provider. This is only the placeholder.
        if (searchField.getText().isNotEmpty() || searchField.isFocused) return
        val placeholder = if (accountsPage) "Ник 3–16 символов" else "Поиск модулей"
        val styled = Text.literal(placeholder).setStyle(Style.EMPTY.withFont(menuFont))
        context.enableScissor(
            searchField.x + 1, searchField.y,
            searchField.x + searchField.width - 2, searchField.y + searchField.height
        )
        context.drawText(textRenderer, styled, searchField.x + 2,
            searchField.y + (searchField.height - 8) / 2, mutedText, false)
        context.disableScissor()
    }

    private fun drawWindow(context: DrawContext, layout: WindowLayout) {
        drawRounded(context, layout.x, layout.y, layout.w, layout.h, 11, background)
        context.drawBorder(layout.x, layout.y, layout.w, layout.h, border)
        if (layout.compact) {
            val dividerY = layout.y + 91
            context.fill(layout.x + 10, dividerY, layout.x + layout.w - 10, dividerY + 1, border)
            return
        }
        drawRounded(context, layout.x + 1, layout.y + 1,
            layout.sidebar - 1, layout.h - 2, 9, sidebarBackground)
        context.fill(layout.x + layout.sidebar, layout.y + 1,
            layout.x + layout.sidebar + 1, layout.y + layout.h - 1, border)
        context.fill(layout.contentX, layout.y + 56,
            layout.x + layout.w - 1, layout.y + 57, border)
        context.fill(layout.x + 1, layout.y + layout.h - 43,
            layout.x + layout.sidebar - 1, layout.y + layout.h - 42, border)
    }

    private fun drawSidebar(context: DrawContext, layout: WindowLayout, mouseX: Int, mouseY: Int) {
        categoryHits.clear()
        val logoX = layout.x + if (layout.compact || layout.sidebar < 120) 13 else 24
        val logo = Text.literal("ASTRA").setStyle(Style.EMPTY.withFont(galahadFont))
        context.drawText(textRenderer, logo, logoX, layout.y + 17, primaryText, false)

        if (layout.compact) {
            accountsNavHit = Rect(layout.x + layout.w - 34, layout.y + 12, 23, 24)
            drawRounded(context, accountsNavHit.x, accountsNavHit.y,
                accountsNavHit.w, accountsNavHit.h, 6,
                if (accountsPage) 0xFF211719.toInt() else rowHover)
            if (accountsPage) {
                context.drawBorder(accountsNavHit.x, accountsNavHit.y,
                    accountsNavHit.w, accountsNavHit.h, 0xFF5C3438.toInt())
            }
            drawAccountIcon(context, accountsNavHit.x + 7, accountsNavHit.y + 5, accent)
            return
        }

        val sectionY = layout.y + 78
        drawText(context, "ФУНКЦИИ", layout.x + 17, sectionY, mutedText)
        val itemStart = sectionY + 19
        val itemHeight = if (layout.sidebar < 120) 31 else 36
        val sidebarClip = Rect(layout.x + 3, layout.y + 60, layout.sidebar - 6,
            (layout.h - 111).coerceAtLeast(0))

        categoryItems.forEachIndexed { index, item ->
            val rect = Rect(layout.x + 8, itemStart + index * itemHeight,
                layout.sidebar - 16, itemHeight - 3)
            val visible = intersect(rect, sidebarClip)
            if (visible != null) {
                categoryHits.add(item.first to visible)
                val active = !accountsPage && selectedCategory == item.first && searchField.getText().isBlank()
                if (active) {
                    drawRounded(context, rect.x, rect.y, rect.w, rect.h, 6, 0xFF211719.toInt())
                    context.drawBorder(rect.x, rect.y, rect.w, rect.h, 0xFF5C3438.toInt())
                } else if (rect.contains(mouseX.toDouble(), mouseY.toDouble())) {
                    drawRounded(context, rect.x, rect.y, rect.w, rect.h, 6, rowHover)
                }
                drawCategoryIcon(context, index, rect.x + 9, rect.y + 8,
                    if (active) accent else secondaryText)
                val labelX = rect.x + if (layout.sidebar < 120) 25 else 32
                val labelW = (rect.w - (labelX - rect.x) - 4).coerceAtLeast(12)
                drawText(context, trimMenuText(item.second, labelW),
                    labelX, rect.y + 8, if (active) accent else primaryText)
            }
        }

        val manageY = itemStart + categoryItems.size * itemHeight + 7
        accountsNavHit = Rect(0, 0, 0, 0)
        if (layout.h >= 350) {
            drawText(context, "УПРАВЛЕНИЕ", layout.x + 17, manageY, mutedText)
            val target = Rect(layout.x + 8, manageY + 17, layout.sidebar - 16, 28)
            accountsNavHit = intersect(target, sidebarClip) ?: Rect(0, 0, 0, 0)
            if (accountsNavHit.w > 0) {
                if (accountsPage) {
                    drawRounded(context, target.x, target.y, target.w, target.h, 6, 0xFF211719.toInt())
                    context.drawBorder(target.x, target.y, target.w, target.h, 0xFF5C3438.toInt())
                } else if (target.contains(mouseX.toDouble(), mouseY.toDouble())) {
                    drawRounded(context, target.x, target.y, target.w, target.h, 6, rowHover)
                }
                drawAccountIcon(context, target.x + 9, target.y + 7,
                    if (accountsPage) accent else secondaryText)
                if (layout.sidebar >= 120) {
                    drawText(context, "Аккаунты", target.x + 32,
                        target.y + 8, if (accountsPage) accent else primaryText)
                }
            }
        }

        val footerY = layout.y + layout.h - 37
        drawRounded(context, layout.x + 13, footerY - 2, 25, 25, 12, 0xFF242326.toInt())
        drawText(context, "?", layout.x + 22, footerY + 6, secondaryText)
        if (layout.sidebar >= 120) {
            drawText(context, trimMenuText(username, layout.sidebar - 62),
                layout.x + 46, footerY + 2, primaryText)
            drawText(context, "Astra Client", layout.x + 46, footerY + 13, mutedText)
        }
    }

    private fun drawCategoryIcon(context: DrawContext, index: Int, x: Int, y: Int, color: Int) {
        // Primitive icons avoid unsupported Unicode symbols in the bundled font.
        when (index) {
            0 -> {
                context.fill(x + 4, y, x + 6, y + 3, color)
                context.fill(x + 4, y + 7, x + 6, y + 10, color)
                context.fill(x, y + 4, x + 3, y + 6, color)
                context.fill(x + 7, y + 4, x + 10, y + 6, color)
                context.drawBorder(x + 3, y + 3, 4, 4, color)
            }
            1 -> {
                context.fill(x + 4, y + 1, x + 6, y + 9, color)
                context.fill(x + 2, y + 1, x + 8, y + 3, color)
                context.fill(x + 2, y + 8, x + 8, y + 10, color)
            }
            2 -> {
                context.drawBorder(x + 1, y + 2, 9, 6, color)
                context.fill(x + 4, y + 4, x + 7, y + 7, color)
            }
            3 -> drawAccountIcon(context, x + 1, y, color)
            else -> {
                context.fill(x + 1, y + 1, x + 4, y + 4, color)
                context.fill(x + 7, y + 1, x + 10, y + 4, color)
                context.fill(x + 1, y + 7, x + 4, y + 10, color)
                context.fill(x + 7, y + 7, x + 10, y + 10, color)
            }
        }
    }

    private fun drawAccountIcon(context: DrawContext, x: Int, y: Int, color: Int) {
        context.fill(x + 4, y, x + 8, y + 4, color)
        context.fill(x + 1, y + 6, x + 11, y + 12, color)
    }

    private fun drawSearchIcon(context: DrawContext, x: Int, y: Int, color: Int) {
        context.drawBorder(x, y, 8, 8, color)
        context.fill(x + 7, y + 7, x + 10, y + 9, color)
    }

    private fun drawGroupMark(context: DrawContext, x: Int, y: Int, color: Int) {
        context.fill(x + 2, y, x + 4, y + 2, color)
        context.fill(x, y + 2, x + 2, y + 4, color)
        context.fill(x + 4, y + 2, x + 6, y + 4, color)
        context.fill(x + 2, y + 4, x + 4, y + 6, color)
    }

    private fun categoryMarker(index: Int): String = when (index) {
        0 -> "Б"
        1 -> "Д"
        2 -> "В"
        3 -> "И"
        else -> "Р"
    }

    private fun drawHeader(context: DrawContext, layout: WindowLayout) {
        val searchX = if (layout.compact) layout.x + 10 else layout.contentX + 13
        val searchY = if (layout.compact) layout.y + 37 else layout.y + 13
        val searchW = if (layout.compact) layout.w - 20 else layout.contentW - 26
        val searchH = if (layout.compact) 23 else 25
        drawRounded(context, searchX, searchY, searchW, searchH, 6, 0xFF101012.toInt())
        context.drawBorder(searchX, searchY, searchW, searchH, border)
        drawSearchIcon(context, searchX + 10, searchY + 7, mutedText)

        if (layout.compact) {
            categoryHits.clear()
            val labels = listOf("БОЙ", "ДВИЖ.", "ВИЗ.", "ИГРОК", "РАЗНОЕ")
            val left = layout.x + 9
            val gap = 4
            val totalW = layout.w - 18
            val tabW = ((totalW - gap * 4) / 5).coerceAtLeast(28)
            labels.forEachIndexed { index, label ->
                val rect = Rect(left + index * (tabW + gap), layout.y + 66, tabW, 20)
                categoryHits.add(categoryItems[index].first to rect)
                val active = !accountsPage && selectedCategory == categoryItems[index].first &&
                    searchField.getText().isBlank()
                if (active) {
                    drawRounded(context, rect.x, rect.y, rect.w, rect.h, 5, 0xFF44282B.toInt())
                    context.drawBorder(rect.x, rect.y, rect.w, rect.h, 0xFF875054.toInt())
                } else {
                    drawRounded(context, rect.x, rect.y, rect.w, rect.h, 5, 0xFF151416.toInt())
                }
                val text = trimMenuText(label, rect.w - 4)
                drawText(context, text, rect.x + (rect.w - menuTextWidth(text)) / 2,
                    rect.y + 6, if (active) accent else secondaryText)
            }
            return
        }

        drawText(context, "МОДУЛИ", layout.contentX + 18, layout.y + 20, primaryText)
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
            Category.COMBAT -> {
                val aim = selected.filter { it.name == "Aim Assist" }
                val fight = selected.filter { it.name != "Aim Assist" }
                listOf(ModuleGroup("ДРАКА", fight), ModuleGroup("НАВЕДЕНИЕ", aim))
                    .filter { it.modules.isNotEmpty() }
            }
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

    private fun drawAccountsPage(context: DrawContext, layout: WindowLayout, mouseX: Int, mouseY: Int) {
        accountHits.clear()
        val left = layout.contentX + 18
        val top = layout.contentY + 14
        val bodyW = (layout.contentW - 36).coerceAtLeast(72)
        drawText(context, "АККАУНТЫ", left, top, primaryText)
        drawText(context, "Локальные офлайн-профили", left, top + 15, mutedText)

        val currentCard = Rect(left, top + 34, bodyW, 66)
        drawRounded(context, currentCard.x, currentCard.y, currentCard.w, currentCard.h, 7, cardBackground)
        context.drawBorder(currentCard.x, currentCard.y, currentCard.w, currentCard.h, border)
        drawText(context, "ТЕКУЩИЙ ПРОФИЛЬ", currentCard.x + 12, currentCard.y + 10, mutedText)
        drawText(context, trimMenuText(AltManager.currentNickname(), currentCard.w - 24),
            currentCard.x + 12, currentCard.y + 29, primaryText)

        val buttonY = currentCard.y + currentCard.h + 10
        val buttonGap = 8
        val buttonW = ((bodyW - buttonGap) / 2).coerceAtLeast(40)
        accountsSaveHit = Rect(left, buttonY, buttonW, 24)
        accountsApplyHit = Rect(left + buttonW + buttonGap, buttonY, buttonW, 24)
        drawAccountButton(context, accountsSaveHit, "СОХРАНИТЬ", accountsSaveHit.contains(mouseX.toDouble(), mouseY.toDouble()))
        drawAccountButton(context, accountsApplyHit, "ПРИМЕНИТЬ", accountsApplyHit.contains(mouseX.toDouble(), mouseY.toDouble()))

        val listTop = buttonY + 36
        drawText(context, "СОХРАНЁННЫЕ НИКИ", left, listTop, secondaryText)
        val viewportTop = listTop + 17
        val viewportBottom = layout.y + layout.h - 17
        accountsListViewport = Rect(left, viewportTop, bodyW, max(0, viewportBottom - viewportTop))
        val names = AltManager.getAll()
        val rowH = 32
        val maxOffset = (names.size * rowH - accountsListViewport.h).coerceAtLeast(0)
        accountsScroll = accountsScroll.coerceIn(0, maxOffset)

        context.enableScissor(
            accountsListViewport.x, accountsListViewport.y,
            accountsListViewport.x + accountsListViewport.w,
            accountsListViewport.y + accountsListViewport.h
        )
        names.forEachIndexed { index, name ->
            val rowY = viewportTop + index * rowH - accountsScroll
            val row = Rect(left, rowY, bodyW, rowH - 2)
            val delete = Rect(left + bodyW - 28, rowY + 3, 22, 22)
            if (row.contains(mouseX.toDouble(), mouseY.toDouble())) {
                drawRounded(context, row.x, row.y, row.w, row.h, 4, rowHover)
            }
            drawText(context, name, row.x + 10, row.y + 8, primaryText)
            drawText(context, "×", delete.x + 7, delete.y + 6, secondaryText)
            val visibleRow = intersect(row, accountsListViewport)
            if (visibleRow != null) {
                val visibleDelete = intersect(delete, accountsListViewport) ?: Rect(0, 0, 0, 0)
                accountHits.add(AccountHit(index, name, visibleRow, visibleDelete))
            }
        }
        context.disableScissor()

        if (names.isEmpty()) {
            drawText(context, "Список пуст. Введи ник в поле поиска сверху и нажми «СОХРАНИТЬ».",
                left, viewportTop + 7, mutedText)
        }
        if (accountsStatus.isNotEmpty()) {
            drawText(context, trimMenuText(accountsStatus, bodyW),
                left, layout.y + layout.h - 11, accent)
        }
    }

    private fun drawAccountButton(context: DrawContext, rect: Rect, label: String, hovered: Boolean) {
        drawRounded(context, rect.x, rect.y, rect.w, rect.h, 5,
            if (hovered) 0xFF44282B.toInt() else 0xFF211719.toInt())
        context.drawBorder(rect.x, rect.y, rect.w, rect.h,
            if (hovered) accent else 0xFF5C3438.toInt())
        drawText(context, trimMenuText(label, rect.w - 12), rect.x + 6, rect.y + 8,
            if (hovered) primaryText else accent)
    }

    private fun saveAccount() {
        val name = searchField.getText().trim()
        if (!AltManager.isValidName(name)) {
            accountsStatus = "Ник: 3–16 латинских букв, цифр или _"
        } else {
            accountsStatus = if (AltManager.add(name)) "Профиль сохранён" else "Такой ник уже есть"
        }
    }

    private fun applyAccount() {
        val name = searchField.getText().trim()
        if (!AltManager.isValidName(name)) {
            accountsStatus = "Некорректный ник"
        } else {
            accountsStatus = if (AltManager.apply(name)) {
                "Выбран офлайн-профиль: " + name
            } else {
                "Сменить ник можно только вне игрового мира"
            }
        }
    }

    private fun drawModuleCards(context: DrawContext, layout: WindowLayout, mouseX: Int, mouseY: Int) {
        moduleHits.clear()
        val groups = groupsForView()
        val cards = if (groups.isEmpty()) {
            listOf(ModuleGroup(
                if (searchField.getText().isNotBlank()) "ПОИСК" else categoryLabel(selectedCategory),
                emptyList()
            ))
        } else groups

        val x = layout.contentX + 15
        val y = layout.contentY + 10
        val availableW = (layout.contentW - 30).coerceAtLeast(60)
        val gap = 12
        val columns = if (!layout.compact && availableW >= 470) 2 else 1
        val columnW = ((availableW - gap * (columns - 1)) / columns).coerceAtLeast(60)
        val rowHeight = if (layout.compact) 36 else if (layout.contentW < 300) 38 else 43
        val viewport = Rect(layout.contentX + 4, layout.contentY,
            (layout.contentW - 8).coerceAtLeast(1), (layout.contentH - 4).coerceAtLeast(1))

        // Calculate all card positions and scroll limits before building hit boxes.
        val heights = IntArray(columns)
        val placements = mutableListOf<CardPlacement>()
        cards.forEach { group ->
            val column = heights.indices.minByOrNull { heights[it] } ?: 0
            val cardHeight = 42 + max(1, group.modules.size) * rowHeight
            val top = heights[column]
            placements.add(CardPlacement(group, column, top, cardHeight))
            heights[column] += cardHeight + 12
        }
        val contentHeight = heights.maxOrNull() ?: 0
        contentMaxScroll = (contentHeight - viewport.h).coerceAtLeast(0)
        contentScroll = contentScroll.coerceIn(0, contentMaxScroll)

        context.enableScissor(
            viewport.x, viewport.y,
            viewport.x + viewport.w, viewport.y + viewport.h
        )
        placements.forEach { placement ->
            val group = placement.group
            val cardX = x + placement.column * (columnW + gap)
            val cardY = y + placement.top - contentScroll
            val card = Rect(cardX, cardY, columnW, placement.height)

            drawRounded(context, card.x, card.y, card.w, card.h, 8, cardBackground)
            context.drawBorder(card.x, card.y, card.w, card.h, border)
            drawText(context, trimMenuText(group.title, card.w - 30),
                card.x + 12, card.y + 12, accent)
            drawGroupMark(context, card.x + card.w - 20, card.y + 11, accent)
            context.fill(card.x + 1, card.y + 35, card.x + card.w - 1,
                card.y + 36, 0xFF1D1B1E.toInt())

            if (group.modules.isEmpty()) {
                val empty = if (searchField.getText().isNotBlank()) "Ничего не найдено" else "Пока нет модулей"
                drawText(context, trimMenuText(empty, card.w - 20),
                    card.x + 12, card.y + 55, mutedText)
            } else {
                group.modules.forEachIndexed { index, module ->
                    val rowY = card.y + 43 + index * rowHeight
                    val row = Rect(card.x + 5, rowY, card.w - 10, rowHeight - 1)
                    if (row.contains(mouseX.toDouble(), mouseY.toDouble())) {
                        drawRounded(context, row.x, row.y, row.w, row.h, 4, rowHover)
                    }

                    val compactRow = row.w < 150
                    val toggleW = if (compactRow) 26 else 30
                    val toggle = Rect(row.x + row.w - toggleW - 2,
                        row.y + (row.h - 15) / 2, toggleW, 15)
                    val settingsW = if (compactRow) 18 else 25
                    val settings = Rect(toggle.x - settingsW - 5, row.y + 3,
                        settingsW, (row.h - 7).coerceAtLeast(12))

                    val visibleRow = intersect(row, viewport)
                    if (visibleRow != null) {
                        val visibleToggle = intersect(toggle, viewport) ?: Rect(0, 0, 0, 0)
                        val visibleSettings = intersect(settings, viewport) ?: Rect(0, 0, 0, 0)
                        moduleHits.add(ModuleHit(module, visibleRow, visibleToggle, visibleSettings))
                    }

                    val textWidth = (settings.x - row.x - 14).coerceAtLeast(12)
                    drawText(context, trimMenuText(module.name, textWidth),
                        row.x + 8, row.y + (row.h - 8) / 2,
                        if (module.isEnabled) primaryText else secondaryText)

                    val dotY = settings.y + settings.h / 2
                    for (dot in 0..2) {
                        context.fill(settings.x + 3 + dot * 4, dotY,
                            settings.x + 5 + dot * 4, dotY + 2, secondaryText)
                    }
                    drawSwitch(context, toggle, module.isEnabled)
                    if (index < group.modules.lastIndex) {
                        context.fill(row.x + 7, row.y + row.h,
                            row.x + row.w - 7, row.y + row.h + 1, 0xFF1E1C1F.toInt())
                    }
                }
            }
        }
        context.disableScissor()

        if (contentMaxScroll > 0) {
            val trackX = layout.x + layout.w - 8
            val trackY = viewport.y + 4
            val trackH = (viewport.h - 8).coerceAtLeast(1)
            context.fill(trackX, trackY, trackX + 2, trackY + trackH, 0xFF242225.toInt())
            val thumbH = max(22, trackH * trackH / (contentHeight + 1)).coerceAtMost(trackH)
            val thumbY = trackY + ((trackH - thumbH).toDouble() *
                contentScroll / contentMaxScroll).roundToInt()
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
            (layout.contentW - 8).coerceAtLeast(1), (layout.contentH - 4).coerceAtLeast(1))
        context.fill(content.x, content.y, content.x + content.w, content.y + content.h, 0xAA080809.toInt())

        val popupW = min(430, (content.w - 12).coerceAtLeast(1))
        val popupH = min(500, (content.h - 12).coerceAtLeast(1))
        val popupX = content.x + (content.w - popupW) / 2
        val popupY = content.y + (content.h - popupH) / 2
        settingsPanel = Rect(popupX, popupY, popupW, popupH)

        drawRounded(context, popupX, popupY, popupW, popupH, 9, 0xFF111113.toInt())
        context.drawBorder(popupX, popupY, popupW, popupH, 0xFF343034.toInt())
        drawText(context, "НАСТРОЙКИ", popupX + 12, popupY + 9, mutedText)
        drawText(context, trimMenuText(module.name, popupW - 66),
            popupX + 12, popupY + 24, primaryText)

        settingsClose = Rect(popupX + popupW - 25, popupY + 6, 18, 18)
        drawRounded(context, settingsClose.x, settingsClose.y,
            settingsClose.w, settingsClose.h, 5, rowHover)
        drawText(context, "×", settingsClose.x + 6, settingsClose.y + 5, secondaryText)

        settingsModuleSwitch = Rect(popupX + popupW - 46, popupY + 27, 32, 16)
        drawSwitch(context, settingsModuleSwitch, module.isEnabled)
        drawText(context, trimMenuText(module.desc, popupW - 25),
            popupX + 12, popupY + 45, secondaryText)

        val bindY = popupY + if (layout.compact) 58 else 66
        settingsBind = Rect(popupX + 10, bindY, (popupW - 20).coerceAtLeast(1),
            if (layout.compact) 20 else 24)
        drawRounded(context, settingsBind.x, settingsBind.y,
            settingsBind.w, settingsBind.h, 5, 0xFF19181A.toInt())
        context.drawBorder(settingsBind.x, settingsBind.y, settingsBind.w, settingsBind.h, border)
        val bindText = if (waitingForBind === module) "НАЖМИ КЛАВИШУ..." else "БИНД: " + formatBind(module.bind)
        drawText(context, trimMenuText(bindText, settingsBind.w - 16),
            settingsBind.x + 8, settingsBind.y + 6, secondaryText)

        val settingTop = popupY + if (layout.compact) 84 else 103
        val settingBottom = popupY + popupH - 8
        val settingViewportHeight = max(0, settingBottom - settingTop)
        val booleanRowHeight = if (layout.compact) 29 else 34
        val numberRowHeight = if (layout.compact) 42 else 48
        val totalSettingsHeight = module.settings.fold(0) { total, setting ->
            total + if (setting is NumberSetting) numberRowHeight else booleanRowHeight
        }
        settingsMaxScroll = (totalSettingsHeight - settingViewportHeight).coerceAtLeast(0)
        settingsScroll = settingsScroll.coerceIn(0, settingsMaxScroll)
        booleanHits.clear()
        sliderHits.clear()

        val settingClip = Rect(popupX + 6, settingTop, popupW - 12, settingViewportHeight)
        context.enableScissor(
            settingClip.x, settingClip.y,
            settingClip.x + settingClip.w, settingClip.y + settingClip.h
        )
        var offset = 0
        module.settings.forEach { setting ->
            val rowHeight = if (setting is NumberSetting) numberRowHeight else booleanRowHeight
            val rowY = settingTop + offset - settingsScroll
            val row = Rect(popupX + 10, rowY, popupW - 20, rowHeight)
            if (intersect(row, settingClip) != null) {
                when (setting) {
                    is BooleanSetting -> {
                        drawText(context, trimMenuText(setting.name, popupW - 78),
                            row.x, row.y + if (layout.compact) 9 else 11, primaryText)
                        val toggle = Rect(popupX + popupW - 48, row.y + 6, 32, 16)
                        drawSwitch(context, toggle, setting.isEnabled)
                        intersect(toggle, settingClip)?.let {
                            booleanHits.add(BooleanHit(setting, it))
                        }
                    }
                    is NumberSetting -> {
                        drawText(context, trimMenuText(setting.name, popupW - 88),
                            row.x, row.y + 4, primaryText)
                        val value = formatNumber(setting.doubleValue, setting.increment)
                        val valueWidth = menuTextWidth(value)
                        drawText(value, popupX + popupW - valueWidth - 14, row.y + 4, accent)
                        val track = Rect(popupX + 14, row.y + if (layout.compact) 24 else 25,
                            popupW - 28, 12)
                        drawSlider(context, track, setting.doubleValue, setting.min, setting.max)
                        intersect(track, settingClip)?.let {
                            sliderHits.add(SliderHit(setting, it))
                        }
                    }
                }
            }
            offset += rowHeight
        }
        context.disableScissor()

        if (module.settings.isEmpty()) {
            drawText(context, "Нет дополнительных параметров",
                popupX + 12, settingTop + 8, mutedText)
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
        val styled = Text.literal(value).setStyle(Style.EMPTY.withFont(menuFont))
        context.drawText(textRenderer, styled, x, y, color, false)
    }

    private fun menuTextWidth(value: String): Int =
        textRenderer.getWidth(Text.literal(value).setStyle(Style.EMPTY.withFont(menuFont)))

    private fun trimMenuText(value: String, maxWidth: Int): String {
        if (maxWidth <= 0) return ""
        if (menuTextWidth(value) <= maxWidth) return value
        var end = value.length
        while (end > 0) {
            end = value.offsetByCodePoints(end, -1)
            val candidate = value.substring(0, end)
            if (menuTextWidth(candidate) <= maxWidth) return candidate
        }
        return ""
    }

    private fun intersect(rect: Rect, clip: Rect): Rect? {
        val left = max(rect.x, clip.x)
        val top = max(rect.y, clip.y)
        val right = min(rect.x + rect.w, clip.x + clip.w)
        val bottom = min(rect.y + rect.h, clip.y + clip.h)
        if (right <= left || bottom <= top) return null
        return Rect(left, top, right - left, bottom - top)
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
            accountsPage = false
            searchField.setText("")
            searchField.setSuggestion("Поиск модулей")
            selectedCategory = it.first
            contentScroll = 0
            return true
        }
        if (accountsNavHit.contains(mouseX, mouseY)) {
            openAccounts()
            return true
        }
        if (accountsPage) {
            if (accountsSaveHit.contains(mouseX, mouseY)) {
                saveAccount()
                return true
            }
            if (accountsApplyHit.contains(mouseX, mouseY)) {
                applyAccount()
                return true
            }
            accountHits.firstOrNull { it.delete.contains(mouseX, mouseY) }?.let {
                AltManager.remove(it.index)
                accountsStatus = "Профиль удалён"
                return true
            }
            accountHits.firstOrNull { it.row.contains(mouseX, mouseY) }?.let {
                searchField.setText(it.name)
                return true
            }
            return super.mouseClicked(mouseX, mouseY, button)
        }
        moduleHits.firstOrNull { it.settings.contains(mouseX, mouseY) }?.let {
            if (it.module.name == "Alt Manager") {
                openAccounts()
                return true
            }
            selectedModule = it.module
            settingsScroll = 0
            settingsOpen = true
            waitingForBind = null
            return true
        }
        moduleHits.firstOrNull { it.toggle.contains(mouseX, mouseY) }?.let {
            if (it.module.name == "Alt Manager") {
                openAccounts()
                return true
            }
            it.module.toggled()
            TriggerBotClient.saveConfig()
            return true
        }
        moduleHits.firstOrNull { it.row.contains(mouseX, mouseY) }?.let {
            if (it.module.name == "Alt Manager") {
                openAccounts()
                return true
            }
            selectedModule = it.module
            settingsScroll = 0
            settingsOpen = true
            waitingForBind = null
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
        if (accountsPage) {
            if (!accountsListViewport.contains(mouseX, mouseY)) return super.mouseScrolled(
                mouseX, mouseY, horizontalAmount, verticalAmount
            )
            val names = AltManager.getAll()
            val maxOffset = (names.size * 32 - accountsListViewport.h).coerceAtLeast(0)
            accountsScroll = (accountsScroll + direction * 28).coerceIn(0, maxOffset)
            return true
        }
        if (settingsOpen) {
            settingsScroll = (settingsScroll + direction * 28).coerceIn(0, settingsMaxScroll)
        } else {
            val layout = calculateLayout()
            val area = Rect(layout.contentX, layout.contentY, layout.contentW, layout.contentH)
            if (!area.contains(mouseX, mouseY)) return super.mouseScrolled(
                mouseX, mouseY, horizontalAmount, verticalAmount
            )
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
        if (accountsPage && keyCode == GLFW.GLFW_KEY_ENTER) {
            applyAccount()
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
        accountsPage = false
        waitingForBind = null
        draggingSlider = null
        AltManager.save()
        TriggerBotClient.saveConfig()
        super.close()
    }
}
