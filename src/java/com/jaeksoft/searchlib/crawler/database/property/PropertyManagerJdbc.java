/**   
 * License Agreement for Jaeksoft SearchLib Community
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.jaeksoft.com
 * 
 * This file is part of Jaeksoft SearchLib Community.
 *
 * Jaeksoft SearchLib Community is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft SearchLib Community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft SearchLib Community. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.crawler.database.property;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.jaeksoft.pojojdbc.Query;
import com.jaeksoft.pojojdbc.Transaction;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseException;
import com.jaeksoft.searchlib.crawler.database.CrawlDatabaseJdbc;

public class PropertyManagerJdbc extends PropertyManager {

	private CrawlDatabaseJdbc database;

	public PropertyManagerJdbc(CrawlDatabaseJdbc database) {
		this.database = database;
	}

	private void update(Transaction transaction, PropertyItem property)
			throws SQLException {
		Query query = transaction.prepare("UPDATE property "
				+ "SET value=? WHERE name=?");
		PreparedStatement st = query.getStatement();
		st.setString(1, property.getValue());
		st.setString(2, property.getName());
		query.update();
	}

	private void insert(Transaction transaction, PropertyItem property)
			throws SQLException {
		Query query = transaction.prepare("INSERT INTO "
				+ "property(name,value) " + "VALUES (?,?)");
		PreparedStatement st = query.getStatement();
		st.setString(1, property.getName());
		st.setString(2, property.getValue());
		query.update();
	}

	private void insertOrUpdate(Transaction transaction, PropertyItem property,
			boolean bIgnoreDuplicate) throws SQLException {
		try {
			insert(transaction, property);
		} catch (SQLException e) {
			// Duplicate Key
			if ("23505".equals(e.getSQLState())) {
				if (bIgnoreDuplicate)
					return;
				update(transaction, property);
				return;
			}
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	protected String getPropertyString(Property prop)
			throws CrawlDatabaseException {
		Transaction transaction = null;
		try {
			transaction = database.getTransaction(true);
			Query query = transaction.prepare("SELECT value FROM property "
					+ "WHERE name=?");
			query.getStatement().setString(1, prop.getName());
			List<PropertyItem> propertyList = (List<PropertyItem>) query
					.getResultList(PropertyItem.class);
			if (propertyList == null || propertyList.size() == 0)
				return null;
			return propertyList.get(0).getValue();
		} catch (SQLException e) {
			throw new CrawlDatabaseException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

	protected void setProperty(PropertyItem property) {
		Transaction transaction = null;
		try {
			transaction = database.getTransaction(false);
			insertOrUpdate(transaction, property, false);
			transaction.commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (transaction != null)
				transaction.close();
		}
	}

}
