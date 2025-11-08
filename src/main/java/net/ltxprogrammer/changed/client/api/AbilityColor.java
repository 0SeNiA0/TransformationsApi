package net.ltxprogrammer.changed.client.api;

import net.ltxprogrammer.changed.ability.AbstractAbilityInstance;

import java.util.Optional;

public interface AbilityColor {
    Optional<Integer> getColor(AbstractAbilityInstance abilityInstance, int layer);
}
