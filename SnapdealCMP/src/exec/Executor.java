package exec;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.AttributeSet.CharacterAttribute;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import def.Logger;
import def.Selenium;
import def.Utils;

/**
 * Automatr
 * executor.Execute.java
 * Purpose: Contains methods to perform test execution.
 * 
 * @author SAURABH SHARMA
 * @version 0.0.10
 */
public class Executor {
	private static HashMap<String, String> testData;
	private static String[] config;
	@SuppressWarnings("rawtypes")
	private static HashMap allTestsHash;
	private static HashMap<String, String> runTimeHash = new HashMap<String, String>();
	private static WebDriver driver;
	private static String parentWindowHandle;
	private static String currentModule;
	private static String currentTest;
	private static HashMap<String, ArrayList<String[]>> testStatuses;
	private static HashMap<String, HashMap<String, ArrayList<String[]>>> status = new HashMap<String, HashMap<String, ArrayList<String[]>>>();
	
	/**
	 * This method finds a web element and dispatches a call for action on this element
	 * @param locatorType [String] {The locator type to identify the locating method}
	 * @param locatorValue [String] {The locator value to provide to locating method}
	 * @param stepDataValue [String] {The data value to be utilised by action}
	 * @param stepAction [String] {The type of action to be performed}
	 * @param miscParams [String[]] {Array of misc params to be utilised by the action method}
	 * @return actionResult [String[2]] {An array of result with status and err message/data value if any}
	 */
	private static String[] handleWebElementAction(String locatorType, String locatorValue, String stepDataValue, String stepAction, String[] miscParams) {
		
		// Check for Locator Inputs
		if( locatorType == "" ) {
			return new String[]{"F", "Cannot find web element without Locator Type!"};
		}
		else if( locatorValue == "" ) {
			return new String[]{"F", "Cannot find web element without Locator Value!"};
		}
		else {
			Object element = Selenium.find(driver, locatorType, locatorValue, null);
			
			// Element not found
			if (element == null) {
				return new String[] {"F", "Could not locate element with { &#39;" + locatorType + "&#39; = &#39;" + locatorValue + "&#39; }."};
			}
			// Element was found
			else if(element instanceof RemoteWebElement || element instanceof WebElement){
				// Perform action step
				return Selenium.elementActions(driver, (WebElement)element, stepAction, stepDataValue, miscParams);
			} else {
				return new String[] {"F", "There is a problem at this step. Could not identify the element type."};
			}
		}
		
	}
	
