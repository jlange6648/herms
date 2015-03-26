package edu.rit.se;

import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import com.telelogic.rhapsody.core.*;

public class ReqExporter {
	
	public static void performAction (IRPApplication app) {
		String output = "<html><head><style>"
				+ "h1 {margin-bottom: 5px; padding: 0;}"
				+ "h1 span.stereotype {font-size: 75%;}"
				+ "span.not-satisfied {font-weight: bold; color: red;}"
				+ "</style></head><body>";
		
		IRPProject project = app.activeProject();
		
		if (project == null) {
			app.writeToOutputWindow(null ,"No Rhapsody project currently open!<br>");
			return;
		}

		output += "<table><tbody>";
		for (Object r : project.getNestedElementsByMetaClass("Requirement", 1).toList())
		{
			output += "<tr><td>";
			IRPRequirement req = (IRPRequirement)r;
			output += "<h1>" + req.getName();
			for (Object s : req.getStereotypes().toList())
			{
				IRPStereotype st = (IRPStereotype) s;
				output += "<span class='stereotype'>&nbsp;&lt;&lt;" + st.getName() + "&gt;&gt;</span>";
			}
			output += "</h1>";
			output += "Description:<ul><li>" + req.getSpecification() + "</ul>";
			
			boolean satisfied = false;
			for (Object ref : req.getReferences().toList())
			{
				IRPModelElement el = (IRPModelElement) ref;
				if (el.getClass().getName().equals("com.telelogic.rhapsody.core.RPDependency"))
				{
					RPDependency dep = (RPDependency)el;
					
					for (Object s : el.getStereotypes().toList())
					{
						if (((IRPStereotype) s).getName().equals("satisfy"))
						{
							String path = dep.getDependent().getFullPathName();
							// Ignore the internal simplified model
							if ( !path.startsWith("CGSimplifiedModelPackage"))
							{
								if (!satisfied)
								{
									output += "Satisfied by<ul>";
								}
								output += "<li>" + path + "<br>";
								satisfied = true;
							}
						}
					}
				}
			}
			if (!satisfied)
			{
				output += "<span class='not-satisfied'>NOT SATISFIED!!!</span><br>";
			}
			else 
			{
				output += "</ul>";
			}
			output += "</td></tr>";
		}
		output += "</table></body></html>";
		
		JEditorPane editor = new JEditorPane ("text/html", output);
		editor.setPreferredSize(new Dimension(800,500));

		// wrap a scrollpane around it
		JScrollPane scrollPane = new JScrollPane(editor);	      
		// display them in a message dialog
		JOptionPane.showMessageDialog(null, scrollPane);
	}

	public static void main(String[] args) {
		IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
		performAction (app);
	}

}
