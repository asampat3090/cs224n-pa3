package cs224n.corefsystems;

import cs224n.coref.*;
import cs224n.util.Pair;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.classify.LinearClassifierFactory;
import edu.stanford.nlp.classify.RVFDataset;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.Triple;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import edu.stanford.nlp.util.logging.StanfordRedwoodConfiguration;

import java.text.DecimalFormat;
import java.util.*;

import static edu.stanford.nlp.util.logging.Redwood.Util.*;

/**
 * @author Gabor Angeli (angeli at cs.stanford)
 */
public class ClassifierBased implements CoreferenceSystem {

	private static <E> Set<E> mkSet(E[] array){
		Set<E> rtn = new HashSet<E>();
		Collections.addAll(rtn, array);
		return rtn;
	}

	private static final Set<Object> ACTIVE_FEATURES = mkSet(new Object[]{

			/*
			 * TODO: Create a set of active features
			 */

			Feature.SamePerson.class,	
			Feature.SameGender.class,	
			Feature.SameNumber.class,
			Feature.HeadMatch.class,
			Feature.SameSentence.class,	
			Pair.make(Feature.NearSentence.class, Feature.YouAndI.class), 

//			Feature.ExactMatch.class,	
//			Feature.ApproxMatch.class,
//			Feature.DistInMentions.class,
//			Feature.DistInSentences.class,
//			Feature.Pronoun.class, 		
//			Feature.InexactMatch.class,	
//			Feature.LongCandidate.class,
//			Feature.LongMention.class,	
//			Feature.NumContains.class,	
//			Feature.NumContained.class,	
//			Feature.SubStringOf.class,	
//			Feature.SubStrings.class,	
//			Feature.YouAndI.class,		
//			Feature.SameTalker.class,	
//			Feature.OddSentence.class, 	
//			Feature.ThisIsIt.class,		
//			Feature.First15A.class,
//			Feature.First15B.class,

			//skeleton for how to create a pair feature
//			Pair.make(Feature.NumContained.class, Feature.NumContains.class),
//			Pair.make(Feature.SameTalker.class, Feature.YouAndI.class), // 0	
//			Pair.make(Feature.SameNumber.class, Feature.YouAndI.class), // 0	
//			Pair.make(Feature.SameNumber.class, Feature.SamePerson.class), 
//			Pair.make(Feature.LongCandidate.class, Feature.LongMention.class), //0
//			Pair.make(Feature.ThisIsIt.class, Feature.LongCandidate.class),
//			Pair.make(Feature.SameNumber.class, Feature.SamePerson.class),
//			Pair.make(Feature.ExactMatch.class, Feature.InexactMatch.class),
//			Pair.make(Feature.Pronoun.class, Feature.SameSentence.class), //0
//			Pair.make(Feature.SameNumber.class, Feature.SameGender.class),//-1
//			Pair.make(Feature.Pronoun.class, Feature.DistInMentions.class),//-1.5
//			Pair.make(Feature.First15A.class, Feature.First15B.class),
//			Pair.make(Feature.OddSentence.class, Feature.SamePerson.class), //  
	});


	private LinearClassifier<Boolean,Feature> classifier;

	public ClassifierBased(){
		StanfordRedwoodConfiguration.setup();
		RedwoodConfiguration.current().collapseApproximate().apply();
	}

