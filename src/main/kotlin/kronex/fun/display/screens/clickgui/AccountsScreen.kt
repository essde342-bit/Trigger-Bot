package kronex.`fun`.display.screens.clickgui

import com.essde342.triggerbot.AltManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text

/**
 * Small Kotlin account manager kept separate from the in-game module grid.
 * Offline profile changes are only accepted by AltManager from the title screen.
 */
internal class AccountsScreen(
    private val selectedName: String? = null,
    private val message: String = ""
) : Screen(Text.literal("Astra Client • Accounts")) {

    private lateinit var nameField: TextFieldWidget

    override fun init() {
        val panelWidth = 360
        val left = (width - panelWidth) / 2
        val top = (height - 250) / 2

        nameField = TextFieldWidget(
            textRenderer,
            left + 18,
            top + 62,
            panelWidth - 36,
            20,
            Text.literal("Имя аккаунта")
        )
        nameField.setMaxLength(16)
        nameField.setSuggestion("Никнейм (3–16 символов)")
        addDrawableChild(nameField)

        addDrawableChild(
            ButtonWidget.builder(Text.literal("Добавить")) {
                val name = nameField.text.trim()
                val added = AltManager.add(name)
                val nextMessage = when {
                    added -> "Аккаунт добавлен и сохранён"
                    !AltManager.isValidName(name) -> "Нужен ник 3–16 символов: A–Z, 0–9, _"
                    else -> "Такой аккаунт уже есть"
                }
                MinecraftClient.getInstance().setScreen(AccountsScreen(if (added) name else selectedName, nextMessage))
            }.dimensions(left + 18, top + 89, 102, 20).build()
        )
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Сохранить")) {
                AltManager.save()
                MinecraftClient.getInstance().setScreen(AccountsScreen(selectedName, "Список аккаунтов сохранён"))
            }.dimensions(left + 128, top + 89, 102, 20).build()
        )

        val accounts = AltManager.getAll()
        accounts.take(6).forEachIndexed { index, name ->
            val y = top + 121 + index * 20
            val label = if (name == selectedName) "✓  $name" else name
            addDrawableChild(
                ButtonWidget.builder(Text.literal(label)) {
                    MinecraftClient.getInstance().setScreen(AccountsScreen(name, message))
                }.dimensions(left + 18, y, panelWidth - 36, 18).build()
            )
        }

        addDrawableChild(
            ButtonWidget.builder(Text.literal("Применить")) {
                val applied = selectedName != null && AltManager.apply(selectedName)
                val nextMessage = when {
                    applied -> "Профиль применён"
                    MinecraftClient.getInstance().world != null -> "Применять профиль можно только в главном меню"
                    else -> "Выбери аккаунт из списка"
                }
                MinecraftClient.getInstance().setScreen(AccountsScreen(selectedName, nextMessage))
            }.dimensions(left + 18, top + 245, 100, 20).build()
        )
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Удалить")) {
                val index = if (selectedName == null) -1 else AltManager.getAll().indexOf(selectedName)
                val removed = index >= 0 && AltManager.remove(index)
                MinecraftClient.getInstance().setScreen(
                    AccountsScreen(null, if (removed) "Аккаунт удалён" else "Сначала выбери аккаунт")
                )
            }.dimensions(left + 126, top + 245, 100, 20).build()
        )
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Назад")) {
                MenuScreen.openGui()
            }.dimensions(left + 234, top + 245, 108, 20).build()
        )
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(0, 0, width, height, 0xE909090B.toInt())
        val panelWidth = 360
        val panelHeight = 286
        val left = (width - panelWidth) / 2
        val top = (height - panelHeight) / 2
        context.fill(left, top, left + panelWidth, top + panelHeight, 0xFF111113.toInt())
        context.drawBorder(left, top, panelWidth, panelHeight, 0xFF343034.toInt())
        context.drawText(textRenderer, "ASTRA / ACCOUNTS", left + 18, top + 14, 0xFFF0EEEE.toInt(), false)
        context.drawText(
            textRenderer,
            "Текущий профиль: " + AltManager.currentNickname(),
            left + 18,
            top + 34,
            0xFF929092.toInt(),
            false
        )
        context.drawText(textRenderer, "СОХРАНЁННЫЕ АККАУНТЫ", left + 18, top + 112, 0xFFE47E82.toInt(), false)
        if (message.isNotBlank()) {
            context.drawText(
                textRenderer,
                textRenderer.trimToWidth(message, panelWidth - 36),
                left + 18,
                top + 270,
                0xFFB7AEB0.toInt(),
                false
            )
        }
        if (AltManager.getAll().isEmpty()) {
            context.drawText(textRenderer, "Список пока пуст", left + 18, top + 137, 0xFF626064.toInt(), false)
        }
        super.render(context, mouseX, mouseY, delta)
    }

    override fun shouldPause(): Boolean = false
}
