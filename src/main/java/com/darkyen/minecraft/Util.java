package com.darkyen.minecraft;

import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.stream.events.Characters;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 */
public class Util {

    public static boolean overlaps(int quadMin, int quadMax, int queryMin, int queryMax) {
        return queryMin <= quadMax && queryMax >= quadMin;
    }


    public static double distance2(@NotNull Location a, @NotNull Location b) {
        if (!a.isWorldLoaded() || !b.isWorldLoaded() || a.getWorld() != b.getWorld()) {
            return Double.POSITIVE_INFINITY;
        }
        return NumberConversions.square(a.getX() - b.getX()) + NumberConversions.square(a.getY() - b.getY()) + NumberConversions.square(a.getZ() - b.getZ());
    }

    public static double distance2(@NotNull Location a, @NotNull Location b, double yScale) {
        if (!a.isWorldLoaded() || !b.isWorldLoaded() || a.getWorld() != b.getWorld()) {
            return Double.POSITIVE_INFINITY;
        }
        return NumberConversions.square(a.getX() - b.getX()) + NumberConversions.square((a.getY() - b.getY()) * yScale) + NumberConversions.square(a.getZ() - b.getZ());
    }

    public static boolean isNear(@NotNull Location a, @NotNull Location b, float distance) {
        if (!a.isWorldLoaded() || !b.isWorldLoaded()) {
            return false;
        }
        if (a.getWorld() != b.getWorld()) {
            return false;
        }
        final double dst2 = NumberConversions.square(a.getX() - b.getX()) + NumberConversions.square(a.getZ() - b.getZ());
        return dst2 < distance * distance;
    }

    public static void set(@NotNull Location target, @NotNull Location source) {
        target.setWorld(source.getWorld());
        target.setX(source.getX());
        target.setY(source.getY());
        target.setZ(source.getZ());
    }

    private static final Pattern TIME_SANITIZER = Pattern.compile("[^a-zA-Z0-9]");
    public static long parseTimeMs(@Nullable String time, long defaultMs, Logger log) {
        if (time == null) {
            return defaultMs;
        }
        final String sanitized = TIME_SANITIZER.matcher(time).replaceAll("");
        if ("never".equalsIgnoreCase(sanitized)) {
            return Long.MAX_VALUE;
        }
        int firstLetterIndex = 0;
        while (firstLetterIndex < sanitized.length() && Character.isDigit(sanitized.charAt(firstLetterIndex))) {
            firstLetterIndex++;
        }
        if (firstLetterIndex >= sanitized.length()) {
            log.log(Level.WARNING, "Time \""+time+"\" is missing an unit");
            return defaultMs;
        }
        if (firstLetterIndex == 0) {
            log.log(Level.WARNING, "Time \""+time+"\" is missing an amount");
            return defaultMs;
        }
        final long amount;
        try {
            amount = Long.parseLong(sanitized.substring(0, firstLetterIndex));
        } catch (NumberFormatException e) {
            log.log(Level.WARNING, "Time \""+time+"\" is invalid");
            return defaultMs;
        }

        final TimeUnit unit;
        switch (sanitized.charAt(firstLetterIndex)) {
            case 's':
                unit = TimeUnit.SECONDS;
                break;
            case 'm':
                unit = TimeUnit.MINUTES;
                break;
            case 'h':
                unit = TimeUnit.HOURS;
                break;
            case 'd':
                unit = TimeUnit.DAYS;
                break;
            default:
                log.log(Level.WARNING, "Time \""+time+"\" has invalid unit");
                return defaultMs;
        }

        return unit.toMillis(amount);
    }

    // https://stackoverflow.com/a/2633161
    public static long saturatedAdd(long x, long y) {
        // Sum ignoring overflow/underflow
        long s = x + y;

        // Long.MIN_VALUE if result positive (potential underflow)
        // Long.MAX_VALUE if result negative (potential overflow)
        long limit = Long.MIN_VALUE ^ (s >> 63);

        // -1 if overflow/underflow occurred, 0 otherwise
        long overflow = ((x ^ s) & ~(x ^ y)) >> 63;

        // limit if overflowed/underflowed, else s
        return ((limit ^ s) & overflow) ^ s;
    }
}
