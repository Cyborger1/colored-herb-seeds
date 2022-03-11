/*
BSD 2-Clause License

Copyright (c) 2021, Cyborger1
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.coloredherbseeds;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashSet;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.api.ModelData;
import net.runelite.api.events.PostItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Colored Herb Seeds",
	description = "Colors herb seeds to use the same palette as the herb they grow into.",
	tags = {"Herbs", "Seeds", "Farming"}
)
public class ColoredHerbSeedsPlugin extends Plugin
{
	private static final ImmutableMap<Integer, Integer> SEED_TO_HERBS = ImmutableMap.<Integer, Integer>builder()
		.put(ItemID.GUAM_SEED, ItemID.GUAM_LEAF)
		.put(ItemID.MARRENTILL_SEED, ItemID.MARRENTILL)
		.put(ItemID.TARROMIN_SEED, ItemID.TARROMIN)
		.put(ItemID.HARRALANDER_SEED, ItemID.HARRALANDER)
		.put(ItemID.RANARR_SEED, ItemID.RANARR_WEED)
		.put(ItemID.TOADFLAX_SEED, ItemID.TOADFLAX)
		.put(ItemID.IRIT_SEED, ItemID.IRIT_LEAF)
		.put(ItemID.AVANTOE_SEED, ItemID.AVANTOE)
		.put(ItemID.KWUARM_SEED, ItemID.KWUARM)
		.put(ItemID.SNAPDRAGON_SEED, ItemID.SNAPDRAGON)
		.put(ItemID.CADANTINE_SEED, ItemID.CADANTINE)
		.put(ItemID.LANTADYME_SEED, ItemID.LANTADYME)
		.put(ItemID.DWARF_WEED_SEED, ItemID.DWARF_WEED)
		.put(ItemID.TORSTOL_SEED, ItemID.TORSTOL)
		.build();

	private ImmutableMap<Integer, SeedHerbColorTuple> colorMap = null;

	@Inject
	Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemManager itemManager;

	@Override
	protected void startUp()
	{
		clientThread.invokeLater(() ->
			{
				ImmutableMap.Builder<Integer, SeedHerbColorTuple> builder = ImmutableMap.builder();
				SEED_TO_HERBS.forEach((key, value) ->
				{
					int seedID = itemManager.getItemComposition(key).getInventoryModel();
					int herbID = itemManager.getItemComposition(value).getInventoryModel();
					ModelData seedModel = client.loadModelData(seedID);
					ModelData herbModel = client.loadModelData(herbID);

					if (seedModel != null && herbModel != null)
					{
						LinkedHashSet<Short> uniqueSeed = new LinkedHashSet<>();
						LinkedHashSet<Short> uniqueHerb = new LinkedHashSet<>();

						for (short c : seedModel.getFaceColors())
						{
							uniqueSeed.add(c);
						}

						for (short c : herbModel.getFaceColors())
						{
							uniqueHerb.add(c);
						}

						builder.put(key, new SeedHerbColorTuple(uniqueSeed, uniqueHerb));
					}
				});

				colorMap = builder.build();

				client.getItemCompositionCache().reset();
				client.getItemModelCache().reset();
				client.getItemSpriteCache().reset();
			}
		);
	}

	@Override
	protected void shutDown() throws Exception
	{
		colorMap = null;
		clientThread.invokeLater(() ->
		{
			client.getItemCompositionCache().reset();
			client.getItemModelCache().reset();
			client.getItemSpriteCache().reset();
		});
	}

	@Subscribe
	public void onPostItemComposition(PostItemComposition event)
	{
		if (colorMap == null)
		{
			return;
		}

		ItemComposition itemComposition = event.getItemComposition();
		SeedHerbColorTuple colorData = colorMap.get(itemComposition.getId());

		if (colorData == null)
		{
			return;
		}

		itemComposition.setColorToReplace(colorData.getSeedColors());
		itemComposition.setColorToReplaceWith(colorData.getHerbColors());
	}
}
