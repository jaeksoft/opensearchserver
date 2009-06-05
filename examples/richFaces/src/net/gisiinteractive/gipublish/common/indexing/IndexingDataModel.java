package net.gisiinteractive.gipublish.common.indexing;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.apache.lucene.queryParser.ParseException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.result.Result;
import com.jaeksoft.searchlib.result.ResultDocument;

public class IndexingDataModel<T> extends ListDataModel {

	private int currentStart;
	private int currentIndex;
	private int size;
	private List<T> list;
	private Object data;
	private Client client;
	private SearchRequest request;
	private IndexedDocument<T> manager;

	public IndexingDataModel(IndexedDocument<T> manager, Client client,
			SearchRequest request) {
		this.client = client;
		this.request = request;
		this.list = new ArrayList<T>();
		this.currentStart = -1;
		this.currentIndex = -1;
		this.size = 0;
		this.manager = manager;
		populate(0);
	}

	private boolean needUpdate(long index) {
		synchronized (this) {

			return index < currentStart
					|| index >= currentStart + request.getRows();
		}
	}

	@Override
	public int getRowCount() {
		synchronized (this) {
			return size;
		}
	}

	@Override
	public Object getRowData() {
		synchronized (this) {
			if (currentIndex == -1 || currentStart == -1)
				return null;
			return list.get(currentIndex - currentStart);
		}
	}

	@Override
	public int getRowIndex() {
		synchronized (this) {
			return (int) currentIndex;
		}
	}

	@Override
	public Object getWrappedData() {
		synchronized (this) {
			return data;
		}
	}

	@Override
	public boolean isRowAvailable() {
		synchronized (this) {
			if (needUpdate(currentIndex))
				return false;
			long realIndex = currentIndex - currentStart;
			if (realIndex < 0)
				return false;
			if (realIndex >= list.size())
				return false;
			return true;
		}
	}

	@Override
	public void setRowIndex(int index) {
		synchronized (this) {
			currentIndex = index;
			if (index == -1)
				return;
			if (needUpdate(index))
				populate(index);
		}
	}

	@Override
	public void setWrappedData(Object data) {
		synchronized (this) {
			this.data = data;
		}
	}

	public void populate(int index) {
		// long time1 = System.currentTimeMillis();

		synchronized (this) {
			if (index == currentStart)
				return;

			request.reset();

			request.setStart(index);
			try {
				// list = null;
				list = new ArrayList<T>();
				size = 0;
				Result result = client.search(request);
				if (result != null && result.getNumFound() > 0) {

					for (ResultDocument resultDocument : result) {
						list.add(manager.createIndexedDocument(resultDocument));
					}
					size = result.getNumFound();

				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (ConnectException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				throw new RuntimeException(e);
			} catch (SyntaxError e) {
				throw new RuntimeException(e);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			currentStart = index;
		}

		// long time2 = System.currentTimeMillis();

		/*
		 * System.out.println("Log Timer populating response from Index " +
		 * request.getRequestName() + " with this query '" +
		 * request.getQueryString() + "' => " + (time2 - time1) + " millisec.");
		 */
	}

	public Iterator<?> pageIterator() {
		synchronized (this) {
			return list.iterator();
		}
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

}
