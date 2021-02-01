package net.runelite.client.plugins.SkullAttackOnly;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("SkullAttackOnlyConfig")
public interface SkullAttackOnlyConfig extends Config
{
    @ConfigItem(
            position = 0,
            keyName = "RemoveAttack",
            name = "Remove Attack",
            description = "Removes the attack option"
    )
    default boolean RemoveAttack()
    {
        return false;
    }


}
