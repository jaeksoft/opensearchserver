package net.gisiinteractive.gipublishweb.impl.jsf.listeners;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;

/**
 * disables caching and storing of the page in browser back button
 * 
 * @author zhamdi
 * 
 */
public class DisableBackButtonCachingPhaseListener implements PhaseListener {
	private static final long serialVersionUID = 1L;

	public void beforePhase(PhaseEvent event) {
		FacesContext facesContext = event.getFacesContext();
		HttpServletResponse response = (HttpServletResponse) facesContext
				.getExternalContext().getResponse();
		response.setHeader("Pragma", "no-cache");
		response.addHeader("Pragma", "no-store");
		response.setHeader("Cache-Control", "no-cache");
		response.addHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "must-revalidate");
		response.addHeader("Expires", "0");
	}

	@Override
	public void afterPhase(PhaseEvent event) {
	}

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.RENDER_RESPONSE;
	}
}
