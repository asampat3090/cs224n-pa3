package cs224n.corefsystems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs224n.coref.ClusteredMention;
import cs224n.coref.Document;
import cs224n.coref.Entity;
import cs224n.coref.Mention;
import cs224n.util.Pair;

public class RuleBased implements CoreferenceSystem {

	@Override
	public void train(Collection<Pair<Document, List<Entity>>> trainingData) {
		// TODO Auto-generated method stub
		// Do nothing - unless statistics needed

	}

	@Override
	public List<ClusteredMention> runCoreference(Document doc) {
		// TODO Auto-generated method stub
		List<ClusteredMention> mentions = new ArrayList<ClusteredMention>();
		Map<String,Entity> clusters = new HashMap<String,Entity>();
		// Exact head matching 
		//(for each mention...)
		for(Mention m : doc.getMentions()){
			//(...get its text)
			String mentionStringHead = m.headWord();
			//(...if we've seen the head of this text before...)
			if(clusters.containsKey(mentionStringHead)){
				//(...add it to the cluster)
				mentions.add(m.markCoreferent(clusters.get(mentionStringHead)));
			} else {
				//(...else create a new singleton cluster)
				ClusteredMention newCluster = m.markSingleton();
				mentions.add(newCluster);
				clusters.put(mentionStringHead,newCluster.entity);
			}
		}
		//(return the mentions)
		return mentions;
	}

}
