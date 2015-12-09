package report;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import def.Logger;
import def.Utils;

/**
 * Selenium Automation Framework
 * reporter.HtmlReport.java
 * Purpose: Contains methods to perform report generation.
 * 
 * @author SAURABH SHARMA
 * @version 0.0.1
 */
public class HTMLReporter {
	// Class Variables
	private static String reportContent = "";
	private static String filename = "";
	
	/**
	 * This method writes the head section of report
	 */
	private static String header() {
		return	 	"<head>\n"
				+ 	"	<title>Test Execution Report</title>\n"
				+	"	<link rel='stylesheet' href='./resources/css/bootstrap.min.css'>\n"
				+ 	"	<script src='./resources/js/jquery.min.js'></script>\n"
				+ 	"	<script src='./resources/js/bootstrap.min.js'></script>\n"
				+ 	"	<script src='./resources/js/report.min.js'></script>\n"
				+ 	"</head>\n";
	}
	
	/**
	 * This method writes the Page Header of the report
	 */
	private static String pageHeader() {
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		return 		"<div class='page-header'>\n"
				+	"	<h1>" + filename + " <small>Test execution report generated on " + dateFormat.format(date) + " at " + timeFormat.format(date) + "</small></h1>\n"
				+	"</div>\n";
	}
	
	/**
	 * This method writes the navigation tabs of the report.
	 */
	private static String navTabs() {
		return 		"<!-- Nav Tabs -->\n"
				+	"<ul class='nav nav-tabs nav-justified'>\n"
				+	"	<li class='active'><a href='#summary' data-toggle='tab'>Summary</a></li>\n"
				+   "	<li><a href='#modules' data-toggle='tab'>Modules</a></li>\n"
				+   "	<li><a href='#details' data-toggle='tab'>Details</a></li>\n"
				+   "</ul>\n";
	}
	
	/**
	 * This method writes the summary tab details using the input parameter.
	 * @param summary - String array with values as [Number of modules, Tests Executed, Test Steps executed, Number of TC passed, Number of TC failed, Number of TC skipped]
	 * @throws ParseException 
	 */
	private static String summaryTab(String[] summary) throws ParseException {
		return    	"<div class='tab-pane active' id='summary'>\n"
				+ 	"	<table class='table table-bordered table-hover'>\n"
				+ 	"			<tbody>\n"
				+ 	"				<tr>\n"
				+ 	"					<td class='info'><strong>Filename</strong></td>\n"
				+ 	"					<td>" + filename + "</td>\n"
				+ 	"				</tr>\n"
				+ 	"				<tr>\n"
				+ 	"					<td class='info'><strong>URL</strong></td>\n"
				+ 	"					<td>" + summary[0] + "</td>\n"
				+	"				</tr>\n"	
				+ 	"				<tr>\n"
				+ 	"					<td class='info'><strong>Driver Type</strong></td>\n"
				+ 	"					<td>" + summary[1] + "</td>\n"
				+	"				</tr>\n"
				+ 	"				<tr>\n"
				+ 	"					<td class='info'><strong>Start Time</strong></td>\n"
				+ 	"					<td>" + summary[2] + "</td>\n"
				+	"				</tr>\n"
				+ 	"				<tr>\n"
				+ 	"					<td class='info'><strong>End Time</strong></td>\n"
				+ 	"					<td>" + summary[3] + "</td>\n"
				+	"				</tr>\n"
				+ 	"				<tr>\n"
				+ 	"					<td class='info'><strong>Total Duration(In Minutes)</strong></td>\n"
				+ 	"					<td>" + Utils.timeDifference("dd/MM/yyyy HH:mm:ss:S", summary[2], summary[3], TimeUnit.MINUTES) + "</td>\n"
				+	"				</tr>\n"
				+ 	"				<tr>\n"
				+ 	"					<td class='info'><strong>Modules</strong></td>\n"
				+ 	"					<td>" + summary[4] + "</td>\n"
				+ 	"				</tr>\n"
                + 	"				<tr>\n"
                + 	"					<td class='info'><strong>Tests Executed</strong></td>\n"
                + 	"					<td>" + summary[5] + "</td>\n"
                + 	"				</tr>\n"
                + 	"				<tr>\n"
                + 	"					<td class='info'><strong>Tests Steps Executed</strong></td>\n"
                + 	"					<td>" + summary[6] + "</td>\n"
                + 	"				</tr>\n"
                + 	"				<tr>\n"
                + 	"					<td class='success'><strong>Passed</strong></td>\n"
                + 	"					<td>" + summary[7] + "</td>\n"
                + 	"				</tr>\n"
                + 	"				<tr>\n"
                + 	"					<td class='danger'><strong>Failed</strong></td>\n"
                + 	"					<td>" + summary[8] + "</td>\n"
                + 	"				</tr>\n"
                + 	"			</tbody>\n"
                + 	"	</table>\n"
				+ 	"</div>\n";
	}
	
