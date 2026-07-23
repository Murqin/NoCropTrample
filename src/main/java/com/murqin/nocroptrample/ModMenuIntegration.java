package com.murqin.nocroptrample;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
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
     * Provides a simple GUI to toggle player and mob trampling prevention.
     * </p>
     */
    public static class NoCropTrampleConfigScreen extends Screen {
        private final Screen parent;

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
            super(Component.literal("NoCropTrample Config"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int leftX = centerX - 155;
            int rightX = centerX + 5;
            int startY = 32;
            int buttonWidth = 150;
            int buttonHeight = 20;
            int rowSpacing = 24;

            // Preset Button
            this.presetButton = this.addRenderableWidget(Button.builder(getPresetButtonText(), this::cyclePreset)
                    .bounds(centerX - 100, startY, 200, buttonHeight)
                    .build());

            // Row 1
            this.emptyButton = this.addRenderableWidget(Button.builder(getEmptyButtonText(), this::toggleEmptyTrampling)
                    .bounds(leftX, startY + rowSpacing, buttonWidth, buttonHeight)
                    .build());
            this.playerButton = this.addRenderableWidget(Button.builder(getPlayerButtonText(), this::togglePlayerTrampling)
                    .bounds(rightX, startY + rowSpacing, buttonWidth, buttonHeight)
                    .build());

            // Row 2
            this.mobButton = this.addRenderableWidget(Button.builder(getMobButtonText(), this::toggleMobTrampling)
                    .bounds(leftX, startY + rowSpacing * 2, buttonWidth, buttonHeight)
                    .build());
            this.featherFallingButton = this.addRenderableWidget(Button.builder(getFeatherFallingButtonText(), this::cycleFeatherFalling)
                    .bounds(rightX, startY + rowSpacing * 2, buttonWidth, buttonHeight)
                    .build());

            // Row 3
            this.leatherBootsButton = this.addRenderableWidget(Button.builder(getLeatherBootsButtonText(), this::toggleLeatherBoots)
                    .bounds(leftX, startY + rowSpacing * 3, buttonWidth, buttonHeight)
                    .build());
            this.petButton = this.addRenderableWidget(Button.builder(getPetButtonText(), this::togglePetTrampling)
                    .bounds(rightX, startY + rowSpacing * 3, buttonWidth, buttonHeight)
                    .build());

            // Row 4
            this.villagerButton = this.addRenderableWidget(Button.builder(getVillagerButtonText(), this::toggleVillagerTrampling)
                    .bounds(leftX, startY + rowSpacing * 4, buttonWidth, buttonHeight)
                    .build());
            this.dehydrationButton = this.addRenderableWidget(Button.builder(getDehydrationButtonText(), this::toggleDehydration)
                    .bounds(rightX, startY + rowSpacing * 4, buttonWidth, buttonHeight)
                    .build());

            // Row 5
            this.emptyReversionButton = this.addRenderableWidget(Button.builder(getEmptyReversionButtonText(), this::toggleEmptyReversion)
                    .bounds(leftX, startY + rowSpacing * 5, buttonWidth, buttonHeight)
                    .build());
            this.particlesButton = this.addRenderableWidget(Button.builder(getParticlesButtonText(), this::toggleParticles)
                    .bounds(rightX, startY + rowSpacing * 5, buttonWidth, buttonHeight)
                    .build());

            // Row 6
            this.soundButton = this.addRenderableWidget(Button.builder(getSoundButtonText(), this::toggleSound)
                    .bounds(leftX, startY + rowSpacing * 6, buttonWidth, buttonHeight)
                    .build());
            this.actionBarButton = this.addRenderableWidget(Button.builder(getActionBarButtonText(), this::toggleActionBar)
                    .bounds(rightX, startY + rowSpacing * 6, buttonWidth, buttonHeight)
                    .build());

            // Done button
            this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.done"),
                    button -> this.onClose())
                    .bounds(centerX - 100, startY + rowSpacing * 7 + 4, 200, buttonHeight)
                    .build());
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
            ModConfig.setPreset(next);
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
                case DISABLED -> ModConfig.FeatherFallingMode.ANY_LEVEL;
                case ANY_LEVEL -> ModConfig.FeatherFallingMode.SCALED;
                case SCALED -> ModConfig.FeatherFallingMode.DISABLED;
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

        private @NonNull Component getPresetButtonText() {
            String name = switch (ModConfig.getActivePreset()) {
                case VANILLA_PLUS -> "Vanilla+";
                case CASUAL -> "Casual";
                case HARDCORE -> "Hardcore";
                case CUSTOM -> "Custom";
            };
            return Component.literal("Preset: ").append(Component.literal(name).withStyle(net.minecraft.ChatFormatting.GOLD));
        }

        private @NonNull Component getEmptyButtonText() {
            return Component.literal("Empty Trampling: ")
                    .append(ModConfig.isPreventEmptyTrampling()
                            ? Component.literal("§aPrevented")
                            : Component.literal("§cAllowed"));
        }

        private @NonNull Component getPlayerButtonText() {
            return Component.literal("Player Trampling: ")
                    .append(ModConfig.isPreventPlayerTrampling()
                            ? Component.literal("§aPrevented")
                            : Component.literal("§cAllowed"));
        }

        private @NonNull Component getMobButtonText() {
            return Component.literal("Mob Trampling: ")
                    .append(ModConfig.isPreventMobTrampling()
                            ? Component.literal("§aPrevented")
                            : Component.literal("§cAllowed"));
        }

        private @NonNull Component getFeatherFallingButtonText() {
            return Component.literal("Feather Falling: ")
                    .append(Component.literal("§e" + ModConfig.getFeatherFallingMode().name()));
        }

        private @NonNull Component getLeatherBootsButtonText() {
            return Component.literal("Leather Boots: ")
                    .append(ModConfig.isProtectWithLeatherBoots()
                            ? Component.literal("§aEnabled")
                            : Component.literal("§cDisabled"));
        }

        private @NonNull Component getPetButtonText() {
            return Component.literal("Pet Trampling: ")
                    .append(ModConfig.isPreventPetTrampling()
                            ? Component.literal("§aPrevented")
                            : Component.literal("§cAllowed"));
        }

        private @NonNull Component getVillagerButtonText() {
            return Component.literal("Villager Trampling: ")
                    .append(ModConfig.isPreventVillagerTrampling()
                            ? Component.literal("§aPrevented")
                            : Component.literal("§cAllowed"));
        }

        private @NonNull Component getDehydrationButtonText() {
            return Component.literal("Dehydration: ")
                    .append(ModConfig.isPreventDehydration()
                            ? Component.literal("§aPrevented")
                            : Component.literal("§cAllowed"));
        }

        private @NonNull Component getEmptyReversionButtonText() {
            return Component.literal("Empty Reversion: ")
                    .append(ModConfig.isPreventEmptyReversion()
                            ? Component.literal("§aPrevented")
                            : Component.literal("§cAllowed"));
        }

        private @NonNull Component getParticlesButtonText() {
            return Component.literal("Particles: ")
                    .append(ModConfig.isEnableParticles()
                            ? Component.literal("§aEnabled")
                            : Component.literal("§cDisabled"));
        }

        private @NonNull Component getSoundButtonText() {
            return Component.literal("Sound: ")
                    .append(ModConfig.isEnableSound()
                            ? Component.literal("§aEnabled")
                            : Component.literal("§cDisabled"));
        }

        private @NonNull Component getActionBarButtonText() {
            return Component.literal("Action Bar Msg: ")
                    .append(ModConfig.isEnableActionBarMessage()
                            ? Component.literal("§aEnabled")
                            : Component.literal("§cDisabled"));
        }

        @Override
        public void extractRenderState(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float delta) {
            super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, delta);
            guiGraphicsExtractor.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        }

        @Override
        public void onClose() {
            if (this.minecraft != null) {
                this.minecraft.setScreenAndShow(this.parent);
            }
        }
    }
}
