/**
 * License Agreement for OpenSearchServer
 * 
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 * 
 * OpenSearchServer is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * OpenSearchServer is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OpenSearchServer. If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.util;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.apache.commons.collections.CollectionUtils;

import com.jaeksoft.searchlib.Logging;

public class ActiveDirectory implements Closeable {

	private final DirContext dirContext;

	private final String domainSearchName;

	public ActiveDirectory(String serverName, String username, String password,
			String domain) throws NamingException {
		if (StringUtils.isEmpty(domain))
			throw new NamingException("The domain is empty");
		Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");

		domainSearchName = getDomainSearch(domain);
		String login = StringUtils.fastConcat(username, "@", domain);
		if (serverName != null) {
			properties.put(Context.PROVIDER_URL,
					StringUtils.fastConcat("ldap://", serverName, ":389"));
		}
		properties.put(Context.SECURITY_PRINCIPAL, login);
		properties.put(Context.SECURITY_CREDENTIALS, password);
		properties.put(Context.REFERRAL, "follow");
		properties.put("java.naming.ldap.attributes.binary", "objectSID");
		dirContext = new InitialLdapContext(properties, null);
	}

	public final static String ATTR_CN = "cn";
	public final static String ATTR_MAIL = "mail";
	public final static String ATTR_GIVENNAME = "givenName";
	public final static String ATTR_MEMBEROF = "memberOf";
	public final static String ATTR_OBJECTSID = "objectSid";
	public final static String ATTR_SAMACCOUNTNAME = "sAMAccountName";
	public final static String ATTR_DN = "DistinguishedName";

	private NamingEnumeration<SearchResult> find(String filterExpr,
			String... returningAttributes) throws NamingException {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setReturningAttributes(returningAttributes);
		return dirContext.search(domainSearchName, filterExpr, searchControls);
	}

	public static final Attributes getAttributes(
			NamingEnumeration<SearchResult> result) throws NamingException {
		if (result == null)
			return null;
		if (!result.hasMore())
			return null;
		SearchResult rs = (SearchResult) result.next();
		return rs.getAttributes();
	}

	public NamingEnumeration<SearchResult> findUser(String username)
			throws NamingException {
		return find(
				StringUtils.fastConcat(
						"(&(objectCategory=person)(objectClass=user)(samAccountType=805306368)(sAMAccountName=",
						username, "))"), ATTR_CN, ATTR_MAIL, ATTR_GIVENNAME,
				ATTR_OBJECTSID, ATTR_SAMACCOUNTNAME, ATTR_MEMBEROF, ATTR_DN);
	}

	private NamingEnumeration<SearchResult> findGroup(String group)
			throws NamingException {
		return find(
				StringUtils.fastConcat(
						"(&(objectCategory=group)(objectClass=group)(samAccountType=268435456)(sAMAccountName=",
						group, "))"), ATTR_CN, ATTR_MAIL, ATTR_GIVENNAME,
				ATTR_OBJECTSID, ATTR_SAMACCOUNTNAME, ATTR_MEMBEROF, ATTR_DN);
	}

	private void findGroups(Collection<ADGroup> groups,
			Collection<ADGroup> collector, Set<String> searchedGroups)
			throws NamingException {
		if (CollectionUtils.isEmpty(groups))
			return;
		List<ADGroup> newGroups = new ArrayList<ADGroup>();
		for (ADGroup group : groups) {
			if (searchedGroups.contains(group.cn1))
				continue;
			collector.add(group);
			searchedGroups.add(group.cn1);
			NamingEnumeration<SearchResult> result = findGroup(group.cn1);
			Attributes attrs = getAttributes(result);
			if (attrs == null)
				continue;
			collectMemberOf(attrs, newGroups);
		}
		findGroups(newGroups, collector, searchedGroups);
	}

	public void findUserGroups(Attributes userAttrs,
			Collection<ADGroup> collector) throws NamingException {
		List<ADGroup> groups = new ArrayList<ADGroup>();
		ActiveDirectory.collectMemberOf(userAttrs, groups);
		TreeSet<String> searchedGroups = new TreeSet<String>();
		findGroups(groups, collector, searchedGroups);
	}

	public void findUserGroup(String userDN, Collection<ADGroup> collector)
			throws NamingException {
		String filter = StringUtils.fastConcat(
				"(member:1.2.840.113556.1.4.1941:=", userDN, ')');
		NamingEnumeration<SearchResult> results = find(filter, ATTR_OBJECTSID,
				ATTR_DN);
		while (results.hasMore()) {
			SearchResult searchResult = results.next();
			Attributes groupAttrs = searchResult.getAttributes();
			Logging.info("ATTRS: " + groupAttrs.toString());
			ADGroup adGroup = new ADGroup(getObjectSID(groupAttrs),
					getStringAttribute(groupAttrs, ATTR_DN));
			collector.add(adGroup);
		}
	}

	@Override
	public void close() {
		try {
			if (dirContext != null)
				dirContext.close();
		} catch (NamingException e) {
			Logging.warn(e);
		}
	}

	private static String getDomainName(String domain) {
		String[] dcs = StringUtils.split(domain, '.');
		return dcs != null && dcs.length > 0 ? dcs[0] : null;
	}

	final public static String getDisplayString(String domain, String user) {
		StringBuilder sb = new StringBuilder();
		String domainName = getDomainName(domain);
		if (domainName != null)
			sb.append(domainName);
		if (user != null) {
			if (sb.length() > 0)
				sb.append('\\');
			sb.append(user);
		}
		return sb.toString().toLowerCase();
	}

	public static void collectMemberOf(Attributes attrs,
			Collection<ADGroup> groups) throws NamingException {
		Attribute tga = attrs.get("memberOf");
		if (tga == null)
			return;
		NamingEnumeration<?> membersOf = tga.getAll();
		while (membersOf.hasMore()) {
			Object memberObject = membersOf.next();
			groups.add(new ADGroup(getObjectSID(attrs), memberObject.toString()));
		}
		membersOf.close();
	}

	public static class ADGroup {

		public final String sid;
		public final String cn1;
		public final String cn2;
		public final String dc;

		private ADGroup(final String sid, final String memberOf) {
			this.sid = sid;
			String[] parts = StringUtils.split(memberOf, ',');
			String lcn1 = null;
			String lcn2 = null;
			String ldc = null;
			for (String part : parts) {
				String[] pair = StringUtils.split(part, "=");
				if (pair == null || pair.length != 2)
					continue;
				if ("cn".equalsIgnoreCase(pair[0])) {
					if (lcn1 == null)
						lcn1 = pair[1];
					else if (lcn2 == null)
						lcn2 = pair[1];
				}
				if (ldc == null && "dc".equalsIgnoreCase(pair[0]))
					ldc = pair[1];
			}
			this.cn1 = lcn1;
			this.cn2 = lcn2;
			this.dc = ldc;
		}
	}

	public static String[] toArray(Collection<ADGroup> groups,
			String... additionalGroups) {
		TreeSet<String> groupSet = new TreeSet<String>();
		for (ADGroup group : groups) {
			if ("builtin".equalsIgnoreCase(group.cn2))
				groupSet.add(group.cn1.toLowerCase());
			else
				groupSet.add(StringUtils.fastConcat(group.dc, '\\', group.cn1)
						.toLowerCase());
			groupSet.add(group.sid);
		}
		if (additionalGroups != null)
			for (String additionalGroup : additionalGroups)
				groupSet.add(additionalGroup);
		return groupSet.toArray(new String[groupSet.size()]);
	}

	public static void main(String[] args) throws NamingException {
		System.out.println(getDisplayString("sp.int.fr", "01234"));
		System.out
				.println(new ADGroup(
						null,
						"CN=GG-TEST-TEST-TEST,OU=Groupes Ressource,OU=Groupes,OU=DSCP,DC=sp,DC=pn,DC=int"));
	}

	private static String getDomainSearch(String domain) {
		String[] dcs = StringUtils.split(domain, '.');
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String dc : dcs) {
			if (!first)
				sb.append(',');
			else
				first = false;
			sb.append("dc=");
			sb.append(dc);
		}
		return sb.toString();
	}

	public static String getStringAttribute(Attributes attrs, String name) {
		Attribute attr = attrs.get(name);
		if (attr == null)
			return null;
		String s = attr.toString();
		if (StringUtils.isEmpty(s))
			return s;
		int i = s.indexOf(':');
		if (i == -1)
			throw new IllegalArgumentException(StringUtils.fastConcat(
					"Wrong returned value: ", s));
		return s.substring(i + 1).trim();
	}

	public static String getObjectSID(Attributes attrs) throws NamingException {
		Attribute attr = attrs.get("objectsid");
		if (attr == null)
			throw new NamingException("No ObjectSID attribute");
		Object attrObject = attr.get();
		if (attrObject == null)
			throw new NamingException("ObjectSID is empty");
		if (attrObject instanceof String) {
			String attrString = (String) attrObject;
			if (attrString.startsWith("S-"))
				return attrString;
			return decodeSID(attrString.getBytes());
		} else if (attrObject instanceof byte[]) {
			return decodeSID((byte[]) attrObject);
		} else
			throw new NamingException("Unknown attribute type: "
					+ attrObject.getClass().getName());
	}

	/**
	 * The binary data is in the form: byte[0] - revision level byte[1] - count
	 * of sub-authorities byte[2-7] - 48 bit authority (big-endian) and then
	 * count x 32 bit sub authorities (little-endian)
	 * 
	 * The String value is: S-Revision-Authority-SubAuthority[n]...
	 * 
	 * Based on code from here -
	 * http://forums.oracle.com/forums/thread.jspa?threadID=1155740&tstart=0
	 */
	public static String decodeSID(byte[] sid) {

		final StringBuilder strSid = new StringBuilder("S-");

		// get version
		final int revision = sid[0];
		strSid.append(Integer.toString(revision));

		// next byte is the count of sub-authorities
		final int countSubAuths = sid[1] & 0xFF;

		// get the authority
		long authority = 0;
		// String rid = "";
		for (int i = 2; i <= 7; i++) {
			authority |= ((long) sid[i]) << (8 * (5 - (i - 2)));
		}
		strSid.append("-");
		strSid.append(Long.toHexString(authority));

		// iterate all the sub-auths
		int offset = 8;
		int size = 4; // 4 bytes for each sub auth
		for (int j = 0; j < countSubAuths; j++) {
			long subAuthority = 0;
			for (int k = 0; k < size; k++) {
				subAuthority |= (long) (sid[offset + k] & 0xFF) << (8 * k);
			}

			strSid.append("-");
			strSid.append(subAuthority);

			offset += size;
		}

		return strSid.toString();
	}

}
