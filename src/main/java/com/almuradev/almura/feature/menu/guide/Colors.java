package com.almuradev.almura.feature.menu.guide;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class Colors {

    public static final Color BLACK         = new Color("black",         TextFormatting.BLACK,          0, 0);
    public static final Color DARK_BLUE     = new Color("dark_blue",     TextFormatting.DARK_BLUE,      1, 170);
    public static final Color DARK_GREEN    = new Color("dark_green",    TextFormatting.DARK_GREEN,     2, 43520);
    public static final Color DARK_AQUA     = new Color("dark_aqua",     TextFormatting.DARK_AQUA,      3, 43690);
    public static final Color DARK_RED      = new Color("dark_red",      TextFormatting.DARK_RED,       4, 11141120);
    public static final Color DARK_PURPLE   = new Color("dark_purple",   TextFormatting.DARK_PURPLE,    5, 11141290);
    public static final Color GOLD          = new Color("gold",          TextFormatting.GOLD,           6, 16755200);
    public static final Color GRAY          = new Color("gray",          TextFormatting.GRAY,           7, 11184810);
    public static final Color DARK_GRAY     = new Color("dark_gray",     TextFormatting.DARK_GRAY,      8, 5592405);
    public static final Color BLUE          = new Color("blue",          TextFormatting.BLUE,           9, 5592575);
    public static final Color GREEN         = new Color("green",         TextFormatting.GREEN,         10, 5635925);
    public static final Color AQUA          = new Color("aqua",          TextFormatting.AQUA,          11, 5636095);
    public static final Color RED           = new Color("red",           TextFormatting.RED,           12, 16733525);
    public static final Color LIGHT_PURPLE  = new Color("light_purple",  TextFormatting.LIGHT_PURPLE,  13, 16733695);
    public static final Color YELLOW        = new Color("yellow",        TextFormatting.YELLOW,        14, 16777045);
    public static final Color WHITE         = new Color("white",         TextFormatting.WHITE,         15, 16777215);
    public static final Color OBFUSCATED    = new Color("obfuscated",    TextFormatting.OBFUSCATED,    16, true);
    public static final Color BOLD          = new Color("bold",          TextFormatting.BOLD,          17, true);
    public static final Color STRIKETHROUGH = new Color("strikethrough", TextFormatting.STRIKETHROUGH, 18, true);
    public static final Color UNDERLINE     = new Color("underline",     TextFormatting.UNDERLINE,     19, true);
    public static final Color ITALIC        = new Color("italic",        TextFormatting.ITALIC,        20, true);
    public static final Color RESET         = new Color("reset",         TextFormatting.RESET,         21);
    private static final List<Color> COLORS;
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(Color.CHAR_COLOR_BEGIN) + "[0-9A-FK-OR]");


    static {
        COLORS = Lists.newArrayList();

        final Field[] declaredFields = Colors.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    final Object obj = field.get(null);
                    if (obj instanceof Color) {
                        COLORS.add((Color) obj);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<Color> getBuiltinColors() {
        return Collections.unmodifiableList(COLORS);
    }

    public static Optional<Color> getByCode(char code) {
        for (Color c : COLORS) {
            if (c.getChatCode() == code) {
                return Optional.of(c);
            }
        }

        return Optional.absent();    }

    public static Optional<Color> getByCode(int code) {
        for (Color c : COLORS) {
            if (c.getChatIntCode() == code) {
                return Optional.of(c);
            }
        }

        return Optional.absent();
    }

    public static String stripColors(String colorized) {
        if (colorized == null) {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher(colorized).replaceAll("");
    }
}
