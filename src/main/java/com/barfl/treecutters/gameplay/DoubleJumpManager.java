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

        Vector velocity = player.getVelocity();
        velocity.setY(upSpeed);

        String jumpType = data.settings.getOrDefault("jumpType", "Look-Based");
        double forwardSpeed = ((9.0 / 5.0) * jumpHeight + 9.0 / 5.0) * 0.15;
        Vector direction = jumpType.equals("Look-Based")
                ? player.getLocation().getDirection().setY(0).normalize()
                : keyBasedDirection(player, input);

        velocity.setX(velocity.getX() + direction.getX() * forwardSpeed);
        velocity.setZ(velocity.getZ() + direction.getZ() * forwardSpeed);

        player.setVelocity(velocity);
    }

    private Vector keyBasedDirection(Player player, Input input) {
        Vector forward = player.getLocation().getDirection().setY(0).normalize();
        Vector right = LocUtil.rotateAroundY(forward, -90).normalize();

        Vector move = new Vector(0, 0, 0);
        if (input.isForward()) move.add(forward);
        if (input.isBackward()) move.subtract(forward);
        if (input.isRight()) move.add(right);
        if (input.isLeft()) move.subtract(right);

        if (move.lengthSquared() == 0) return forward;
        return move.normalize();
    }
}
