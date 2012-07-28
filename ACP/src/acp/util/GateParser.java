package acp.util;
import gate.*;
import gate.creole.*;
import gate.util.*;

public class GateParser {

	public String SentenceSplitter = "gate.creole.splitter.RegexSentenceSplitter";

	/** The Corpus Pipeline application to contain ANNIE */
	private SerialAnalyserController annieController;

	public void init() throws GateException 
	{
		// create a serial analyser controller to run ANNIE with
		annieController = (SerialAnalyserController) Factory.createResource(
				"gate.creole.SerialAnalyserController", Factory.newFeatureMap(),
				Factory.newFeatureMap(), "ANNIE_" + Gate.genSym());
		
		ProcessingResource pr = (ProcessingResource)
				Factory.createResource("gate.creole.tokeniser.DefaultTokeniser", Factory.newFeatureMap());
		annieController.add(pr);

		pr = (ProcessingResource)
				Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", Factory.newFeatureMap());
		annieController.add(pr);

		/*pr = (ProcessingResource)
				Factory.createResource("gate.creole.splitter.SentenceSplitter", Factory.newFeatureMap());
		annieController.add(pr);*/
		pr = (ProcessingResource)
				Factory.createResource(this.SentenceSplitter, Factory.newFeatureMap());
		annieController.add(pr);

		pr = (ProcessingResource)
				Factory.createResource("gate.creole.POSTagger", Factory.newFeatureMap());
		annieController.add(pr);

		pr = (ProcessingResource)
				Factory.createResource("gate.creole.ANNIETransducer", Factory.newFeatureMap());
		annieController.add(pr);

		pr = (ProcessingResource)
				Factory.createResource("gate.creole.orthomatcher.OrthoMatcher", Factory.newFeatureMap());
		annieController.add(pr);

	} // initAnnie()

	/** Tell ANNIE's controller about the corpus you want to run on */
	public void setCorpus(Corpus corpus) {
//		cleanUp();
		
		annieController.setCorpus(corpus);
	} // setCorpus

//	public void cleanUp() {
//		Corpus corp = annieController.getCorpus();
//		if (corp != null && !corp.isEmpty()) {
//			for (int i = 0; i < corp.size(); i++) {
//				Document doc1 = (Document) corp.remove(i);
//				corp.unloadDocument(doc1);
//				Factory.deleteResource(corp);
//
//				Factory.deleteResource(doc1);
//			}
//			
//			annieController.getDocument().cleanup();
//		}
//	}

	/** Run ANNIE */
	public void execute() throws GateException {
		annieController.execute();
	} // execute()
}