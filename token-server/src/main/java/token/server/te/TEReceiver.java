package token.server.te;

import java.util.Map;

import mo.org.jboss.netty.util.Timeout;
import mo.org.jboss.netty.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import token.server.R;

public class TEReceiver implements InitializingBean {
	
	private Logger log = LoggerFactory.getLogger(TEReceiver.class);
	
	private Timer timer;
	
	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public void afterPropertiesSet() throws Exception {
		log = LoggerFactory.getLogger(TEReceiver.class.getName());
	}

	public void onMessage(Object obj) {
        if (obj instanceof Map<?, ?>) {
            Map<String, Object> bean = (Map<String, Object>) obj;

            String correlationId = (String) bean.get(R.message.correlation);

            if (correlationId != null) {
            	Timeout timeout = timer.cancelTimeout(correlationId);
            	
            	if (timeout != null) {
            		((AsyncCallback) timeout.getTask()).complete(obj);
            	}
            }
        } else {
            log.warn("object " + obj + " is not an instance of java.util.Map");
        }
 	}
}
