package cs224n.corefsystems;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.*;


import cs224n.coref.ClusteredMention;
import cs224n.coref.Document;
import cs224n.coref.*;
import cs224n.util.Pair;

public class BetterBaseline implements CoreferenceSystem {

	@Override
	public void train(Collection<Pair<Document, List<Entity>>> trainingData) {
		// TODO Auto-generated method stub
		// Gather some statistics we can use for runCoreference?
	}

	@Override
	public List<ClusteredMention> runCoreference(Document doc) {
		// TODO Auto-generated method stub

		ArrayList<ClusteredMention> clusters = new ArrayList<ClusteredMention>();
		Map<String,Entity> entities =  new HashMap<String,Entity>();
		String prevMentionString = "";
		for (Mention m : doc.getMentions()) {
			// First Pass: Assign mention to the next mention (n-1 ClusteredMentions)
			if(entities.containsKey(prevMentionString)){
				// create clusteredmention with previous mention
				clusters.add(m.markCoreferent(entities.get(prevMentionString)));	
			}
			// create new cluster everytime (to be marked coreferent at next step
			ClusteredMention newCluster = m.markSingleton();
			clusters.add(newCluster);
			entities.put(m.gloss(),newCluster.entity);
			prevMentionString = m.gloss();
		}

	return clusters;
	}

}
