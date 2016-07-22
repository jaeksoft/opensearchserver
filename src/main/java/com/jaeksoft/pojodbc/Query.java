/**   
 * License Agreement for OpenSearchServer Pojodbc
 *
 * Copyright 2008-2013 Emmanuel Keller / Jaeksoft
 * Copyright 2014-2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaeksoft.pojodbc;

import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jaeksoft.pojodbc.connection.ConnectionManager;

/**
 * Represents an SQL query. In JDBC view, a query contains at least a
 * PreparedStatement. It can also contains a ResultSet. Statement and ResultSet
 * are automatically closed when Query or Transaction is closed.
 * <p>
 * The most important behavior is to return a list of Pojo instead of a
 * ResultSet.
 * </p>
 * <p>
 * The example show how to use it.
 * </p>
 * 
 * <pre>
 * Transaction transaction = null;
 * try {
 *   // Obtain a new transaction from the ConnectionManager
 *   transaction = connectionManager.getNewTransaction(false,
 *                             javax.sql.Connection.TRANSACTION_READ_COMMITTED);
 *   // Start a new Query
 *   Query query = transaction.prepare(&quot;SELECT * FROM MyTable WHERE status=?&quot;);
 *   query.getStatement().setString(1, &quot;open&quot;);
 *   query.setFirstResult(0);
 *   query.setMaxResults(10);
 *   
 *   // Get the result
 *   List&lt;MyPojo&gt; myPojoList = query.getResultList(MyPojo.class));
 *   
 *   // do everything you need
 *   
 * } finally {
 *   // Release the transaction
 *   if (transaction != null)
 *     transaction.close();
 * }
 * </pre>
 * 
 * 
 */
public class Query {

	private ResultSet resultSet;
	private HashMap<Class<?>, List<?>> resultListMap;
	private PreparedStatement statement;
	private int firstResult;
	private int maxResults;

	static protected Logger logger = Logger.getLogger(Query.class.getCanonicalName());

	protected Query(PreparedStatement statement) {
		this.statement = statement;
		firstResult = 0;
		maxResults = -1;
		resultListMap = new HashMap<Class<?>, List<?>>();
	}

