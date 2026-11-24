// NamePoolScreen — open with M key; manage name pool + config strings
package com.sparkynox.lunofucked.gui;

import com.sparkynox.lunofucked.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class NamePoolScreen extends Screen {

    private final Screen parent;

    // name pool list scroll
    private int scrollOffset = 0;
    private static final int ROW_HEIGHT = 14;
    private static final int LIST_TOP   = 60;
    private static final int LIST_BOT_MARGIN = 100; // space for config fields below

    // input for adding a new name
    private TextFieldWidget addNameField;

    // config fields
    private TextFieldWidget tpaTargetField;
    private TextFieldWidget loginPassField;
    private TextFieldWidget redeemCodeField;

    public NamePoolScreen(Screen parent) {
        super(Text.literal("§b[LunoFucked] §fName Pool & Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int listBottom = height - LIST_BOT_MARGIN;

        // ── add name field + button ──────────────────────────────────────────
        addNameField = new TextFieldWidget(textRenderer, cx - 100, listBottom + 6, 150, 16,
                Text.literal("New name…"));
        addNameField.setMaxLength(16);
        addNameField.setPlaceholder(Text.literal("New name…"));
        addDrawableChild(addNameField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> {
            String name = addNameField.getText().trim();
            if (!name.isEmpty() && name.matches("[a-zA-Z0-9_]+") && !ModConfig.namePool.contains(name)) {
                ModConfig.namePool.add(name);
                ModConfig.save();
                addNameField.setText("");
            }
        }).dimensions(cx + 54, listBottom + 6, 44, 16).build());

        // ── config string fields ──────────────────────────────────────────────
        int fy = listBottom + 28;

        tpaTargetField = new TextFieldWidget(textRenderer, cx - 100, fy, 200, 16,
                Text.literal("TPA Target"));
        tpaTargetField.setText(ModConfig.tpaTarget);
        tpaTargetField.setMaxLength(32);
        addDrawableChild(tpaTargetField);

        fy += 22;
        loginPassField = new TextFieldWidget(textRenderer, cx - 100, fy, 200, 16,
                Text.literal("Login Password"));
        loginPassField.setText(ModConfig.loginPassword);
        loginPassField.setMaxLength(64);
        addDrawableChild(loginPassField);

        fy += 22;
        redeemCodeField = new TextFieldWidget(textRenderer, cx - 100, fy, 200, 16,
                Text.literal("Redeem Code"));
        redeemCodeField.setText(ModConfig.redeemCode);
        redeemCodeField.setMaxLength(64);
        addDrawableChild(redeemCodeField);

        // ── save + back ───────────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), btn -> saveAndClose())
                .dimensions(cx - 50, height - 22, 100, 18).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        // title
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 8, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Press §fDelete §7on a name to remove it"),
                width / 2, 20, 0xAAAAAA);

        // name pool list
        int listBottom = height - LIST_BOT_MARGIN;
        int visibleRows = (listBottom - LIST_TOP) / ROW_HEIGHT;
        List<String> pool = ModConfig.namePool;

        ctx.fill(width / 2 - 110, LIST_TOP - 2, width / 2 + 110, listBottom, 0x88000000);

        for (int i = scrollOffset; i < Math.min(pool.size(), scrollOffset + visibleRows); i++) {
            int rowY = LIST_TOP + (i - scrollOffset) * ROW_HEIGHT;
            String name = pool.get(i);
            boolean hovered = mouseX >= width / 2 - 108 && mouseX <= width / 2 + 108
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (hovered) ctx.fill(width / 2 - 108, rowY, width / 2 + 108, rowY + ROW_HEIGHT, 0x44FFFFFF);
            ctx.drawTextWithShadow(textRenderer, Text.literal("§f" + (i + 1) + ". §b" + name),
                    width / 2 - 106, rowY + 2, 0xFFFFFF);
        }

        // section labels
        int fy = listBottom + 28;
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7TPA Target:"),          width / 2 - 100, fy - 10, 0xAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Login Password:"),   width / 2 - 100, fy + 12, 0xAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Redeem Code:"),         width / 2 - 100, fy + 34, 0xAAAAAA);

        // fields
        addNameField.render(ctx, mouseX, mouseY, delta);
        tpaTargetField.render(ctx, mouseX, mouseY, delta);
        loginPassField.render(ctx, mouseX, mouseY, delta);
        redeemCodeField.render(ctx, mouseX, mouseY, delta);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizAmount, double vertAmount) {
        int listBottom = height - LIST_BOT_MARGIN;
        int visibleRows = (listBottom - LIST_TOP) / ROW_HEIGHT;
        int maxScroll = Math.max(0, ModConfig.namePool.size() - visibleRows);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - vertAmount));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // click on a name row + right click = delete
        int listBottom = height - LIST_BOT_MARGIN;
        int visibleRows = (listBottom - LIST_TOP) / ROW_HEIGHT;
        if (mouseX >= width / 2 - 108 && mouseX <= width / 2 + 108
                && mouseY >= LIST_TOP && mouseY < listBottom && button == 1) {
            int idx = scrollOffset + (int)(mouseY - LIST_TOP) / ROW_HEIGHT;
            if (idx >= 0 && idx < ModConfig.namePool.size()) {
                ModConfig.namePool.remove(idx);
                ModConfig.save();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Delete key on focused: handled via mouseClicked (right-click); keep default
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void saveAndClose() {
        ModConfig.tpaTarget         = tpaTargetField.getText().trim();
        ModConfig.loginPassword  = loginPassField.getText().trim();
        ModConfig.redeemCode        = redeemCodeField.getText().trim();
        ModConfig.save();
        close();
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
