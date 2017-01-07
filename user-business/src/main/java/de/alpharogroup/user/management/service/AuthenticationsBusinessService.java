package de.alpharogroup.user.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.alpharogroup.user.management.service.api.AuthenticationsService;
import de.alpharogroup.user.management.service.api.UserTokensService;
import de.alpharogroup.user.management.service.api.UsersService;
import lombok.Getter;
import lombok.Setter;

/**
 * The class {@link AuthenticationsBusinessService} provides authentication methods.
 */
@Transactional
@Service("authenticationsService")
public class AuthenticationsBusinessService implements AuthenticationsService {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The user tokens business service. */
	@Autowired
	@Getter
	@Setter
	private UserTokensService userTokensService;

	/** The user tokens business service. */
	@Autowired
	@Getter
	@Setter
	private UsersService usersService;

	@Override
	public String newAuthenticationToken(String username) {		
		return userTokensService.newAuthenticationToken(username);
	}
	
}