	/**
	 * This method writes the module-wise summary details.
	 * @param moduleSum The HashMap of module summary with Module Name as Key and Summary Detail as Value.
	 */
	private static String moduleTab(HashMap<?,?> moduleSum) {
		String temp =   "<div class='tab-pane' id='modules'>\n"
					+   "	<table class='table table-bordered table-hover'>\n"
					+   "		<thead>\n"
					+   "			<tr class='info'>\n"
					+   "				<th>Module Name</th>\n"
					+   "				<th>Number of Test Cases</th>\n"
					+   "				<th>Passed Test Cases</th>\n"
					+   "				<th>Failed Test Cases</th>\n"
					+   "				<th>Skipped Test Cases</th>\n"
					+   "			</tr>\n"
					+   "		</thead>\n"
					+   "		<tbody>\n";
		
		Set<?> set = moduleSum.entrySet();
		Iterator<?> i = set.iterator();
		while(i.hasNext()) {
			Map.Entry<?, ?> me = (Map.Entry<?, ?>)i.next();
			String[] moduleStatus = (String[])me.getValue();
			temp += 	"			<tr>\n"
					+	"				<td>" + me.getKey().toString() + "</td>\n"
					+	"				<td>" + moduleStatus[0] + "</td>\n"
					+	"				<td>" + moduleStatus[1] + "</td>\n"
					+	"				<td>" + moduleStatus[2] + "</td>\n"
					+	"				<td>" + moduleStatus[3] + "</td>\n"
					+	" 			</tr>\n";
		}
		
		temp 		+=	"		</tbody>\n"
					+ 	"	</table>\n"
					+ 	"</div>\n";
		return temp;
	}
	
	private static String testCaseDetail(ArrayList<String[]> currentTestCaseSteps, String moduleName, String testCaseName) throws ParseException {
		// Write Previous Test Case Info
		String content = "";
		int StepCounter = 0;
		
		String header = "							<div class='panel panel-success'>\n";
		for(String[] testStep: currentTestCaseSteps) {
			if(testStep[2].contains("FAIL")) {
				header = 	"							<div class='panel panel-danger'>\n";
			}
		}
		content	+= 	header
				+ 	"								<div class='panel-heading'>\n"
				+ 	"									<h4 class='panel-title'>\n"
				+ 	"										<a data-toggle='collapse' data-parent='#" + moduleName + "_test_cases' href='#" + testCaseName + "'>" + testCaseName + "</a>\n"
				+ 	"									</h4>"
				+ 	"								</div>\n"
				+ 	"								<div id='" + testCaseName + "' class='panel-collapse collapse'>\n"
				+ 	"									<div class='panel-body'>\n"
				+ 	"										<table class='table table-bordered table-hover'>\n"
    			+ 	"											<thead>\n"
    			+ 	"												<tr class='info'>\n"
    			+ 	"													<th>Step Number</th>\n"
    			+ 	"													<th>Test Step Description</th>\n"
    			+ 	"													<th>Status</th>\n"
    			+	"													<th>Start Time</th>\n"
    			+ 	"													<th>End Time</th>\n"
    			+   "													<th>Duration <br/>(in Seconds)</th>\n"
    			+ 	"												</tr>\n"
    			+ 	"											</thead>\n"
    			+	"											<tbody>\n";
			
		for (String[] testStep : currentTestCaseSteps) {
			
			if(testStep[2].contains("FAIL")) {
				content += "								<tr class='danger'>\n";
			} else if(testStep[2].contains("WARNING")) {
				content += "								<tr class='warning'>\n";
			} else {
				content += "								<tr>\n";
			}
			
			content += "									<td>" + (++StepCounter) + "</td>\n";
			
			for (int j = 1; j < testStep.length; j++) {
				if(j==2 && (testStep[j].contains("FAIL") || testStep[2].contains("WARNING"))) {
					String[] t = testStep[j].split(":");
					content +=	"									<td><a class='show-modal' href='" + Utils.uglify(moduleName.toLowerCase() + "_" + testStep[0].toLowerCase() + "_" + testStep[1].toLowerCase() + "_error.png") + "' data-msg = '" + Utils.strConcat(t, 1, t.length-1) + "'>" + testStep[j].split(":")[0] + "</a></td>\n";
				} else {
					content +=	"									<td>" + testStep[j] + "</td>\n";
				}
			}
			content += "									<td>" + Utils.timeDifference("dd/MM/yyyy HH:mm:ss:S", testStep[3], testStep[4], TimeUnit.MILLISECONDS)/(1000.00) + "</td>\n" 
				 +	"							</tr>\n";
			
		}
			// Close TC Detail boday
		content 	+=	"											</tbody>\n"
				+ 	"										</table>\n"
				+ 	"									</div>\n"
				+ 	"								</div>\n"
				+ 	"							</div>\n";
		
		return content;
	}
	
