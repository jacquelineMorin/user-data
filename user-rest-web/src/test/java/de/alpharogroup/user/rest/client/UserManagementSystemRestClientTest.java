/*
 * Copyright 2015 Alpha Ro Group UG (haftungsbeschrängt).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.alpharogroup.user.rest.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.alpharogroup.auth.Credentials;
import de.alpharogroup.collections.SetExtensions;
import de.alpharogroup.collections.pairs.KeyValuePair;
import de.alpharogroup.collections.pairs.Triple;
import de.alpharogroup.cxf.rest.client.AbstractRestClient;
import de.alpharogroup.cxf.rest.client.WebClientExtensions;
import de.alpharogroup.date.CalculateDateExtensions;
import de.alpharogroup.date.CreateDateExtensions;
import de.alpharogroup.file.search.PathFinder;
import de.alpharogroup.user.domain.Permission;
import de.alpharogroup.user.domain.RelationPermission;
import de.alpharogroup.user.domain.ResetPassword;
import de.alpharogroup.user.domain.Role;
import de.alpharogroup.user.domain.User;
import de.alpharogroup.user.rest.api.BaseAuthenticationsResource;
import de.alpharogroup.user.rest.api.PermissionsResource;
import de.alpharogroup.user.rest.api.RelationPermissionsResource;
import de.alpharogroup.user.rest.api.ResetPasswordsResource;
import de.alpharogroup.user.rest.api.RolesResource;
import de.alpharogroup.user.rest.api.BaseUsersResource;

/**
 * The class {@link UserManagementSystemRestClientTest}.
 *
 * Note: you have to start a rest server to test this or you have to mock it.
 */
public class UserManagementSystemRestClientTest
{

	private TLSClientParameters tlsClientParameters;

	private UserManagementSystemRestClient restClient;

	private BaseAuthenticationsResource authenticationsResource;

	private BaseUsersResource usersResource;

	private PermissionsResource permissionsResource;

	private User promoterUser;

	private User recommendedUser;

	private RolesResource rolesResource;


	@BeforeClass
	public static void setUpClass() throws Exception
	{

	}

	@AfterClass
	public static void tearDownClass() throws Exception
	{
	}

	@BeforeMethod
	public void setUpMethod() throws Exception
	{
		if (restClient == null)
		{
			restClient = new UserManagementSystemRestClient(
				AbstractRestClient.DEFAULT_BASE_HTTPS_URL);
			tlsClientParameters = WebClientExtensions.newTLSClientParameters(PathFinder.getSrcTestResourcesDir(),
				"keystore.ks", "JKS", "wicket");
			authenticationsResource = restClient.getAuthenticationsResource();

			usersResource = restClient.getUsersResource();
			permissionsResource = restClient.getPermissionsResource();
			rolesResource = restClient.getRolesResource();
			// set client params with key and trust managers. The keystore is the same as jetty
			WebClientExtensions.setTLSClientParameters(authenticationsResource,
				tlsClientParameters);
			WebClientExtensions.setTLSClientParameters(usersResource, tlsClientParameters);
			WebClientExtensions.setTLSClientParameters(permissionsResource, tlsClientParameters);
			WebClientExtensions.setTLSClientParameters(rolesResource, tlsClientParameters);
		}
	}

	@AfterMethod
	public void tearDownMethod() throws Exception
	{
	}

	/**
	 * Test the {@link BaseAuthenticationsResource}.
	 */
	@Test(enabled = true)
	public void testAuthenticationsResource()
	{
		final Credentials credentials = Credentials.builder().username("michael.knight")
			.password("xxx").build();

		// final String json = JsonTransformer.toJsonQuietly(credentials);
		// {"username":"michael.knight","password":"xxx"}
		// https://localhost:8443/auth/credentials
		final Response tokenResponse = authenticationsResource.authenticate(credentials);
		final String token = tokenResponse.readEntity(String.class);
		AssertJUnit.assertNotNull(token);

	}

	/**
	 * Test the {@link PermissionsResource}.
	 */
	@Test(enabled = true)
	public void testPermissionsResource()
	{

		final String permissionName = "view_user_images";

		final Permission expected = Permission.builder().permissionName(permissionName)
			.shortcut("vui").description("This permission grant to view the images of a user")
			.build();

		Permission actual = permissionsResource.findByName(expected.getPermissionName());

		if (actual == null)
		{
			actual = permissionsResource.create(expected);
		}

		AssertJUnit.assertEquals(expected.getDescription(), actual.getDescription());
		AssertJUnit.assertEquals(expected.getPermissionName(), actual.getPermissionName());
		AssertJUnit.assertEquals(expected.getShortcut(), actual.getShortcut());

	}

