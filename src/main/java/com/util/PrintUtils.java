package com.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class PrintUtils {
	public static final Logger logger = LoggerFactory.getLogger(PrintUtils.class);
	static ObjectMapper mapper = (new ObjectMapper()).registerModule((Module) new JodaModule());

	public static void prettyPrintObject(Object o) {
		try {
			logger.info("++++++++++++++++++++" + o.getClass() + "++++++++++++++++++++++++++++++");
			logger.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o));
		} catch (JsonProcessingException e) {
			logger.error("ERROR PRINTING OBJECT : ", e);
		}
	}

	public static void printObject(Object o) {
		try {
			logger.info("++++++++++++++++++++" + o.getClass() + "++++++++++++++++++++++++++++++");
			logger.info(mapper.writeValueAsString(o));
		} catch (JsonProcessingException e) {
			PrintUtils.logger.error("Error in printObject ", e);
		}
	}

	public static ObjectMapper getMapper() {
		return mapper;
	}
}