	/**
	 * This method executes a test step by using data values.
	 * @param testStep [ArrayList String ] - Should contain [Step Name, Locator Type, Locator Value, Action, Data Value]
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static String[] executeTestStep(Object testStep) {
		
		// Declare variables
		Object element;
		String stepName, stepAction, stepDataValue, stepLocatorType, stepLocatorValue;
		String[] stepArray, actionResult, stepResult, miscParams;
		int stepReduction;
		
		// Initialize step array
		stepArray = (String[]) ((ArrayList)testStep).toArray(new String[((ArrayList)testStep).size()]);
		
		// Assume that by default, every action is a success
		actionResult = new String[] {".", null};
		stepResult = new String[4];
		
		// Initialize Status
		stepResult[0] = stepArray[0];
		stepResult[2] = Utils.now("dd/MM/yyyy HH:mm:ss:S");
		
		// Check to see if step was to be skipped
		if(stepArray[0].startsWith("//")) {
			stepResult[1] = "SKIP";
			stepResult[3] = Utils.now("dd/MM/yyyy HH:mm:ss:S");
			System.out.print("S");
			return stepResult;
		}
		
		// ========================================================================================================================
		// INITIALIZE STEP ACTION VARIABLES
		// ========================================================================================================================
			
			stepName = stepArray[0];
			stepLocatorType = stepArray[1];
			stepLocatorValue = stepArray[2];
			stepAction = stepArray[3];
			
			// TODO: Check of variable lookup conflicts
			// Step Test Data
			if( stepArray[4] != "" ) {
				// Lookup in Runtime Hash
				if ( runTimeHash.get(stepArray[4]) != null ) stepDataValue = runTimeHash.get(stepArray[4]);
				// Lookup in Test Data
				else if ( testData.get(stepArray[4]) != null ) stepDataValue = testData.get(stepArray[4]);
				// Assign directly
				else stepDataValue = stepArray[4];
			} else {
				stepDataValue = "";
			}
			
			// Miscellaneous Parameters
			miscParams = null;
			if(stepArray.length - 5 > 0) {
				miscParams = new String[5];
				for (int i = 5, j = 0; i < stepArray.length; i++, j++) {
					miscParams[j] = stepArray[i];
				}
			}
			
			element = new Object();
			
			stepReduction = 0;
		
		// ========================================================================================================================
		// END OF INITIALIZE STEP ACTION VARIABLES
		// ========================================================================================================================
		
		// ========================================================================================================================
		// PERFORM ACTION
		// ========================================================================================================================
		switch(stepAction.toLowerCase()) {
			
			// ====================================================================================================================
			// UTILITY FUNCTIONS
			// ====================================================================================================================
		
			// Running another test within a test
			case "run":
				
				// Get contained test and its test steps
				HashMap containedTest = (HashMap)allTestsHash.get(stepLocatorType);
				ArrayList<String[]> containedTestSteps = (ArrayList<String[]>)((ArrayList<String[]>)(containedTest).get(stepLocatorValue)).clone();
				
				// Catcher for Parse Exception
				try {
					
					// Check if step reduction over-ride provided
					if(stepDataValue != "") {
						
						stepReduction = (Integer.parseInt(stepDataValue));
						
						// Check if steps to remove is greater than steps in the test
						if(Math.abs(stepReduction) > containedTestSteps.size()) {
							actionResult = new String[] {"F", "Number of steps to remove is greater than the steps in the test."};
						}
						// Removal index within range
						else {
							while (stepReduction != 0) {
								
								// Remove steps from the front
								if (stepReduction > 0) {
									containedTestSteps.remove(stepReduction - 1); 
									stepReduction--;
								}
								// Remove steps from the back
								else {
									containedTestSteps.remove(containedTestSteps.size() + stepReduction); 
									stepReduction++;
								}
							}
						}
					}
					
					executeTest(currentTest, containedTestSteps, false).get(currentTest);
					
					return null;
					
				} catch (Exception e) {
					actionResult = new String[] {"F", "Test Step Data Value is not a whole number."};
				}
				
				break;
				
			// Wait
			case "wait":
			try {
				Thread.sleep(Integer.parseInt(stepDataValue) * 100);
			} catch (NumberFormatException e) {
				actionResult = new String[] {"F", "Test Step Data Value is not a number."};
			} catch (InterruptedException e) {
				actionResult = new String[] {"F", "Unexpected error. 'Thread.sleep' failed! Please log an issue at http://vageeshb.github.io/Automatr/"};
			}
				break;
			
			// Print
			case "print":
				if( stepDataValue != "" )
					System.out.print(stepDataValue);
				else
					System.out.print("Please provide a valid input to print!");
				break;
				
			// ====================================================================================================================
			// END OF UTILITY FUNCS
			// ====================================================================================================================
			
			// ====================================================================================================================
			// ASSERTION/VERIFICATION RELATED FUNCS
			// ====================================================================================================================
		
			// Assertion
			case "assert":
			case "assertfalse":
				if(stepLocatorType == "")
					actionResult = new String[] {"F", "Unable to locate element without locator type. Please recheck your step."};
				else {
					if (stepLocatorType.equalsIgnoreCase("url"))
						actionResult = Selenium.stringActions(driver, stepLocatorType, stepAction, stepDataValue, miscParams);
					else {
						
						actionResult = handleWebElementAction(stepLocatorType, stepLocatorValue, stepDataValue, stepAction, miscParams);
					}
				}
				break;
				
			// Equality
			case "equal":
				
				if(stepLocatorValue != "" && stepDataValue != "") {
					// Lookups for Stored Values
					// Test Data Lookup
					if(testData.get(stepLocatorValue) != null) stepLocatorValue = testData.get(stepLocatorValue);
					
					// Run Time Lookup
					if(runTimeHash.get(stepLocatorValue) != null) stepLocatorValue = runTimeHash.get(stepLocatorValue);
					
					// Equality Check
					if(!stepLocatorValue.contains(stepDataValue) && !stepDataValue.contains(stepLocatorValue)) {
						actionResult = new String[]{"F", "Expected &#39;" + stepDataValue + "&#39;, Actual &#39;" + stepLocatorValue + "|."};
					}
				}
				// Handlers for incorrect equation
				else if (stepLocatorValue != "" && stepDataValue == "") {
					actionResult = new String[]{"F", "RHS value missing, cannot perform ( '" + stepLocatorValue + "' == null )."};
				} 
				else if(stepLocatorValue == "" && stepDataValue != "") {
					actionResult = new String[]{"F", "LHS value missing, cannot perform ( null == '" + stepDataValue + "' )"};
				}
				else {
					actionResult = new String[]{"F", "Both hand-side value missing, cannot perform equal assertion."};
				}
				break;
				
			// Inequality
			case "notequal":
				
				if (stepLocatorValue != "" && stepDataValue != "") {
					// Lookups for Stored Values
					// Test Data Lookup
					if(testData.get(stepLocatorValue) != null) stepLocatorValue = testData.get(stepLocatorValue);
					
					// Run Time Lookup
					if(runTimeHash.get(stepLocatorValue) != null) stepLocatorValue = runTimeHash.get(stepLocatorValue);
					
					// Inequality Check
					if(stepLocatorValue.contains(stepDataValue) || stepDataValue.contains(stepLocatorValue)) {
						actionResult = new String[]{"F", "Expected &#39;" + stepDataValue + "&#39;, Actual &#39;" + stepLocatorValue + "|."};
					}
				}
				// Handlers for incorrect equation
				else if (stepLocatorValue != "" && stepDataValue == "") {
					actionResult = new String[]{"F", "RHS value missing, cannot perform ( &#39;" + stepLocatorValue + "&#39; == null )."};
				} 
				else if (stepLocatorValue == "" && stepDataValue != "") {
					actionResult = new String[]{"F", "LHS value missing, cannot perform ( null == &#39;" + stepDataValue + "&#39; )"};
				}
				else {
					actionResult = new String[]{"F", "Both hand-side value missing, cannot perform equal assertion."};
				}
				break;
				
			// ========================================================================================================================
			// END OF ASSERTION/VERIFICATION RELATED FUNCS
			// ========================================================================================================================
		
			// ========================================================================================================================
			// DRIVER RELATED FUNCS
			// ========================================================================================================================
		
			// Close Driver
			case "close":
				driver.close();
				break;
			// Quit Driver
			case "quit":
				driver.quit();
				driver = null;
				break;
			// Open Driver and get URL
			case "open/get":
				driver = Selenium.initDriver(stepDataValue, config[1]);
				break;
			
			//Special case for navigate when you know certain part of url will be fixed only rest to be changed. 
			// This speciallu helpgul in case of multiple login accounts. 
			case "get/semiurl":
				String URL[] = driver.getCurrentUrl().split("com/");
				URL[0] = URL[0] + stepDataValue;
				driver.get(URL[0]);
				break;
				
			// Open Driver
			case "open":
				driver = Selenium.initDriver(null, config[1]);
				break;
			
			// Get URL
			case "get":
				driver.get(stepDataValue);
				break;
			
			// Alert handle
			case "acceptalert":
				actionResult = Selenium.miscActions(driver, stepAction, null, miscParams);
				break;
			
			// Execute JAVASCRIPT
			case "javascript":
				actionResult = Selenium.miscActions(driver, stepAction, stepDataValue, miscParams);
				break;
			
			// Switch to driver window
			case "switchto":
				actionResult = Selenium.miscActions(driver, stepAction, stepDataValue, miscParams);
				break;
				
			case "switchtoparent":
				driver.switchTo().window(parentWindowHandle);
				break;
			
			// Evaluate an expression
			case "evaluate":
				String expression = miscParams[0];
				String newExpression = "";
				expression = expression.replaceAll("\\s","");
				String[] t = expression.split("(?<=[-+*/()])|(?=[-+*/()])");
				for (int i = 0; i < t.length; i++) {
					String variable = t[i];
					if( runTimeHash.get(variable) != null ) {
						newExpression += runTimeHash.get(variable);
					}
					else if( testData.get(variable) != null ) {
						newExpression += testData.get(variable);
					}
					else {
						newExpression += variable;
					}

				}
				miscParams[0] = (!newExpression.equals("")) ? newExpression : miscParams[0];
				actionResult = Selenium.miscActions(driver, stepAction, stepDataValue, miscParams);
				if(actionResult[0].equals(".")) {
					if(runTimeHash.get(stepDataValue) != null) {
						runTimeHash.remove(stepDataValue);
					}
					//System.out.print(stepDataValue+" -- "+actionResult[1]);
					if((actionResult[1] instanceof String))
					{
						//System.out.print("he");
						if (!(actionResult[1].contains(".")))
						actionResult[1] = actionResult[1].concat(".00");
						//System.out.print("re"+actionResult[1]);
					}
					runTimeHash.put(stepDataValue, actionResult[1]);
					//System.out.print("we");
				}
				break;
					
			// ========================================================================================================================
			// END OF DRIVER RELATED FUNCS
			// ========================================================================================================================
					
			// ========================================================================================================================
			// WEB ELEMENT RELATED FUNCS
			// ========================================================================================================================
				
			// Element not displayed or not present
			case "isnotdisplayed":
			case "isnotpresent":
				actionResult = Selenium.isNotDisplayed(driver, stepLocatorType, stepLocatorValue);
				break;
				
			// Runtime data save
			case "save":
			case "saveselected":
				
				actionResult = handleWebElementAction(stepLocatorType, stepLocatorValue, stepDataValue, stepAction, miscParams);
				
				// If action was succesfull, store the value to a hash
				if(actionResult[0].equals(".")) {
					if(runTimeHash.get(stepDataValue) != null) {
						runTimeHash.remove(stepDataValue);
					}
					runTimeHash.put(stepDataValue, actionResult[1]);
				}
				break;
				
			case "savebyintcomma":
			
				//System.out.println("in here");
				actionResult = handleWebElementAction(stepLocatorType, stepLocatorValue, stepDataValue, stepAction, miscParams);
				
				// If action was succesfull, store the value to a hash
				if(actionResult[0].equals(".")) {
					if(runTimeHash.get(stepDataValue) != null) {
						runTimeHash.remove(stepDataValue);
					}
					runTimeHash.put(stepDataValue, actionResult[1]);
				}
				break;
			// will convert normal string like 1200.00 to 1,200.00	
			case "converttocomma":
				//System.out.print("comma");
				//System.out.println(stepDataValue);
				if (stepDataValue != null) {
					//System.out.println("---");
										
					 //DecimalFormat df = new DecimalFormat("#.00");
					if (Double.parseDouble(stepDataValue) > 999.00)
					{
						int len = stepDataValue.length();
						if (len > 6 && len <= 9)
						{
							String FinalString = stepDataValue.substring(0,stepDataValue.length()-6);
							FinalString = FinalString.concat(",");
							FinalString = FinalString.concat(stepDataValue.substring(stepDataValue.length()-6, stepDataValue.length()));
							//System.out.print(FinalString);
							actionResult = new String[] {".",null};
							runTimeHash.remove(stepArray[4]);
							runTimeHash.put(stepArray[4], FinalString);
						}
						
						if (len > 9 && len <= 12)
						{
							String FinalString = stepDataValue.substring(0,stepDataValue.length()-9);
							FinalString = FinalString.concat(",");
							FinalString = FinalString.concat(stepDataValue.substring(stepDataValue.length()-9, stepDataValue.length()-6));
							FinalString = FinalString.concat(",");
							FinalString = FinalString.concat(stepDataValue.substring(stepDataValue.length()-6, stepDataValue.length()));
							//System.out.print(FinalString);
							actionResult = new String[] {".",null};
							runTimeHash.remove(stepArray[4]);
							runTimeHash.put(stepArray[4], FinalString);
						}
						
						//System.out.println(FinalString);
						
					}
					
									
				}
				else
				{
					actionResult = new String[] {"F", "The value is null"};
				}
				
				
				break;
				
			case "converttopercent":
				if (stepDataValue != null) {
				//System.out.println("in");
					if (stepDataValue.contains("."))
					{
						//System.out.println("to");
						stepDataValue = stepDataValue.replace(".", "%");
						actionResult = new String[] {".",null};
						runTimeHash.remove(stepArray[4]);
						runTimeHash.put(stepArray[4], stepDataValue);
					}
					else
					{
						actionResult = new String[] {"F", "The value is null"};
					}
					
				}
				break;
				
			// will convert 1200.00 to 1.200,00	
			case "converttodot":
				//System.out.print("here");
				//System.out.println(stepDataValue);
				if (stepDataValue != null) {
					//System.out.println("---");
										
					
					if (Integer.parseInt(stepDataValue) > 999)
					{
						int len = stepDataValue.length();
						if (len > 6 && len <= 9)
						{
							String FinalString = stepDataValue.substring(0,stepDataValue.length()-6);
							FinalString = FinalString.concat(".");
							FinalString = FinalString.concat(stepDataValue.substring(stepDataValue.length()-6, stepDataValue.length()-3));
							FinalString = FinalString.concat(",");
							FinalString = FinalString.concat(stepDataValue.substring(stepDataValue.length()-2, stepDataValue.length()));
							
							
							actionResult = new String[] {".",null};
							runTimeHash.remove(stepArray[4]);
							runTimeHash.put(stepArray[4], FinalString);
						}
						
						if (len > 9 && len <= 12)
						{
							String FinalString = stepDataValue.substring(0,stepDataValue.length()-9);
							FinalString = FinalString.concat(".");
							FinalString = FinalString.concat(stepDataValue.substring(stepDataValue.length()-9, stepDataValue.length()-6));
							FinalString = FinalString.concat(".");
							FinalString = FinalString.concat(stepDataValue.substring(stepDataValue.length()-6, stepDataValue.length()-3));
							FinalString = FinalString.concat(",");
							FinalString = FinalString.concat(stepDataValue.substring(stepDataValue.length()-2, stepDataValue.length()));
							
							
							actionResult = new String[] {".",null};
							runTimeHash.remove(stepArray[4]);
							runTimeHash.put(stepArray[4], FinalString);
						}
						
					}
					
									
				}
				else
				{
					actionResult = new String[] {"F", "The value is null"};
				}
				
				
				break;
			
			// Count number of elements
			case "count":	
				int localCounter = 0;
				
				while(true) {
					// Find web elements
					List<WebElement> elementList = Selenium.findElements(driver, stepLocatorType, stepLocatorValue);
					
					// Verify Element Count
					if(elementList != null && elementList.size() == Integer.parseInt(stepDataValue)) {
						break;
					}
					
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						actionResult = new String[] {"F", "Unexpected error. 'Thread.sleep' failed! "};
					}
					
					localCounter++;
					
					if(localCounter == 50) {
						if(elementList == null)
							actionResult = new String[] {"F", "Expected number of elements - &#39;" + stepDataValue + "&#39;, but found - 0."};
						else if (elementList.size() != Integer.parseInt(stepDataValue))
							actionResult = new String[] {"F","Expected number of elements - &#39;" + stepDataValue + "&#39;, but found - &#39;" + elementList.size() + "&#39;."};
						break;
					}
				}
				
				break;
			
			// Element attribute related
			case "getattribute":
				actionResult = handleWebElementAction(stepLocatorType, stepLocatorValue, stepDataValue, stepAction, miscParams);

				// If action was succesfull, store the value to a hash
				if(actionResult[0].equals(".")) {
					if(runTimeHash.get(stepDataValue) != null) {
						runTimeHash.remove(stepDataValue);
					}
					runTimeHash.put(stepDataValue, actionResult[1]);
				}
				break;
				
			case "assertattribute":
				actionResult = handleWebElementAction(stepLocatorType, stepLocatorValue, stepDataValue, stepAction, miscParams);
				break;
				
			// General purpose
				case "isdisplayed":
				case "ispresent":
				case "isempty":
				case "hover":
				case "send":
			// Link/Button related
				case "click":
				case "rightclick":
			// Multi-element
				case "draganddrop":
			// Input box related
				case "input":
				case "clear":
			// Select box related
				case "selectbyvalue":
				case "selectbyindex":
				case "selectbytext":
			// Check box related
				case "ischecked":
				case "isnotchecked":
			// Runtime Data Match
				case "match":
					actionResult = handleWebElementAction(stepLocatorType, stepLocatorValue, stepDataValue, stepAction, miscParams);
					break;
			// ========================================================================================================================
			// END OF WEB ELEMENT RELATED FUNCS
			// ========================================================================================================================
				
			default:
				actionResult = new String[] {"F", "Unknown Action Type - &#39;" + stepAction + "&#39; specified, please read the documentation for possible actions."};
				break;
		}
		
		
		// ========================================================================================================================
		// STEP RESULTS EXTRACTION
		// ========================================================================================================================
		if (actionResult[0].equalsIgnoreCase(".")) {
			System.out.print(".");
			stepResult[1] = "PASS";
		} 
		else {
			
			System.out.print(actionResult[0].toUpperCase());
			
			// Assign step status
			if(actionResult[0].equalsIgnoreCase("f"))
				stepResult[1] = "FAIL: " + actionResult[1];
			else
				stepResult[1] = "WARNING: " + actionResult[1];
			
			// Take screenshot			
			String tempFileName = Utils.uglify(currentModule + "_" + currentTest + "_" + stepName + "_error.png"); 
			
			// If element was present, highlight the element, else take complete screenshot
			if(element != null && element instanceof WebElement)
				Selenium.screenshot(driver, tempFileName, (WebElement)element, actionResult[0].toUpperCase());
			else
				Selenium.screenshot(driver, tempFileName, null, null);
		}
		// ========================================================================================================================
		// END OF STEP RESULTS EXTRACTION
		// ========================================================================================================================
		
		// Record end time of step execution
		stepResult[3] = Utils.now("dd/MM/yyyy HH:mm:ss:SS");
		
		return stepResult;
	}
	
	/**
	 * This method takes in a hash of tests and passes each test step to method 'executeTestStep'
	 * @param moduleName [String] - The name of the current module
	 * @param test [HashMap] - A hash containing Test Name as Key and Test Steps as Value.
	 * @returns ]HashMap] - Test Name as Key and Test Step Status Array as Values.
	 */
	@SuppressWarnings("rawtypes")
	private static HashMap<String, ArrayList<String[]>> executeTest(String testName, ArrayList test, boolean openDriver) {
		
		// Set Current Test
		currentTest = testName;
		
		// Initialize driver and run-time hash if called from module run
		if(openDriver == true) {
			runTimeHash = new HashMap<String, String>();
			driver = Selenium.initDriver(config[0], config[1]);
			parentWindowHandle = driver.getWindowHandle();
		}
		
		for(Object testStep: test) {
			
			String[] testStepResult = executeTestStep(testStep);
			
			// If self contained test was not run, add status
			if(testStepResult != null) {
				
				if(testStatuses.get(currentTest) == null) testStatuses.put(currentTest, new ArrayList<String[]>());

				testStatuses.get(currentTest).add(testStepResult);
			}
		}
			
		// Close driver if called from parent module
		if(openDriver == true) { 
			driver.close();
			driver.quit();
		}
		
		return testStatuses;
	}
	
	/**
	 * This method performs the whole execution of tests using the config, exection hash, test data and default steps.
	 * @param config [String[]] - The configuration settings.
	 * @param execManagerHash [HashMap] - The execution hash with Module Name as Key and Tests as Values.
	 * @param testsHash [HashMap] - All the tests present in the data file.
	 * @param testData [HashMap] - The test data to be used.
	 * @return HashMap with Module Name as Key and Test Case Result HashMap as Values.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static HashMap<String, HashMap<String, ArrayList<String[]>>> performExecution(String[] config, HashMap execManagerHash, HashMap testsHash, HashMap testData) {
		
		// Run only the tests as defined in execution manager hash
		Set set = execManagerHash.entrySet();
		Iterator i = set.iterator();
		
		// Set class variables
		setConfig(config);
		setTestData(testData);
		setallTestsHash(testsHash);
		
		// Perform Execution Module-wise
		while(i.hasNext()) {
			

			Map.Entry me = (Map.Entry)i.next();
			
			Logger.separator();

			// Extract module name and tests to be run
			String moduleName = me.getKey().toString();
			ArrayList<String> tests = (ArrayList<String>)me.getValue();
			
			System.out.println("Executing Module: " + moduleName);
			
			currentModule = moduleName;
			
			// Initialize Test Statuses for this module
			testStatuses = new HashMap<String, ArrayList<String[]>>();
			
			for (String test : tests) {
				
				System.out.print("\n" + test + ": ");
				// Initialize before execution
				if(status.get(moduleName) == null) {
					HashMap<String, ArrayList<String[]>> tempTestHash = new HashMap<String, ArrayList<String[]>>();
					tempTestHash.put(test, new ArrayList<String[]>());
					status.put(moduleName, tempTestHash);
				} 
				
				// Get test steps of this test
				ArrayList testSteps = (ArrayList)((HashMap)testsHash.get(moduleName)).get(test);
				if(testSteps != null && testSteps.size() > 0)
					executeTest(test, testSteps, true);
				else
					System.out.println("No test steps found for this test case. Please re-check!! Skipping ahead.");
				
			}
			
			status.put(moduleName, testStatuses);
			
			Logger.separator();
			
		}
		
		return status;
	}
	
	// Getter and Setters
	public static HashMap<?,?> getTestData() {
		return testData;
	}
	private static void setTestData(HashMap<String, String> testDataList) {
		Executor.testData = testDataList;
	}

	public static String[] getConfig() {
		return config;
	}

	private static void setConfig(String[] config) {
		Executor.config = config;
	}

	@SuppressWarnings("rawtypes")
	public static HashMap getallTestsHash() {
		return allTestsHash;
	}

	@SuppressWarnings("rawtypes")
	private static void setallTestsHash(HashMap allTestsHash) {
		Executor.allTestsHash = allTestsHash;
	}

}
