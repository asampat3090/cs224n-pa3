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
		for (int i = 0; i<doc.getMentions().size()-1;i=i+2) {
			Mention thisMention = doc.getMentions().get(i);
			Mention nextMention = doc.getMentions().get(i+1);
			// First Pass: Assign mention to the next mention (n-1 ClusteredMentions)
			ClusteredMention thisCluster = thisMention.markSingleton();
			ClusteredMention newCluster = nextMention.markCoreferent(thisCluster);
			clusters.add(newCluster);	
		}

	return clusters;
	}

}
