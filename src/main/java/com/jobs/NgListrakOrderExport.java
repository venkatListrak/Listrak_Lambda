package com.jobs;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

import com.config.ApplicationConfig;
import com.model.OrderDetails;
import com.util.SFTPClient;
import com.opencsv.CSVWriter;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.util.DBUtils;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import static com.common.FTConstants.LISTRAK_ORDER_CSV_HEADER;


public class NgListrakOrderExport implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final String STAGE = System.getenv("STAGE") != null ? System.getenv("STAGE") : "DEV";
    public static final String DAYS = System.getenv("DAYS") != null ? System.getenv("DAYS") : "1";
    public static final String processed = System.getenv("processed") != null ? System.getenv("processed") : "false";
    private static final Logger logger = LoggerFactory.getLogger(NgListrakOrderExport.class);
    public static String SFTP_LOCATION = System.getenv("SFTP_LOCATION") != null ? System.getenv("SFTP_LOCATION")
            : "/home/ignitiv/FT/iws-aws-migration/Listrak/ListrakLambda/AWS_Cron";
    public static String POSTGRES_DB_USER;
    public static String POSTGRES_DB_PWD;
    public static String POSTGRES_DB_URL;
    public static String SFTP_HOST;
    public static String SFTP_USERNAME;
    public static String SFTP_PASSWORD;
    public static String SFTP_PORT;

    public NgListrakOrderExport () {
        this.initializeConfiguration();
    }

    private void initializeConfiguration () {
        String configFile = this.getClass().getClassLoader().getResource("env-config.json").getFile();
        JSONObject jsonobject = ApplicationConfig.getEnvJsonObject(configFile, STAGE);

        POSTGRES_DB_USER = jsonobject.getString("postgresDbUser");
        POSTGRES_DB_PWD = jsonobject.getString("postgresDbPwd");
        POSTGRES_DB_URL = jsonobject.getString("postgresDbUrl");
        SFTP_HOST = jsonobject.getString("sftpHost");
        SFTP_USERNAME = jsonobject.getString("sftpUserName");
        SFTP_PASSWORD = jsonobject.getString("sftpPassword");
        SFTP_PORT = jsonobject.getString("sftpport");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest (APIGatewayProxyRequestEvent httpRequest, Context context) {
        // TODO Auto-generated method stub
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("EST"));
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        logger.info("process started at : " + new DateTime());
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Connection con = DBUtils.getConnection(POSTGRES_DB_URL, POSTGRES_DB_USER, POSTGRES_DB_PWD);
            String query = DBUtils.createSelectQuery("ft_order_details", "order_number");
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(query);
            String filePath = "Listrak_Order_Feed.csv";
            PrintWriter pw = new PrintWriter(filePath);
            pw.println(LISTRAK_ORDER_CSV_HEADER);
            Map<Integer, OrderDetails> oMap = convertToList(rs);
            if (!oMap.isEmpty() && oMap != null) {
                boolean wSuccess = writeCsv(oMap, pw);
                pw.flush();
                pw.close();
                List<Integer> oNumbers = new ArrayList<>(oMap.keySet());
                if (wSuccess) {
//                    DBUtils.updateShipmentWithBatchDate(con, "ft_order_details", oNumbers);
                }
            } else {
                if (rs.isClosed()) {
                    logger.debug("result set is closed");
                    response.setBody("Invalid data");
                } else {
                    logger.info("No order records found for date : " + Calendar.getInstance().getTime());
                    response.setBody("No data to generate report");
                }
            }
            uploadFileToSFTP(filePath);
            logger.info("Listrak Order Data Feed process succeeded: : " + new DateTime());
            stopWatch.stop();
            logger.info("Time taken for NgListrackCustomerProxy process in HH:MM:SS.SSS  =  " + DurationFormatUtils.formatDurationHMS(stopWatch.getTime()));
            return response;
        } catch (Exception e) {
            logger.info("Listrak Order Data Feed process failed:  " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                logger.info("Failed to close SQL Connection:" + e.getMessage());
            }
        }
    }


    public Map<Integer, OrderDetails> convertToList (ResultSet result) {
        // TODO Auto-generated method stub
        Map<Integer, OrderDetails> oMap = new HashMap<>();
        try {
            if (result != null) {
                while (result.next()) {
                    OrderDetails entryModel = new OrderDetails();
                    entryModel.setEmail(result.getString("email"));
                    entryModel.setOrderNumber(result.getInt("order_number"));
                    entryModel.setDateEntered(
                            result.getString("date_entered") != null ? result.getString("date_entered") : "");
                    entryModel.setOrderTotal(result.getDouble("order_total"));
                    entryModel.setItemTotal(result.getDouble("item_total"));
                    entryModel.setTaxTotal(result.getDouble("tax_total"));
                    entryModel.setShippingTotal(result.getDouble("shipping_total"));
                    entryModel.setHandlingTotal(result.getDouble("handling_total"));
                    entryModel.setStatus(result.getInt("status"));
                    entryModel.setShipDate(result.getString("ship_date"));
                    entryModel.setTrackingNumber(
                            result.getString("tracking_number") != null ? result.getString("tracking_number") : "");
                    entryModel.setShippingMethod(
                            result.getString("shipping_method") != null ? result.getString("shipping_method") : "");
                    entryModel
                            .setCouponCode(result.getString("coupon_code") != null ? result.getString("coupon_code") : "");
                    entryModel.setDiscounttotal(result.getDouble("discount_total"));
                    entryModel.setSource(result.getString("source") != null ? result.getString("source") : "");
                    if (!oMap.containsKey((result.getInt("order_number")))) {
                        oMap.put(result.getInt("order_number"), entryModel);
                    }
                }
            }
        } catch (Exception e) {
            logger.info("Failed in convertToList method: " + e.getMessage());
        }
        return oMap;
    }


    private boolean writeCsv (Map<Integer, OrderDetails> oMap, PrintWriter pw) {
        try {
            ColumnPositionMappingStrategy<OrderDetails> mappingStrategy = new ColumnPositionMappingStrategy<>();
            mappingStrategy.setType(OrderDetails.class);

            StatefulBeanToCsv<OrderDetails> builder = new StatefulBeanToCsvBuilder<OrderDetails>(pw)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                    .build();
            List<OrderDetails> orderDetailsList = new ArrayList<>(oMap.values());
            builder.write(orderDetailsList);
            return true;
        } catch (Exception e) {
            logger.info("Failed in writeCsv method:  while writing Order Data into CSV file" + e.getMessage());
        }
        return false;
    }


    private void uploadFileToSFTP (String csvFilePath) {
        SFTPClient sftpClient = new SFTPClient();
        try {
            sftpClient.connect();
            sftpClient.upload(csvFilePath, NgListrakOrderExport.SFTP_LOCATION);

        } catch (Exception e) {
            logger.info("Error While uploading csv to sftp location " + e);
        } finally {
            sftpClient.disconnect();
        }
        logger.info("Report file uploaded to the sftp location");
    }

}
