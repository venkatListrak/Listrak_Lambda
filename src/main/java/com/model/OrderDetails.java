package com.model;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
@Getter
@Setter
@Data
public class OrderDetails {
	@CsvBindByPosition(position = 0)
	private String email;
	@CsvBindByPosition(position = 1)
	private Integer orderNumber;
	@CsvBindByPosition(position = 2)
	private String dateEntered;
	@CsvBindByPosition(position = 3)
	private Double orderTotal;
	@CsvBindByPosition(position = 4)
	private Double itemTotal;
	@CsvBindByPosition(position = 5)
	private Double taxTotal;
	@CsvBindByPosition(position = 6)
	private Double shippingTotal;
	@CsvBindByPosition(position = 7)
	private Double handlingTotal;
	@CsvBindByPosition(position = 8)
	private Integer status;
	@CsvBindByPosition(position = 9)
	private String shipDate;
	@CsvBindByPosition(position = 10)
	private String trackingNumber;
	@CsvBindByPosition(position = 11)
	private String shippingMethod;
	@CsvBindByPosition(position = 12)
	private String couponCode;
	@CsvBindByPosition(position = 13)
	private Double discounttotal;
	@CsvBindByPosition(position = 14)
	private String source;
	@CsvBindByPosition(position = 15)
	private DateTime updateDate;
}

