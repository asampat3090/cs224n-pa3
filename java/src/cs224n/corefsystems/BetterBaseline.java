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
		if(doc.getMentions().size() % 2 == 0) {// if even
			for (int i = 0; i<(doc.getMentions().size()-1);i=i+2) {
				Mention thisMention = doc.getMentions().get(i);
				Mention nextMention = doc.getMentions().get(i+1);
				// First Pass: Assign mention to the next mention (n-1 ClusteredMentions)
				ClusteredMention thisCluster = thisMention.markSingleton();
				ClusteredMention newCluster = nextMention.markCoreferent(thisCluster.entity);
				clusters.add(thisCluster);
				clusters.add(newCluster);	
			}
		} else { // if odd
			clusters.add(doc.getMentions().get(0).markSingleton());
			if(doc.getMentions().size()>1) {
				for (int i = 1; i<(doc.getMentions().size()-1);i=i+2) {
					Mention thisMention = doc.getMentions().get(i);
					Mention nextMention = doc.getMentions().get(i+1);
					// First Pass: Assign mention to the next mention (n-1 ClusteredMentions)
					ClusteredMention thisCluster = thisMention.markSingleton();
					ClusteredMention newCluster = nextMention.markCoreferent(thisCluster.entity);
					clusters.add(thisCluster);
					clusters.add(newCluster);	
				}	
			}
		}
		return clusters;
	}

}
