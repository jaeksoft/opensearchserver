package net.gisiinteractive.gipublishweb.controller.common;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletContext;

import net.gisiinteractive.common.exceptions.BusinessException;

import org.apache.catalina.core.ApplicationContextFacade;

public class CommonJsfShortcuts {

	public CommonJsfShortcuts() {
	}

	@SuppressWarnings("unchecked")
	public static HashSet<Long> getSelectionState(Class clazz) {
		return getSelectionState(clazz, "");
	}

	/**
	 * handles selection of an element in a list to diffuse it at the session
	 * level
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static HashSet<Long> getSelectionState(Class clazz, String context) {
		Map<String, Object> sessionMap = getFacesContext().getExternalContext()
				.getSessionMap();
		String name = "SState_" + context + clazz.getSimpleName();
		HashSet<Long> toReturn = (HashSet<Long>) sessionMap.get(name);
		if (toReturn == null) {
			toReturn = new HashSet<Long>();
			sessionMap.put(name, toReturn);
		}
		return toReturn;
	}

	public static FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}

	public UIComponent find(String id) {
		return getFacesContext().getViewRoot().findComponent(id);
	}

	public static Application getApplication() {
		if (getFacesContext() == null)
			return null;
		return getFacesContext().getApplication();
	}

	@SuppressWarnings("unchecked")
	public static <T> T getThroughEl(String el, Class<T> clazz) {
		Application application = getApplication();
		if (application == null)
			throw new BusinessException("FacesContext not available",
					BusinessException.Type.LOG);
		return (T) application.evaluateExpressionGet(getFacesContext(), "#{"
				+ el + "}", clazz);
	}

	public static File getServerRoot() {
		try {
			ApplicationContextFacade servletContext = (ApplicationContextFacade) FacesContext
					.getCurrentInstance().getExternalContext().getContext();
			if (servletContext == null)
				System.err.println("Servlet Context est nul.");

			return getServerRoot(servletContext);
		} catch (NullPointerException e) {
			System.err.println("Non User connection yet usable by FacesContext.");
		}
		
		return null;
	}

	public static File getServerRoot(ServletContext ctx) {
		File destination = new File(ctx.getRealPath("index.jsp"));
		if (!destination.exists())
			throw new RuntimeException(
					"Cannot resolve absolute path on server: " + destination);
		return destination.getParentFile();
	}

	@SuppressWarnings("unchecked")
	public static <T> T getComponent(ActionEvent actionEvent, Class<T> clazz) {
		UIComponent component = actionEvent.getComponent();
		while (!clazz.isInstance(component)) {
			component = component.getParent();
		}
		if (component == null)
			throw new IllegalArgumentException("No " + clazz.getSimpleName()
					+ " in structure");
		return (T) component;
	}

	/**
	 * Copied from {@link UIViewRoot}
	 * <p>
	 * Return the {@link UIComponent} (if any) with the specified
	 * <code>id</code>, searching recursively starting at the specified
	 * <code>base</code>, and examining the base component itself, followed by
	 * examining all the base component's facets and children (unless the base
	 * component is a {@link NamingContainer}, in which case the recursive scan
	 * is skipped.
	 * </p>
	 * 
	 * @param base
	 *            Base {@link UIComponent} from which to search
	 * @param id
	 *            Component identifier to be matched
	 */
	public static UIComponent findComponent(UIComponent base, String id,
			boolean checkId) {
		if (checkId && id.equals(base.getId())) {
			return base;
		}
		// Search through our facets and children
		UIComponent result = null;
		for (Iterator<UIComponent> i = base.getFacetsAndChildren(); i.hasNext();) {
			UIComponent kid = (UIComponent) i.next();
			if (!(kid instanceof NamingContainer)) {
				if (checkId && id.equals(kid.getId())) {
					result = kid;
					break;
				}
				result = findComponent(kid, id, true);
				if (result != null) {
					break;
				}
			} else if (id.equals(kid.getId())) {
				result = kid;
				break;
			}
		}
		return (result);

	}

}
