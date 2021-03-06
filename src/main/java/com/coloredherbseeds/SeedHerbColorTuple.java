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

import java.util.Arrays;
import java.util.Set;
import lombok.Value;

@Value
public class SeedHerbColorTuple
{
	short[] seedColors;
	short[] herbColors;

	public SeedHerbColorTuple(short[] seed, short[] herb)
	{
		int l1 = seed.length;
		int l2 = herb.length;

		this.seedColors = seed;

		if (l1 < l2)
		{
			this.herbColors = Arrays.copyOf(herb, l1);
		}
		else if (l1 > l2)
		{
			short[] nherb = new short[l1];
			for (int i = 0; i < l1; i++)
			{
				nherb[i] = herb[i % l2];
			}
			this.herbColors = nherb;
		}
		else
		{
			this.herbColors = herb;
		}
	}

	public SeedHerbColorTuple(Set<Short> seed, Set<Short> herb)
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