	public FeatureExtractor<Pair<Mention,ClusteredMention>,Feature,Boolean> extractor = 
	   new FeatureExtractor<Pair<Mention, ClusteredMention>, Feature, Boolean>() {
		private <E> Feature feature(Class<E> clazz, Pair<Mention,ClusteredMention> input, Option<Double> count){
			
			//--Variables
			Mention onPrix = input.getFirst(); //the first mention (referred to as m_i in the handout)
			Mention candidate = input.getSecond().mention; //the second mention (referred to as m_j in the handout)
			Entity candidateCluster = input.getSecond().entity; //the cluster containing the second mention
			String aA = onPrix.gloss();
			String a = aA.toLowerCase();
			String bB = candidate.gloss();
			String b = bB.toLowerCase();
			Document doc = onPrix.doc;
			int aSentIndex = doc.indexOfSentence(onPrix.sentence);
			int bSentIndex = doc.indexOfSentence(candidate.sentence);

			//--Features
			if(clazz.equals(Feature.ExactMatch.class)){
				return new Feature.ExactMatch(aA.equals(bB));

			} else if(clazz.equals(Feature.InexactMatch.class)) {
				return new Feature.InexactMatch((!aA.equals(bB)) && a.equals(b));
				
			} else if(clazz.equals(Feature.ApproxMatch.class)) {
				return new Feature.ApproxMatch(a.equals(b));
				
			} else if(clazz.equals(Feature.HeadMatch.class)) {
				return new Feature.HeadMatch(onPrix.headWord().toLowerCase().equals(
											candidate.headWord().toLowerCase()));
				
			} else if(clazz.equals(Feature.DistInMentions.class)) {
				return new Feature.DistInMentions(
						doc.indexOfMention(onPrix) - doc.indexOfMention(candidate));
			
			} else if(clazz.equals(Feature.DistInSentences.class)) {
				return new Feature.DistInSentences(aSentIndex - bSentIndex);
				
			} else if(clazz.equals(Feature.Pronoun.class)) {
				return new Feature.Pronoun(Pronoun.isSomePronoun(aA) && Pronoun.isSomePronoun(bB));
			
			} else if(clazz.equals(Feature.NumContained.class)) {
				int numContained = 0;
				for(String word : onPrix.text())
					if (candidate.text().contains(word)) numContained++;
				return new Feature.NumContained(numContained);

			} else if(clazz.equals(Feature.NumContains.class)) {
				int numContains = 0;
				for(String word : candidate.text())
					if (onPrix.text().contains(word)) numContains++;
				return new Feature.NumContains(numContains);
				
			} else if(clazz.equals(Feature.SubStringOf.class)) {
				boolean ret = false;
				for(Mention m : candidateCluster.mentions)
					ret = ret || m.gloss().toLowerCase().contains(a);
				return new Feature.SubStringOf(ret);
			
			} else if(clazz.equals(Feature.SubStrings.class)) {
				boolean ret = false;
				for(Mention m : candidateCluster.mentions)
					ret = ret || a.contains(m.gloss().toLowerCase());
				return new Feature.SubStrings(ret);
						
			} else if(clazz.equals(Feature.SameTalker.class)) {
				return new Feature.SameTalker(
						onPrix.headToken().speaker().equals(
						candidate.headToken().speaker())		);
			
			} else if(clazz.equals(Feature.YouAndI.class)) {
				Pair<Boolean, Boolean> pair = Util.firstAndSecond(onPrix, candidate);
				if (!pair.getFirst()) count.set(0.0);
				return new Feature.YouAndI(pair.getSecond());

			} else if(clazz.equals(Feature.SameGender.class)) {
				Pair<Boolean, Boolean> pair = Util.haveGenderAndAreSameGender(onPrix, candidate);
				if (!pair.getFirst()) count.set(0.0);
				return new Feature.SameGender(pair.getSecond());
				
			} else if(clazz.equals(Feature.SamePerson.class)) {
				Pair<Boolean, Boolean> pair = Util.havePersonAndAreSamePerson(onPrix, candidate);
				if (!pair.getFirst()) count.set(0.0);
				return new Feature.SamePerson(pair.getSecond());
				
			} else if(clazz.equals(Feature.SameNumber.class)) {
				Pair<Boolean, Boolean> pair = Util.haveNumberAndAreSameNumber(onPrix, candidate);
				if (!pair.getFirst()) count.set(0.0);
				return new Feature.SameNumber(pair.getSecond());
			
			} else if(clazz.equals(Feature.SameSentence.class)) {
				return new Feature.SameSentence(aSentIndex > bSentIndex + 2);
				
			} else if(clazz.equals(Feature.NearSentence.class)) {
				int dist = aSentIndex - bSentIndex;
				if (dist == 0) count.set(0.0);
				return new Feature.NearSentence(dist <= 3);

			} else if(clazz.equals(Feature.ThisIsIt.class)) {
				return new Feature.ThisIsIt(a.equalsIgnoreCase("it"));
				
			} else if(clazz.equals(Feature.LongCandidate.class)) {
				return new Feature.LongCandidate(candidate.length() > 2);
					
			} else if(clazz.equals(Feature.LongMention.class)) {
				return new Feature.LongMention(onPrix.length() > 2);			
				
			} else if(clazz.equals(Feature.First15A.class)) {
				return new Feature.First15A(onPrix.beginIndexInclusive > 8);			
				
			} else if(clazz.equals(Feature.First15B.class)) {
				return new Feature.First15B(candidate.beginIndexInclusive > 8);			
				
//			} else if(clazz.equals(Feature.NewFeature.class)) {
				/*
				 * TODO: Add features to return for specific classes. Implement calculating values of features here.
				 */
			
			} else {
				throw new IllegalArgumentException("Unregistered feature: " + clazz);
			}
		}

		@SuppressWarnings({"unchecked"})
		@Override
		protected void fillFeatures(Pair<Mention, ClusteredMention> input, Counter<Feature> inFeatures, Boolean output, Counter<Feature> outFeatures) {
			//--Input Features
			for(Object o : ACTIVE_FEATURES){
				if(o instanceof Class){
					//(case: singleton feature)
					Option<Double> count = new Option<Double>(1.0);
					Feature feat = feature((Class) o, input, count);
					if(count.get() > 0.0){
						inFeatures.incrementCount(feat, count.get());
					}
				} else if(o instanceof Pair){
					//(case: pair of features)
					Pair<Class,Class> pair = (Pair<Class,Class>) o;
					Option<Double> countA = new Option<Double>(1.0);
					Option<Double> countB = new Option<Double>(1.0);
					Feature featA = feature(pair.getFirst(), input, countA);
					Feature featB = feature(pair.getSecond(), input, countB);
					if(countA.get() * countB.get() > 0.0){
						inFeatures.incrementCount(new Feature.PairFeature(featA, featB), countA.get() * countB.get());
					}
				}
			}

			//--Output Features
			if(output != null){
				outFeatures.incrementCount(new Feature.CoreferentIndicator(output), 1.0);
			}
		}

		@Override
		protected Feature concat(Feature a, Feature b) {
			return new Feature.PairFeature(a,b);
		}
	};

