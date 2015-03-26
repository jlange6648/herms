package edu.rit.se;

import javax.swing.JOptionPane;

import com.telelogic.rhapsody.core.IRPApplication;
import com.telelogic.rhapsody.core.RPUserPlugin;

public class RequirementsReport extends RPUserPlugin {

	
	protected IRPApplication m_rhpApplication = null;

	// called when the plug-in is loaded
	@Override
	public void RhpPluginInit(final IRPApplication rpyApplication) {

		// keep the application interface for later use
		m_rhpApplication = rpyApplication;
		// show the build number
		//JOptionPane.showMessageDialog(null, "Hello world from SimplePlugin.RhpPluginInit.\n You are using Rhapsody build "	+ m_rhpApplication.getBuildNo());
		m_rhpApplication.writeToOutputWindow(null , "Requirements Report Plugin Initialized.\n");
	}

	@Override
	public void RhpPluginInvokeItem() {
		// TODO Auto-generated method stub
		m_rhpApplication.writeToOutputWindow(null , "Requirements Report Plugin Running...\n");
		ReqExporter.performAction(m_rhpApplication);
	}

	@Override
	public void OnMenuItemSelect(String menuItem) {
		// TODO Auto-generated method stub
		m_rhpApplication.writeToOutputWindow(null , "Menu: " + menuItem + "\n");
	}

	@Override
	public void OnTrigger(String trigger) {
		// TODO Auto-generated method stub
		m_rhpApplication.writeToOutputWindow(null , "Trigger: " + trigger + "\n");
	}

	@Override
	public boolean RhpPluginCleanup() {
		// TODO Auto-generated method stub
		m_rhpApplication.writeToOutputWindow(null , "Plugin Cleanup\n");
		m_rhpApplication = null;
		return false;
	}

	@Override
	public void RhpPluginFinalCleanup() {
		// TODO Auto-generated method stub
		m_rhpApplication.writeToOutputWindow(null , "Final Cleanup\n");
	}

}
