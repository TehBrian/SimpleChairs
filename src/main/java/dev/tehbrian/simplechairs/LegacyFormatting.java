package dev.tehbrian.simplechairs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class LegacyFormatting {

  private LegacyFormatting() {
  }

  public static Component on(final String s) {
    return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
  }

}
