/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nsu.ui;

import java.sql.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Dave Syer
 */
public class BDRespository implements StatisticsRepository {

	private static Connection con = null;
	private static Statement st = null;

	public static final String url = "jdbc:mysql://localhost:3306/weightdetector?useUnicode=true&characterEncoding=utf8";
	public static final String user = "root";
	public static final String pwd = "";

	public void startConnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, user, pwd);
			st = con.createStatement();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void closeAll() throws SQLException {
		con.close();
		st.close();
	}

	@Override
	public ConcurrentMap<Long, Statistics> findAll() throws SQLException {
		ConcurrentMap<Long, Statistics> stat = new ConcurrentHashMap<Long, Statistics>();
		try {
			startConnection();
			ResultSet rs = st.executeQuery("select * from statistics;");

			while (rs.next()) {
				Statistics statistics = new Statistics();
				statistics.setId(rs.getLong(1));
				statistics.setWeight(rs.getString(3));
				statistics.setDate(rs.getString(2));
				statistics.setDelta(rs.getString(4));

				stat.putIfAbsent(rs.getLong(1), statistics);
			}
		}finally {
			closeAll();
		}
		return stat;
	}

	@Override
	public Statistics save(Statistics stat) throws SQLException {
		Long id = null;
		try {
			startConnection();

			String queryStudent = "insert into statistics (date, weight, delta)" +
					" values ('" + stat.getDate() + "', '" + stat.getWeight() + "', '" +
					stat.getDelta() + "');";

			st.executeUpdate(queryStudent);
			ResultSet potencial_id = st.executeQuery("select id from statistics where weight = '" + stat.getWeight() + "';");
			while (potencial_id.next()) {
				id = potencial_id.getLong(1);
			}

		}finally {
			closeAll();
		}
		stat.setId(id);

		return stat;
	}

	@Override
	public Statistics findStatistics(Long id) {
		return null;
	}

}
