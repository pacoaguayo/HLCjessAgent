package examples.jess;

import com.dpsframework.domain.ESontology;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.domain.DFGUIManagement.DFAppletOntology;
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/** 
 * HLCjessAgent based on HL Cardoso proposal rule-based engine integration.
 * 
 * @version: (Adapted to Jess 8.1ap and JAVA-JDK 8.x).
 * @author Aguayo-Canela, FJ (2020)
 * <p>
 * <small>Department of Electric Engineering, Systems and Automatic<br>
 *        School of Industrial Engineering and Information Technology<br>
 *        &copy; <b>University of Leon</b> - Spain. <a
 *        href="http://www.unileon.es">{http://www.unileon.es}</a>
 * </small>
 */
public class HLCjessAgent extends Agent {
	private static final long serialVersionUID = 141238749848L;
		
	int maxJessPasses = 400;
	
	
	protected void setup() { 
		Object[] args = getArguments();
		if (args != null) {
			for (int i = 0; i < args.length; ++i) {
				if (i == 0) {
					maxJessPasses = Integer.parseInt(args[i].toString());
				}
			}
		}

		
		System.out.println("Engine cycles at: "+maxJessPasses);
		
		// Two cyclic behaviour in parallel.
		JessBehaviour a = new JessBehaviour(this, maxJessPasses);
		a.setBehaviourName("RBengine");
		addBehaviour(a);
		
		PingListenerBehaviour b = new PingListenerBehaviour(this);
		b.setBehaviourName("PingPong");
		addBehaviour(b);
		
		// HL Cardoso flavor. Only one Cyclic Behaviour.
//		JessBehaviour400 c = new JessBehaviour400(this, maxJessPasses);
//		c.setBehaviourName("RBengine");
//		addBehaviour(c);
		

		
		
	}
	
} // End of HLCjessAgent.
