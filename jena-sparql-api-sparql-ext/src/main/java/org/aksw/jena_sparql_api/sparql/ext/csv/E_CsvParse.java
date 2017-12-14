package org.aksw.jena_sparql_api.sparql.ext.csv;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.sparql.ext.json.RDFDatatypeJson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.codehaus.plexus.util.cli.CommandLineUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


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
	
	public static Stream<JsonObject> parseCsv(Reader reader, String optionStr) {
		// Parse options
		String[] args;
		try {
			args = CommandLineUtils.translateCommandline(optionStr);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		CsvFormatParser csvFormatParser = new CsvFormatParser();
		CSVFormat csvFormat = csvFormatParser.parse(args, CSVFormat.EXCEL);

		Stream<JsonObject> result = parseCsv(reader, csvFormat);
		return result;
	}
	
	public static Stream<JsonObject> parseCsv(Reader reader, CSVFormat csvFormat) {
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
			IOUtils.closeQuietly(reader);
			throw new RuntimeException(e);
		}

		//csvReader
		Iterator<CSVRecord> it = csvParser.iterator();
		
		
		boolean firstRowAsLabels = true;
		
		//List<String> headers = new ArrayList<>();
		String[] labels = null;
		if(firstRowAsLabels) {
			
		}
		
		

		Stream<JsonObject> rowObjStream = Streams.stream(it)
			.map(csvRecord -> Lists.newArrayList(csvRecord))
			.map(row -> {
				JsonObject obj = new JsonObject();
				
				for(int i = 0; i < row.size(); ++i) {
					String value = row.get(i);
					
					String label = labels != null && i < labels.length ? labels[i] : null;
					label = label == null ? "" + "col" + i : label;
	
					
					obj.addProperty(label, value);			
				}
				return obj;
			})
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
		String[] args;
		try {
			args = CommandLineUtils.translateCommandline(optionStr);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		CsvFormatParser csvFormatParser = new CsvFormatParser();
		CSVFormat csvFormat = csvFormatParser.parse(args, CSVFormat.EXCEL);
		
		Stream<JsonObject> rowObjStream = parseCsv(new StringReader(csvStr), csvFormat);
		
		JsonArray arr = new JsonArray();
		rowObjStream.forEach(arr::add);

		
//		RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName(RDFDatatypeJson.IRI);
		Node node = jsonToNode(arr);
		NodeValue result = NodeValue.makeNode(node);

		return result;
	}

	public static Node jsonToNode(JsonElement json) {
		RDFDatatype dtype = RDFDatatypeJson.INSTANCE;
		Node result = NodeFactory.createLiteralByValue(json, dtype);
		return result;
	}
}
