package cs224n.coref;

import cs224n.util.Pair;

import java.util.Set;

/**
 * @author Gabor Angeli (angeli at cs.stanford)
 */
public interface Feature {

  //-----------------------------------------------------------
  // TEMPLATE FEATURE TEMPLATES
  //-----------------------------------------------------------
  public static class PairFeature implements Feature {
    public final Pair<Feature,Feature> content;
    public PairFeature(Feature a, Feature b){ this.content = Pair.make(a, b); }
    public String toString(){ return content.toString(); }
    public boolean equals(Object o){ return o instanceof PairFeature && ((PairFeature) o).content.equals(content); }
    public int hashCode(){ return content.hashCode(); }
  }

  public static abstract class Indicator implements Feature {
    public final boolean value;
    public Indicator(boolean value){ this.value = value; }
    public boolean equals(Object o){ return o instanceof Indicator && o.getClass().equals(this.getClass()) && ((Indicator) o).value == value; }
    public int hashCode(){ 
    	return this.getClass().hashCode() ^ Boolean.valueOf(value).hashCode(); }
    public String toString(){ 
    	return this.getClass().getSimpleName() + "(" + value + ")"; }
  }

  public static abstract class IntIndicator implements Feature {
    public final int value;
    public IntIndicator(int value){ this.value = value; }
    public boolean equals(Object o){ return o instanceof IntIndicator && o.getClass().equals(this.getClass()) && ((IntIndicator) o).value == value; }
    public int hashCode(){ 
    	return this.getClass().hashCode() ^ value; 
    }
    public String toString(){ return this.getClass().getSimpleName() + "(" + value + ")"; }
  }

  public static abstract class BucketIndicator implements Feature {
    public final int bucket;
    public final int numBuckets;
    public BucketIndicator(int value, int max, int numBuckets){
      this.numBuckets = numBuckets;
      bucket = value * numBuckets / max;
      if(bucket < 0 || bucket >= numBuckets){ throw new IllegalStateException("Bucket out of range: " + value + " max="+max+" numbuckets="+numBuckets); }
    }
    public boolean equals(Object o){ return o instanceof BucketIndicator && o.getClass().equals(this.getClass()) && ((BucketIndicator) o).bucket == bucket; }
    public int hashCode(){ return this.getClass().hashCode() ^ bucket; }
    public String toString(){ return this.getClass().getSimpleName() + "(" + bucket + "/" + numBuckets + ")"; }
  }

  public static abstract class Placeholder implements Feature {
    public Placeholder(){ }
    public boolean equals(Object o){ return o instanceof Placeholder && o.getClass().equals(this.getClass()); }
    public int hashCode(){ return this.getClass().hashCode(); }
    public String toString(){ return this.getClass().getSimpleName(); }
  }

  public static abstract class StringIndicator implements Feature {
    public final String str;
    public StringIndicator(String str){ this.str = str; }
    public boolean equals(Object o){ return o instanceof StringIndicator && o.getClass().equals(this.getClass()) && ((StringIndicator) o).str.equals(this.str); }
    public int hashCode(){ return this.getClass().hashCode() ^ str.hashCode(); }
    public String toString(){ return this.getClass().getSimpleName() + "(" + str + ")"; }
  }

  public static abstract class SetIndicator implements Feature {
    public final Set<String> set;
    public SetIndicator(Set<String> set){ this.set = set; }
    public boolean equals(Object o){ return o instanceof SetIndicator && o.getClass().equals(this.getClass()) && ((SetIndicator) o).set.equals(this.set); }
    public int hashCode(){ return this.getClass().hashCode() ^ set.hashCode(); }
    public String toString(){
      StringBuilder b = new StringBuilder();
      b.append(this.getClass().getSimpleName());
      b.append("( ");
      for(String s : set){
        b.append(s).append(" ");
      }
      b.append(")");
      return b.toString();
    }
  }
  
  /*
   * TODO: If necessary, add new feature types
   */

  //-----------------------------------------------------------
  // REAL FEATURE TEMPLATES
  //-----------------------------------------------------------

  public static class CoreferentIndicator extends Indicator {
    public CoreferentIndicator(boolean coreferent){ super(coreferent); }
  }

  public static class ExactMatch extends Indicator {
    public ExactMatch(boolean exactMatch){ super(exactMatch); }
  }
  
  public static class DistInMentions extends IntIndicator {
	  public DistInMentions(int dist){ super(dist); }
  }
  
  public static class DistInSentences extends IntIndicator {
	  public DistInSentences (int dist){ super(dist); }
  }
  
  public static class Pronoun extends Indicator {
	  public Pronoun(boolean isPronoun) { super(isPronoun); } 
  }
  
  public static class NamedEntType extends StringIndicator {
	  public NamedEntType(String name) {super(name); }
  }
  
  //My type of features
  public static class InexactMatch extends Indicator {
	  public InexactMatch (boolean val){ super(val); }
  }
  
  public static class ApproxMatch extends Indicator {
	  public ApproxMatch (boolean val){ super(val); }
  }
  
  public static class LongCandidate extends Indicator {
	  public LongCandidate (boolean val){ super(val); }
  }

  public static class LongMention extends Indicator {
	  public LongMention (boolean val){ super(val); }
  }

  public static class NumContains extends IntIndicator {
	  public NumContains (int dist){ super(dist); }
  }

  public static class NumContained extends IntIndicator {
	  public NumContained (int dist){ super(dist); }
  }

  public static class SubStringOf extends Indicator {
	  public SubStringOf (boolean val){ super(val); }
  }
  public static class SubStrings extends Indicator {
	  public SubStrings (boolean val){ super(val); }
  }

  public static class YouAndI extends Indicator {
	  public YouAndI (boolean val){ super(val); }
  }

  public static class SameTalker extends Indicator {
	  public SameTalker(boolean val){ super(val); }
  }
  
  public static class SamePerson extends Indicator {
	  public SamePerson(boolean val){ super(val); }
  }

  public static class SameGender extends Indicator {
	  public SameGender(boolean val){ super(val); }
  }
  
  public static class SameNumber extends Indicator {
	  public SameNumber (boolean val) {super(val);}
  }

  public static class SameSentence extends Indicator {
	  public SameSentence(boolean val){ super(val); }
  }

  public static class NearSentence extends Indicator {
	  public NearSentence(boolean val){ super(val); }
  }

  public static class ThisIsIt extends Indicator {
	  public ThisIsIt (boolean val) {super(val);}
  }
  
  public static class HeadMatch extends Indicator {
	  public HeadMatch (boolean val) {super(val);}
  }
  
  public static class First15A extends Indicator{
	  public First15A (boolean val) {super(val);}
  }

  public static class First15B extends Indicator{
	  public First15B (boolean val) {super(val);}
  }
  /*
   * TODO: Add values to the indicators here.
   */

}
