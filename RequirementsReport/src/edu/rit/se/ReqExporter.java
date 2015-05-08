package edu.rit.se;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.telelogic.rhapsody.core.*;

class fileResult {
	public fileResult (File filename, int line) {
		this.filename = filename;
		this.line = line;
	}
	public File filename;
	public int line;
}

public class ReqExporter {
	
	public static void performAction (final IRPApplication app) {
		
		// create a temp directory for the HTMLized source files
		Path tmpdir;
		try {
			tmpdir = Files.createTempDirectory("req_report_");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		String output = "<html><head><style>"
				+ "h1 {margin-bottom: 5px; padding: 0;}"
				+ "h1 span.stereotype {font-size: 75%;}"
				+ "span.not-satisfied {font-weight: bold; color: red;}"
				+ "</style></head><body>";
		
		final IRPProject project = app.activeProject();
		
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
								output += "<li>" + path;
								satisfied = true;
								if (dep.getDependent() instanceof RPState)
								{
									RPState st = (RPState) dep.getDependent();
									output += "<a href='state:/" +
											st.getGUID() + "'> (open diagram)</a>";
								}
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

			Vector<fileResult> fileMatches = parseSourceFiles (tmpdir, buildPath, req.getName(), app);
			if (!fileMatches.isEmpty())
			{
				output += "Source File:<ul>";
				
				for (fileResult match : fileMatches)
				{
					output += "<li><a href='" + match.filename.toURI().toString() + "'>" + match.filename.getName() + " line " + match.line + "</a>";
				}
				output += "</ul>";
			}
			output += "</td></tr>";
		}
		output += "</table></body></html>";
		
		final JEditorPane editor = new JEditorPane();
		editor.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
		editor.setEditable(false);
		editor.setPreferredSize(new Dimension(800,500));
		editor.setText(output);

		editor.addHyperlinkListener(new HyperlinkListener() {
			@Override
		    public void hyperlinkUpdate(HyperlinkEvent e) {
		        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		        	if (e.getDescription().startsWith("file:/"))
		        	{
		        		// open the file in the default system editor.
		        		if (Desktop.isDesktopSupported())
		        		{
		        			Desktop desktop = Desktop.getDesktop();
		        			try {
		        				URI u = new URI(e.getDescription());
								desktop.browse(u);
							} catch (IOException e1) {
								e1.printStackTrace();
							} catch (URISyntaxException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
		        		}		        		
		        	}
		        	else if (e.getDescription().startsWith("state:/"))
		        	{
		        		String state = e.getDescription().replaceFirst("state:/", "");
		        		RPState st = (RPState) project.findElementByGUID(state);
		        		IRPStatechartDiagram d = st.getItsStatechart().getStatechartDiagram();
		        		d.highLightElement();

		        	}
		        }
		    }
		});

		// wrap a scrollpane around it
		JScrollPane scrollPane = new JScrollPane(editor);	      
		// display them in a message dialog
		JOptionPane.showMessageDialog(null, scrollPane);
	}


	
	public static Vector<fileResult> parseSourceFiles (Path tmpdir, String path, String requirement, IRPApplication app)
	{
		Vector<fileResult> values = new Vector<fileResult>();
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
							fileResult r = new fileResult(child, line);
							values.add (r);
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

	public static class SimpleThread extends Thread {
	    public SimpleThread(String str, IRPApplication app) {
	    	super(str);
	    	this.m_app = app;
	    }
	    IRPApplication m_app;
	    public void run() {
	    	performAction (m_app);
	    }
	}
	
	public static void main(String[] args) {
		
		IRPApplication app = RhapsodyAppServer.getActiveRhapsodyApplication();
		//
		performAction (app);
		//parseSourceFiles ("D:\\herms\\DefaultComponent\\production", "BOIL-PHASE-2");
		//for (String match : parseSourceFiles ("D:\\herms\\DefaultComponent\\production", "BOIL-PHASE-2"))
		//{
		//	System.out.println ( match );
		//}
	}

}
