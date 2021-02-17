package examples.jess;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/** 
 * Ping Listener Behaviour for Comparative-Analysis responder requirements.
 * 
 * @author Aguayo-Canela, FJ (2020)
 * <p>
 * <small>Department of Electric Engineering, Systems and Automatic<br>
 *        School of Industrial Engineering and Information Technology<br>
 *        &copy; <b>University of Leon</b> - Spain. <a
 *        href="http://www.unileon.es">{http://www.unileon.es}</a>
 * </small>
 */
public class PingListenerBehaviour extends CyclicBehaviour {
	private static final long serialVersionUID = 18876654543L;

	MessageTemplate mt0 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST_WHEN);
	MessageTemplate mt1 = MessageTemplate.MatchOntology("presence");
	
	private MessageTemplate mtPing = MessageTemplate.and(mt0,mt1);
    private Agent myAgent = null;
    
    
	
	public PingListenerBehaviour(Agent a) {
		super(a);
		myAgent = a;
	}
	
	
	
	
	public void action() {
		ACLMessage msg = myAgent.receive( mtPing );
		if (msg != null) {
			
			ACLMessage reply =  new ACLMessage(ACLMessage.INFORM);
			reply.addReceiver( msg     .getSender() );
			reply.setSender(   myAgent .getAID());
			
		      
			if ( msg.getContent().contains(":")) {				
				// -------------------------------------------------------------------
				// WARNING !! :: only on Laptops. Erase when comparative is made
				//            :: in a network and with JADE containers over
				//               different host and agents on those containers.
				//            :: 
				//            :: Tip to simulate network latency ++time 
				//            :: for agents in the same host.
				// -------------------------------------------------------------------
				Integer netLatency = 2000000; Integer i;
				for(i=0; i<netLatency; i++) { i.toString(); }
				// -------------------------------------------------------------------
				// End of LAN-Simulation.
				
				reply.setOntology("presence");
				reply.setPerformative(ACLMessage.INFORM);
				reply.setPostTimeStamp(System.currentTimeMillis()); 
				String content = msg.getContent() + 
						reply.getPostTimeStamp()  + ":" +
						myAgent.getLocalName()    + ":"; 
				reply.setContent(content);
			}
			else {
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
				reply.setContent("Content of ping-message has an error. \n" +
				                 "See, ---analizador.bsh--, line 209");
			}
			myAgent.send(reply);
		}
		else {
			block();
		}
	}

}