	/**
	 * This method writes the test details of each module, including test name, test step name and status.
	 * @param hashMap The HashMap contains each module test details
	 * @throws ParseException 
	 */
	@SuppressWarnings({ "unchecked" })
	private static String detailTab(HashMap<?, ?> hashMap) throws ParseException {
		boolean activeFlag = true;
		
		String temp = 	"<div class='tab-pane' id='details'>\n"
					+ 	"	<div class='panel panel-default'>\n"
					+ 	"		<div class='panel-heading'><strong>Details of Test Execution</strong></div>\n"
					+ 	"			<div class='panel-body'>\n"
					+ 	"				<ul class='nav nav-tabs nav-justified'>\n";
		
		Set<?> set = hashMap.keySet();
		Iterator<?> i = set.iterator();
		while(i.hasNext()) {
			String moduleName = i.next().toString().toUpperCase();
			if(activeFlag == true) {
				temp += 	"					<li class='active'><a href='#" + moduleName + "' data-toggle='tab'>" + moduleName + "</a></li>\n";
				activeFlag = false;
			} else {
				temp += 	"					<li><a href='#" + moduleName + "' data-toggle='tab'>" + moduleName + "</a></li>\n";
			}
		}
		
		temp 		+=	"				</ul>\n"
        			+ 	"				<br>\n"
        			+	"				<div class='tab-content'>\n";
		activeFlag = true;
		set = hashMap.entrySet();
		i = set.iterator();
		while(i.hasNext()) {
			
			Map.Entry<String, ArrayList<String[]>> me = (Map.Entry<String, ArrayList<String[]>>)i.next();
			String moduleName = me.getKey().toString().toUpperCase();
			ArrayList<String[]> moduleSteps = (ArrayList<String[]>)me.getValue();
			
			if(activeFlag == true) {
				temp	+=  "					<div class='tab-pane active' id='" + moduleName + "'>\n";
				activeFlag = false;
			} else {
				temp	+=  "					<div class='tab-pane' id='" + moduleName + "'>\n";
			}
			
			temp 	+= 	"						<div class='panel-group' id='" + moduleName + "_test_cases'>\n";
			
			// Sort the test cases
			Collections.sort(moduleSteps, new Comparator<String[]>() {
	            public int compare(String[] strings, String[] otherStrings) {
	                return strings[0].compareTo(otherStrings[0]);
	            }
	        });
			
			// Set current test case name to first test case name
			String currentTestCaseName = moduleSteps.get(0)[0];
			
			ArrayList<String[]> currentTestCaseSteps = new ArrayList<String[]>();
			
			for (String[] steps : moduleSteps) {

				if(!(currentTestCaseName.equalsIgnoreCase(steps[0]))) {
					
					temp += testCaseDetail(currentTestCaseSteps, moduleName, currentTestCaseName);
					
					// Empty array list and write again
					currentTestCaseName = steps[0];
					currentTestCaseSteps = new ArrayList<String[]>();
					currentTestCaseSteps.add(steps);
				} else {
					currentTestCaseSteps.add(steps);
				}
			}
			
			temp += testCaseDetail(currentTestCaseSteps, moduleName, currentTestCaseName);
        	temp 	+=	"		</div>\n"
        			+ 	"	</div>\n";
		}
		temp += "</div>\n";
		
		return temp;
	}
	
