package com.nadir.endersnbt.client.gui;

import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.nbt.CompoundTag;

public class NbtTreeListWidget extends ObjectSelectionList<NbtTreeEntry> {
    private final NbtEditorScreen parentScreen;
    private final NbtNode rootNode;

    public NbtTreeListWidget(NbtEditorScreen parentScreen, net.minecraft.client.Minecraft minecraft, int width, int height, int y0, int itemHeight, CompoundTag tag) {
        super(minecraft, width, height, y0, itemHeight);
        this.parentScreen = parentScreen;
        this.rootNode = new NbtNode("root", tag, 0, null);
        this.rootNode.expanded = true;
        this.rebuildList();
    }

    public NbtNode getRootNode() {
        return rootNode;
    }

    public NbtEditorScreen getParentScreen() {
        return parentScreen;
    }

    public void rebuildList() {
        this.clearEntries();
        for (NbtNode child : rootNode.children) {
            addNodesRecursively(child);
        }
        this.setScrollAmount(this.getScrollAmount());
    }

    private void addNodesRecursively(NbtNode node) {
        this.addEntry(new NbtTreeEntry(this, node));
        if (node.expanded) {
            for (NbtNode child : node.children) {
                addNodesRecursively(child);
            }
        }
    }
}
