package token.server.controller;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import token.server.model.AuthException;
import token.server.model.User;
import token.server.service.AuthManager;
import token.server.service.JsonHelper;

@Controller
public class AuthenticationController {
	
	private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
	
	@Resource(name = "authManager")
	private AuthManager authManager;
	
	@Resource(name = "jsonHelper")
	private JsonHelper json;
	
	@RequestMapping(value = "/accounts/sign-in", method = RequestMethod.POST)
	public void signIn(
			@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "password", required = true) String password,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		User u = null;
		int resultCode = HttpServletResponse.SC_OK;
		
		try {
			u = authManager.authenticate(username.trim(), password.trim());
		} catch (AuthException e) {
			resultCode = HttpServletResponse.SC_UNAUTHORIZED;
		} catch (Throwable t) {
			log.error("unable to authenticate user " + username, t);
			resultCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		
		if (resultCode == HttpServletResponse.SC_OK) {
			json.sendHttpResponse(response, u, true);
		} else {
			response.sendError(resultCode);
		}
	}

	@RequestMapping(value = "/accounts/sign-out", method = RequestMethod.POST)
	public void signOut(
			@RequestHeader(value = "Authorization", required = true) String authToken,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		if (!StringUtils.hasText(authToken)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		int resultCode = HttpServletResponse.SC_OK;
		
		try {
			authManager.deauthenticate(authToken);
		} catch (AuthException e) {
			resultCode = HttpServletResponse.SC_UNAUTHORIZED;
		} catch (Throwable t) {
			log.error("unable to deauthenticate auth token " + authToken, t);
			resultCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		
		if (resultCode != HttpServletResponse.SC_OK) {
			response.sendError(resultCode);
		}
	}

	@RequestMapping(value = "/accounts/change-password", method = RequestMethod.POST)
	public void changePassword(
			@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "password", required = true) String password,
			@RequestParam(value = "new_password", required = false) String newpassword,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		if (!StringUtils.hasText(username) || !StringUtils.hasText(password) || !StringUtils.hasText(newpassword)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		int resultCode = HttpServletResponse.SC_OK;
		
		try {
			authManager.changePassword(username, password, newpassword);
		} catch (AuthException e) {
			resultCode = HttpServletResponse.SC_UNAUTHORIZED;
		} catch (Throwable t) {
			log.error("unable to change user password for user " + username, t);
			resultCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		
		if (resultCode != HttpServletResponse.SC_OK) {
			response.sendError(resultCode);
		}
	}
}
