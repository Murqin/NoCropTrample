package com.murqin.nocroptrample;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;

/**
 * Mod Menu integration for NoCropTrample configuration screen.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return NoCropTrampleConfigScreen::new;
    }

    /**
     * Configuration screen for the NoCropTrample mod.
     * <p>
     * Content is laid out via a scrollable {@link GridLayout} inside a
     * {@link HeaderAndFooterLayout} so the screen stays usable regardless of window size
     * or GUI scale - fixed pixel coordinates would push later rows (and the Done button)
     * off-screen once the widget count grows.
     * </p>
     */
    public static class NoCropTrampleConfigScreen extends Screen {
        private static final int BUTTON_WIDTH = 150;
        private static final int BUTTON_HEIGHT = 20;

        private final Screen parent;
        private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

        private Button presetButton;
        private Button emptyButton;
        private Button playerButton;
        private Button mobButton;
        private Button featherFallingButton;
        private Button leatherBootsButton;
        private Button petButton;
        private Button villagerButton;
        private Button dehydrationButton;
        private Button emptyReversionButton;
        private Button particlesButton;
        private Button soundButton;
        private Button actionBarButton;

        public NoCropTrampleConfigScreen(Screen parent) {
            super(Component.translatable("nocroptrample.config.title"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            this.layout.addTitleHeader(this.title, this.font);

            this.presetButton = this.layout.addToHeader(
                    Button.builder(getPresetButtonText(), this::cyclePreset)
                            .size(200, BUTTON_HEIGHT)
                            .build(),
                    settings -> settings.alignHorizontallyCenter());

            if (isConnectedToRemoteServer()) {
                this.layout.addToHeader(
                        new StringWidget(Component.translatable("nocroptrample.config.warning.remote_server"), this.font)
                                .setMaxWidth(320),
                        settings -> settings.alignHorizontallyCenter());
            }

            this.playerButton = Button.builder(getPlayerButtonText(), this::togglePlayerTrampling)
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            this.featherFallingButton = Button.builder(getFeatherFallingButtonText(), this::cycleFeatherFalling)
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            this.leatherBootsButton = Button.builder(getLeatherBootsButtonText(), this::toggleLeatherBoots)
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();

            this.mobButton = Button.builder(getMobButtonText(), this::toggleMobTrampling)
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            this.petButton = Button.builder(getPetButtonText(), this::togglePetTrampling)
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            this.villagerButton = Button.builder(getVillagerButtonText(), this::toggleVillagerTrampling)
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            this.emptyButton = Button.builder(getEmptyButtonText(), this::toggleEmptyTrampling)
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();

            this.dehydrationButton = Button.builder(getDehydrationButtonText(), this::toggleDehydration)
                    .tooltip(Tooltip.create(Component.translatable("nocroptrample.config.button.dehydration.tooltip")))
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            this.emptyReversionButton = Button.builder(getEmptyReversionButtonText(), this::toggleEmptyReversion)
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();

            this.particlesButton = Button.builder(getParticlesButtonText(), this::toggleParticles)
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            this.soundButton = Button.builder(getSoundButtonText(), this::toggleSound)
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            this.actionBarButton = Button.builder(getActionBarButtonText(), this::toggleActionBar)
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();

            LinearLayout content = LinearLayout.vertical().spacing(10);
            content.addChild(buildCategory("nocroptrample.config.category.player_footwear", this.playerButton, this.featherFallingButton, this.leatherBootsButton));
            content.addChild(buildCategory("nocroptrample.config.category.entity_protection", this.mobButton, this.petButton, this.villagerButton, this.emptyButton));
            content.addChild(buildCategory("nocroptrample.config.category.farmland_maintenance", this.dehydrationButton, this.emptyReversionButton));
            content.addChild(buildCategory("nocroptrample.config.category.visual_sound", this.particlesButton, this.soundButton, this.actionBarButton));

            ScrollableLayout scrollable = new ScrollableLayout(this.minecraft, content, this.layout.getContentHeight());
            this.layout.addToContents(scrollable);

            this.layout.addToFooter(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                    .size(200, BUTTON_HEIGHT)
                    .build());

            this.layout.visitWidgets(this::addRenderableWidget);
            this.repositionElements();
        }

        @Override
        protected void repositionElements() {
            this.layout.arrangeElements();
        }

        /**
         * Builds a titled, two-column group of toggle buttons.
         *
         * @param categoryTitleKey the translation key for the header shown above the buttons
         * @param buttons          the buttons belonging to this category
         * @return a vertical layout containing the header and a button grid
         */
        private LinearLayout buildCategory(String categoryTitleKey, Button... buttons) {
            LinearLayout category = LinearLayout.vertical().spacing(4);
            category.addChild(
                    new StringWidget(Component.translatable(categoryTitleKey).withStyle(ChatFormatting.YELLOW), this.font),
                    settings -> settings.alignHorizontallyCenter());

            GridLayout grid = new GridLayout().columnSpacing(10).rowSpacing(4);
            GridLayout.RowHelper rowHelper = grid.createRowHelper(2);
            for (Button button : buttons) {
                rowHelper.addChild(button);
            }
            category.addChild(grid, settings -> settings.alignHorizontallyCenter());

            return category;
        }

        private boolean isConnectedToRemoteServer() {
            return this.minecraft != null && this.minecraft.level != null && !this.minecraft.hasSingleplayerServer();
        }

        private void updateButtonLabels() {
            if (presetButton != null) presetButton.setMessage(getPresetButtonText());
            if (emptyButton != null) emptyButton.setMessage(getEmptyButtonText());
            if (playerButton != null) playerButton.setMessage(getPlayerButtonText());
            if (mobButton != null) mobButton.setMessage(getMobButtonText());
            if (featherFallingButton != null) featherFallingButton.setMessage(getFeatherFallingButtonText());
            if (leatherBootsButton != null) leatherBootsButton.setMessage(getLeatherBootsButtonText());
            if (petButton != null) petButton.setMessage(getPetButtonText());
            if (villagerButton != null) villagerButton.setMessage(getVillagerButtonText());
            if (dehydrationButton != null) dehydrationButton.setMessage(getDehydrationButtonText());
            if (emptyReversionButton != null) emptyReversionButton.setMessage(getEmptyReversionButtonText());
            if (particlesButton != null) particlesButton.setMessage(getParticlesButtonText());
            if (soundButton != null) soundButton.setMessage(getSoundButtonText());
            if (actionBarButton != null) actionBarButton.setMessage(getActionBarButtonText());
        }

        private void cyclePreset(Button button) {
            ModConfig.ModPreset current = ModConfig.getActivePreset();
            ModConfig.ModPreset next = switch (current) {
                case VANILLA_PLUS -> ModConfig.ModPreset.CASUAL;
                case CASUAL -> ModConfig.ModPreset.HARDCORE;
                case HARDCORE -> ModConfig.ModPreset.CUSTOM;
                case CUSTOM -> ModConfig.ModPreset.VANILLA_PLUS;
            };

            if (current == ModConfig.ModPreset.CUSTOM && this.minecraft != null) {
                // Leaving CUSTOM overwrites whatever the player fine-tuned with the next preset's
                // values, so confirm first instead of silently discarding it.
                this.minecraft.setScreenAndShow(new ConfirmScreen(
                        confirmed -> {
                            this.minecraft.setScreenAndShow(this);
                            if (confirmed) {
                                applyPreset(next);
                            }
                        },
                        Component.translatable("nocroptrample.config.confirm.overwrite_custom.title"),
                        Component.translatable("nocroptrample.config.confirm.overwrite_custom.message")));
                return;
            }

            applyPreset(next);
        }

        private void applyPreset(ModConfig.ModPreset preset) {
            ModConfig.setPreset(preset);
            updateButtonLabels();
        }

        private void toggleEmptyTrampling(Button button) {
            ModConfig.setPreventEmptyTrampling(!ModConfig.isPreventEmptyTrampling());
            updateButtonLabels();
        }

        private void togglePlayerTrampling(Button button) {
            ModConfig.setPreventPlayerTrampling(!ModConfig.isPreventPlayerTrampling());
            updateButtonLabels();
        }

        private void toggleMobTrampling(Button button) {
            ModConfig.setPreventMobTrampling(!ModConfig.isPreventMobTrampling());
            updateButtonLabels();
        }

        private void cycleFeatherFalling(Button button) {
            ModConfig.FeatherFallingMode current = ModConfig.getFeatherFallingMode();
            ModConfig.FeatherFallingMode next = switch (current) {
                case ALWAYS -> ModConfig.FeatherFallingMode.REQUIRE_FEATHER_FALLING;
                case REQUIRE_FEATHER_FALLING -> ModConfig.FeatherFallingMode.SCALED_BY_LEVEL;
                case SCALED_BY_LEVEL -> ModConfig.FeatherFallingMode.ALWAYS;
            };
            ModConfig.setFeatherFallingMode(next);
            updateButtonLabels();
        }

        private void toggleLeatherBoots(Button button) {
            ModConfig.setProtectWithLeatherBoots(!ModConfig.isProtectWithLeatherBoots());
            updateButtonLabels();
        }

        private void togglePetTrampling(Button button) {
            ModConfig.setPreventPetTrampling(!ModConfig.isPreventPetTrampling());
            updateButtonLabels();
        }

        private void toggleVillagerTrampling(Button button) {
            ModConfig.setPreventVillagerTrampling(!ModConfig.isPreventVillagerTrampling());
            updateButtonLabels();
        }

        private void toggleDehydration(Button button) {
            ModConfig.setPreventDehydration(!ModConfig.isPreventDehydration());
            updateButtonLabels();
        }

        private void toggleEmptyReversion(Button button) {
            ModConfig.setPreventEmptyReversion(!ModConfig.isPreventEmptyReversion());
            updateButtonLabels();
        }

        private void toggleParticles(Button button) {
            ModConfig.setEnableParticles(!ModConfig.isEnableParticles());
            updateButtonLabels();
        }

        private void toggleSound(Button button) {
            ModConfig.setEnableSound(!ModConfig.isEnableSound());
            updateButtonLabels();
        }

        private void toggleActionBar(Button button) {
            ModConfig.setEnableActionBarMessage(!ModConfig.isEnableActionBarMessage());
            updateButtonLabels();
        }

        private static @NonNull Component enabledStatus(boolean enabled) {
            return enabled
                    ? Component.translatable("nocroptrample.status.enabled").withStyle(ChatFormatting.GREEN)
                    : Component.translatable("nocroptrample.status.disabled").withStyle(ChatFormatting.RED);
        }

        private static @NonNull Component preventedStatus(boolean prevented) {
            return prevented
                    ? Component.translatable("nocroptrample.status.prevented").withStyle(ChatFormatting.GREEN)
                    : Component.translatable("nocroptrample.status.allowed").withStyle(ChatFormatting.RED);
        }

        private static @NonNull MutableComponent presetName(ModConfig.ModPreset preset) {
            String key = switch (preset) {
                case VANILLA_PLUS -> "nocroptrample.preset.vanilla_plus";
                case CASUAL -> "nocroptrample.preset.casual";
                case HARDCORE -> "nocroptrample.preset.hardcore";
                case CUSTOM -> "nocroptrample.preset.custom";
            };
            return Component.translatable(key);
        }

        private static @NonNull MutableComponent featherFallingModeName(ModConfig.FeatherFallingMode mode) {
            String key = switch (mode) {
                case ALWAYS -> "nocroptrample.feather_falling.always";
                case REQUIRE_FEATHER_FALLING -> "nocroptrample.feather_falling.require_feather_falling";
                case SCALED_BY_LEVEL -> "nocroptrample.feather_falling.scaled_by_level";
            };
            return Component.translatable(key);
        }

        private @NonNull Component getPresetButtonText() {
            return Component.translatable("nocroptrample.config.button.preset")
                    .append(presetName(ModConfig.getActivePreset()).withStyle(ChatFormatting.GOLD));
        }

        private @NonNull Component getEmptyButtonText() {
            return Component.translatable("nocroptrample.config.button.empty").append(preventedStatus(ModConfig.isPreventEmptyTrampling()));
        }

        private @NonNull Component getPlayerButtonText() {
            return Component.translatable("nocroptrample.config.button.player").append(preventedStatus(ModConfig.isPreventPlayerTrampling()));
        }

        private @NonNull Component getMobButtonText() {
            return Component.translatable("nocroptrample.config.button.mob").append(preventedStatus(ModConfig.isPreventMobTrampling()));
        }

        private @NonNull Component getFeatherFallingButtonText() {
            return Component.translatable("nocroptrample.config.button.feather_falling")
                    .append(featherFallingModeName(ModConfig.getFeatherFallingMode()).withStyle(ChatFormatting.YELLOW));
        }

        private @NonNull Component getLeatherBootsButtonText() {
            return Component.translatable("nocroptrample.config.button.leather_boots").append(enabledStatus(ModConfig.isProtectWithLeatherBoots()));
        }

        private @NonNull Component getPetButtonText() {
            return Component.translatable("nocroptrample.config.button.pet").append(preventedStatus(ModConfig.isPreventPetTrampling()));
        }

        private @NonNull Component getVillagerButtonText() {
            return Component.translatable("nocroptrample.config.button.villager").append(preventedStatus(ModConfig.isPreventVillagerTrampling()));
        }

        private @NonNull Component getDehydrationButtonText() {
            return Component.translatable("nocroptrample.config.button.dehydration").append(preventedStatus(ModConfig.isPreventDehydration()));
        }

        private @NonNull Component getEmptyReversionButtonText() {
            return Component.translatable("nocroptrample.config.button.empty_reversion").append(preventedStatus(ModConfig.isPreventEmptyReversion()));
        }

        private @NonNull Component getParticlesButtonText() {
            return Component.translatable("nocroptrample.config.button.particles").append(enabledStatus(ModConfig.isEnableParticles()));
        }

        private @NonNull Component getSoundButtonText() {
            return Component.translatable("nocroptrample.config.button.sound").append(enabledStatus(ModConfig.isEnableSound()));
        }

        private @NonNull Component getActionBarButtonText() {
            return Component.translatable("nocroptrample.config.button.action_bar").append(enabledStatus(ModConfig.isEnableActionBarMessage()));
        }

        @Override
        public void onClose() {
            if (this.minecraft != null) {
                this.minecraft.setScreenAndShow(this.parent);
            }
        }
    }
}
