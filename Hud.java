package client.confis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

public final class Hud {
    private static int frames, fps;
    private static long lastFpsTime = System.nanoTime();
    private static boolean shiftWasDown;

    private Hud() {}

    public static int fps() { return fps; }

    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        pollMenuKey(mc);
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        countFrame();
        Cps.poll(mc);

        boolean preview = mc.screen instanceof ConfigScreen;
        int W = g.guiWidth(), H = g.guiHeight();
        for (Module m : Modules.ALL) {
            Config.ModuleCfg c = m.cfg();
            if (!c.enabled) continue;
            if (!m.movable()) {
                if (m instanceof Modules.WaypointsModule) Waypoints.render(g, mc, c);
                continue;
            }
            m.draw(g, mc, c, m.posX(mc, W), m.posY(mc, H), preview);
        }
    }

    /** Right Shift opens / closes the menu. Polled from the render thread's GL context. */
    private static void pollMenuKey(Minecraft mc) {
        long window = GLFW.glfwGetCurrentContext();
        if (window == 0L) return;
        boolean down = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (down && !shiftWasDown) {
            if (mc.screen == null && mc.player != null) mc.setScreen(new ConfigScreen());
            else if (mc.screen instanceof ConfigScreen) mc.setScreen(null);
        }
        shiftWasDown = down;
    }

    private static void countFrame() {
        frames++;
        long now = System.nanoTime();
        if (now - lastFpsTime >= 1_000_000_000L) {
            fps = frames;
            frames = 0;
            lastFpsTime = now;
        }
    }
}
