/*
 * Copyright 2011 gitblit.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tshank.sshproxy.models;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.tshank.sshproxy.Constants;
import org.tshank.sshproxy.Constants.AccessPermission;
import org.tshank.sshproxy.Constants.AccountType;
import org.tshank.sshproxy.utils.ArrayUtils;
import org.tshank.sshproxy.utils.ModelUtils;
import org.tshank.sshproxy.utils.SecureRandom;
import org.tshank.sshproxy.utils.StringUtils;


/**
 * UserModel is a serializable model class that represents a user and the user's
 * restricted repository memberships. Instances of UserModels are also used as
 * servlet user principals.
 *
 * @author James Moger
 *
 */
public class UserModel implements Principal, Serializable, Comparable<UserModel> {

	private static final long serialVersionUID = 1L;

	public static final UserModel ANONYMOUS = new UserModel();

	private static final SecureRandom RANDOM = new SecureRandom();

	// field names are reflectively mapped in EditUser page
	public String username;
	public String password;
	public String cookie;
	public String displayName;
	public String emailAddress;
	public String organizationalUnit;
	public String organization;
	public String locality;
	public String stateProvince;
	public String countryCode;
	public boolean canAdmin;
	public boolean canFork;
	public boolean canCreate;
	public boolean excludeFromFederation;
	public boolean disabled;
	// retained for backwards-compatibility with RPC clients
	@Deprecated
	public final Set<String> repositories = new HashSet<String>();
	public final Map<String, AccessPermission> permissions = new LinkedHashMap<String, AccessPermission>();
	public final Set<TeamModel> teams = new TreeSet<TeamModel>();

	// non-persisted fields
	public boolean isAuthenticated;
	public AccountType accountType;

	public UserPreferences userPreferences;

	public UserModel(String username) {
		this.username = username;
		this.isAuthenticated = true;
		this.accountType = AccountType.LOCAL;
		this.userPreferences = new UserPreferences(this.username);
	}

	private UserModel() {
		this.username = "$anonymous";
		this.isAuthenticated = false;
		this.accountType = AccountType.LOCAL;
		this.userPreferences = new UserPreferences(this.username);
	}

	public boolean isLocalAccount() {
		return !Constants.EXTERNAL_ACCOUNT.equals(password)
				|| accountType == null
				|| accountType.isLocal();
	}

	/**
	 * This returns true if the user has admin privileges or the user has admin
	 * privileges because of a team membership.
	 *
	 * @return true if the user can admin
	 */
	public boolean canAdmin() {
		if (canAdmin) {
			return true;
		}
		if (!ArrayUtils.isEmpty(teams)) {
			for (TeamModel team : teams) {
				if (team.canAdmin) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This returns true if the user has create privileges or the user has create
	 * privileges because of a team membership.
	 *
	 * @return true if the user can admin
	 */
	public boolean canCreate() {
		if (canCreate) {
			return true;
		}
		if (!ArrayUtils.isEmpty(teams)) {
			for (TeamModel team : teams) {
				if (team.canCreate) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if the user is allowed to create the specified repository.
	 *
	 * @param repository
	 * @return true if the user can create the repository
	 */
	public boolean canCreate(String repository) {
		if (canAdmin()) {
			// admins can create any repository
			return true;
		}
		if (canCreate()) {
			String projectPath = StringUtils.getFirstPathElement(repository);
			if (!StringUtils.isEmpty(projectPath) && projectPath.equalsIgnoreCase(getPersonalPath())) {
				// personal repository
				return true;
			}
		}
		return false;
	}


	public TeamModel getTeam(String teamname) {
		if (teams == null) {
			return null;
		}
		for (TeamModel team : teams) {
			if (team.name.equalsIgnoreCase(teamname)) {
				return team;
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return username;
	}

	public String getDisplayName() {
		if (StringUtils.isEmpty(displayName)) {
			return username;
		}
		return displayName;
	}

	public String getPersonalPath() {
		return ModelUtils.getPersonalPath(username);
	}

	public UserPreferences getPreferences() {
		return userPreferences;
	}

	@Override
	public int hashCode() {
		return username.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof UserModel) {
			return username.equals(((UserModel) o).username);
		}
		return false;
	}

	@Override
	public String toString() {
		return username;
	}

	@Override
	public int compareTo(UserModel o) {
		return username.compareTo(o.username);
	}

	/**
	 * Returns true if the name/email pair match this user account.
	 *
	 * @param name
	 * @param email
	 * @return true, if the name and email address match this account
	 */
	public boolean is(String name, String email) {
		// at a minimum a username or display name AND email address must be supplied
		if (StringUtils.isEmpty(name) || StringUtils.isEmpty(email)) {
			return false;
		}
		boolean nameVerified = name.equalsIgnoreCase(username) || name.equalsIgnoreCase(getDisplayName());
		boolean emailVerified = false;
		if (StringUtils.isEmpty(emailAddress)) {
			// user account has not specified an email address
			// fail
			emailVerified = false;
		} else {
			// user account has specified an email address
			emailVerified = email.equalsIgnoreCase(emailAddress);
		}
		return nameVerified && emailVerified;
	}

	public boolean isMyPersonalRepository(String repository) {
		String projectPath = StringUtils.getFirstPathElement(repository);
		return !StringUtils.isEmpty(projectPath) && projectPath.equalsIgnoreCase(getPersonalPath());
	}

	public String createCookie() {
		return StringUtils.getSHA1(RANDOM.randomBytes(32));
	}
}
