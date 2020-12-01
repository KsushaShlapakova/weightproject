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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * @author Dave Syer
 */
public class BDRespository implements StatisticsRepository {

	private static Connection con = null;
	private static Statement st = null;

	public static final String url = "jdbc:mysql://localhost:3306/weightdetector?useSSL=false";
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
	public ArrayList<Statistics> findAll() throws SQLException {
		ArrayList<Statistics> stat = new ArrayList<Statistics>();
		// все, что закомменчено - альтернативное решение, если вдруг понадобится
//		ArrayList<Date> dateSorted = new ArrayList<Date>();
//		ArrayList<Statistics> sortedStat = new ArrayList<Statistics>();
//		Double delta = null;

		try{
			startConnection();
			ResultSet rs = st.executeQuery("select * from statistics;");

			while (rs.next()) {
				Statistics statistics = new Statistics();
				statistics.setId(rs.getLong(1));
				statistics.setWeight(rs.getString(3));
				statistics.setDate(rs.getString(2));

				stat.add(statistics);
			}

//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//			for (Statistics statistics : stat) {
//				dateSorted.add(dateFormat.parse(statistics.getDate()));
//			}
//
//			Collections.sort(dateSorted);
//
//			for (Date date: dateSorted) {
//				for (Statistics statistics: stat) {
//					if (dateFormat.parse(statistics.getDate()).equals(date)) {
//						sortedStat.add(statistics);
//					}
//				}
//			}
//
//			for (int i=0; i<sortedStat.size(); i++) {
//				if (i==0) {
//					sortedStat.get(0).setDelta("0,0 кг");
//				}
//				else {
//					delta = Double.parseDouble(sortedStat.get(i).getWeight())-Double.parseDouble(sortedStat.get(i-1).getWeight());
//					sortedStat.get(i).setDelta(String.format("%.1f", delta) + " кг");
//				}
//			}


		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAll();
		}

//		return sortedStat;
		// возвращает неотсортированный список по дате, у всех дельта = null
		return stat;
	}


	@Override
	public Statistics save(Statistics stat) throws SQLException {
		Long id = null;
		try {
			startConnection();

			String queryStudent = "insert into statistics (date, weight)" +
					" values ('" + stat.getDate() + "', '" + stat.getWeight() + "');";

			st.executeUpdate(queryStudent);
			ResultSet potentialId = st.executeQuery("select id from statistics where weight = '" + stat.getWeight() + "'and date= '" + stat.getDate() + "';");
			while (potentialId.next()) {
				id = potentialId.getLong(1);
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
