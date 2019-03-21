import java.util.Set;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

/**
 * Class for a miner.
 */
public class Miner extends Thread {
	private Integer hashCount;
	private CommunicationChannel channel;
	private Set<Integer> solved;

	/**
	 * Creates a {@code Miner} object.
	 * 
	 * @param hashCount
	 *            number of times that a miner repeats the hash operation when
	 *            solving a puzzle.
	 * @param solved
	 *            set containing the IDs of the solved rooms
	 * @param channel
	 *            communication channel between the miners and the wizards
	 */
	public Miner(Integer hashCount, Set<Integer> solved, CommunicationChannel channel) {
		this.hashCount = hashCount;
		this.solved = solved;
		this.channel = channel;
	}

	Semaphore sem = new Semaphore(1);

	@Override
	public void run() {
		while (true) {
			try {
				channel.lock.lock();
				Message cr_room = null;
				Message adj_room = null;
				boolean no_exception = true;
				try {
					cr_room = channel.getMessageWizardChannel();
	
					if (cr_room.getData() == Wizard.EXIT) {
						return;
					}
	
					if (cr_room.getData() != Wizard.END) {
						adj_room = channel.getMessageWizardChannel();
					}
				} catch (Exception e) {
					no_exception = false;
				} finally {
					channel.lock.unlock();
				}
	
				if (cr_room != null && adj_room != null && no_exception) {
					if (!this.solved.contains(new Integer(adj_room.getCurrentRoom()))) {
						Message result = new Message(0, 0, "");
						result.setCurrentRoom(adj_room.getCurrentRoom());
						result.setParentRoom(cr_room.getCurrentRoom());
						result.setData(encryptMultipleTimes(adj_room.getData(), this.hashCount));
						channel.putMessageMinerChannel(result);
						solved.add(adj_room.getCurrentRoom());
					}
				}
			} catch (Exception e) {
				
			}
		}
	}

	private String encryptMultipleTimes(String input, Integer count) {
        String hashed = input;
        for (int i = 0; i < count; ++i) {
            hashed = encryptThisString(hashed);
        }

        return hashed;
    }
	
	private String encryptThisString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // convert to string
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
            String hex = Integer.toHexString(0xff & messageDigest[i]);
            if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
    
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
