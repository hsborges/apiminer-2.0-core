package org.apiminer.entities.mining;

public enum RuleMetrics {

	LIFT {
		
		@Override
		public double compute(int premiseSupport, int consequenceSupport,
				int totalSupport, int totalTransactions) {
			double confidence = CONFIDENCE.compute(premiseSupport,
					consequenceSupport, totalSupport, totalTransactions);
			return confidence
					/ ((double) consequenceSupport / (double) totalTransactions);
		}

		@Override
		public double computeRelative(int premiseSupport,
				int consequenceSupport, int totalSupport, int totalTransactions) {
			
			double p1 = (double) totalSupport / (double) totalTransactions;
			double p2 = ((double)premiseSupport / (double)totalTransactions) * ((double)consequenceSupport / (double)totalTransactions);
			return p1 / p2;
		}
		
	},
	LEVERAGE {
		@Override
		public double compute(int premiseSupport, int consequenceSupport,
				int totalSupport, int totalTransactions) {
			double coverageForItemSet = (double) totalSupport
					/ (double) totalTransactions;
			double expectedCoverageIfIndependent = ((double) premiseSupport / (double) totalTransactions)
					* ((double) consequenceSupport / (double) totalTransactions);
			return coverageForItemSet - expectedCoverageIfIndependent;
		}

		@Override
		public double computeRelative(int premiseSupport,
				int consequenceSupport, int totalSupport, int totalTransactions) {
			
			double p1 = (double) totalSupport / (double) totalTransactions;
			double p2 = ((double)premiseSupport / (double)totalTransactions) * ((double)consequenceSupport / (double)totalTransactions);
			return p1 - p2;
		}
		
	},
	CONFIDENCE {
		@Override
		public double compute(int premiseSupport, int consequenceSupport,
				int totalSupport, int totalTransactions) {
			return (double) totalSupport / (double) premiseSupport;
		}

		@Override
		public double computeRelative(int premiseSupport,
				int consequenceSupport, int totalSupport, int totalTransactions) {
			
			double p1 = (double) totalSupport / (double) totalTransactions;
			double p2 = (double)premiseSupport / (double)totalTransactions;			
			return p1 / p2;
		}
		
	},
	CONVICTION {
		@Override
		public double compute(int premiseSupport, int consequenceSupport,
				int totalSupport, int totalTransactions) {
			double num = (double) premiseSupport
					* (double) (totalTransactions - consequenceSupport)
					/ (double) totalTransactions;
			double denom = premiseSupport - totalSupport + 1;
			return num / denom;
		}

		@Override
		public double computeRelative(int premiseSupport,
				int consequenceSupport, int totalSupport, int totalTransactions) {

			if (CONFIDENCE.compute(premiseSupport, consequenceSupport, totalSupport, totalTransactions) == 1) {
				return 0;
			}
			
			double p1 = ( (double)premiseSupport / (double)totalTransactions) * ((double) (totalTransactions - consequenceSupport) / (double) totalTransactions );
			double p2 = (double)( premiseSupport - totalSupport ) / (double) totalTransactions;
			return p1 / p2;
		}
		
	},
	JACCARD {
		@Override
		public double compute(int premiseSupport, int consequenceSupport,
				int totalSupport, int totalTransactions) {
			return (double) totalSupport
					/ ((double) premiseSupport + (double) consequenceSupport - (double) totalSupport);
		}

		@Override
		public double computeRelative(int premiseSupport,
				int consequenceSupport, int totalSupport, int totalTransactions) {
			
			double p1 = (double) totalSupport / (double) totalTransactions;
			double p2 = ((double) premiseSupport / (double) totalTransactions);
			double p3 = ((double) consequenceSupport / (double) totalTransactions);
			
			return p1 / (p2 + p3 - p1);
		}
		
	};

	public static final RuleMetrics parse(String metricName) {
		for (RuleMetrics mt : RuleMetrics.values()) {
			if (mt.toString().equalsIgnoreCase(metricName)) {
				return mt;
			}
		}
		return null;
	}

	public abstract double compute(int premiseSupport, int consequenceSupport,
			int totalSupport, int totalTransactions);

	public abstract double computeRelative(int premiseSupport,
			int consequenceSupport, int totalSupport, int totalTransactions);

}
