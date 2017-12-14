package org.aksw.jena_sparql_api.sparql.ext.csv;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;

import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class CsvFormatParser {

	public static final CSVFormat DEFAULT_CSV_FORMAT = CSVFormat.EXCEL;
	
	protected CSVFormat defaultCsvFormat;
	
	protected NonOptionArgumentSpec<String> baseCsvFormatOs;
	protected OptionSpec<Character> fieldDelimiterOs;
    protected OptionSpec<String> fieldSeparatorOs;
    protected OptionSpec<Character> escapeCharacterOs;

    protected OptionParser optionParser;
    
    
    protected Map<String, String> predefinedFormats;
    
    public CsvFormatParser() {
    	this(new OptionParser());
    }

    public CsvFormatParser(OptionParser optionParser) {
    	this.optionParser = optionParser;


    	this.defaultCsvFormat = DEFAULT_CSV_FORMAT;
    	
    	initOptionSpecs();
    }
    
    
    public OptionParser getOptionParser() {
        return optionParser;
    }


    public void initOptionSpecs() { //OptionParser optionParser) {    	
    	
    	predefinedFormats = Arrays.asList(CSVFormat.Predefined.values()).stream()
    			.map(Enum::name)
    			.collect(Collectors.toMap(
    				String::toLowerCase,
    				x -> x,
    				(u, v) -> { throw new IllegalStateException(); },
    				LinkedHashMap::new
    			));
    	
    	baseCsvFormatOs = optionParser
    		.nonOptions("Base predefined CSV format, one of " + predefinedFormats.values());
    	
    	fieldSeparatorOs = optionParser
                .acceptsAll(Arrays.asList("s", "separator"), "CSV field separator")
                .withRequiredArg()
                //.defaultsTo(defaultCsvFormat.getRecordSeparator())
                ;

    	fieldDelimiterOs = optionParser
                .acceptsAll(Arrays.asList("d", "delimiter"), "CSV field delimiter")
                .withRequiredArg()
                .withValuesConvertedBy(new ValueConverterCharacter())
                .defaultsTo(defaultCsvFormat.getDelimiter())
                ;

    	escapeCharacterOs = optionParser
                .acceptsAll(Arrays.asList("e", "escape"), "CSV field escape symbol")
                .withRequiredArg()
                .withValuesConvertedBy(new ValueConverterCharacter())
                //.defaultsTo(defaultCsvFormat.getEscapeCharacter())
                ;
    }


    public CSVFormat parse(String[] args, CSVFormat fallBackCsvFormat) {    	
        OptionSet options = optionParser.parse(args);

        CSVFormat csvFormat = null;
        if(options.has(baseCsvFormatOs)) {
        	String baseFormat = baseCsvFormatOs.value(options);
        	baseFormat = baseFormat == null ? "" : baseFormat.trim().toLowerCase();
        	
        	if(!baseFormat.isEmpty()) {
        		String formatId = predefinedFormats.get(baseFormat);
        		
        		if(formatId != null) {
	        		csvFormat = CSVFormat.valueOf(formatId);
        		} else {
	        		throw new RuntimeException("No CSV format known by name '" + formatId + "', available: " + predefinedFormats.values());
        		}
        		
//        		try {
//	        		csvFormat = CSVFormat.valueOf(formatId);
//        		} catch(Exception e) {	        	
//		        	if(csvFormat == null) {
//		        		throw new RuntimeException("No CSV format known by name '" + baseFormat + "', available: " + predefinedFormats.values());
//		        }
        	}
        }

        if(csvFormat == null) {        
        	csvFormat = fallBackCsvFormat;
        }
    
    	if(csvFormat == null) {
    		csvFormat = CSVFormat.EXCEL;
    	}
    	
    	if(csvFormat == null) {
    		csvFormat = defaultCsvFormat;
    	}

    	csvFormat = csvFormat
    		.withRecordSeparator(fieldSeparatorOs.value(options))
    		.withDelimiter(fieldDelimiterOs.value(options))
    		.withEscape(escapeCharacterOs.value(options))
        	;
        
        return csvFormat;
    }
}