package com.barfl.treecutters.gameplay;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerData;
import com.barfl.treecutters.data.PlayerSession;
import com.barfl.treecutters.util.LocUtil;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.util.Vector;

public final class DoubleJumpManager {
    private final Treecutters plugin;

    public DoubleJumpManager(Treecutters plugin) {
        this.plugin = plugin;
    }

    public void refreshGroundedCharges(Player player) {
        PlayerSession session = plugin.data().session(player.getUniqueId());
        if (player.isOnGround()) {
            session.canDoubleJump = 1 + (int) session.stat("triple_jumps");
        }
    }

    public void handleInput(PlayerInputEvent event) {
        Player player = event.getPlayer();
        Input input = event.getInput();
        PlayerSession session = plugin.data().session(player.getUniqueId());

        boolean justPressed = input.isJump() && !session.jumpHeld;
        session.jumpHeld = input.isJump();
        if (!justPressed) return;

        if (player.isOnGround() || session.canDoubleJump <= 0) return;

        session.canDoubleJump -= 1;

        PlayerData data = plugin.data().get(player.getUniqueId());
        double jumpHeight = session.stat("jump_height");
        double upSpeed = (jumpHeight + 1) * 0.2;

        Vector velocity = new Vector(0, upSpeed, 0);

        String jumpType = data.settings.getOrDefault("jumpType", "Look-Based");
        double baseForward = ((9.0 / 5.0) * jumpHeight + 9.0 / 5.0) * 0.15;
        Vector push;
        if (jumpType.equals("Look-Based")) {
            push = player.getLocation().getDirection().multiply(baseForward);
        } else {
            push = keyBasedDirection(player, input).multiply(baseForward);
        }

        player.setVelocity(velocity.add(push));
    }

    private Vector keyBasedDirection(Player player, Input input) {
        Vector move = new Vector(0, 0, 0);
        if (input.isForward()) move.add(new Vector(0, 0, 1));
        if (input.isBackward()) move.add(new Vector(0, 0, -1));
        if (input.isLeft()) move.add(new Vector(1, 0, 0));
        if (input.isRight()) move.add(new Vector(-1, 0, 0));

        if (move.lengthSquared() == 0) {
            return player.getLocation().getDirection().setY(0).normalize();
        }
        move.normalize();
        move = LocUtil.rotateAroundY(move, -player.getLocation().getYaw());
        move = LocUtil.rotateAroundZ(move, -player.getLocation().getPitch());
        return move;
    }
}
