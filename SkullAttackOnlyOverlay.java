package net.runelite.client.plugins.SkullAttackOnly;

import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static net.runelite.client.plugins.SkullAttackOnly.SkullAttackOnlyPlugin.*;


class SkullAttackOnlyOverlay extends OverlayPanel
{

	private final Client client;
	private final SkullAttackOnlyPlugin plugin;
	private final SkullAttackOnlyConfig config;


	@Inject
	private ItemManager itemManager;

	@Inject
	public SkullAttackOnlyOverlay(Client client, SkullAttackOnlyPlugin plugin, SkullAttackOnlyConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	BufferedImage skullimage = null;

	@Override
	public Dimension render(Graphics2D graphics)
	{

		if (skullimage == null)
		{
			skullimage = itemManager.getImage(553);
		}

		if (client.getLocalPlayer().getHealthRatio() == 0) // clear list if we die
		{
			SkullAttackOnlyPlugin.InCombatList.clear();
		}

		for (SkullAttackOnlyPlugin.InCombat p : InCombatList) {

			if (p.value.until(LocalDateTime.now(), ChronoUnit.MINUTES) >= 20) {
				InCombatList.remove(p);
			}
		}


		for (Player p : client.getPlayers()) {

			if (SkullAttackOnlyPlugin.GetInList(p.getName())) {
				if (p.getHealthRatio() == 0) {
					SkullAttackOnlyPlugin.RemoveFromList(p.getName()); // remove from list if they are dead
				}
				OverlayUtil.renderImageLocation(client, graphics, p.getLocalLocation(), skullimage, 0);
			}

		}

		return super.render(graphics);
	}
}
