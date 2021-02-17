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
public class JessBehaviour400 extends CyclicBehaviour {
	private static final long serialVersionUID = 141238749849L;
	private final int MAX_JESS_PASSES = 400;
	private final String SUDOKUS_PATH = "nodes/Node/engine/";
	
	
	MessageTemplate mt0 = null;
	MessageTemplate mt1 = null;
	MessageTemplate mtPing = null;
	
	MessageTemplate mtJ0 = null;
	MessageTemplate mtJ1 = null;
	MessageTemplate mtJess = null;
	
	MessageTemplate mtBoth = null;
	

	// Proposal: HLC, only one Cyclic Behaviour, so OR mtPing OR mtJess.
	// -----------------------------------------------------------------
	Object outValue 	= new Object();
	String sudokuFile 	= ""; 
	String sValue 		= ""; 
	String sPath 		= SUDOKUS_PATH;
	
    Agent myAgent       = null;
    int maxJessPasses   = MAX_JESS_PASSES;
    private jess.Rete innEng = null;
	private boolean stopMatch = false;

	JessBehaviour400(Agent a, int passes) {
		super(a);
		maxJessPasses = passes;
		myAgent = a;
		innEng = new jess.Rete();

		// -------------------------------------------------------------------------------
		// Load Content Manager on HLCjessAgents to understand ACLMessage sent to
		// engine.
		// -------------------------------------------------------------------------------
		myAgent.getContentManager().registerOntology(ESontology.getInstance());

		mt0 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST_WHEN);
		mt1 = MessageTemplate.MatchOntology("presence");

		mtPing = MessageTemplate.and(mt0, mt1);

		mtJ0 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		mtJ1 = MessageTemplate.MatchOntology("Expert-Systems");

		mtJess = MessageTemplate.and(mtJ0, mtJ1);

		mtBoth = MessageTemplate.or(mtPing, mtJess);

		// -------------------------------------------------------------------------------
		// Load HLCjessAgents capabilities: sudoku.clp + solve.clp + output-frills.clp
		// -------------------------------------------------------------------------------

		// Clear engine:---------------
		try {
			innEng.clear();
			outValue = true;
		} catch (JessException e) {
			outValue = false;
			e.printStackTrace();
		}

		// Load application:-----------
		sValue = sPath + "sudoku.clp";
		try {
			if ((new File(sValue)).exists()) {
				innEng.batch(sValue);
				outValue = true;
				System.out.println("Application: " + sValue + " loaded !");
			} else {
				outValue = false;
			}
		} catch (JessException e) {
			System.out.println("File: " + sValue + " not found.");
			outValue = false;
			e.printStackTrace();
		}

		// Load solver application:----
		sValue = sPath + "solve.clp";
		try {
			if ((new File(sValue)).exists()) {
				innEng.batch(sValue);
				outValue = true;
				System.out.println("Application: " + sValue + " loaded !");
			} else {
				outValue = false;
			}
		} catch (JessException e) {
			System.out.println("File: " + sValue + " not found.");
			outValue = false;
			e.printStackTrace();
		}

		// Format solutions:-----------
		sValue = sPath + "output-frills.clp";
		try {
			if ((new File(sValue)).exists()) {
				innEng.batch(sValue);
				outValue = true;
				System.out.println("Application: " + sValue + " loaded !");
			} else {
				outValue = false;
			}
		} catch (JessException e) {
			System.out.println("File: " + sValue + " not found.");
			outValue = false;
			e.printStackTrace();
		}
	} // HLCJessAgent initialized.

	public void action() {

		ACLMessage msg = myAgent.receive(mtBoth);
		if (msg != null) {
			stopMatch = false;
			
			if (    mtJess.match(msg)   ) {
				ACLMessage reply = new ACLMessage(ACLMessage.INFORM_IF);
				reply.addReceiver(msg.getSender());
				reply.setSender(myAgent.getAID());
				reply.setOntology("Expert-Systems");
				reply.addUserDefinedParameter("psTimeInit", Long.toString(System.currentTimeMillis()));
				reply.addUserDefinedParameter("psSender", myAgent.getLocalName());

				if (msg.getContent().contains(":")) {

					// Load file request from ACLMessage.
					// -------------------------------------------------
					sValue = sPath + msg.getUserDefinedParameter("psProblem");

					try {
						if ((new File(sValue)).exists()) {
							System.out.println("Exercise named: " + sValue + " loaded !");
							innEng.batch(sValue);
							innEng.reset();
							innEng.run();
							stopMatch  = true;

							// Another implementation of rule-based engine in Agents.
							// ------------------------------------------------------
							Integer cycles = Integer.valueOf(maxJessPasses);
							// outValue = (long) innEng.run(cycles);

							System.out.println("Exercise named: " + sValue + " resolved with cycles =" + outValue);
						} else {
							outValue = false;
						}
					} catch (JessException e) {
						System.out.println("File: " + sValue + " not found.");
						outValue = false;
						e.printStackTrace();
					}

					reply.setPostTimeStamp(System.currentTimeMillis());
					String content = msg.getContent() + reply.getPostTimeStamp() + ":" + myAgent.getLocalName() + ":";
					reply.setContent(content);

				} else {
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
					reply.setContent("Content of ping-message has an error. \n" + "See, ---analizador.bsh--, line 209");
				}
				myAgent.send(reply);
				stopMatch  = false;
			} 
			if (  mtPing.match(msg)  &&  !stopMatch ) {
				ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
				reply.addReceiver(msg.getSender());
				reply.setSender(myAgent.getAID());

				if (msg.getContent().contains(":")) {
					// -------------------------------------------------------------------
					// WARNING !! :: only on Laptops. Erase when comparative is made
					// :: in a network and with JADE containers over
					// different host and agents on those containers.
					// ::
					// :: Tip to simulate network latency ++time
					// :: for agents in the same host.
					// -------------------------------------------------------------------
					Integer netLatency = 2000000;
					Integer i;
					for (i = 0; i < netLatency; i++) {
						i.toString();
					}
					// -------------------------------------------------------------------
					// End of LAN-Simulation.

					reply.setOntology("presence");
					reply.setPerformative(ACLMessage.INFORM);
					reply.setPostTimeStamp(System.currentTimeMillis());
					String content = msg.getContent() + reply.getPostTimeStamp() + ":" + myAgent.getLocalName() + ":";
					reply.setContent(content);
				} else {
					reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
					reply.setContent("Content of ping-message has an error. \n" + "See, ---analizador.bsh--, line 209");
				}
				myAgent.send(reply);
			}
			
		} else {
			block();
		}

	}

}
