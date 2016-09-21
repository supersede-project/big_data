package eu.supersede.feedbackanalysis.consolidation;

import java.util.List;

import eu.supersede.feedbackanalysis.ds.AnalysisReport;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

public interface AnalysisConsolidator {
	public AnalysisReport getSummary(List<UserFeedback> userFeedback);
}
