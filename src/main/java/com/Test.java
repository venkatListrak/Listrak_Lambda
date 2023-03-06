package com;

import java.util.HashMap;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.jobs.NgListrakOrderExport;

public class Test {

	public static void main(String[] args) {

		NgListrakOrderExport test = new NgListrakOrderExport();
		APIGatewayProxyRequestEvent roxyRequestEvent = new APIGatewayProxyRequestEvent();
		HashMap<String, String> map = new HashMap<String, String>();
		roxyRequestEvent.setHeaders(map);
		test.handleRequest(roxyRequestEvent, null);

	}

}
