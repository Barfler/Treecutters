package com.barfl.treecutters.tree;

import org.bukkit.util.Vector;

import java.util.List;

public record TreeSizeVariables(List<Vector> logPositions, int height, int branchReqs, int meteorSize) {
}
