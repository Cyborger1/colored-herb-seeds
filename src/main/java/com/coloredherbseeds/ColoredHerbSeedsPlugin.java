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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.inject.Inject;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
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
	@Value
	private static class SeedColorTuple
	{
		short[] seed;
		short[] herb;

		public SeedColorTuple(short[] seed, short[] herb)
		{
			int l1 = seed.length;
			int l2 = herb.length;

			this.seed = seed;

			if (l1 < l2)
			{
				this.herb = Arrays.copyOf(herb, l1);
			}
			else if (l1 > l2)
			{
				short[] nherb = new short[l1];
				for (int i = 0; i < l1; i++)
				{
					nherb[i] = herb[i % l2];
				}
				this.herb = nherb;
			}
			else
			{
				this.herb = herb;
			}
		}

		public SeedColorTuple(Set<Short> seed, Set<Short> herb)
		{
			this(convert(seed), convert(herb));
		}

		private static short[] convert(Set<Short> set)
		{
			short[] s = new short[set.size()];
			int i = 0;
			for (short v : set)
			{
				s[i++] = v;
			}
			return s;
		}
	}

	private static final ImmutableMap<Integer, Integer> SEED_TO_HERBS = ImmutableMap.<Integer, Integer>builder()
		.put(5291, 249)
		.put(5292, 251)
		.build();

	private ImmutableMap<Integer, SeedColorTuple> colorMap;

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
				ImmutableMap.Builder<Integer, SeedColorTuple> builder = ImmutableMap.builder();
				SEED_TO_HERBS.forEach((key, value) ->
				{
					ModelData seed = client.loadModelData(key);
					ModelData herb = client.loadModelData(value);

					LinkedHashSet<Short> uniqueSeed = new LinkedHashSet<>();
					LinkedHashSet<Short> uniqueHerb = new LinkedHashSet<>();

					for (short c : seed.getFaceColors())
					{
						uniqueSeed.add(c);
					}

					for (short c : herb.getFaceColors())
					{
						uniqueHerb.add(c);
					}

					builder.put(key, new SeedColorTuple(uniqueSeed, uniqueHerb));
				});

				colorMap = builder.build();
			}
		);

		resetCaches();
	}

	@Override
	protected void shutDown() throws Exception
	{
		colorMap = null;
		resetCaches();
	}

	@Subscribe
	public void onPostItemComposition(PostItemComposition event)
	{
		ItemComposition itemComposition = event.getItemComposition();
		int itemId = itemComposition.getId();
		SeedColorTuple colorData = colorMap.get(itemId);

		if (colorData == null)
		{
			return;
		}

		System.out.println(itemComposition.getColorToReplace());
		System.out.println(itemComposition.getColorToReplaceWith());

		try
		{
			itemComposition.setColorToReplace(colorData.seed);
			itemComposition.setColorToReplaceWith(colorData.herb);
		}
		catch (Exception e)
		{
			log.error("Could not modify the item composition", e);
		}
	}

	private void resetCaches()
	{
		clientThread.invokeLater(() ->
		{
			client.getItemCompositionCache().reset();
			client.getItemModelCache().reset();
			client.getItemSpriteCache().reset();
		});
	}
}
