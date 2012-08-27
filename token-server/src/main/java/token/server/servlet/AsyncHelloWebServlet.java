package token.server.servlet;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import token.server.model.AuthException;
import token.server.service.AsyncHelloManager;
import token.server.service.JsonHelper;
import token.server.te.AsyncCallback;
import token.server.util.ObjectHelper;

@WebServlet(
		name = "AsyncHelloWebServlet",
		urlPatterns = { "/async-hello/*" },
		asyncSupported = true
		)
public class AsyncHelloWebServlet extends HttpServlet {
	
	private static final long serialVersionUID = -8002326026422913626L;

	private static final Logger log = LoggerFactory.getLogger(AsyncHelloWebServlet.class);
	
	private AsyncHelloManager helloManager;
	
	private JsonHelper json;

	@Override
	public void init() throws ServletException {
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		
		helloManager = ctx.getBean("asyncHelloManager", AsyncHelloManager.class);
		
		json = ctx.getBean("jsonHelper", JsonHelper.class);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String str_menuId = req.getRequestURI().substring(req.getContextPath().length());
		Long menuId;
		
		if (str_menuId.length() <= 10) {
			menuId = null;
		} else {
			menuId = ObjectHelper.tryLongValueOf(str_menuId.substring(10));
		}
		
		if (menuId != null) {
			doGet(
					req.getHeader("Authorization"),
					menuId,
					req.getParameter("account_number"),
					req.getParameter("customer_id"),
					req, resp);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private void doGet(String authToken, Long menuId,
			String accountNumber, String customerId,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		if (!StringUtils.hasText(authToken)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		if (!StringUtils.hasText(accountNumber) || !StringUtils.hasText(customerId) || (menuId == null)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		AsyncContext actx;
		
		if (request.isAsyncStarted()) {
			actx = request.getAsyncContext();
		} else {
			actx = request.startAsync(request, response);
		}
		
		AsyncCallback callback = new AsyncCallback(actx) {
			
			protected void onComplete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
				if (result == null) {
					return;
				}
				
				json.sendHttpResponse(resp, result, false);
			}
			
			@Override
			protected void onTimeout() throws Exception {
				((HttpServletResponse) actx.getResponse()).sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT);
			}
		};
		
		int resultCode = HttpServletResponse.SC_OK;
		
		try {
			helloManager.hello(callback,
					authToken.trim(), menuId,
					accountNumber.trim(), customerId.trim());
		} catch (AuthException e) {
			resultCode = HttpServletResponse.SC_UNAUTHORIZED;
		} catch (IllegalArgumentException e) {
			resultCode = HttpServletResponse.SC_BAD_REQUEST;
		} catch (Throwable t) {
			log.error("unable to inquiry purchase (menu:" + menuId + ", customer-id:" + customerId + ")", t);
			resultCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		
		if (resultCode != HttpServletResponse.SC_OK) {
			callback.cancel();
			
			response.sendError(resultCode);
		}
	}
}
