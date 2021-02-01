package net.runelite.client.plugins.SkullAttackOnly;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@PluginDescriptor(
		name = "SkullAttack",
		description = "Anti-Skull Tricking plugin",
		tags = {"overlay", "skull","skulltrick"}
)
@Singleton
@Slf4j
public class SkullAttackOnlyPlugin extends Plugin
{

	private static final  StringBuilder SB = new StringBuilder(64);

	@Setter
	@Getter
	Predicate<? super Integer> MagicAnims = id -> id.equals(710) || id.equals(711) || id.equals(1161) || id.equals(1162) || id.equals(1167) || id.equals(7855) || id.equals(1978) || id.equals(1979);

	@Setter
	@Getter
	Predicate<? super Integer> MeleeAnims = id -> id.equals(376) || id.equals(381) || id.equals(386) || id.equals(390) || id.equals(393) || id.equals(395) || id.equals(400) || id.equals(401) || id.equals(406) || id.equals(407) || id.equals(414) || id.equals(419) || id.equals(422) || id.equals(423) || id.equals(428) || id.equals(429) || id.equals(440) || id.equals(1058) || id.equals(1060) || id.equals(1062) || id.equals(1378) || id.equals(1658) || id.equals(1665) || id.equals(1667) || id.equals(2066) || id.equals(2067) || id.equals(2078) || id.equals(2661) || id.equals(3297) || id.equals(3298) || id.equals(3852) || id.equals(5865) || id.equals(7004) || id.equals(7045) || id.equals(7054) || id.equals(7514) || id.equals(7515) || id.equals(7516) || id.equals(7638) || id.equals(7640) || id.equals(7642) || id.equals(7644) || id.equals(8056) || id.equals(8145);

	@Setter
	@Getter
	Predicate<? super Integer> MageMeleeAnims = id -> id.equals(393) || id.equals(414) || id.equals(419) || id.equals(428) || id.equals(440) || id.equals(2078);

	@Getter
	@Setter
	Predicate<? super Integer> RangeAnims = id -> id.equals(426) || id.equals(929) || id.equals(1074) || id.equals(4230) || id.equals(5061) || id.equals(6600) || id.equals(7218) || id.equals(7521) || id.equals(7552) || id.equals(7555) || id.equals(7617) || id.equals(8194) || id.equals(8195) || id.equals(8292);


	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SkullAttackOnlyConfig config;

	@Provides
	SkullAttackOnlyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkullAttackOnlyConfig.class);
	}

	@Inject
	private SkullAttackOnlyOverlay overlay;

	@Inject
	private MouseManager mouseManager;

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);

	}

	public class InCombat {
		String name;
		LocalDateTime value;

		InCombat(String name, LocalDateTime newvalue) {
			this.name = name;
			this.value = newvalue;
		}

	}

	static List<InCombat> InCombatList = new ArrayList<>();

	static boolean GetInList(String name) {
		for (InCombat p : InCombatList) {
			if (p.name.contains(name)) {
				return true;
			}
		}

		return false;
	}

	static void RemoveFromList(String name) {
		for (InCombat p : InCombatList) {
			if (p.name.contains(name) && p.name.length() == name.length()) {
				InCombatList.remove(p);
			}
		}

	}

	public static String removeTags(String str, boolean removeLevels)
	{
		int strLen = str.length();
		if (removeLevels)
		{
			int levelIdx =  StringUtils.lastIndexOf(str, "  (level");
			if (levelIdx >= 0)
			{
				strLen = levelIdx;
			}
		}

		int open, close;
		if ((open = StringUtils.indexOf(str, '<')) == -1
				|| (close = StringUtils.indexOf(str, '>', open)) == -1)
		{
			return strLen == str.length() ? str : str.substring(0, strLen - 1);
		}

		// If the string starts with a < we can maybe take a shortcut if this
		// is the only tag in the string (take the substring after it)
		if (open == 0)
		{
			if ((open = close + 1) >= strLen)
			{
				return "";
			}

			if ((open = StringUtils.indexOf(str, '<', open)) == -1
					|| (StringUtils.indexOf(str, '>', open)) == -1)
			{
				return StringUtils.substring(str, close + 1);
			}

			// Whoops, at least we know the last value so we can go back to where we were
			// before :)
			open = 0;
		}

		SB.setLength(0);
		int i = 0;
		do
		{
			while (open != i)
			{
				SB.append(str.charAt(i++));
			}

			i = close + 1;
		}
		while ((open = StringUtils.indexOf(str, '<', close)) != -1
				&& (close = StringUtils.indexOf(str, '>', open)) != -1
				&& i < strLen);

		while (i < strLen)
		{
			SB.append(str.charAt(i++));
		}

		return SB.toString();
	}

	boolean isNamePlayer(String name)
	{
		boolean returnval = false;

		for (Player p : client.getPlayers()) {
			if (p.getName().contains(name) && p.getName().length() == name.length()) {
				returnval = true;
			}
		}

		return returnval;
	}


	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded) {

		if (menuEntryAdded.getTarget().length() > 3 && menuEntryAdded.getTarget().contains("  (level")) {

			String name = removeTags(menuEntryAdded.getTarget(), true);
			
			if (isNamePlayer(name)) {

				MenuEntry[] menuEntries = this.client.getMenuEntries();
				final MenuEntry lastEntry = menuEntries[menuEntries.length - 1];
				lastEntry.setTarget(menuEntryAdded.getTarget() + "<col=FF1E1E> (UnSkulled)");

				for (InCombat p : InCombatList) {
					if (p.name.contains(name) && p.name.length() == name.length()) {
						lastEntry.setTarget(menuEntryAdded.getTarget() + "<col=26E119> (Skulled)");
					}
				}


				if (config.RemoveAttack()) {

					if (lastEntry.getOption().contains("Attack") && lastEntry.getTarget().contains("<col=FF1E1E> (UnSkulled)")) {

						final MenuEntry[] array;
						menuEntries = (array = Arrays.copyOf(menuEntries, menuEntries.length - 1));
					}

				}

				this.client.setMenuEntries(menuEntries);

			}

		}

	}
	
	@Subscribe
	public void onAnimationChanged(final AnimationChanged event) {
		//client.getLogger().info(event.getActor().getName());

		Actor theplayer = event.getActor();

		if (theplayer instanceof Player) {
			if (theplayer.getInteracting() == client.getLocalPlayer() && (getMageMeleeAnims().test(theplayer.getAnimation()) || getMeleeAnims().test(theplayer.getAnimation()) || getRangeAnims().test(theplayer.getAnimation()) || getMagicAnims().test(theplayer.getAnimation()))) {
				if (!GetInList(theplayer.getName())) {
					InCombatList.add(new InCombat(theplayer.getName(), LocalDateTime.now())); // add to list if they are attacking us
				}
			}
		}

	}

	@Subscribe
	public void onGameTick(GameTick event)
	{

	}

}
