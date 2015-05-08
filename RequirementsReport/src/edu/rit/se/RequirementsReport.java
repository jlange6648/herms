package edu.rit.se;

import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.RPUserPlugin;

public class RequirementsReport extends RPUserPlugin {

	
	protected IRPApplication m_rhpApplication = null;

	// called when the plug-in is loaded
	@Override
	public void RhpPluginInit(final IRPApplication rpyApplication) {

		// keep the application interface for later use
		m_rhpApplication = rpyApplication;
		m_rhpApplication.writeToOutputWindow(null , "Requirements Report Plugin Initialized.\n");
	}

	@Override
	public void RhpPluginInvokeItem() {
		// TODO Auto-generated method stub
		m_rhpApplication.writeToOutputWindow(null , "Requirements Report Plugin Running...\n");
		//ReqExporter.performAction(m_rhpApplication);
		new ReqExporter.SimpleThread("test", m_rhpApplication).start();
		m_rhpApplication.writeToOutputWindow(null , "Requirements Report Plugin Done.\n");

	}

	@Override
	public void OnMenuItemSelect(String menuItem) {
	}

	@Override
	public void OnTrigger(String trigger) {
	}

	@Override
	public boolean RhpPluginCleanup() {
		m_rhpApplication = null;
		return false;
	}

	@Override
	public void RhpPluginFinalCleanup() {
	}

}
