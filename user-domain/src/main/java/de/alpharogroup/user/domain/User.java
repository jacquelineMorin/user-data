package de.alpharogroup.user.domain;

import java.util.HashSet;
import java.util.Set;

import de.alpharogroup.domain.BaseDomainObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper=true)
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseDomainObject<Integer> {

	/** The serial Version UID. */
	private static final long serialVersionUID = 1L;
	/** The attribute active, if true the user account is active. */
	private Boolean active;
	/** A Flag that indicates if the user account is locked or not. */
	private Boolean locked;
	/** The hash from the password hashed with sha512. */
	private String pw;
	/** The roles of the user. */
	private Set<Role> roles = new HashSet<>();
	/** The salt that is used to compute the hash. */
	private String salt;
	/** The user name. */
	private String username;

}