	/**
	 * Test the {@link RelationPermissionsResource}.
	 */
	@Test(enabled = true)
	public void testRelationPermissionsResource()
	{
		final RelationPermissionsResource resource = restClient.getRelationPermissionsResource();
		// set client params with key and trust managers. The keystore is the same as jetty
		WebClientExtensions.setTLSClientParameters(resource, tlsClientParameters);
		AssertJUnit.assertNotNull(resource);

		final User promoter = getPromoterUser();
		final User recommendedUser = getRecommendedUser();

		final Permission permission = permissionsResource.findByName("view_user_images");

		final RelationPermission expected = RelationPermission.builder().provider(promoter)
			.subscriber(recommendedUser).permissions(SetExtensions.newHashSet(permission)).build();

		// http://localhost:8080/relation/permission/find/all
		final KeyValuePair<User, User> providerToSubscriber = KeyValuePair.<User, User> builder()
			.key(promoter).value(recommendedUser).build();

		RelationPermission relationPermission = resource
			.findRelationPermissions(providerToSubscriber);

		if (relationPermission == null)
		{
			relationPermission = resource.create(expected);
		}
		AssertJUnit.assertNotNull(relationPermission);

		final Triple<User, User, Permission> searchCriteria = Triple
			.<User, User, Permission> builder().left(promoter).middle(recommendedUser)
			.right(permission).build();

		final List<RelationPermission> relationPermissions = resource.find(searchCriteria);
		System.out.println(relationPermissions);
		// AssertJUnit.assertNotNull(relationPermission);

	}

	/**
	 * Test the {@link ResetPasswordsResource}.
	 */
	@Test(enabled = true)
	public void testResetPasswordsResource()
	{
		final ResetPasswordsResource resource = restClient.getResetPasswordsResource();
		// set client params with key and trust managers. The keystore is the same as jetty
		WebClientExtensions.setTLSClientParameters(resource, tlsClientParameters);
		AssertJUnit.assertNotNull(resource);
		final Date now = CreateDateExtensions.now();
		final Date expiryDate = CalculateDateExtensions.addDays(now, 1);

		ResetPassword resetPassword;

		resetPassword = resource.findResetPassword(getPromoterUser());
		if (resetPassword == null)
		{
			resetPassword = ResetPassword.builder().starttime(now).expiryDate(expiryDate)
				.user(getPromoterUser()).generatedPassword("very-secret").build();
			resetPassword = resource.create(resetPassword);
		}
		AssertJUnit.assertNotNull(resetPassword);

		final KeyValuePair<User, String> userAndGenPw = KeyValuePair.<User, String> builder()
			.key(getPromoterUser()).value("very-secret").build();

		final ResetPassword actual = resource.findResetPassword(userAndGenPw);
		AssertJUnit.assertNotNull(actual);


	}

	/**
	 * Test the {@link RolesResource}.
	 */
	@Test(enabled = true)
	public void testRolesResource()
	{
		AssertJUnit.assertNotNull(rolesResource);
		final Role role = getAdminRole();
		AssertJUnit.assertNotNull(role);

		final List<Permission> permissions = rolesResource.findAllPermissions(role);
		AssertJUnit.assertNotNull(permissions);


	}

	private Role getAdminRole() {
		Role role = rolesResource.findRole("ADMIN");
		if (role == null)
		{
			role = Role.builder().rolename("ADMIN").description("The admin role").build();
			role = rolesResource.create(role);
		}
		return role;
	}

	/**
	 * Test the {@link BaseUsersResource}.
	 */
	@Test(enabled = false)
	public void testUsersResource()
	{
		AssertJUnit.assertNotNull(usersResource);

		final String username = "james.dean@gmail.com";
		final User expected = usersResource.findUserWithUsername(username);

		AssertJUnit.assertEquals(expected.getUsername(), "james.dean");
	}

	public User getPromoterUser()
	{
		if (promoterUser == null)
		{
			final String promoterEmail = "michael.knight@gmail.com";
			promoterUser = usersResource.findUserWithUsername(promoterEmail);
			if (promoterUser.getActive() == null)
			{
				promoterUser.setActive(true);
				usersResource.update(promoterUser);
			}
		}
		return promoterUser;
	}

	public User getRecommendedUser()
	{
		if (recommendedUser == null)
		{
			final String promoterEmail = "james.dean@gmail.com";
			recommendedUser = usersResource.findUserWithUsername(promoterEmail);
			if (recommendedUser.getActive() == null)
			{
				recommendedUser.setActive(true);
				usersResource.update(recommendedUser);
			}
		}
		return recommendedUser;
	}

	/**
	 * Factory method for create new {@link ArrayList} and returns as {@link List}.
	 *
	 * @param <T>            the generic type
	 * @param elements the elements to add in the new {@link ArrayList}.
	 * @return the new {@link List}.
	 * @deprecated use the synonyme method in ListExtensions.
	 */
	@Deprecated
	@SafeVarargs
	public static <T> List<T> newArrayList(final T... elements) {
		final List<T> list = new ArrayList<>();
		Collections.addAll(list, elements);
		return list;
	}

}
