package com.murqin.nocroptrample.client;

import com.murqin.nocroptrample.NoCropTrample;
import com.murqin.nocroptrample.config.ConfigSnapshot;
import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Configuration screen for the NoCropTrample mod.
 * <p>
 * Wired up from Fabric via ModMenu and from NeoForge via {@code IConfigScreenFactory}.
 * Adapts to local/singleplayer (editable), remote servers running the mod
 * (read-only live view), legacy servers without sync, and unmodded servers.
 * </p>
 */
public class NoCropTrampleConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_SPACING = 24;
    private static final double SLIDER_MAX_FALL_DISTANCE = 20.0;
    private static final double SLIDER_STEP = 0.5;

    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_NOTICE_TEXT = 0xFFE0E0E0;
    private static final int COLOR_WARNING = 0xFFFFCC55;
    private static final int COLOR_MUTED = 0xFFAAAAAA;

    private final Screen parent;
    private final List<NoticeLine> noticeLines = new ArrayList<>();

    public NoCropTrampleConfigScreen(Screen parent) {
        super(Component.translatable("screen.nocroptrample.title"));
        this.parent = parent;
    }

    private enum ScreenMode {
        LOCAL_EDITABLE,
        REMOTE_SERVER_SYNCED,
        REMOTE_SERVER_LEGACY,
        REMOTE_SERVER_ABSENT
    }

    private record NoticeLine(FormattedCharSequence text, int y, int color) {
    }

    @Override
    protected void init() {
        noticeLines.clear();

        ClientConfigState.setOnUpdateListener(() -> {
            Minecraft mc = this.minecraft;
            if (mc != null) {
                mc.execute(() -> {
                    if (mc.gui.screen() == this) {
                        this.rebuildWidgets();
                    }
                });
            }
        });

        ScreenMode mode = resolveMode();
        switch (mode) {
            case LOCAL_EDITABLE -> initSettingsWidgets(ModConfig.snapshot(), true, 0);
            case REMOTE_SERVER_SYNCED -> {
                ConfigSnapshot serverSnapshot = ClientConfigState.getServerConfig();
                if (serverSnapshot == null) {
                    serverSnapshot = ConfigSnapshot.DEFAULTS;
                }
                int bannerBottomY = layoutWrappedNotice(34, List.of(
                        new StyledText(Component.translatable("screen.nocroptrample.remote.readonly"), COLOR_WARNING),
                        new StyledText(Component.translatable("screen.nocroptrample.remote.command_hint"), COLOR_MUTED)));
                initSettingsWidgets(serverSnapshot, false, bannerBottomY + 6);
            }
            case REMOTE_SERVER_LEGACY -> initMessageOnlyScreen(
                    Component.translatable("screen.nocroptrample.remote.legacy"), COLOR_WARNING);
            case REMOTE_SERVER_ABSENT -> initMessageOnlyScreen(
                    Component.translatable("screen.nocroptrample.remote.not_installed"), COLOR_NOTICE_TEXT);
        }
    }

    private ScreenMode resolveMode() {
        if (this.minecraft == null || this.minecraft.getConnection() == null || this.minecraft.hasSingleplayerServer()) {
            return ScreenMode.LOCAL_EDITABLE;
        }
        if (ClientConfigState.getServerConfig() != null) {
            return ScreenMode.REMOTE_SERVER_SYNCED;
        }
        if (this.minecraft.getConnection().getCommands().getRoot().getChild(NoCropTrample.MOD_ID) != null) {
            return ScreenMode.REMOTE_SERVER_LEGACY;
        }
        return ScreenMode.REMOTE_SERVER_ABSENT;
    }

    private record StyledText(Component text, int color) {
    }

    private int layoutWrappedNotice(int startY, List<StyledText> entries) {
        int maxWidth = Math.max(160, Math.min(340, this.width - 32));
        int y = startY;
        for (StyledText entry : entries) {
            for (FormattedCharSequence line : this.font.split(entry.text(), maxWidth)) {
                noticeLines.add(new NoticeLine(line, y, entry.color()));
                y += this.font.lineHeight + 2;
            }
            y += 2;
        }
        return y;
    }

    private void initMessageOnlyScreen(Component message, int color) {
        int maxWidth = Math.max(160, Math.min(320, this.width - 32));
        List<FormattedCharSequence> wrapped = this.font.split(message, maxWidth);
        int textHeight = wrapped.size() * (this.font.lineHeight + 2);
        int startY = Math.max(48, (this.height - textHeight - 40) / 2);

        int y = startY;
        for (FormattedCharSequence line : wrapped) {
            noticeLines.add(new NoticeLine(line, y, color));
            y += this.font.lineHeight + 2;
        }

        int doneY = Math.min(this.height - 32, Math.max(y + 16, this.height / 2 + 24));
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.done"),
                        button -> this.onClose())
                .bounds(this.width / 2 - 100, doneY, 200, BUTTON_HEIGHT)
                .build());
    }

    private void initSettingsWidgets(ConfigSnapshot snapshot, boolean editable, int minStartY) {
        int centerX = this.width / 2;
        int leftX = centerX - BUTTON_WIDTH / 2;
        int totalBodyHeight = 4 * ROW_SPACING + BUTTON_HEIGHT + 12 + BUTTON_HEIGHT;
        int idealStartY = Math.max(40, (this.height - totalBodyHeight) / 2);
        int maxSafeStartY = Math.max(36, this.height - totalBodyHeight - 10);
        int startY = editable
                ? Math.min(idealStartY, maxSafeStartY)
                : Math.max(minStartY, Math.min(idealStartY, maxSafeStartY));

        Button emptyButton = Button.builder(
                        getBoolOptionText("screen.nocroptrample.option.empty", snapshot.preventEmptyTrampling()),
                        button -> {
                            ModConfig.setPreventEmptyTrampling(!ModConfig.isPreventEmptyTrampling());
                            button.setMessage(getBoolOptionText(
                                    "screen.nocroptrample.option.empty", ModConfig.isPreventEmptyTrampling()));
                        })
                .bounds(leftX, startY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screen.nocroptrample.option.empty.tooltip")))
                .build();
        emptyButton.active = editable;
        this.addRenderableWidget(emptyButton);

        Button playerButton = Button.builder(
                        getBoolOptionText("screen.nocroptrample.option.player", snapshot.preventPlayerTrampling()),
                        button -> {
                            ModConfig.setPreventPlayerTrampling(!ModConfig.isPreventPlayerTrampling());
                            button.setMessage(getBoolOptionText(
                                    "screen.nocroptrample.option.player", ModConfig.isPreventPlayerTrampling()));
                        })
                .bounds(leftX, startY + ROW_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screen.nocroptrample.option.player.tooltip")))
                .build();
        playerButton.active = editable;
        this.addRenderableWidget(playerButton);

        Button mobButton = Button.builder(
                        getBoolOptionText("screen.nocroptrample.option.mob", snapshot.preventMobTrampling()),
                        button -> {
                            ModConfig.setPreventMobTrampling(!ModConfig.isPreventMobTrampling());
                            button.setMessage(getBoolOptionText(
                                    "screen.nocroptrample.option.mob", ModConfig.isPreventMobTrampling()));
                        })
                .bounds(leftX, startY + ROW_SPACING * 2, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screen.nocroptrample.option.mob.tooltip")))
                .build();
        mobButton.active = editable;
        this.addRenderableWidget(mobButton);

        Button featherFallingButton = Button.builder(
                        getFeatherFallingText(snapshot.minFeatherFallingLevel()),
                        button -> {
                            int next = (ModConfig.getMinFeatherFallingLevel() + 1)
                                    % (ConfigSnapshot.MAX_FEATHER_FALLING_LEVEL + 1);
                            ModConfig.setMinFeatherFallingLevel(next);
                            button.setMessage(getFeatherFallingText(ModConfig.getMinFeatherFallingLevel()));
                        })
                .bounds(leftX, startY + ROW_SPACING * 3, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screen.nocroptrample.option.feather_falling.tooltip")))
                .build();
        featherFallingButton.active = editable;
        this.addRenderableWidget(featherFallingButton);

        FallDistanceSlider fallDistanceSlider = new FallDistanceSlider(
                leftX, startY + ROW_SPACING * 4, BUTTON_WIDTH, BUTTON_HEIGHT,
                snapshot.maxSafeFallDistance(), editable);
        fallDistanceSlider.setTooltip(
                Tooltip.create(Component.translatable("screen.nocroptrample.option.fall_distance.tooltip")));
        fallDistanceSlider.active = editable;
        this.addRenderableWidget(fallDistanceSlider);

        int footerY = startY + ROW_SPACING * 4 + BUTTON_HEIGHT + 12;
        if (editable) {
            int halfWidth = (BUTTON_WIDTH - 8) / 2;
            this.addRenderableWidget(Button.builder(
                            Component.translatable("screen.nocroptrample.button.reset"),
                            button -> {
                                ModConfig.reset();
                                this.rebuildWidgets();
                            })
                    .bounds(leftX, footerY, halfWidth, BUTTON_HEIGHT)
                    .build());

            this.addRenderableWidget(Button.builder(
                            Component.translatable("gui.done"),
                            button -> this.onClose())
                    .bounds(leftX + halfWidth + 8, footerY, halfWidth, BUTTON_HEIGHT)
                    .build());
        } else {
            this.addRenderableWidget(Button.builder(
                            Component.translatable("gui.done"),
                            button -> this.onClose())
                    .bounds(centerX - 100, footerY, 200, BUTTON_HEIGHT)
                    .build());
        }
    }

    private static @NonNull Component getBoolOptionText(String key, boolean prevented) {
        Component state = prevented
                ? Component.translatable("screen.nocroptrample.state.prevented").withStyle(ChatFormatting.GREEN)
                : Component.translatable("screen.nocroptrample.state.allowed").withStyle(ChatFormatting.RED);
        return Component.translatable(key, state);
    }

    private static @NonNull Component getFeatherFallingText(int level) {
        Component value = (level <= 0)
                ? Component.translatable("screen.nocroptrample.option.feather_falling.off").withStyle(ChatFormatting.YELLOW)
                : Component.translatable("screen.nocroptrample.option.feather_falling.level", Integer.toString(level))
                        .withStyle(ChatFormatting.AQUA);
        return Component.translatable("screen.nocroptrample.option.feather_falling", value);
    }

    private static @NonNull Component getFallDistanceText(double distance) {
        Component value = (distance <= 0.0)
                ? Component.translatable("screen.nocroptrample.option.fall_distance.unlimited")
                        .withStyle(ChatFormatting.GREEN)
                : Component.translatable("screen.nocroptrample.option.fall_distance.blocks", formatDistance(distance))
                        .withStyle(ChatFormatting.AQUA);
        return Component.translatable("screen.nocroptrample.option.fall_distance", value);
    }

    private static String formatDistance(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-6) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("\\.?0+$", "");
    }

    @Override
    public void extractRenderState(
            @NonNull GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float delta) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, delta);
        guiGraphicsExtractor.centeredText(this.font, this.title, this.width / 2, 16, COLOR_WHITE);
        for (NoticeLine line : noticeLines) {
            guiGraphicsExtractor.centeredText(this.font, line.text(), this.width / 2, line.y(), line.color());
        }
    }

    @Override
    public void removed() {
        ClientConfigState.clearOnUpdateListener();
        super.removed();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }

    private static final class FallDistanceSlider extends AbstractSliderButton {
        private final boolean editable;
        private double displayDistance;

        private FallDistanceSlider(int x, int y, int width, int height, double initialDistance, boolean editable) {
            super(x, y, width, height, getFallDistanceText(initialDistance), toNormalized(initialDistance));
            this.editable = editable;
            this.displayDistance = initialDistance;
        }

        private static double toNormalized(double distance) {
            return Mth.clamp(distance, 0.0, SLIDER_MAX_FALL_DISTANCE) / SLIDER_MAX_FALL_DISTANCE;
        }

        private static double fromNormalized(double normalized) {
            double raw = Mth.clamp(normalized, 0.0, 1.0) * SLIDER_MAX_FALL_DISTANCE;
            return Math.round(raw / SLIDER_STEP) * SLIDER_STEP;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(getFallDistanceText(this.displayDistance));
        }

        @Override
        protected void applyValue() {
            double snapped = fromNormalized(this.value);
            this.displayDistance = snapped;
            if (this.editable && Double.compare(snapped, ModConfig.getMaxSafeFallDistance()) != 0) {
                ModConfig.setMaxSafeFallDistance(snapped);
            }
        }
    }
}
