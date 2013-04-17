package com.brightcove.opensource.workflowengine.actors;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.brightcove.opensource.workflowengine.Actor;
import com.brightcove.opensource.workflowengine.Workflow;
import com.brightcove.opensource.workflowengine.record.Property;
import com.brightcove.opensource.workflowengine.record.Record;

/**
 * <p>
 *    A CSV (Comma Separated Value) input adapter.
 * </p>
 * 
 * <p>
 *    This actor will read a CSV file from disk, and convert each line into a
 *    Record for upstream consumers.  While labelled CSV, this should be able
 *    to parse any character-delimited file if properly escaped.
 * </p>
 * 
 * <p>
 *    The following configuration properties will be honored:<ul>
 *        <li>input-file - Location of CSV file to read in</li>
 *        <li>delimiter - Delimiter separating columns (defaults to ,)</li>
 *        <li>quote - Quote character defining cells (defaults to ')</li>
 *        <li>header-row - Header row to use to define column names</li>
 *        <li>has-header-row - If true (and header-row is not specified),
 *            column names will be pulled from first line of file.  If
 *            header-row is also specified, header-row will be used for column
 *            names, but the first row of the file will be skipped.</li>
 *    </ul>
 * </p>
 *    
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public class CSVInputAdapter extends Actor {
	
	/**
	 * <p>
	 *    Default constructor - workflow this actor is part of must be
	 *    provided
	 * </p>
	 * 
	 * @param workflow Workflow this actor will be part of
	 */
	public CSVInputAdapter(Workflow workflow){
		super(workflow);
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Actor#run()
	 */
	public void run(){
		Property<String> inputFileProp = getFirstProperty("input-file");
		if((inputFileProp == null) || (inputFileProp.getValue() == null)){
			die("CSVAdapter requires an input-file property.");
		}
		File inputFile = new File(inputFileProp.getValue());
		if(! inputFile.exists()){
			die("[ERR] CSVAdapter input file (" + inputFile.getAbsolutePath() + ") does not exist.");
		}
		
		Character delimiter = ',';
		Character quote     = '"';
		
		String delimiterProp = getFirstPropertyValue("delimiter");
		if(delimiterProp != null){
			delimiter = delimiterProp.charAt(0);
		}
		
		String quoteProp = getFirstPropertyValue("quote");
		if(quoteProp != null){
			quote = quoteProp.charAt(0);
		}
		
		CSVRecord headerRow     = null;
		String    headerRowProp = getFirstPropertyValue("header-row");
		if(headerRowProp != null){
			Reader              reader = new StringReader(headerRowProp);;
			Iterable<CSVRecord> parser = null;
			
			try {
				parser = CSVFormat.newBuilder().withDelimiter(delimiter).withQuoteChar(quote).parse(reader);
			}
			catch (Exception e) {
				die("[ERR] CSVAdapter can't parse header row (" + headerRowProp + ").  Exception caught: '" + e + "'.");
			}
			
			for(CSVRecord csvRecord : parser){
				if(headerRow == null){
					headerRow = csvRecord;
				}
			}
		}
		
		Boolean hasHeaderRow     = false;
		String  hasHeaderRowProp = getFirstPropertyValue("has-header-row");
		if("true".equalsIgnoreCase(hasHeaderRowProp)){
			hasHeaderRow = true;
		}
		
		if(! hasStarted()){
			FileReader          reader = null;
			Iterable<CSVRecord> parser = null;
			try {
				reader = new FileReader(inputFile);
				parser = CSVFormat.newBuilder().withDelimiter(delimiter).withQuoteChar(quote).parse(reader);
			}
			catch (Exception e) {
				die("[ERR] CSVAdapter can't parse input file (" + inputFile.getAbsolutePath() + ").  Exception caught: '" + e + "'.");
			}
			
			Integer rowIdx = 0;
			for (CSVRecord csvRecord : parser) {
				rowIdx++;
				
				if(hasHeaderRow && (rowIdx == 1)){
					if(headerRow == null){
						// Only use header row from file if it wasn't supplied by property
						headerRow = csvRecord;
					}
				}
				else{
					Record record = new Record();
					for(Integer idx=0; idx<csvRecord.size(); idx++){
						String propName = "Column-"+idx;
						if((headerRow != null) && (idx < headerRow.size())){
							propName = headerRow.get(idx);
						}
						
						Property<?> prop = new Property<String>(propName, csvRecord.get(idx));
						record.addProperty(prop);
					}
					
					handleRecord(record);
				}
			}
		}
	}
}
