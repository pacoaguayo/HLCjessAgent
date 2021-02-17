package examples.jess;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MsgListening extends CyclicBehaviour {

	
	private static final long serialVersionUID = 1L;
	
	// a reference to the JessBehaviour instance
	private JessBehaviour jessBeh;

	MsgListening(Agent agent, JessBehaviour jessBeh) {
		super(agent);
		// save reference to the JessBehaviour instance
		this.jessBeh = jessBeh;
	}

	public void action() {
		MessageTemplate mt = new MessageTemplate(null); // some template
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			// put into Jess engine
			if (jessBeh.newMsg(msg)) {
				// do something

			} else {
				block();
			}

		}

	}

}
// end MsgListening class