package com.nadir.endersnbt.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.IntTag;
import net.minecraft.resources.ResourceLocation;

public class EnchantmentSelectionScreen extends Screen {
    private final NbtEditorScreen parent;
    private final NbtNode levelsNode;
    private EnchantmentSelectList list;

    public EnchantmentSelectionScreen(NbtEditorScreen parent, NbtNode levelsNode) {
        super(Component.literal("Select Enchantment"));
        this.parent = parent;
        this.levelsNode = levelsNode;
    }

    @Override
    protected void init() {
        this.list = new EnchantmentSelectList(this.minecraft, this.width, this.height, 40, 20);
        var registry = this.minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for (ResourceLocation id : registry.keySet()) {
            this.list.addEnchant(id.toString());
        }
        this.addRenderableWidget(this.list);

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> this.minecraft.setScreen(parent))
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    public class EnchantmentSelectList extends ObjectSelectionList<EnchantmentSelectList.Entry> {
        public EnchantmentSelectList(net.minecraft.client.Minecraft minecraft, int width, int height, int y0, int itemHeight) {
            super(minecraft, width, height, y0, itemHeight);
        }

        public void addEnchant(String enchantId) {
            this.addEntry(new Entry(enchantId));
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final String enchantId;

            public Entry(String enchantId) {
                this.enchantId = enchantId;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
                guiGraphics.drawString(minecraft.font, enchantId, left + 5, top + 5, 0xFFFFFF, false);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    levelsNode.getOrCreateChild(enchantId, IntTag.valueOf(1)).tag = IntTag.valueOf(1);
                    parent.rebuildEnchantmentList();
                    minecraft.setScreen(parent);
                    return true;
                }
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.literal(enchantId);
            }
        }
    }
}
