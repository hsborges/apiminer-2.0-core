package org.apiminer.recommendation.associations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apiminer.daos.ProjectDAO;
import org.apiminer.daos.RuleDAO;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.ApiElement;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.example.AssociatedElement;
import org.apiminer.entities.example.Recommendation;
import org.apiminer.entities.mining.MiningResult;
import org.apiminer.entities.mining.Rule;

/**
 * @author Hudson S. Borges
 *
 */
public class AssociationsRecommender {
	
	private final Logger logger = Logger.getLogger(AssociationsRecommender.class);
	
	private final RuleDAO ruleDAO = new RuleDAO();
	
	private MiningResult miningResult;
	
	public AssociationsRecommender(MiningResult miningResult) {
		this.miningResult = miningResult;
	}
	
	public List<Recommendation> findAllRecommendedAssociations(){
		List<Recommendation> result = new ArrayList<Recommendation>();

		Set<ApiClass> apiClass = new ProjectDAO().findSourceAPI().getApiClass();
		for (ApiClass apc : apiClass) {
			for (ApiMethod apm : apc.getApiMethods()) {
				Recommendation rc = findRecommendedAssociation(apm);
				result.add(rc);
			}
		}
		
		return result;
	}

	private Recommendation findRecommendedAssociation(ApiMethod fromMethod){
		Recommendation rc = new Recommendation();
		rc.setFromElement(fromMethod);
		rc.setAssociatedElements(new ArrayList<AssociatedElement>());
		
		List<Rule> rules = ruleDAO.findByResultAndPremise(miningResult.getId(), Collections.singleton(fromMethod));
		
		logger.debug("Making recommended sets");
		for (Rule r : rules) {
			AssociatedElement rs = new AssociatedElement();
			rs.setElements(new HashSet<ApiElement>(r.getConsequence().getElements()));
			rs.setRuleBased(r.getSimpleRule());
			rs.setRecommendedAssociations(rc);

			rc.getAssociatedElements().add(rs);
		}
		
		logger.debug(String.format("Ordering the recommended sets"));
		Collections.sort(rc.getAssociatedElements(), new AssociationsComparator());
		
		return rc;
	}
	
}