	/**
	 * This method writes the error modal element used to display error screenshots.
	 */
	private static String modal() {
		return 		"<!-- Error modal -->\n"
				+	"<div class='modal fade' id='error-modal' tabindex='-1' role='dialog' aria-labelledby='error-modal-label' aria-hidden='true' style='display: none;'>\n"
				+	"	<div class='modal-dialog'>\n"
				+ 	"		<div class='modal-content'>\n"
				+ 	"			<div class='modal-header'>\n"
				+ 	"				<button type='button' class='close' data-dismiss='modal' aria-hidden='true'>X</button>\n"
				+ 	"				<h4 class='modal-title text-center' id='myModalLabel'><strong>Error Screen Shot</strong></h4>\n"
				+ 	"			</div>\n"
				+ 	"			<div class='modal-body'>\n"
				+ 	"				<div class='text-center'>\n"
				+   "					<h4 id='fail-error-msg'></h4>"
				+ 	"					<img id='modal-image' src='' class='img-responsive'>\n"
				+	"				</div>\n"
				+ 	"			</div>\n"
				+ 	"			<div class='modal-footer'>\n"
				+ 	"				<button type='button' class='btn btn-default' data-dismiss='modal'>Close</button>\n"
				+ 	"			</div>\n"
				+ 	"		</div>\n"
				+ 	"	</div>\n"
				+ 	"</div>\n";
	}
	
	
	/**
	 * This method writes the footer content.
	 */
	private static String footer() {
		return 		"<div id='footer'>\n"
				+ 	"	<div class='text-center'>\n"
				+ 	"		<hr>\n"
				+ 	"		<span>Generated using <strong>SAF - v0.0.1</strong></span>\n"
				+ 	"		<span>&copy; 2014 VB</span>\n"
				+ 	"	</div>\n"
				+ 	"</div>\n";
	}
	
	
	/**
	 * This method uses all the other method to fill the report.
	 * @param results The results HashMap contains data to be provided to other functions.
	 * @throws ParseException 
	 */
	private static void fillReport(HashMap<?,?> results) throws ParseException {
		
		reportContent += "<html>\n"
				+ header()
				+ "<body>\n"
				+ "<div class='container'>\n"
				+ pageHeader()
				+ navTabs()
				+ "<br>\n<div class='tab-content'>\n"
				+ "<!-- Summary Tab -->\n"
				+ summaryTab((String[])results.get("summary"))
				+ "\n<!-- Module Tab -->\n"
				+ moduleTab((HashMap<?,?>)results.get("modules"))
				+ "\n<!-- Details Tab -->\n"
				+ detailTab((HashMap<?,?>)results.get("details"))
				+ "</div>\n"
				+ modal()
				+ footer()
				+ "</div>\n"
				+ "</body>\n"
				+ "</html>\n";		
	}
	
	/**
	 * This method creates and copies report resources for a report.
	 * @param dirName Target directory to which resources will be copied.
	 * @throws IOException
	 */
	private static void createReportResources(String dirName) throws IOException {
		String currentDir = System.getProperty("user.dir");
		
		File srcDir = new File(currentDir + "/resources/report");
		File trgDir = new File(dirName + "/resources");

		FileUtils.copyDirectory(srcDir, trgDir);
	}
	
	
	private static void copyErrorScreenshots(String dirName) throws IOException {
		// Get Present Working Directory
		String errDirName = System.getProperty("user.dir") + "/temp";
		File srcDir = new File(errDirName);
		if (srcDir.exists()) {
			FileUtils.copyDirectory(srcDir, new File(dirName));
			FileUtils.deleteDirectory(srcDir);
		}
	}
	
	/**
	 * This method creates the report, writes the class variable 'reportContent' to the created report file.
	 * @throws IOException
	 */
	public static void createReport() throws IOException {
		
		// Get Present Working Directory
		String currentDir = System.getProperty("user.dir");
		
		// Create reports directory if it does not exist
		Utils.makeDir(currentDir + "/reports");
		
		// Create individual report directory and copy resources(css,js) to that folder
		Utils.makeDir(currentDir + "/reports/" + filename);
		String reportFolder = currentDir + "/reports/" + filename;
		
		createReportResources(reportFolder);
		
		// Copy error screenshots
		copyErrorScreenshots(currentDir + "/reports/" + filename);
		
		// Create report.html in the folder, print contents of test execution and close file
		PrintWriter writer = new PrintWriter(currentDir + "/reports/" + filename + "/report.html", "UTF-8");
		writer.println(reportContent);
		writer.close();
	}
	
	/**
	 * This method takes in the necessary HashMap to generate the sections of report.
	 * @param testExecutionResult A HashMap with (summary, modules, details) as Key and (Summary details, Module Details, Test Details) as Values.
	 * @throws IOException
	 * @throws ParseException 
	 */
	public static void generate(HashMap<String, ?> testExecutionResult) throws IOException, ParseException {
		setFilename();
		fillReport(testExecutionResult);
		createReport();
		
		Logger.separator();
		System.out.println("Report Generated : " + filename + "/report.html");
		Logger.separator();
	}
	
	// Getter and Setter functions
	public static String getReport() {
		return reportContent;
	}
	public static String getFilename() {
		return filename;
	}
	private static void setFilename() {
		DateFormat fileFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		HTMLReporter.filename = fileFormat.format(date).toString();
	}
}
