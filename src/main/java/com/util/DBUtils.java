/**
 *  ===========================================================================================================================
 *  Description : Class to handle database operations
 *  Created By  : Prabhat
 *
 *  VERSION     MODIFIED BY       (MM/DD/YY)      Description
 *  ============================================================================================================================
 *  1.0         Prabhat        16/06/22       To handle database operations
 *  ============================================================================================================================
 */
package com.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.jobs.NgListrakOrderExport;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBUtils {

	private static Logger logger;

	private static Connection con = null;
	public static ArrayList<Integer> orderId = new ArrayList<Integer>();

	static {
		logger = LoggerFactory.getLogger(DBUtils.class);
	}

	public static Connection getConnection(String url, String username, String password) {
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("url " + url + " , username " + username + " , password " + password);
			}
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage(), e);
			}
		} catch (SQLException e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage(), e);
			}
		}
		return con;
	}

	private static String converDateToString(DateTime date) throws ParseException {
		String dateString = date.toString();
		dateString = dateString.substring(0, dateString.indexOf("."));
		dateString = dateString.replace("T", " ");
		return dateString;
	}

	/**
	 *
	 * @param tableName
	 * @param dateField
	 * @return
	 */
	public static String createSelectQuery(String tableName, String dateField/* , String orderType */) {
		SimpleDateFormat fomatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -Integer.parseInt(NgListrakOrderExport.DAYS));
		String query = "SELECT * FROM " + tableName + " WHERE processed = "+Boolean.parseBoolean(NgListrakOrderExport.processed)+" ORDER BY " + dateField;
		PrintUtils.logger.info(query);
		if (logger.isDebugEnabled()) {
			logger.debug("Query is " + query);
		}
		return query;
	}

	/**
	 * @throws SQLException
	 */
	public static void updateShipmentWithBatchDate(Connection con, String tableName, List<Integer> orderNoList) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String s = "";
			for (Integer integer : orderNoList) {
				if (integer == orderNoList.get(orderNoList.size()-1)) {
					s = s + integer ;
				}else {
					s = s + integer + ",";
				}
			}
			String updateQuery = "Update " + tableName + " set processed = ? where order_number in ("+s+")";
			stmt = con.prepareStatement(updateQuery);
			stmt.setObject(1, Boolean.TRUE, java.sql.Types.BOOLEAN);
			con.setAutoCommit(true);
			stmt.executeUpdate();
			if (!con.getAutoCommit()) {
				con.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error updating statement details table. ");
		} finally {
			stmt.close();
		}

	}

}
