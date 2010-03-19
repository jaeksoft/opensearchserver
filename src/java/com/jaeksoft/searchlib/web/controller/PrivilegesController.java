/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2010 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller;

import java.util.Set;

import javax.naming.NamingException;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.IndexRole;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class PrivilegesController extends CommonController implements
		ListitemRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7649444539904657758L;

	private User user;

	private String confirmPassword;

	private String selectedUserName;

	private IndexRole selectedIndexRole;

	private String selectedIndex;

	private String selectedRole;

	private Set<String> indexList;

	public PrivilegesController() throws SearchLibException, NamingException {
		super();
		user = new User("", "", false);
		confirmPassword = null;
		selectedUserName = null;
		selectedIndexRole = null;
		selectedIndex = null;
		selectedRole = null;
		indexList = null;
	}

	public Set<String> getUserNameSet() throws SearchLibException {
		return ClientCatalog.getUserList().getUserNameSet();
	}

	public Set<String> getIndexList() throws SearchLibException {
		if (indexList != null)
			return indexList;
		indexList = ClientCatalog.getClientCatalog(getLoggedUser());
		return indexList;

	}

	public User getUser() {
		return user;
	}

	public boolean selected() {
		return this.selectedUserName != null;
	}

	public boolean notSelected() {
		return !selected();
	}

	public String getSelectedUserName() {
		return this.selectedUserName;
	}

	public void setSelectedUserName(String name) throws SearchLibException {
		this.selectedUserName = name;
		user = new User(ClientCatalog.getUserList().get(name));
		confirmPassword = user.getPassword();
	}

	public String getSelectedIndex() {
		return this.selectedIndex;
	}

	public void setSelectedIndex(String indexName) throws SearchLibException {
		this.selectedIndex = indexName;
	}

	public IndexRole getSelectedIndexRole() {
		return this.selectedIndexRole;
	}

	public void setSelectedIndexRole(IndexRole indexRole)
			throws SearchLibException {
		this.selectedIndexRole = indexRole;
	}

	public Role getSelectedRole() {
		if (selectedRole == null)
			return null;
		return Role.valueOf(selectedRole);
	}

	public void setSelectedRole(Role role) throws SearchLibException {
		this.selectedRole = role == null ? null : role.name();
	}

	public Role[] getRoles() {
		return Role.values();
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String pass) {
		this.confirmPassword = pass;
	}

	private void validUser() throws SearchLibException {
		if (confirmPassword == null || confirmPassword.length() == 0)
			throw new SearchLibException("Please enter a password");
		if (!confirmPassword.equals(user.getPassword()))
			throw new SearchLibException("Passwords does not match");
	}

	public void onSave() throws InterruptedException, SearchLibException {
		try {
			validUser();
		} catch (SearchLibException e) {
			Messagebox.show(e.getMessage(), "Jaeksoft OpenSearchServer",
					Messagebox.OK, org.zkoss.zul.Messagebox.EXCLAMATION);
			return;
		}
		if (selectedUserName != null)
			user.copyTo(ClientCatalog.getUserList().get(selectedUserName));
		else
			ClientCatalog.getUserList().add(user);
		ClientCatalog.saveUserList();
		flushPrivileges(user);
		onCancel();
	}

	public void onCancel() {
		user = new User("", "", false);
		selectedUserName = null;
		confirmPassword = null;
		selectedIndex = null;
		reloadPage();
	}

	public void onDelete() throws SearchLibException {
		ClientCatalog.getUserList().remove(selectedUserName);
		ClientCatalog.saveUserList();
		onCancel();
	}

	public void onAddPrivilege() {
		user.addRole(selectedIndex, selectedRole);
		reloadPage();
	}

	public void onRoleRemove(Event event) {
		user.removeRole((IndexRole) event.getData());
		reloadPage();
	}

	public String getCurrentEditMode() throws SearchLibException {
		return selectedUserName == null ? "Create a new user"
				: "Edit the user " + selectedUserName;
	}

	@Override
	public void reloadPage() {
		indexList = null;
		super.reloadPage();
	}

	@Override
	public void reset() {
	}

	@Override
	public void render(Listitem listitem, Object data) throws Exception {
		IndexRole indexRole = (IndexRole) data;
		new Listcell(indexRole.getIndexName()).setParent(listitem);
		new Listcell(indexRole.getRole().name()).setParent(listitem);
		Listcell listcell = new Listcell();
		Button button = new Button("remove");
		button.addForward(null, "privileges", "onRoleRemove", indexRole);
		button.setParent(listcell);
		listcell.setParent(listitem);
	}

}
