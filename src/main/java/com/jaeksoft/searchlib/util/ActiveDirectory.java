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
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.collections.CollectionUtils;

import com.jaeksoft.searchlib.Logging;

/**
 * License Agreement for OpenSearchServer
 * 
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

public class ActiveDirectory implements Closeable {

	private DirContext dirContext = null;
	private String domainSearchName = null;

	public ActiveDirectory(String username, String password, String domain)
			throws NamingException {
		if (StringUtils.isEmpty(domain))
			throw new NamingException("The domain is empty");
		Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		properties.put(Context.PROVIDER_URL,
				StringUtils.fastConcat("LDAP://", domain));
		properties.put(Context.SECURITY_PRINCIPAL,
				StringUtils.fastConcat(username, "@", domain));
		properties.put(Context.SECURITY_CREDENTIALS, password);
		properties.put("java.naming.ldap.attributes.binary", "objectSID");
		properties.put(Context.REFERRAL, "follow");
		dirContext = new InitialDirContext(properties);
		domainSearchName = getDomainSearch(domain);
	}

	public final static String ATTR_CN = "cn";
	public final static String ATTR_MAIL = "mail";
	public final static String ATTR_GIVENNAME = "givenName";
	public final static String ATTR_MEMBEROF = "memberOf";
	public final static String ATTR_OBJECTSID = "objectSid";
	public final static String ATTR_SAMACCOUNTNAME = "sAMAccountName";

	public final static String[] DefaultReturningAttributes = { ATTR_CN,
			ATTR_MAIL, ATTR_GIVENNAME, ATTR_OBJECTSID, ATTR_SAMACCOUNTNAME,
			ATTR_MEMBEROF };

	private NamingEnumeration<SearchResult> find(String filterExpr,
			String... returningAttributes) throws NamingException {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		if (returningAttributes == null || returningAttributes.length == 0)
			returningAttributes = DefaultReturningAttributes;
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

	public NamingEnumeration<SearchResult> findUser(String username,
			String... returningAttributes) throws NamingException {
		return find(
				StringUtils.fastConcat(
						"(&((&(objectCategory=Person)(objectClass=User)))(samaccountname=",
						username, "))"), returningAttributes);
	}

	private NamingEnumeration<SearchResult> findGroup(String group)
			throws NamingException {
		return find(StringUtils
				.fastConcat(
						"(&((&(objectCategory=Group)(objectClass=Group)))(samaccountname=",
						group, "))"));
	}

	private void findGroups(Collection<ADGroup> groups,
			Collection<ADGroup> collector, Set<String> searchedGroups)
			throws NamingException {
		if (CollectionUtils.isEmpty(groups))
			return;
		List<ADGroup> newGroups = new ArrayList<ADGroup>();
		for (ADGroup group : groups) {
			if (searchedGroups.contains(group.cn))
				continue;
			collector.add(group);
			searchedGroups.add(group.cn);
			NamingEnumeration<SearchResult> result = findGroup(group.cn);
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

	@Override
	public void close() {
		try {
			if (dirContext != null)
				dirContext.close();
			dirContext = null;
		} catch (NamingException e) {
			Logging.warn(e);
		}
	}

	private static String getDomainName(String domain) {
		String[] dcs = StringUtils.split(domain.toUpperCase(), '.');
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
		return sb.toString();
	}

	public static void collectMemberOf(Attributes attrs,
			Collection<ADGroup> groups) throws NamingException {
		Attribute tga = attrs.get("memberOf");
		if (tga == null)
			return;
		NamingEnumeration<?> membersOf = tga.getAll();
		while (membersOf.hasMore()) {
			Object memberObject = membersOf.next();
			groups.add(new ADGroup(memberObject.toString()));
		}
		membersOf.close();
	}

	public static class ADGroup {

		public final String cn;
		public final String dc;

		private ADGroup(final String memberOf) {
			String[] parts = StringUtils.split(memberOf, ',');
			String lcn = null;
			String ldc = null;
			for (String part : parts) {
				String[] pair = StringUtils.split(part, "=");
				if (pair == null || pair.length != 2)
					continue;
				if (lcn == null && "CN".equals(pair[0]))
					lcn = pair[1];
				if (ldc == null && "DC".equals(pair[0]))
					ldc = pair[1].toUpperCase();
			}
			this.cn = lcn;
			this.dc = ldc;
		}
	}

	public static String[] toArray(List<ADGroup> groups) {
		if (groups == null)
			return null;
		String[] array = new String[groups.size()];
		int i = 0;
		for (ADGroup group : groups)
			array[i++] = StringUtils.fastConcat(group.dc, '\\', group.cn);
		return array;
	}

	public static void main(String[] args) {
		System.out.println(getDisplayString("sp.int.fr", "01234"));
		System.out
				.println(new ADGroup(
						"CN=GG-TEST-TEST-TEST,OU=Groupes Ressource,OU=Groupes,OU=DSCP,DC=sp,DC=pn,DC=int"));
	}

	private static String getDomainSearch(String domain) {
		String[] dcs = StringUtils.split(domain.toUpperCase(), '.');
		StringBuilder sb = new StringBuilder();
		for (String dc : dcs) {
			if (sb.length() > 0)
				sb.append(',');
			sb.append("DC=");
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
		return s.substring(i + 1);
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
