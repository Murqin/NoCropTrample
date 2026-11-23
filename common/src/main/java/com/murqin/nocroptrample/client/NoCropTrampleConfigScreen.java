package com.murqin.nocroptrample.client;

import com.murqin.nocroptrample.ModGameRules;
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
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Configuration screen for the NoCropTrample mod.
 * <p>
 * Wired up from Fabric via ModMenu and from NeoForge via {@code IConfigScreenFactory}.
 * Adapts to title screen defaults ({@code nocroptrample.json}), active singleplayer
 * world GameRules, remote servers running the mod (read-only live view),
 * legacy servers without sync, and unmodded servers.
 * </p>
 */
public class NoCropTrampleConfigScreen extends Screen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_SPACING = 24;

    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_NOTICE_TEXT = 0xFFE0E0E0;
    private static final int COLOR_WARNING = 0xFFFFCC55;
    private static final int COLOR_MUTED = 0xFFAAAAAA;

    private final Screen parent;
    private final List<NoticeLine> noticeLines = new ArrayList<>();
    private ScreenMode activeMode = ScreenMode.TITLE_DEFAULTS;
    private ConfigSnapshot workingSnapshot = ConfigSnapshot.DEFAULTS;
    private int titleY = 14;
    private int localEditSeq = 0;

    private Button emptyButton;
    private Button playerButton;
    private Button mobButton;
    private Button featherFallingButton;
    private FallDistanceSlider fallDistanceSlider;
    private Runnable updateListener;

    public NoCropTrampleConfigScreen(Screen parent) {
        super(Component.translatable("screen.nocroptrample.title"));
        this.parent = parent;
    }

    private enum ScreenMode {
        TITLE_DEFAULTS,
        SINGLEPLAYER_WORLD,
        REMOTE_SERVER_SYNCED,
        REMOTE_SERVER_LEGACY,
        REMOTE_SERVER_ABSENT
    }

    private record NoticeLine(FormattedCharSequence text, int y, int color) {
    }

    private record StyledText(Component text, int color) {
    }

    @Override
    protected void init() {
        noticeLines.clear();
        this.emptyButton = null;
        this.playerButton = null;
        this.mobButton = null;
        this.featherFallingButton = null;
        this.fallDistanceSlider = null;
        ClientConfigState.clearOnUpdateListener(this.updateListener);
        this.updateListener = null;

        boolean compact = this.height <= 240;
        this.titleY = compact ? 8 : 14;
        int noticeStartY = compact ? 22 : 32;

        this.activeMode = resolveMode();
        switch (this.activeMode) {
            case TITLE_DEFAULTS -> {
                this.workingSnapshot = ModConfig.snapshot();
                int hintBottomY = layoutWrappedNotice(noticeStartY, compact, List.of(
                        new StyledText(Component.translatable("screen.nocroptrample.local.defaults_hint"), COLOR_MUTED)));
                initSettingsWidgets(this.workingSnapshot, true, hintBottomY + (compact ? 2 : 4), compact);
            }
            case SINGLEPLAYER_WORLD -> {
                ConfigSnapshot cached = ClientConfigState.getServerConfig();
                this.workingSnapshot = cached != null ? cached : ModConfig.snapshot();
                int hintBottomY = layoutWrappedNotice(noticeStartY, compact, List.of(
                        new StyledText(Component.translatable("screen.nocroptrample.local.world_hint"), COLOR_MUTED)));
                initSettingsWidgets(this.workingSnapshot, true, hintBottomY + (compact ? 2 : 4), compact);
                requestSingleplayerSnapshotSync();
                registerRemoteUpdateListener();
            }
            case REMOTE_SERVER_SYNCED -> {
                ConfigSnapshot serverSnapshot = ClientConfigState.getServerConfig();
                this.workingSnapshot = serverSnapshot != null ? serverSnapshot : ConfigSnapshot.DEFAULTS;
                int bannerBottomY = layoutWrappedNotice(noticeStartY, compact, List.of(
                        new StyledText(Component.translatable("screen.nocroptrample.remote.readonly"), COLOR_WARNING),
                        new StyledText(Component.translatable("screen.nocroptrample.remote.command_hint"), COLOR_MUTED)));
                initSettingsWidgets(this.workingSnapshot, false, bannerBottomY + (compact ? 2 : 4), compact);
                registerRemoteUpdateListener();
            }
            case REMOTE_SERVER_LEGACY -> {
                initMessageOnlyScreen(
                        Component.translatable("screen.nocroptrample.remote.legacy"), COLOR_WARNING);
                registerRemoteUpdateListener();
            }
            case REMOTE_SERVER_ABSENT -> {
                initMessageOnlyScreen(
                        Component.translatable("screen.nocroptrample.remote.not_installed"), COLOR_NOTICE_TEXT);
                registerRemoteUpdateListener();
            }
        }
    }

    private void registerRemoteUpdateListener() {
        Runnable listener = () -> {
            Minecraft mc = this.minecraft;
            if (mc == null) {
                return;
            }
            mc.execute(() -> {
                if (mc.gui.screen() != this) {
                    return;
                }
                if (this.activeMode == ScreenMode.REMOTE_SERVER_LEGACY
                        || this.activeMode == ScreenMode.REMOTE_SERVER_ABSENT) {
                    if (ClientConfigState.getServerConfig() != null) {
                        this.rebuildWidgets();
                    }
                    return;
                }
                if (this.activeMode == ScreenMode.REMOTE_SERVER_SYNCED
                        || this.activeMode == ScreenMode.SINGLEPLAYER_WORLD) {
                    ConfigSnapshot incoming = ClientConfigState.getServerConfig();
                    if (incoming != null && !incoming.equals(this.workingSnapshot)) {
                        applyExternalSnapshot(incoming);
                    }
                }
            });
        };
        this.updateListener = listener;
        ClientConfigState.setOnUpdateListener(listener);
    }

    private ScreenMode resolveMode() {
        if (this.minecraft == null || this.minecraft.getConnection() == null) {
            return ScreenMode.TITLE_DEFAULTS;
        }
        if (this.minecraft.hasSingleplayerServer()) {
            return ScreenMode.SINGLEPLAYER_WORLD;
        }
        if (ClientConfigState.getServerConfig() != null) {
            return ScreenMode.REMOTE_SERVER_SYNCED;
        }
        if (this.minecraft.getConnection().getCommands().getRoot().getChild(NoCropTrample.MOD_ID) != null) {
            return ScreenMode.REMOTE_SERVER_LEGACY;
        }
        return ScreenMode.REMOTE_SERVER_ABSENT;
    }

    private void requestSingleplayerSnapshotSync() {
        Minecraft mc = this.minecraft;
        if (mc == null) {
            return;
        }
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null) {
            return;
        }
        int seqAtRequest = this.localEditSeq;
        server.execute(() -> {
            ConfigSnapshot snap = ModGameRules.snapshot(server.getGameRules());
            mc.execute(() -> {
                if (mc.gui.screen() == this
                        && this.activeMode == ScreenMode.SINGLEPLAYER_WORLD
                        && this.localEditSeq == seqAtRequest
                        && !snap.equals(this.workingSnapshot)) {
                    applyExternalSnapshot(snap);
                }
            });
        });
    }

    private void applyExternalSnapshot(ConfigSnapshot incoming) {
        this.workingSnapshot = incoming;
        syncWidgetsFromSnapshot(incoming, false);
    }

    private void syncWidgetsFromSnapshot(ConfigSnapshot snapshot, boolean forceSlider) {
        if (this.emptyButton != null) {
            this.emptyButton.setMessage(getBoolOptionText("screen.nocroptrample.option.empty", snapshot.preventEmptyTrampling()));
        }
        if (this.playerButton != null) {
            this.playerButton.setMessage(getBoolOptionText("screen.nocroptrample.option.player", snapshot.preventPlayerTrampling()));
        }
        if (this.mobButton != null) {
            this.mobButton.setMessage(getBoolOptionText("screen.nocroptrample.option.mob", snapshot.preventMobTrampling()));
        }
        if (this.featherFallingButton != null) {
            this.featherFallingButton.setMessage(getFeatherFallingText(snapshot.minFeatherFallingLevel()));
        }
        if (this.fallDistanceSlider != null) {
            this.fallDistanceSlider.syncFromSnapshot(snapshot.maxSafeFallDistance(), forceSlider);
        }
    }

    private void flushPendingSlider() {
        if (this.fallDistanceSlider != null) {
            this.fallDistanceSlider.commitPending();
        }
    }

    private void commitSingleChange(ConfigSnapshot updated, Consumer<IntegratedServer> serverMutator) {
        this.workingSnapshot = updated;
        this.localEditSeq++;
        switch (this.activeMode) {
            case TITLE_DEFAULTS -> ModConfig.applyAndSave(updated);
            case SINGLEPLAYER_WORLD -> {
                if (this.minecraft != null) {
                    IntegratedServer server = this.minecraft.getSingleplayerServer();
                    if (server != null) {
                        server.execute(() -> serverMutator.accept(server));
                    }
                }
            }
            case REMOTE_SERVER_SYNCED, REMOTE_SERVER_LEGACY, REMOTE_SERVER_ABSENT -> {
            }
        }
    }

    private void applyResetDefaults() {
        this.workingSnapshot = ConfigSnapshot.DEFAULTS;
        this.localEditSeq++;
        switch (this.activeMode) {
            case TITLE_DEFAULTS -> ModConfig.reset();
            case SINGLEPLAYER_WORLD -> {
                if (this.minecraft != null) {
                    IntegratedServer server = this.minecraft.getSingleplayerServer();
                    if (server != null) {
                        server.execute(() -> ModGameRules.applySnapshot(server, ConfigSnapshot.DEFAULTS));
                    }
                }
            }
            case REMOTE_SERVER_SYNCED, REMOTE_SERVER_LEGACY, REMOTE_SERVER_ABSENT -> {
            }
        }
        syncWidgetsFromSnapshot(ConfigSnapshot.DEFAULTS, true);
    }

    private int layoutWrappedNotice(int startY, boolean compact, List<StyledText> entries) {
        int maxWidth = Math.max(160, Math.min(340, this.width - 24));
        int lineStep = this.font.lineHeight + (compact ? 1 : 2);
        int y = startY;
        for (StyledText entry : entries) {
            for (FormattedCharSequence line : this.font.split(entry.text(), maxWidth)) {
                noticeLines.add(new NoticeLine(line, y, entry.color()));
                y += lineStep;
            }
            y += compact ? 1 : 2;
        }
        return y;
    }

    private void initMessageOnlyScreen(Component message, int color) {
        int maxWidth = Math.max(160, Math.min(320, this.width - 24));
        List<FormattedCharSequence> wrapped = this.font.split(message, maxWidth);
        int lineStep = this.font.lineHeight + 2;
        int textHeight = wrapped.size() * lineStep;
        int startY = Math.max(28, (this.height - textHeight - 36) / 2);

        int y = startY;
        for (FormattedCharSequence line : wrapped) {
            noticeLines.add(new NoticeLine(line, y, color));
            y += lineStep;
        }

        int doneY = Math.min(this.height - 26, Math.max(y + 12, this.height / 2 + 20));
        int doneWidth = Math.min(200, Math.max(140, this.width - 24));
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.done"),
                        button -> this.onClose())
                .bounds(this.width / 2 - doneWidth / 2, doneY, doneWidth, BUTTON_HEIGHT)
                .build());
    }

    private void initSettingsWidgets(ConfigSnapshot snapshot, boolean editable, int minStartY, boolean compact) {
        int buttonWidth = Math.max(180, Math.min(250, this.width - 24));
        int rowSpacing = compact ? 21 : ROW_SPACING;
        int footerGap = compact ? 4 : 10;
        int totalBodyHeight = 4 * rowSpacing + BUTTON_HEIGHT + footerGap + BUTTON_HEIGHT;

        if (minStartY + totalBodyHeight > this.height - 4) {
            rowSpacing = 21;
            footerGap = 4;
            totalBodyHeight = 4 * rowSpacing + BUTTON_HEIGHT + footerGap + BUTTON_HEIGHT;
        }

        int centerX = this.width / 2;
        int leftX = centerX - buttonWidth / 2;
        int idealStartY = Math.max(compact ? 28 : 40, (this.height - totalBodyHeight) / 2);
        int maxSafeStartY = Math.max(compact ? 24 : 36, this.height - totalBodyHeight - 4);
        int startY = Math.max(minStartY, Math.min(idealStartY, maxSafeStartY));

        this.emptyButton = Button.builder(
                        getBoolOptionText("screen.nocroptrample.option.empty", snapshot.preventEmptyTrampling()),
                        button -> {
                            flushPendingSlider();
                            boolean next = !this.workingSnapshot.preventEmptyTrampling();
                            ConfigSnapshot updated = new ConfigSnapshot(
                                    next,
                                    this.workingSnapshot.preventPlayerTrampling(),
                                    this.workingSnapshot.preventMobTrampling(),
                                    this.workingSnapshot.minFeatherFallingLevel(),
                                    this.workingSnapshot.maxSafeFallDistance());
                            commitSingleChange(updated, server -> ModGameRules.setPreventEmptyTrampling(server, next));
                            button.setMessage(getBoolOptionText("screen.nocroptrample.option.empty", next));
                        })
                .bounds(leftX, startY, buttonWidth, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screen.nocroptrample.option.empty.tooltip")))
                .build();
        this.emptyButton.active = editable;
        this.addRenderableWidget(this.emptyButton);

        this.playerButton = Button.builder(
                        getBoolOptionText("screen.nocroptrample.option.player", snapshot.preventPlayerTrampling()),
                        button -> {
                            flushPendingSlider();
                            boolean next = !this.workingSnapshot.preventPlayerTrampling();
                            ConfigSnapshot updated = new ConfigSnapshot(
                                    this.workingSnapshot.preventEmptyTrampling(),
                                    next,
                                    this.workingSnapshot.preventMobTrampling(),
                                    this.workingSnapshot.minFeatherFallingLevel(),
                                    this.workingSnapshot.maxSafeFallDistance());
                            commitSingleChange(updated, server -> ModGameRules.setPreventPlayerTrampling(server, next));
                            button.setMessage(getBoolOptionText("screen.nocroptrample.option.player", next));
                        })
                .bounds(leftX, startY + rowSpacing, buttonWidth, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screen.nocroptrample.option.player.tooltip")))
                .build();
        this.playerButton.active = editable;
        this.addRenderableWidget(this.playerButton);

        this.mobButton = Button.builder(
                        getBoolOptionText("screen.nocroptrample.option.mob", snapshot.preventMobTrampling()),
                        button -> {
                            flushPendingSlider();
                            boolean next = !this.workingSnapshot.preventMobTrampling();
                            ConfigSnapshot updated = new ConfigSnapshot(
                                    this.workingSnapshot.preventEmptyTrampling(),
                                    this.workingSnapshot.preventPlayerTrampling(),
                                    next,
                                    this.workingSnapshot.minFeatherFallingLevel(),
                                    this.workingSnapshot.maxSafeFallDistance());
                            commitSingleChange(updated, server -> ModGameRules.setPreventMobTrampling(server, next));
                            button.setMessage(getBoolOptionText("screen.nocroptrample.option.mob", next));
                        })
                .bounds(leftX, startY + rowSpacing * 2, buttonWidth, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screen.nocroptrample.option.mob.tooltip")))
                .build();
        this.mobButton.active = editable;
        this.addRenderableWidget(this.mobButton);

        this.featherFallingButton = Button.builder(
                        getFeatherFallingText(snapshot.minFeatherFallingLevel()),
                        button -> {
                            flushPendingSlider();
                            int next = (this.workingSnapshot.minFeatherFallingLevel() + 1)
                                    % (ConfigSnapshot.MAX_FEATHER_FALLING_LEVEL + 1);
                            ConfigSnapshot updated = new ConfigSnapshot(
                                    this.workingSnapshot.preventEmptyTrampling(),
                                    this.workingSnapshot.preventPlayerTrampling(),
                                    this.workingSnapshot.preventMobTrampling(),
                                    next,
                                    this.workingSnapshot.maxSafeFallDistance());
                            commitSingleChange(updated, server -> ModGameRules.setMinFeatherFallingLevel(server, next));
                            button.setMessage(getFeatherFallingText(next));
                        })
                .bounds(leftX, startY + rowSpacing * 3, buttonWidth, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screen.nocroptrample.option.feather_falling.tooltip")))
                .build();
        this.featherFallingButton.active = editable;
        this.addRenderableWidget(this.featherFallingButton);

        FallDistanceSlider slider = new FallDistanceSlider(
                leftX, startY + rowSpacing * 4, buttonWidth, BUTTON_HEIGHT,
                snapshot.maxSafeFallDistance(), editable,
                newDistance -> {
                    ConfigSnapshot updated = new ConfigSnapshot(
                            this.workingSnapshot.preventEmptyTrampling(),
                            this.workingSnapshot.preventPlayerTrampling(),
                            this.workingSnapshot.preventMobTrampling(),
                            this.workingSnapshot.minFeatherFallingLevel(),
                            newDistance);
                    commitSingleChange(updated, server -> ModGameRules.setMaxSafeFallDistance(server, newDistance));
                });
        slider.setTooltip(
                Tooltip.create(Component.translatable("screen.nocroptrample.option.fall_distance.tooltip")));
        slider.active = editable;
        this.fallDistanceSlider = slider;
        this.addRenderableWidget(slider);

        int footerY = startY + rowSpacing * 4 + BUTTON_HEIGHT + footerGap;
        if (editable) {
            int halfWidth = (buttonWidth - 8) / 2;
            this.addRenderableWidget(Button.builder(
                            Component.translatable("screen.nocroptrample.button.reset"),
                            button -> {
                                if (this.fallDistanceSlider != null) {
                                    this.fallDistanceSlider.discardPending();
                                }
                                applyResetDefaults();
                            })
                    .bounds(leftX, footerY, halfWidth, BUTTON_HEIGHT)
                    .build());

            this.addRenderableWidget(Button.builder(
                            Component.translatable("gui.done"),
                            button -> this.onClose())
                    .bounds(leftX + halfWidth + 8, footerY, halfWidth, BUTTON_HEIGHT)
                    .build());
        } else {
            int doneWidth = Math.min(200, buttonWidth);
            this.addRenderableWidget(Button.builder(
                            Component.translatable("gui.done"),
                            button -> this.onClose())
                    .bounds(centerX - doneWidth / 2, footerY, doneWidth, BUTTON_HEIGHT)
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

    private static @NonNull Component getFallDistanceText(int distance) {
        Component value = (distance <= 0)
                ? Component.translatable("screen.nocroptrample.option.fall_distance.unlimited")
                        .withStyle(ChatFormatting.GREEN)
                : Component.translatable("screen.nocroptrample.option.fall_distance.blocks", Integer.toString(distance))
                        .withStyle(ChatFormatting.AQUA);
        return Component.translatable("screen.nocroptrample.option.fall_distance", value);
    }

    @Override
    public void extractRenderState(
            @NonNull GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float delta) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, delta);
        guiGraphicsExtractor.centeredText(this.font, this.title, this.width / 2, this.titleY, COLOR_WHITE);
        for (NoticeLine line : noticeLines) {
            guiGraphicsExtractor.centeredText(this.font, line.text(), this.width / 2, line.y(), line.color());
        }
    }

    @Override
    public void removed() {
        if (this.fallDistanceSlider != null) {
            this.fallDistanceSlider.commitPending();
            this.fallDistanceSlider = null;
        }
        ClientConfigState.clearOnUpdateListener(this.updateListener);
        this.updateListener = null;
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
        private final IntConsumer onChange;
        private int displayDistance;
        private int committedDistance;
        private boolean dragging;

        private FallDistanceSlider(
                int x, int y, int width, int height,
                int initialDistance, boolean editable, IntConsumer onChange) {
            super(x, y, width, height, getFallDistanceText(initialDistance), toNormalized(initialDistance));
            this.editable = editable;
            this.onChange = onChange;
            this.displayDistance = initialDistance;
            this.committedDistance = initialDistance;
        }

        private static double toNormalized(int distance) {
            return Mth.clamp(distance, 0, ConfigSnapshot.MAX_SAFE_FALL_DISTANCE)
                    / (double) ConfigSnapshot.MAX_SAFE_FALL_DISTANCE;
        }

        private static int fromNormalized(double normalized) {
            return (int) Math.round(Mth.clamp(normalized, 0.0, 1.0) * ConfigSnapshot.MAX_SAFE_FALL_DISTANCE);
        }

        private void commitPending() {
            if (this.editable && this.displayDistance != this.committedDistance) {
                this.committedDistance = this.displayDistance;
                this.onChange.accept(this.displayDistance);
            }
        }

        private void discardPending() {
            this.committedDistance = this.displayDistance;
        }

        private void syncFromSnapshot(int distance, boolean force) {
            if (!force && (this.dragging || this.canChangeValue)) {
                return;
            }
            if (force) {
                this.dragging = false;
                this.canChangeValue = false;
            }
            this.displayDistance = distance;
            this.committedDistance = distance;
            this.value = toNormalized(distance);
            this.updateMessage();
        }

        @Override
        public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
            this.dragging = this.active;
            super.onClick(event, doubleClick);
        }

        @Override
        public void onRelease(@NonNull MouseButtonEvent event) {
            this.dragging = false;
            super.onRelease(event);
            commitPending();
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (!focused) {
                this.dragging = false;
                commitPending();
            }
        }

        @Override
        public boolean keyPressed(@NonNull KeyEvent event) {
            if (event.isSelection()) {
                this.canChangeValue = !this.canChangeValue;
                if (!this.canChangeValue) {
                    commitPending();
                }
                return true;
            }
            if (this.canChangeValue && this.editable && (event.isLeft() || event.isRight())) {
                int delta = event.isLeft() ? -1 : 1;
                int next = Mth.clamp(this.displayDistance + delta, 0, ConfigSnapshot.MAX_SAFE_FALL_DISTANCE);
                this.setValue(toNormalized(next));
                return true;
            }
            return false;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(getFallDistanceText(this.displayDistance));
        }

        @Override
        protected void applyValue() {
            this.displayDistance = fromNormalized(this.value);
        }
    }
}
