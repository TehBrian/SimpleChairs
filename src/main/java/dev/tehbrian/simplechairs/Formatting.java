package dev.tehbrian.simplechairs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Formatting {

  private Formatting() {
  }

  public static Component legacy(final String s) {
    return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
  }

}
