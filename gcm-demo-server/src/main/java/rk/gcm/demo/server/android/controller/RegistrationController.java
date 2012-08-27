package rk.gcm.demo.server.android.controller;

import java.io.IOException;

import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import rk.gcm.demo.server.model.User;
import rk.gcm.demo.server.service.AuthManager;

@Controller
public class RegistrationController {
	
	private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);
	
	@Resource(name = "authManager")
	private AuthManager authManager;

	@RequestMapping(value = "/android/register", method = RequestMethod.POST)
	public void register(
			@RequestParam(value = "username") String username,
			@RequestParam(value = "passwd") String password,
			@RequestParam(value = "registration_id") String regId,
			HttpServletRequest request,
			HttpServletResponse response)
			throws IOException, ServletException {
		
		if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		if (!StringUtils.hasText(regId)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		User u;
		
		try {
			u = authManager.authenticate(username.trim(), password.trim(), regId.trim());
		} catch (AuthException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		} catch (Throwable t) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
		PrintStream out = new PrintStream(response.getOutputStream());
		
		out.print("SID=");
		out.println(u.getSID());
		
		out.print("AuthToken=");
		out.println(u.getAuthToken());
		
		out.flush();
	}

	@RequestMapping(value = "/android/unregister", method = RequestMethod.POST)
	public void auth(@RequestHeader(value = "Auth") String authToken,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		if (!StringUtils.hasText(authToken)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		User u = null;
		
		int statusCode = HttpServletResponse.SC_OK;
		
		try {
			u = userManager.findAuthTokenOrFail(authToken);
		} catch (EntityNotFoundException e) {
			statusCode = HttpServletResponse.SC_UNAUTHORIZED;
		} catch (Throwable t) {
			log.error("unable to unregister auth token " + authToken, t);
			
			statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		
		if (statusCode != HttpServletResponse.SC_OK) {
			auditLogger.logoutFail(authToken);
			
			response.sendError(statusCode);
			return;
		}
		
		if (u.getAuthToken() != null) {
			userManager.compareAndUpdateAuthTokenAndGCM(u.getSID(), u.getAuthToken(), u.getRegId(), null, null);
		}
		
		auditLogger.logoutSuccess(authToken, u.getSID());
		
		response.sendError(HttpServletResponse.SC_OK);
	}
}
