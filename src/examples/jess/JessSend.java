package examples.jess;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jess.Context;
import jess.Funcall;
import jess.JessException;
import jess.Value;
import jess.ValueVector;

public class JessSend implements jess.Userfunction {
	private Agent myAgent;

	public JessSend(Agent a) {
		myAgent = a;
	}

	// Function name to be used in Jess
	public String getName() {
		return ("send");
	}
	
	// Called when (send ...) is executed at Jess
	public Value call(ValueVector vv, Context context) throws JessException {
		// get function arguments
		// vv.get(...

		// prepare message to send
		ACLMessage msg = new ACLMessage();
		// msg.set...
		// ...

		// send the message
		myAgent.send(msg);

		return Funcall.TRUE;
	}

} // end JessSend class