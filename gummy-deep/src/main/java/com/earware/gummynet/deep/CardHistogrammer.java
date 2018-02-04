package com.earware.gummynet.deep;

import java.io.*;
import java.util.*;

import com.earware.gummynet.gin.Card;
import com.earware.gummynet.gin.Deck;

public class CardHistogrammer {

	public static InputStream in = System.in;
	public static PrintStream out = System.out;
	
	public static void main(String[] args) {
		try {
			LineNumberReader in = new LineNumberReader(new InputStreamReader(System.in));
			String line;
			String model = null;
			int[] counts = new int[52];
			int linesRead=0;
			while ((line=in.readLine())!=null) {
				linesRead++;
				if (line.startsWith("zoo")) {
					if (model!=null) {
						output(model, counts);
					}
					for (int ppp=0; ppp<counts.length; ppp++) {counts[ppp]=0;}
					model=line;
				} else {
					StringTokenizer tok = new StringTokenizer(line);
					int i=0;
					int count=0;
					while (tok.hasMoreTokens()) {
						String token = tok.nextToken();
						i++;
						if (i==1) {
							count = Integer.parseInt(token);
						} else if (i>3) {
							String card = token;
							if (card.startsWith("[")) {
								card=card.substring(1);
							}
							if (!card.equals("]")) {
								int ndx=Deck.card2Int(Card.fromString(card));
								counts[ndx]+=count;
							}
						}
					}
				}
			}
			output(model, counts);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void output(String model, int[] counts) {
		int distinct=0;
		int total=0;
		for (int i=0; i<counts.length; i++) {
			if (counts[i]>0) {
				distinct++;
				total+=counts[i];
			}
		}
		out.println(model + "  distinct cards: " + distinct + "  winning hands=" + total/7 + "/(" + total + ")");
		for (int i=0; i<counts.length; i++) {
			if (counts[i]>0) {
				out.println(Deck.int2Card(i) + " " + counts[i]);
			}
		}
	}
}
