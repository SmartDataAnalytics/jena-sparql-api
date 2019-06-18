package org.aksw.jena_sparql_api.sparql.ext.csv;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.sparql.ext.json.RDFDatatypeJson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;


/**
 * CSV parsing function which yields a json object.
 * - head: Array
 * - rows: Array of objects
 * 
 * @author raven Oct 30, 2017
 *
 */
public class E_CsvParse
	extends FunctionBase2
{	
	private static final Logger logger = LoggerFactory.getLogger(E_CsvParse.class);

	
	public static Stream<JsonElement> parseCsv(Reader reader, String optionStr) throws IOException {
		// Parse options
		String[] args;
		try {
			args = CommandLineUtils.translateCommandline(optionStr);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		OptionParser optionParser = new OptionParser();

		OptionSpec<?> rowAsObjectOs = optionParser
                .acceptsAll(Arrays.asList("o"), "Output rows as objects")
                ;

		OptionSpec<?> firstRowAsHeadersOs = optionParser
                .acceptsAll(Arrays.asList("h"), "First row as headers")
                ;
		
		CsvFormatParser csvFormatParser = new CsvFormatParser(optionParser);

        OptionSet options = optionParser.parse(args);

		CSVFormat csvFormat = csvFormatParser.parse(options, CSVFormat.EXCEL);
		

		boolean firstRowAsHeaders = options.has(firstRowAsHeadersOs);
		boolean rowAsObject = options.has(rowAsObjectOs) || firstRowAsHeaders;

		
		Stream<JsonElement> result = parseCsv(reader, csvFormat, rowAsObject, firstRowAsHeaders);
		return result;
	}
	
	public static JsonArray csvRecordToJsonArray(CSVRecord row) {
		JsonArray result = new JsonArray();
		
		for(int i = 0; i < row.size(); ++i) {
			String value = row.get(i);

			result.add(value);
		}
		return result;
	}
	
	public static JsonObject csvRecordToJsonObject(CSVRecord row, String[] labels) {
		JsonObject obj = new JsonObject();
		
		for(int i = 0; i < row.size(); ++i) {
			String value = row.get(i);
			
			String label = labels != null && i < labels.length ? labels[i] : null;
			label = label == null ? "" + "col" + i : label;
			
			obj.addProperty(label, value);			
		}

		return obj;
	}
	
	
	public static Stream<JsonElement> parseCsv(
			Reader reader,
			CSVFormat csvFormat,
			boolean rowAsObject,
			boolean firstRowAsLabels) throws IOException {
//		CSVParserBuilder csvParserBuilder = new CSVParserBuilder();
//		ICSVParser csvParser = csvParserBuilder
//				.build();
//
//		
//		CSVReaderBuilder csvReaderBuilder = new CSVReaderBuilder(reader);
//		csvReaderBuilder.withCSVParser(csvParser);
//		CSVReader csvReader = csvReaderBuilder.build();

		CSVParser csvParser;
		try {
			csvParser = new CSVParser(reader, csvFormat);
		} catch (IOException e) {
			reader.close();
			//IOUtils.closeQuietly(reader);
			throw new RuntimeException(e);
		}

		//csvReader
		Iterator<CSVRecord> it = csvParser.iterator();
		
		
		//boolean firstRowAsLabels = true;
		
		//List<String> headers = new ArrayList<>();
		String[] tmp = null;
		if(firstRowAsLabels && it.hasNext()) {
			CSVRecord r = it.next();
			tmp = Iterators.toArray(r.iterator(), String.class);
		}
		
		String[] labels = tmp;
		

		Function<? super CSVRecord, ? extends JsonElement> rowJsonEncoder = rowAsObject
				? r -> csvRecordToJsonObject(r, labels)
				: E_CsvParse::csvRecordToJsonArray;

		Stream<JsonElement> rowObjStream = Streams.stream(it)
			// Obtain the list of cells of the csvRecord
			.map(x -> (JsonElement)rowJsonEncoder.apply(x))
			.onClose(() -> {
				try { csvParser.close(); } catch(Exception e) { throw new RuntimeException(e); }
			});

		return rowObjStream;
	}
	
	
	@Override
	public NodeValue exec(NodeValue v1, NodeValue v2) {
		String csvStr = v1.isString() ? v1.getString() : null;
		String optionStr = v2.isBlank() ? "" : v2.isString() ? v2.getString() : null;

		// Parse options
//		String[] args;
//		try {
//			args = CommandLineUtils.translateCommandline(optionStr);
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}
		
//		CsvFormatParser csvFormatParser = new CsvFormatParser();
//		CSVFormat csvFormat = csvFormatParser.parse(args, CSVFormat.EXCEL);
		
		Stream<JsonElement> rowObjStream;
		try {
			rowObjStream = parseCsv(new StringReader(csvStr), optionStr);
		} catch (IOException e) {
			logger.warn("Failed to parse csv input", e);
			rowObjStream = Collections.<JsonElement>emptySet().stream();
		}
		
		JsonArray arr = new JsonArray();
		rowObjStream.forEach(arr::add);

		
//		RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName(RDFDatatypeJson.IRI);
		Node node = RDFDatatypeJson.jsonToNode(arr);
		NodeValue result = NodeValue.makeNode(node);

		return result;
	}
}
