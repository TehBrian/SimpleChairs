package dev.tehbrian.simplechairs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class LegacyFormatting {

  private LegacyFormatting() {
  }

  public static @NonNull Component on(final String s) {
    return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
  }

}
