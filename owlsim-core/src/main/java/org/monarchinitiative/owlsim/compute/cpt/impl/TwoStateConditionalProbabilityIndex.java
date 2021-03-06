package org.monarchinitiative.owlsim.compute.cpt.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.ConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * An implementation of {@link ConditionalProbabilityIndex} in which only two states are
 * possible:
 * 
 *  0 = u (unknown/false/off)
 *  1 = t (true/on)
 *  
 * TODO: Use Abstract parent class
 * 
 * @author cjm
 *
 */
public class TwoStateConditionalProbabilityIndex implements ConditionalProbabilityIndex {

	private Logger LOG = Logger.getLogger(TwoStateConditionalProbabilityIndex.class);
	BMKnowledgeBase kb;
	private char UNKNOWN = 'u';
	private char ON = 't';
	private char[] STATES = { UNKNOWN, ON };

	Double[][] conditionalProbabilityByChildParentState;
	Map<Integer,Character>[][] parentStateMapByIndex;
	
	/**
	 * @param size
	 */
	public TwoStateConditionalProbabilityIndex(int size) {
		super();
		init(size);
	}
	/**
	 * @param kb
	 */
	public TwoStateConditionalProbabilityIndex(BMKnowledgeBase kb) {
		super();
		this.kb = kb;
		init(kb.getNumClassNodes());
	}
	private void init(int size) {
		conditionalProbabilityByChildParentState = new Double[size][];
		parentStateMapByIndex = 
				 (Map<Integer,Character>[][])new Map[size][];
	}
	
	/**
	 * @param kb
	 * @return CPI
	 */
	public static ConditionalProbabilityIndex create(BMKnowledgeBase kb) {
		return new TwoStateConditionalProbabilityIndex(kb.getNumClassNodes());
	}
	
	/**
	 * @param size
	 * @return CPI
	 */
	public static ConditionalProbabilityIndex create(int size) {
		return new TwoStateConditionalProbabilityIndex(size);
	}

	public Double getConditionalProbabilityChildIsOn(int clsIndex, int parentsState) {
		return conditionalProbabilityByChildParentState[clsIndex][parentsState];
	}
	public Map<Integer, Character> getParentsToStateMapping(int clsIndex, int parentsState) {
		return parentStateMapByIndex[clsIndex][parentsState];
	}
	public int getNumberOfParentStates(int clsIndex) {
		return parentStateMapByIndex[clsIndex] == null ?
				0 : parentStateMapByIndex[clsIndex].length;
	}
	
	public void setConditionalProbabilityChildIsOn(int childClassIndex, int parentsState, int numStates, double cp) throws IncoherentStateException {
		if (conditionalProbabilityByChildParentState[childClassIndex] == null)
			conditionalProbabilityByChildParentState[childClassIndex] = new Double[numStates];
		if (cp < 0.0) {
			throw new IncoherentStateException("Pr(C|Parents)="+cp);
		}
		if (cp > 1.0) {
			throw new IncoherentStateException("Pr(C|Parents)="+cp);
		}
		conditionalProbabilityByChildParentState[childClassIndex][parentsState] = cp;
	}

	public void calculateConditionalProbabilities(BMKnowledgeBase kb) throws IncoherentStateException {
		this.kb = kb;
		//int[] icpca = kb.getIndividualCountPerClassArray();
		int totalInds = kb.getIndividualsBM(kb.getRootIndex()).cardinality();

		LOG.info("Calculating all CPTs...");
		for (String cid : kb.getClassIdsInSignature()) {
			LOG.debug("   Calculating CPT for "+cid);
			int cix = kb.getClassIndex(cid);
			int numIndividualsForChild = kb.getIndividualsBM(cix).cardinality();

			EWAHCompressedBitmap sups = kb.getDirectSuperClassesBM(cid);
			List<Integer> pixs = sups.getPositions(); // ASSUME STABLE ORDERING
			int numParents = pixs.size();
			
			// assume two states for now: will be extendable to yes, no, unknown
			int numStates = (int) Math.pow(2, numParents);

			if (numParents == 0) {
				LOG.debug("Root: "+cid);
				continue;
			}
			if (parentStateMapByIndex[cix] == null)
				parentStateMapByIndex[cix] = new Map[numStates];

			for (int parentState=0; parentState<numStates; parentState++) {
				
				// Pr(C=on | P1=S1, ..., Pn=Sn) = |C| / |{ p in P & P=on } |
				
				Map<Integer, Character> parentStateMap = calculateParentStateMapForIndex(parentState, pixs);

				EWAHCompressedBitmap allIndsForOnParentsBM = null;
				for (int pix : parentStateMap.keySet()) {
					char state = parentStateMap.get(pix);
					if (state == ON) {
						EWAHCompressedBitmap indsBM = kb.getIndividualsBM(pix);
						if (allIndsForOnParentsBM == null)
							allIndsForOnParentsBM = indsBM;
						else
							allIndsForOnParentsBM = allIndsForOnParentsBM.and(indsBM);
					}
					
				}

				int numIndividualsForOnParents = 
						allIndsForOnParentsBM == null ? 
								totalInds : allIndsForOnParentsBM.cardinality();
				double conditionalProbability = 
						numIndividualsForChild / (double) numIndividualsForOnParents;
				LOG.debug("  CP for "+parentStateMap+" = "+numIndividualsForChild+"/"+numIndividualsForOnParents+" = "+conditionalProbability);
				setConditionalProbabilityChildIsOn(cix, parentState, 
						numStates, conditionalProbability);
				parentStateMapByIndex[cix][parentState] = parentStateMap;
			}
			
		}
		LOG.info("DONE Calculating all CPTs");

	}
	
	private Map<Integer,Character> calculateParentStateMapForIndex(int parentState, 
			List<Integer> parentIxs) {
		int numStateTypes = STATES.length;
		Map<Integer,Character> parentStateMap = new HashMap<Integer,Character>();
		for (int i=0; i<parentIxs.size(); i++) {
			int mod = parentState % numStateTypes;
			Integer p = parentIxs.get(i);
			parentStateMap.put(p, STATES[mod]);
			parentState = parentState / numStateTypes;
		}
		
		return parentStateMap;
	}
}
