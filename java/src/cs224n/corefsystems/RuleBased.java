package cs224n.corefsystems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Math;

import cs224n.coref.ClusteredMention;
import cs224n.coref.Document;
import cs224n.coref.Entity;
import cs224n.coref.Mention;
import cs224n.coref.Util;
import cs224n.coref.Pronoun;
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
		
		// Using sieve process - high precision to low precision
		
		// Additive rules //
		
		// Step 0 : Case insensitive string match
	    //(for each mention...)
	    for(Mention m : doc.getMentions()){
	      //(...get its text)
	      String mentionString = m.gloss();
	      //(...if we've seen this text before...)
	      if(clusters.containsKey(mentionString.toLowerCase())){
	        //(...add it to the cluster)
	        mentions.add(m.markCoreferent(clusters.get(mentionString.toLowerCase())));
	      } else {
	        //(...else create a new singleton cluster)
	        ClusteredMention newCluster = m.markSingleton();
	        mentions.add(newCluster);
	        clusters.put(mentionString.toLowerCase(),newCluster.entity);
	      }
	    }			
		
		// Step 1 : Case insensitive head matching 
		// Loop through each of the clustered mentions (= # mentions in doc)
		for(ClusteredMention cm : mentions){
			// Loop through each of the clusters
			for (Map.Entry<String, Entity> entry : clusters.entrySet()) {
				Entity cluster = entry.getValue();
				// check if head word matches any of the mentions in the cluster
				for(Mention mention : cluster.mentions) {
					// Re cluster current clustered mention to new entity if matches case insensitive head word in entity
					if(cm.mention.headWord().toLowerCase().equals(mention.headWord().toLowerCase())) {
						cm=cm.mention.markCoreferent(cluster);
						break;
					}
				}
			}
		}
		
		// Step 2 : Case insensitive word inclusion
		// Loop through each of the clustered mentions(= # mentions in doc)
		for(ClusteredMention cm : mentions){
			// Loop through each of the clusters
			for (Map.Entry<String, Entity> entry : clusters.entrySet()) {
				Entity cluster = entry.getValue();
				// check if head word matches any of the mentions in the cluster
				for(Mention mention : cluster.mentions) {
					// Re cluster current clustered mention to new entity if matches a head word in entity
					if(cm.mention.headWord().toLowerCase().contains(mention.headWord().toLowerCase())) {
						cm=cm.mention.markCoreferent(cluster);
						break;
					}
				}
			}
		}
		
		// Subtractive Rules //
		
		// Step 2: Gender and number consistency 
		// reallocate if mention and entity have different 
		for(ClusteredMention cm: mentions ) {
			// Check if mention and entity in cm have same gender o/w remove coref
			Pair<Boolean,Boolean> res = new Pair<Boolean,Boolean>(null, null); 
			Pair<Boolean,Boolean> res2 = new Pair<Boolean,Boolean>(null, null); 
			res = Util.haveGenderAndAreSameGender(cm.mention, cm.entity);
			res2 = Util.haveNumberAndAreSameNumber(cm.mention, cm.entity);
			if(((res.getFirst() && !res.getSecond()) || (res2.getFirst() && !res2.getSecond())) && cm.entity.mentions.size()>1) {
				// Remove coreference 
				cm.mention.removeCoreference();
				// Allocate mention to singleton cluster
				//mentions.add(cm.mention.markSingleton());
				cm.mention.markSingleton();
			}
		}
		
		// Step 3: Pronouns rules 
		// Check pronoun sentence distance <3  
		for(ClusteredMention cm: mentions) {
			// check if mention is pronoun - then apply constraint 
			if(Pronoun.isSomePronoun(cm.mention.headWord())) {
				int pronounSentInd = doc.indexOfSentence(cm.mention.sentence); 
				for(Mention m:cm.entity.mentions) {
					int tempLen = doc.indexOfSentence(m.sentence);
					if(Math.abs(tempLen-pronounSentInd)>3) {
						// Remove coreference 
						cm.mention.removeCoreference();
						// Allocate mention to singleton cluster
						//mentions.add(cm.mention.markSingleton());
						cm.mention.markSingleton();
					}
				}
			}
		}
		
		// Step 4: Named Entity Rules
		
		
		
		//(return the mentions)
		return mentions;
	}

}
