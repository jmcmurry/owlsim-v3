package org.monarchinitiative.owlsim.compute.kb.impl;

import java.net.URISyntaxException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Tests a OWLAPI implementation of a KB
 * 
 * Also tests:
 *  - LabelMapper
 *  
 *
 */
public  class DisjointClassesTest extends AbstractOwlTest {

	private Logger LOG = Logger.getLogger(DisjointClassesTest.class);

	@Test
	public void negativeAssertionTest() throws OWLOntologyCreationException, URISyntaxException, NonUniqueLabelException {
		load("simple-pheno-with-negation.owl");
		int nOk = 0;
		for (String i : kb.getIndividualIdsInSignature()) {
			Set<String> nts = kb.getClassIds(kb.getNegatedTypesBM(i));
			LOG.info(i + " NTS="+nts);
			if (i.equals("http://x.org/ind-dec-all")) {
				if (nts.contains("http://x.org/inc-bone-length") && 
						nts.contains("http://x.org/hyperplastic-heart")) {
					nOk++;
				}
			}
			if (i.equals("http://x.org/ind-inc-all")) {
				if (nts.contains("http://x.org/dec-bone-length") && 
						nts.contains("http://x.org/hypoplastic-heart")) {
					nOk++;
					
				}
			}
		}
		Assert.assertTrue(nOk == 2);
	}

	/**
	 * Identical to previous test, but uses weaker annotation property
	 * rather than disjointness axioms
	 * 
	 * @throws OWLOntologyCreationException
	 * @throws URISyntaxException
	 * @throws NonUniqueLabelException
	 */
	@Test
	public void negativeAssertionAnnPropsTest() throws OWLOntologyCreationException, URISyntaxException, NonUniqueLabelException {
		load("simple-pheno-with-negation-with-annprops.owl");
		int nOk = 0;
		for (String i : kb.getIndividualIdsInSignature()) {
			Set<String> nts = kb.getClassIds(kb.getNegatedTypesBM(i));
			LOG.info(i + " DIRECT NTS="+kb.getClassIds(kb.getDirectNegatedTypesBM(i)));
			LOG.info(i + " NTS="+nts);
			if (i.equals("http://x.org/ind-dec-all")) {
				if (nts.contains("http://x.org/inc-bone-length") && 
						nts.contains("http://x.org/hyperplastic-heart")) {
					nOk++;
				}
			}
			if (i.equals("http://x.org/ind-inc-all")) {
				if (nts.contains("http://x.org/dec-bone-length") && 
						nts.contains("http://x.org/hypoplastic-heart")) {
					nOk++;
					
				}
			}
		}
		Assert.assertTrue(nOk == 2);
	}


}
