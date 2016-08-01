package edu.illinois.cs.cogcomp.srl.learn;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;

import java.util.ArrayList;
import java.util.List;

public class SRLSentenceStructure implements IStructure {

	public final SRLSentenceInstance x;
	public final List<SRLPredicateStructure> ys;

	public SRLSentenceStructure(SRLSentenceInstance instance, List<SRLPredicateStructure> ys) {
		this.x = instance;
		this.ys = ys;
	}

	public PredicateArgumentView getView(SRLManager manager, TextAnnotation ta) {
		String viewName = manager.getPredictedViewName();
		PredicateArgumentView pav = new PredicateArgumentView(viewName,
				manager.getSRLSystemIdentifier(), ta, 1.0);

		int nullId = manager.getArgumentId(SRLManager.NULL_LABEL);
		for (SRLPredicateStructure y : this.ys) {
			SRLPredicateInstance x = y.x;

			SRLMulticlassInstance senseInstance = x.getSenseInstance();
			IntPair predicateSpan = senseInstance.getConstituent().getSpan();
			String predicateLemma = senseInstance.getPredicateLemma();

			Constituent predicate = new Constituent("Predicate", viewName, ta,
					predicateSpan.getFirst(), predicateSpan.getSecond());
			predicate.addAttribute(PredicateArgumentView.LemmaIdentifier, predicateLemma);

			String sense = manager.getSense(y.getSense());
			predicate.addAttribute(PredicateArgumentView.SenseIdentifer, sense);

			List<Constituent> args = new ArrayList<>();
			List<String> relations = new ArrayList<>();

			List<SRLMulticlassInstance> candidateInstances = x.getCandidateInstances();

			for (int candidateId = 0; candidateId < candidateInstances.size(); candidateId++) {
				if (y.getArgLabel(candidateId) == nullId)
					continue;

				SRLMulticlassInstance ci = candidateInstances.get(candidateId);
				IntPair span = ci.getConstituent().getSpan();

				assert span.getFirst() <= span.getSecond() : ta;

				String label = manager.getArgument(y.getArgLabel(candidateId));

				args.add(new Constituent(label, viewName, ta, span.getFirst(), span.getSecond()));
				relations.add(label);
			}

			pav.addPredicateArguments(predicate, args,
					relations.toArray(new String[relations.size()]),
					new double[relations.size()]);

		}

		return pav;
	}
}
