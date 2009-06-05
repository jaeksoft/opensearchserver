package net.gisiinteractive.common.view.jsf;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import net.gisiinteractive.gipublish.common.utils.GisiLogger;

/**
 * follows JSF lifecycle
 * 
 * @author zhamdi
 * 
 */
public class DebugPhaseListener implements PhaseListener {
	private static final long serialVersionUID = 1L;
	private static GisiLogger LOG = GisiLogger
			.getLogger(DebugPhaseListener.class);

	@Override
	public void afterPhase(PhaseEvent event) {
		LOG.info("############### finished :" + event.getPhaseId());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void beforePhase(PhaseEvent event) {
		if (event.getPhaseId().equals(PhaseId.INVOKE_APPLICATION)) {
			Map<String, Object> parameterMap = ((HttpServletRequest) event
					.getFacesContext().getExternalContext().getRequest())
					.getParameterMap();
			Set<String> keySet = parameterMap.keySet();
			for (String key : keySet) {
				Object value = parameterMap.get(key);
				if (value instanceof String[])
					System.err.println("# " + key + " : "
							+ Arrays.asList((String[]) value));
				else
					System.err.println("# " + key + " : " + value);

			}
		}
		LOG.info("############### starting :" + event.getPhaseId());
	}

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
