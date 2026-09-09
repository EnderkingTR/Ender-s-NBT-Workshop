package com.nadir.endersnbt.client.gui;

import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import java.util.Map;

public class EnchantmentListWidget extends ObjectSelectionList<EnchantmentListWidget.Entry> {
    private final NbtEditorScreen parent;

    public EnchantmentListWidget(NbtEditorScreen parent, Minecraft minecraft, int width, int height, int y0, int itemHeight) {
        super(minecraft, width, height, y0, itemHeight);
        this.parent = parent;
    }

    public void rebuild(NbtNode levelsNode) {
        this.clearEntries();
        if (levelsNode != null) {
            for (NbtNode ench : levelsNode.children) {
                this.addEntry(new Entry(ench));
            }
        }
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final NbtNode node;
        private final Button minusBtn;
        private final Button plusBtn;
        private long plusHoldTime = 0;
        private long minusHoldTime = 0;
        private long lastActionTime = 0;

        public Entry(NbtNode node) {
            this.node = node;
            this.minusBtn = Button.builder(Component.literal("-"), btn -> {
                int lvl = getLevel();
                if (lvl > 1) {
                    setLevel(lvl - 1);
                } else {
                    node.parent.children.remove(node);
                    rebuild(node.parent);
                }
            }).bounds(0, 0, 20, 20).build();

            this.plusBtn = Button.builder(Component.literal("+"), btn -> {
                int lvl = getLevel();
                if (lvl < 255) setLevel(lvl + 1);
            }).bounds(0, 0, 20, 20).build();
        }

        private int getLevel() {
            if (node.tag instanceof IntTag intTag) return intTag.getAsInt();
            return 1;
        }

        private void setLevel(int lvl) {
            node.tag = IntTag.valueOf(lvl);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            String name = node.name.replace("minecraft:", "");
            guiGraphics.drawString(Minecraft.getInstance().font, name + " " + getLevel(), left + 5, top + 5, 0xFFFFFF, false);

            this.minusBtn.setX(left + width - 65);
            this.minusBtn.setY(top);
            this.minusBtn.render(guiGraphics, mouseX, mouseY, partialTick);

            this.plusBtn.setX(left + width - 40);
            this.plusBtn.setY(top);
            this.plusBtn.render(guiGraphics, mouseX, mouseY, partialTick);

            boolean mouseDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            long currentTime = net.minecraft.Util.getMillis();
            if (mouseDown) {
                if (this.plusBtn.isHovered()) {
                    if (plusHoldTime == 0) plusHoldTime = currentTime;
                    else if (currentTime - plusHoldTime > 500) {
                        long delay = Math.max(10, 100 - (currentTime - plusHoldTime - 500) / 10);
                        if (currentTime - lastActionTime > delay) {
                            int lvl = getLevel();
                            if (lvl < 255) setLevel(lvl + 1);
                            lastActionTime = currentTime;
                        }
                    }
                } else if (this.minusBtn.isHovered()) {
                    if (minusHoldTime == 0) minusHoldTime = currentTime;
                    else if (currentTime - minusHoldTime > 500) {
                        long delay = Math.max(10, 100 - (currentTime - minusHoldTime - 500) / 10);
                        if (currentTime - lastActionTime > delay) {
                            int lvl = getLevel();
                            if (lvl > 1) setLevel(lvl - 1);
                            lastActionTime = currentTime;
                        }
                    }
                } else {
                    plusHoldTime = 0;
                    minusHoldTime = 0;
                }
            } else {
                plusHoldTime = 0;
                minusHoldTime = 0;
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.minusBtn.mouseClicked(mouseX, mouseY, button)) return true;
            if (this.plusBtn.mouseClicked(mouseX, mouseY, button)) return true;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public Component getNarration() {
            return Component.literal(node.name);
        }
    }
}
