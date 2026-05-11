package net.scarletvaloria.worldbreaker.index;


public class WorldbreakerClientState {
    public static boolean initialized = false;
    public static int charges = 3;

    public static void set(int value) {
        charges = value;
    }

    public static float flashIntensity = 0f;

    public static void triggerFlash() {
        flashIntensity = 1.0f;
    }
}
