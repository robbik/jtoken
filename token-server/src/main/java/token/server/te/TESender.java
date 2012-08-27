package token.server.te;

import java.util.Map;
import java.util.UUID;

import token.server.R;

import mo.org.jboss.netty.util.Timer;

public class TESender {
	
	private long timeToLive;
	
	private boolean relativeExpiredDate;
	
	private Timer timer;
	
	private final Object monitor;
	
	private String prefix;
	
	private int counter;
	
	public TESender() {
		monitor = new Object();
		
		prefix = "TE ".concat(UUID.randomUUID().toString()).concat("-");
		counter = 0;
	}
	
	public void setTimer(Timer timer) {
		this.timer = timer;
	}
    
    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }
    
    public void setRelativeExpiredDate(boolean relativeExpiredDate) {
        this.relativeExpiredDate = relativeExpiredDate;
    }
    
	public void send(AsyncCallback callback, final Map<String, Object> bean, boolean oneWay) throws Exception {
		String correlationId = null;

        if (!oneWay) {
			synchronized (monitor) {
				if (counter >= Integer.MAX_VALUE) {
					counter = 0;
					prefix = "TE ".concat(UUID.randomUUID().toString()).concat("-");
				}
				
				correlationId = prefix.concat(String.valueOf(++counter));
			}
			
			bean.put(R.message.correlation, correlationId);
			
			long deadline;
			
	        if (relativeExpiredDate) {
	        	deadline = System.currentTimeMillis() + timeToLive;
	        } else {
	        	deadline = timeToLive;
	        }
	        
			timer.newTimeout(correlationId, callback, deadline);
		}
		
		try {
			// TODO send!
		} catch (Exception e) {
			if (!oneWay) {
				timer.cancelTimeout(correlationId);
			}
			
			throw e;
		}
	}
}