	public void train(Collection<Pair<Document, List<Entity>>> trainingData) {
		startTrack("Training");
		//--Variables
		RVFDataset<Boolean, Feature> dataset = new RVFDataset<Boolean, Feature>();
		LinearClassifierFactory<Boolean, Feature> fact = new LinearClassifierFactory<Boolean,Feature>();
		//--Feature Extraction
		startTrack("Feature Extraction");
		for(Pair<Document,List<Entity>> datum : trainingData){
			//(document variables)
			Document doc = datum.getFirst();
			List<Entity> goldClusters = datum.getSecond();
			List<Mention> mentions = doc.getMentions();
			Map<Mention,Entity> goldEntities = Entity.mentionToEntityMap(goldClusters);
			startTrack("Document " + doc.id);
			//(for each mention...)
			for(int i=0; i<mentions.size(); i++){
				//(get the mention and its cluster)
				Mention onPrix = mentions.get(i);
				Entity source = goldEntities.get(onPrix);
				if(source == null){ throw new IllegalArgumentException("Mention has no gold entity: " + onPrix); }
				//(for each previous mention...)
				int oldSize = dataset.size();
				for(int j=i-1; j>=0; j--){
					//(get previous mention and its cluster)
					Mention cand = mentions.get(j);
					Entity target = goldEntities.get(cand);
					if(target == null){ throw new IllegalArgumentException("Mention has no gold entity: " + cand); }
					//(extract features)
					Counter<Feature> feats = extractor.extractFeatures(Pair.make(onPrix, cand.markCoreferent(target)));
					//(add datum)
					dataset.add(new RVFDatum<Boolean, Feature>(feats, target == source));
					//(stop if
					if(target == source){ break; }
				}
				//logf("Mention %s (%d datums)", MentionsonPrix.toString(), dataset.size() - oldSize);
			}
			endTrack("Document " + doc.id);
		}
		endTrack("Feature Extraction");
		//--Train Classifier
		startTrack("Minimizer");
		this.classifier = fact.trainClassifier(dataset);
		endTrack("Minimizer");
		//--Dump Weights
		startTrack("Features");
		//(get labels to print)
		Set<Boolean> labels = new HashSet<Boolean>();
		labels.add(true);
		//(print features)
		for(Triple<Feature,Boolean,Double> featureInfo : this.classifier.getTopFeatures(labels, 0.0, true, 100, true)){
			Feature feature = featureInfo.first();
			Boolean label = featureInfo.second();
			Double magnitude = featureInfo.third();
			log(FORCE,new DecimalFormat("0.000").format(magnitude) + " [" + label + "] " + feature);
		}
		end_Track("Features");
		endTrack("Training");
	}

	public List<ClusteredMention> runCoreference(Document doc) {
		//--Overhead
		startTrack("Testing " + doc.id);
		//(variables)
		List<ClusteredMention> rtn = new ArrayList<ClusteredMention>(doc.getMentions().size());
		List<Mention> mentions = doc.getMentions();
		int singletons = 0;
		//--Run Classifier
		for(int i=0; i<mentions.size(); i++){
			//(variables)
			Mention onPrix = mentions.get(i);
			int coreferentWith = -1;
			//(get mention it is coreferent with)
			for(int j=i-1; j>=0; j--){
				ClusteredMention cand = rtn.get(j);
				boolean coreferent = classifier.classOf(new RVFDatum<Boolean, Feature>(extractor.extractFeatures(Pair.make(onPrix, cand))));
				if(coreferent){
					coreferentWith = j;
					break;
				}
			}
			//(mark coreference)
			if(coreferentWith < 0){
				singletons += 1;
				rtn.add(onPrix.markSingleton());
			} else {
				//log("Mention " + onPrix + " coreferent with " + mentions.get(coreferentWith));
				rtn.add(onPrix.markCoreferent(rtn.get(coreferentWith)));
			}
		}
		//log("" + singletons + " singletons");
		//--Return
		endTrack("Testing " + doc.id);
		return rtn;
	}

	private class Option<T> {
		private T obj;
		public Option(T obj){ this.obj = obj; }
		public Option(){};
		public T get(){ return obj; }
		public void set(T obj){ this.obj = obj; }
		public boolean exists(){ return obj != null; }
	}
}
