package net.gisiinteractive.gipublishweb.impl.jsf;

import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import com.sun.faces.application.ActionListenerImpl;

public class GisiActionListener extends ActionListenerImpl {
	protected ActionListener original;

	public GisiActionListener(ActionListener original) {
		this.original = original;
	}

	@Override
	public void processAction(ActionEvent event) {
		super.processAction(event);
	}

}
