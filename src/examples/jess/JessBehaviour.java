package examples.jess;

import java.io.File;

import com.dpsframework.domain.ESontology;

import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.domain.DFGUIManagement.DFAppletOntology;
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jess.JessException;

/** 
 * JessBehaviour proposed by HLCardoso and adapted to Comparative analysis.
 * 
 * @author Aguayo-Canela, FJ (2020)
 * <p>
 * <small>Department of Electric Engineering, Systems and Automatic<br>
 *        School of Industrial Engineering and Information Technology<br>
 *        &copy; <b>University of Leon</b> - Spain. <a
 *        href="http://www.unileon.es">{http://www.unileon.es}</a>
 * </small>
 */
public class JessBehaviour extends CyclicBehaviour {
	private static final long serialVersionUID = 141238749847L;
	
	private final int MAX_JESS_PASSES = 400;
	private final String SUDOKUS_PATH = "nodes/Node/engine/";
	
	MessageTemplate mtJ0 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
	MessageTemplate mtJ1 = MessageTemplate.MatchOntology("Expert-Systems");
	
	private MessageTemplate mtJess = MessageTemplate.and(mtJ0, mtJ1);
	
	Object outValue 	= new Object();
	String sudokuFile 	= ""; 
	String sValue 		= ""; 
	String sPath 		= SUDOKUS_PATH;
	
    Agent myAgent       = null;
    int maxJessPasses   = MAX_JESS_PASSES;
    private jess.Rete innEng = null;

	private int executedPasses;

	
	JessBehaviour(Agent a, int passes) {
		super(a);
		maxJessPasses = passes;
		myAgent = a;
		innEng = new jess.Rete();

		
		
		// -------------------------------------------------------------------------------
		// Load Content Manager on HLCjessAgents to understand ACLMessage sent to engine.
		// -------------------------------------------------------------------------------
		myAgent.getContentManager().registerOntology(ESontology.getInstance());
		
		

		// -------------------------------------------------------------------------------
		// Load HLCjessAgents capabilities: sudoku.clp + solve.clp + output-frills.clp
		// -------------------------------------------------------------------------------
		
		// Clear engine:---------------
		try{
			innEng.clear(); 
			outValue = true;	
		} catch (JessException e) {
			outValue = false;
			e.printStackTrace();
		}

		// Load application:-----------
		sValue = sPath + "sudoku.clp";		
		try {
			if (  (new File(sValue)).exists()   ){
				innEng.batch(sValue);
				outValue = true;
				System.out.println("Application: "+sValue+" loaded !");
			} else {
				outValue = false;
			}
		} catch (JessException e) {
			System.out.println("File: "+sValue + " not found.");
			outValue = false;
			e.printStackTrace();
		}
		
		
		// Load solver application:----
		sValue = sPath + "solve.clp";
		try {
			if (  (new File(sValue)).exists()   ){
				innEng.batch(sValue);
				outValue = true;
				System.out.println("Application: "+sValue+" loaded !");
			} else {
				outValue = false;
			}
		} catch (JessException e) {
			System.out.println("File: "+sValue + " not found.");
			outValue = false;
			e.printStackTrace();
		}
		
		
		
		// Format solutions:-----------
		sValue = sPath + "output-frills.clp";
		try {
			if (  (new File(sValue)).exists()   ){
				innEng.batch(sValue);
				outValue = true;
				System.out.println("Application: "+sValue+" loaded !");
			} else {
				outValue = false;
			}
		} catch (JessException e) {
			System.out.println("File: "+sValue + " not found.");
			outValue = false;
			e.printStackTrace();
		}
	} // HLCJessAgent initialized.

	
	
	
	
	public void action() {
		
		ACLMessage msg = myAgent.receive( mtJess );
		
		if (msg != null) {
			ACLMessage reply = new ACLMessage(ACLMessage.INFORM_IF);
			reply.addReceiver(msg.getSender());
			reply.setSender(myAgent.getAID());
			reply.setOntology("Expert-Systems");
			reply.addUserDefinedParameter("psTimeInit", Long.toString( System.currentTimeMillis()) );
		    reply.addUserDefinedParameter("psSender", myAgent.getLocalName());
			if (msg.getContent().contains(":")) {

				
				// Load file request from ACLMessage.
				// -------------------------------------------------
				sValue = sPath + msg.getUserDefinedParameter("psProblem");

				try {
					if (  (new File(sValue)).exists()   ){
						System.out.println("Exercise named: "+sValue+" loaded !");
						innEng.batch(sValue);
						innEng.reset();
						innEng.run();
						
						
						// Another implementation of rule-based engine in Agents.
						// ------------------------------------------------------
						Integer cycles = Integer.valueOf(maxJessPasses);
						//outValue = (long) innEng.run(cycles);	
						
						System.out.println("Exercise named: "+sValue+" resolved with cycles ="+outValue);
					} else {
						outValue = false;
					}
				} catch (JessException e) {
					System.out.println("File: "+sValue + " not found.");
					outValue = false;
					e.printStackTrace();
				}


				reply.setPostTimeStamp(System.currentTimeMillis()); 
				String content = msg.getContent() + 
						reply.getPostTimeStamp()  + ":" +
						myAgent.getLocalName()    + ":"; 
				reply.setContent(content);				
				


				
			} else {
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
				reply.setContent("Content of ping-message has an error. \n" + "See, ---analizador.bsh--, line 209");
			}
			myAgent.send(reply);
		} else {
			block();
		}
			
			
//
//		try {
//			// run a maximum number of steps
//			executedPasses = innEng.run(MAX_JESS_PASSES);
//		} catch (JessException je) {
//			je.printStackTrace();
//		}
//		// if the engine stopped, block this behaviour
//		if (executedPasses < MAX_JESS_PASSES)
//			block();
//		// the behaviour shall be unblocked by a call to restart()
	}

	boolean addFact(String jessFact) {
		// assert the fact into the Jess engine
		try {
			innEng.assertString(jessFact);
		} catch (JessException je) {
			return false;
		}
		// if blocked, wake up!
		if (!isRunnable())
			restart();
		// message asserted
		return true;
	}

	boolean newMsg(ACLMessage msg) {
		String jf = ""; // use msg to assemble a Jess construct
		// "feed" Jess engine
		return addFact(jf);
	}

}








