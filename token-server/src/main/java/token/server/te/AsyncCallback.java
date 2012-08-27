package token.server.te;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mo.org.jboss.netty.util.Timeout;
import mo.org.jboss.netty.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AsyncCallback implements TimerTask, AsyncListener {
	
	protected final Logger logger;
	
	protected AsyncContext actx;
	
	protected Object result;
	
	protected boolean completed;
	
	public AsyncCallback(AsyncContext actx) {
		this.actx = actx;
		this.actx.addListener(this);
		this.actx.setTimeout(-1);
		
		this.logger = LoggerFactory.getLogger(getClass());
	}
	
	public final void cancel() {
		this.result = null;
		this.completed = false;

		actx.complete();
	}
	
	public final void complete(Object result) {
		this.result = result;
		this.completed = true;
		
		actx.complete();
	}
	
	public final void run(Timeout timeout) throws Exception {
		this.result = null;
		this.completed = false;
		
		try {
			onTimeout();
		} catch (Throwable t) {
			logger.warn("an exception is thrown", t);
		}
		
		actx.complete();
	}
	
	public void onComplete(AsyncEvent event) throws IOException {
		if (!completed) {
			return;
		}
		
		try {
			onComplete(
					(HttpServletRequest) event.getSuppliedRequest(),
					(HttpServletResponse) event.getSuppliedResponse());
		} catch (Throwable t) {
			logger.warn("an exception is thrown", t);
		}

	}

	public final void onTimeout(AsyncEvent event) throws IOException {
		// do nothing
	}

	public final void onError(AsyncEvent event) throws IOException {
		// do nothing
	}

	public final void onStartAsync(AsyncEvent event) throws IOException {
		actx = event.getAsyncContext();
		actx.addListener(this);
	}

	protected abstract void onComplete(HttpServletRequest req, HttpServletResponse resp) throws Exception;
	
	protected void onTimeout() throws Exception {
		// do nothing
	}
}
