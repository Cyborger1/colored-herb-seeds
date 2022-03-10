package com.coloredherbseeds;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ColoredHerbSeedsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ColoredHerbSeedsPlugin.class);
		RuneLite.main(args);
	}
}