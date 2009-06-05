package net.gisiinteractive.common.data.selection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.gisiinteractive.gipublish.common.indexing.IndexingDataModel;

/**
 * Abstract class designed to analyse the existence of the current instance
 * (identified by {@link SelectableElement#getHashableId()} in the list returned
 * by {@link SelectableElement#getHashedIndex()}. A typical use is to pass a
 * shared hashedIndex at construction or immediately after loading(to avoid
 * unconsitent state).
 * 
 * Subclasses can override {@link SelectableElement#getHashedIndex()} and access
 * a different index dynamically.
 * 
 * @author zhamdi
 * 
 * @param <K>
 */
public abstract class SelectableElement<K> {

	protected HashSet<K> hashedIndex;

	protected abstract K getHashableId();

	public boolean isSelected() {

		return getHashedIndex().contains(getHashableId());

	}

	public void setSelected(boolean selected) {

		if (selected)
			getHashedIndex().add(getHashableId());
		else
			getHashedIndex().remove(getHashableId());
	}

	/**
	 * Toggles selection.
	 * 
	 * @return the final state.
	 */
	public boolean toggleSelection() {
		boolean state = !isSelected();
		setSelected(state);
		return state;
	}

	public HashSet<K> getHashedIndex() {
		return hashedIndex;
	}

	public void setHashedIndex(HashSet<K> hashedIndex) {
		this.hashedIndex = hashedIndex;
	}

	@SuppressWarnings("unchecked")
	public static <T extends SelectableElement<?>> List<T> getSelected(
			List<T> documents) {

		List<T> toReturn = new ArrayList<T>();

		if (documents instanceof IndexingDataModel) {
			IndexingDataModel indexingDataModel = (IndexingDataModel) documents;
			List toIterate = (List) indexingDataModel.getWrappedData();
			for (Iterator iterator = toIterate.iterator(); iterator.hasNext();) {

				T document = (T) iterator.next();
				if (document.isSelected())
					toReturn.add(document);
			}

		} else {
			for (T document : documents) {
				if (document.isSelected())
					toReturn.add(document);
			}
		}

		return toReturn;
	}

}
