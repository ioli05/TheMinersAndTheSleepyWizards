import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap; 
import java.util.Map; 

/**
 * Class that implements the channel used by wizards and miners to communicate.
 */
public class CommunicationChannel {
	public BlockingQueue<Message> minerchannel;
	public BlockingQueue<Message> wizardchannel;
	public Lock lock;
	HashMap<Integer, BlockingQueue<Message>> map;

	private int MAX_CAPACITY = 100000;

	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	public CommunicationChannel() {
		minerchannel = new ArrayBlockingQueue<Message>(MAX_CAPACITY);
		wizardchannel = new ArrayBlockingQueue<Message>(MAX_CAPACITY);
		lock = new ReentrantLock();
	}

	/**
	 * Puts a message on the miner channel (i.e., where miners write to and wizards
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageMinerChannel(Message message) {		
		synchronized (this) {
			minerchannel.add(message);
		}
	}

	/**
	 * Gets a message from the miner channel (i.e., where miners write to and
	 * wizards read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageMinerChannel() {
		while (minerchannel.isEmpty()) {
		}

		Message result = null;
		synchronized (this) {
			result = minerchannel.poll();
		}

		return result;	
	}

	/**
	 * Puts a message on the wizard channel (i.e., where wizards write to and miners
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */	
	private Message lastMessage = null;
	public void putMessageWizardChannel(Message message) {
		synchronized (this) {
			wizardchannel.add(message);
		}
	}

	/**
	 * Gets a message from the wizard channel (i.e., where wizards write to and
	 * miners read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageWizardChannel() {		
		while (wizardchannel.isEmpty()) {
		}

		Message result = null;
		synchronized (this) {
			result = wizardchannel.poll();
			if (!result.getData().equals(Wizard.END) && !result.getData().equals(Wizard.EXIT)) {
				if (result.getCurrentRoom() == -1) {
					result.setCurrentRoom(0);
				}
			}
		}

		return result;
	}
}
