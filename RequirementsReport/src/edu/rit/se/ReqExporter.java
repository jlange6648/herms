package edu.rit.se;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

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

		String buildPath = project.getActiveConfiguration().getDirectory(1, "");		
		File dir = new File(buildPath);
		File[] directoryListing = dir.listFiles();
		if (directoryListing == null) {
			System.out.println("No Code!");
			output += "<h1>Note: No generated source code found.</h1>";
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
		
			boolean refined = false;
			for (Object d : req.getDependencies().toList())
			{
				RPDependency dep = (RPDependency) d;

				if (!refined)
				{
					output += "Refines Requirement<ul>";
				}
				output += "<li>" + dep.getDependsOn().getName() + "<br>";
				refined = true;
			}
			if (refined)
			{
				output += "</ul>";
			}
			
			refined = false;
			for (Object ref : req.getReferences().toList())
			{
				IRPModelElement el = (IRPModelElement) ref;
				if (el.getClass().getName().equals("com.telelogic.rhapsody.core.RPDependency"))
				{
					RPDependency dep = (RPDependency)el;
					
					for (Object s : el.getStereotypes().toList())
					{
						if (((IRPStereotype) s).getName().equals("refine"))
						{
							String path = dep.getDependent().getFullPathName();
							// Ignore the internal simplified model
							if ( !path.startsWith("CGSimplifiedModelPackage"))
							{
								if (!refined)
								{
									output += "Refined By Requirement<ul>";
								}
								output += "<li>" + dep.getDependent().getName() + "<br>";
								refined = true;
							}
						}
					}
				}
			}
			if (refined)
			{
				output += "</ul>";
			}

			Vector<String> fileMatches = parseSourceFiles (buildPath, req.getName(), app);
			if (!fileMatches.isEmpty())
			{
				output += "Source File:<ul>";
				
				for (String match : fileMatches)
				{
					output += "<li>" + match;
				}
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

	public static Vector<String> parseSourceFiles (String path, String requirement, IRPApplication app)
	{
		Vector<String> values = new Vector<String>();
		File dir = new File(path);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				Scanner scanner;
				try {
					scanner = new Scanner(child);
					int line = 0;
					while (scanner.hasNextLine()) {
						line++;
						final String lineFromFile = scanner.nextLine();
						if(lineFromFile.contains("// Realizes requirement " + requirement)) { 
					       // a match!
							values.add (child.getName() + ":" + line);
						}
					}
					scanner.close();
				} catch (FileNotFoundException e) {
					// nothing					
				}
			}
		}
		return values;
	}
	
	public static void main(String[] args) {
		IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
		performAction (app);
		//parseSourceFiles ("D:\\herms\\DefaultComponent\\production", "BOIL-PHASE-2");
		//for (String match : parseSourceFiles ("D:\\herms\\DefaultComponent\\production", "BOIL-PHASE-2"))
		//{
		//	System.out.println ( match );
		//}
	}

}