	/**
	 * @param firstResult
	 *            the position of the first result
	 */
	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}

	/**
	 * @param maxResults
	 *            the maximum number of rows returned
	 */
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	/**
	 * Close all component of that query (ResultSet and Statement)
	 */
	protected void closeAll() {
		ConnectionManager.close(resultSet, statement, null);
	}

	private class MethodColumnIndex {
		private int columnIndex;
		private Method method;

		private MethodColumnIndex(int columnIndex, Method method) {
			this.columnIndex = columnIndex;
			this.method = method;
		}

		private void invoke(Object bean, ResultSet resultSet) throws Exception {
			if (method == null)
				return;
			Object colObject = resultSet.getObject(columnIndex);
			try {
				if (colObject != null)
					method.invoke(bean, colObject);
			} catch (Exception e) {
				if (method == null)
					throw new Exception("No method found for column " + columnIndex, e);
				throw new Exception("Error on column " + columnIndex + " method " + method.getName()
						+ (colObject == null ? "" : " object class is " + colObject.getClass().getName()), e);
			}
		}
	}

	private <T> List<T> createBeanList(Class<T> beanClass) throws Exception {
		// Find related methods and columns
		ResultSetMetaData rs = resultSet.getMetaData();
		int columnCount = rs.getColumnCount();
		BeanInfo beanInfo;
		beanInfo = Introspector.getBeanInfo(beanClass);
		PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
		ArrayList<MethodColumnIndex> methods = new ArrayList<MethodColumnIndex>();

		if (logger.isLoggable(Level.FINEST))
			logger.finest("Search properties for bean " + beanClass.getSimpleName());
		for (int i = 1; i <= columnCount; i++) {
			String columnName = rs.getColumnLabel(i);
			for (PropertyDescriptor propDesc : props) {
				if (propDesc.getWriteMethod() != null && propDesc.getName().equalsIgnoreCase(columnName)) {
					methods.add(new MethodColumnIndex(i, propDesc.getWriteMethod()));
					if (logger.isLoggable(Level.FINEST))
						logger.finest(
								"Found property \"" + propDesc.getName() + "\" for column name \"" + columnName + "\"");
					break;
				}
			}
		}
		// Create bean list
		List<T> list = new ArrayList<T>();
		moveToFirstResult();
		int limit = maxResults;
		while (resultSet.next() && limit-- != 0) {
			@SuppressWarnings("unchecked")
			T bean = (T) Beans.instantiate(beanClass.getClassLoader(), beanClass.getCanonicalName());
			for (MethodColumnIndex methodColumnIndex : methods)
				methodColumnIndex.invoke(bean, resultSet);
			list.add(bean);
		}
		return list;
	}

	private void moveToFirstResult() throws SQLException {
		if (firstResult == 0)
			return;
		switch (statement.getResultSetType()) {
		case ResultSet.TYPE_FORWARD_ONLY:
			int i = firstResult;
			while (i-- > 0)
				resultSet.next();
			break;
		default:
			resultSet.absolute(firstResult);
			break;
		}
	}

	private static List<Row> createRowList(ResultSet resultSet, int limit) throws SQLException {
		ResultSetMetaData rs = resultSet.getMetaData();
		int columnCount = rs.getColumnCount();
		ArrayList<Row> rows = new ArrayList<Row>();
		while (resultSet.next() && limit-- != 0)
			rows.add(new Row(columnCount, resultSet));
		return rows;
	}

	private List<Row> createRowList() throws SQLException {
		moveToFirstResult();
		List<Row> rows = createRowList(resultSet, maxResults);
		return rows;
	}

	/**
	 * Get the PreparedStatement used by that Query
	 * 
	 * @return a PreparedStatement
	 */
	public PreparedStatement getStatement() {
		return statement;
	}

	/**
	 * Release the last ResultSet (if any) and the last ResultList.
	 */
	public void reUse() {
		if (resultSet != null) {
			ConnectionManager.close(resultSet, null, null);
			resultSet = null;
		}
		resultListMap.clear();
	}

	private void checkResultSet() throws SQLException {
		if (resultSet != null)
			return;
		if (maxResults != -1)
			statement.setFetchSize(maxResults);
		resultSet = statement.executeQuery();
	}

	/**
	 * Returns the list of POJO. The list is cached. Every subsequent call
	 * returns the same list.
	 * 
	 * @param beanClass
	 *            The class name of POJO returned in the list
	 * @param <T>
	 *            The type of the pojo
	 * @return a list of POJO
	 * @throws Exception
	 *             if any JDBC error occurs
	 */
	public <T> List<T> getResultList(Class<T> beanClass) throws Exception {
		@SuppressWarnings("unchecked")
		List<T> resultList = (List<T>) resultListMap.get(beanClass);
		if (resultList != null)
			return (List<T>) resultList;
		checkResultSet();
		resultList = createBeanList(beanClass);
		resultListMap.put(beanClass, resultList);
		return resultList;
	}

	/**
	 * @return a list of Row object.
	 * @throws SQLException
	 *             if any JDBC error occurs
	 */
	public List<Row> getResultList() throws SQLException {
		checkResultSet();
		return createRowList();
	}

	/**
	 * Do a PreparedStatement.executeUpdate(). A convenient way to execute an
	 * INSERT/UPDATE/DELETE SQL statement.
	 * 
	 * @return a row count
	 * @throws SQLException
	 *             if any JDBC error occurs
	 */
	public int update() throws SQLException {
		return statement.executeUpdate();
	}

	/**
	 * Returns the generated keys after an insert statement
	 * 
	 * @return the list of generated keys
	 * @throws SQLException
	 *             if any JDBC error occurs
	 */
	public List<Row> getGeneratedKeys() throws SQLException {
		return createRowList(statement.getGeneratedKeys(), -1);
	}

	/**
	 * FirstResult and MaxResults parameters are ignored.
	 * 
	 * @return the number of row found for a select
	 * @throws SQLException
	 *             if any JDBC error occurs
	 */
	public int getResultCount() throws SQLException {
		checkResultSet();
		resultSet.last();
		return resultSet.getRow();
	}

	/**
	 * Get the ResultSet used by that Query.
	 * 
	 * @return the JDBC ResultSet
	 * @throws SQLException
	 *             if any JDBC error occurs
	 */
	public ResultSet getResultSet() throws SQLException {
		checkResultSet();
		return resultSet;
	}
}
