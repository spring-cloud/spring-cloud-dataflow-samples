package io.spring.billrun;

import java.util.List;

import io.spring.billrun.model.Bill;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBatchTest
public class BillrunApplicationTests {

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	@Before
	public void setup()  {
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
	}

	@Test
	public void testJobResults() {
		testResult();
	}

	private void testResult() {
		List<Bill> billStatements = this.jdbcTemplate.query("select id, " +
						"first_name, last_name, minutes, data_usage, bill_amount " +
						"FROM bill_statements ORDER BY id",
				(rs, rowNum) -> new Bill(rs.getLong("id"),
						rs.getString("FIRST_NAME"), rs.getString("LAST_NAME"),
						rs.getLong("DATA_USAGE"), rs.getLong("MINUTES"),
						rs.getDouble("bill_amount")));
		assertEquals(5, billStatements.size());

		Bill billStatement = billStatements.get(0);
		assertEquals(6, billStatement.getBillAmount(), 1e-15);
		assertEquals("jane", billStatement.getFirstName());
		assertEquals("doe", billStatement.getLastName());
		assertEquals(new Long(1), billStatement.getId());
		assertEquals(new Long(500), billStatement.getMinutes());
		assertEquals(new Long(1000), billStatement.getDataUsage());
	}
}
