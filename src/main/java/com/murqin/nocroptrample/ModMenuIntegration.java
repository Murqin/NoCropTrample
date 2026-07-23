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

        public NoCropTrampleConfigScreen(Screen parent) {
            super(Component.literal("NoCropTrample Config"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int leftX = centerX - 155;
            int rightX = centerX + 5;
            int startY = 35;
            int buttonWidth = 150;
            int buttonHeight = 20;
            int rowSpacing = 24;

            // Row 0
            this.addRenderableWidget(Button.builder(getEmptyButtonText(), this::toggleEmptyTrampling)
                    .bounds(leftX, startY, buttonWidth, buttonHeight)
                    .build());
            this.addRenderableWidget(Button.builder(getPlayerButtonText(), this::togglePlayerTrampling)
                    .bounds(rightX, startY, buttonWidth, buttonHeight)
                    .build());

            // Row 1
            this.addRenderableWidget(Button.builder(getMobButtonText(), this::toggleMobTrampling)
                    .bounds(leftX, startY + rowSpacing, buttonWidth, buttonHeight)
                    .build());
            this.addRenderableWidget(Button.builder(getFeatherFallingButtonText(), this::cycleFeatherFalling)
                    .bounds(rightX, startY + rowSpacing, buttonWidth, buttonHeight)
                    .build());

            // Row 2
            this.addRenderableWidget(Button.builder(getLeatherBootsButtonText(), this::toggleLeatherBoots)
                    .bounds(leftX, startY + rowSpacing * 2, buttonWidth, buttonHeight)
                    .build());
            this.addRenderableWidget(Button.builder(getPetButtonText(), this::togglePetTrampling)
                    .bounds(rightX, startY + rowSpacing * 2, buttonWidth, buttonHeight)
                    .build());

            // Row 3
            this.addRenderableWidget(Button.builder(getVillagerButtonText(), this::toggleVillagerTrampling)
                    .bounds(leftX, startY + rowSpacing * 3, buttonWidth, buttonHeight)
                    .build());
            this.addRenderableWidget(Button.builder(getDehydrationButtonText(), this::toggleDehydration)
                    .bounds(rightX, startY + rowSpacing * 3, buttonWidth, buttonHeight)
                    .build());

            // Row 4
            this.addRenderableWidget(Button.builder(getEmptyReversionButtonText(), this::toggleEmptyReversion)
                    .bounds(leftX, startY + rowSpacing * 4, buttonWidth, buttonHeight)
                    .build());
            this.addRenderableWidget(Button.builder(getParticlesButtonText(), this::toggleParticles)
                    .bounds(rightX, startY + rowSpacing * 4, buttonWidth, buttonHeight)
                    .build());

            // Row 5
            this.addRenderableWidget(Button.builder(getSoundButtonText(), this::toggleSound)
                    .bounds(leftX, startY + rowSpacing * 5, buttonWidth, buttonHeight)
                    .build());
            this.addRenderableWidget(Button.builder(getActionBarButtonText(), this::toggleActionBar)
                    .bounds(rightX, startY + rowSpacing * 5, buttonWidth, buttonHeight)
                    .build());

            // Done button
            this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.done"),
                    button -> this.onClose())
                    .bounds(centerX - 100, startY + rowSpacing * 6 + 6, 200, buttonHeight)
                    .build());
        }

        private void toggleEmptyTrampling(Button button) {
            ModConfig.setPreventEmptyTrampling(!ModConfig.isPreventEmptyTrampling());
            button.setMessage(getEmptyButtonText());
        }

        private void togglePlayerTrampling(Button button) {
            ModConfig.setPreventPlayerTrampling(!ModConfig.isPreventPlayerTrampling());
            button.setMessage(getPlayerButtonText());
        }

        private void toggleMobTrampling(Button button) {
            ModConfig.setPreventMobTrampling(!ModConfig.isPreventMobTrampling());
            button.setMessage(getMobButtonText());
        }

        private void cycleFeatherFalling(Button button) {
            ModConfig.FeatherFallingMode current = ModConfig.getFeatherFallingMode();
            ModConfig.FeatherFallingMode next = switch (current) {
                case DISABLED -> ModConfig.FeatherFallingMode.ANY_LEVEL;
                case ANY_LEVEL -> ModConfig.FeatherFallingMode.SCALED;
                case SCALED -> ModConfig.FeatherFallingMode.DISABLED;
            };
            ModConfig.setFeatherFallingMode(next);
            button.setMessage(getFeatherFallingButtonText());
        }

        private void toggleLeatherBoots(Button button) {
            ModConfig.setProtectWithLeatherBoots(!ModConfig.isProtectWithLeatherBoots());
            button.setMessage(getLeatherBootsButtonText());
        }

        private void togglePetTrampling(Button button) {
            ModConfig.setPreventPetTrampling(!ModConfig.isPreventPetTrampling());
            button.setMessage(getPetButtonText());
        }

        private void toggleVillagerTrampling(Button button) {
            ModConfig.setPreventVillagerTrampling(!ModConfig.isPreventVillagerTrampling());
            button.setMessage(getVillagerButtonText());
        }

        private void toggleDehydration(Button button) {
            ModConfig.setPreventDehydration(!ModConfig.isPreventDehydration());
            button.setMessage(getDehydrationButtonText());
        }

        private void toggleEmptyReversion(Button button) {
            ModConfig.setPreventEmptyReversion(!ModConfig.isPreventEmptyReversion());
            button.setMessage(getEmptyReversionButtonText());
        }

        private void toggleParticles(Button button) {
            ModConfig.setEnableParticles(!ModConfig.isEnableParticles());
            button.setMessage(getParticlesButtonText());
        }

        private void toggleSound(Button button) {
            ModConfig.setEnableSound(!ModConfig.isEnableSound());
            button.setMessage(getSoundButtonText());
        }

        private void toggleActionBar(Button button) {
            ModConfig.setEnableActionBarMessage(!ModConfig.isEnableActionBarMessage());
            button.setMessage(getActionBarButtonText());
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
