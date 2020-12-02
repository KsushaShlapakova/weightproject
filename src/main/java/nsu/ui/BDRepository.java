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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class BDRepository implements StatisticsRepository {

	private static Connection con = null;
	private static Statement st = null;

	public static final String url = "jdbc:mysql://localhost:3306/weightdetector?useUnicode=true&characterEncoding=utf8";
	public static final String user = "root";
	public static final String pwd = "password";

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
	public List<Statistics> findAll() throws SQLException {
		List<Statistics> stat;
		try {
			startConnection();
			ResultSet rs = st.executeQuery("select * from statistics;");
			TreeMap<LocalDate, Statistics> sorted = new TreeMap<>();
			DateTimeFormatter formatter
					= DateTimeFormatter.ofPattern("yyyy-dd-MM");
			while (rs.next()) {
				Statistics statistics = new Statistics();
				statistics.setId(rs.getLong(1));
				statistics.setWeight(Float.toString(rs.getFloat(3)));
				statistics.setDate(rs.getString(2));
				sorted.put(LocalDate.parse(statistics.getDate(), formatter), statistics);
			}
			stat = new ArrayList<>(sorted.values());
			stat.get(0).setDelta("0.0");
			for(int i = 1; i < stat.size(); i++){
				Statistics temp = stat.get(i);
				float delta = Float.parseFloat(temp.getWeight()) - Float.parseFloat(stat.get(i-1).getWeight());
				String stringDelta =  (delta > 0) ? "+" + delta : Float.toString(delta);
				temp.setDelta(stringDelta);
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
