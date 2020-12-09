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
import java.util.TreeMap;

public class BDRepository implements StatisticsRepository {

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
	public ArrayList<Statistics> findAll(User user) throws SQLException {

		ArrayList<Statistics> stat = new ArrayList<Statistics>();
		TreeMap<LocalDate, Statistics> sorted = new TreeMap<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		try {
			startConnection();

			ResultSet rs = st.executeQuery("select * from statistics where user_id='" + user.getId()+"';");

			while (rs.next()) {
				Statistics statistics = new Statistics();
				statistics.setId(rs.getLong("id"));
				statistics.setWeight(Float.toString(rs.getFloat("weight")));
				statistics.setDate(rs.getString("date"));

				sorted.put(LocalDate.parse(statistics.getDate(), formatter), statistics);
			}

			stat = new ArrayList<>(sorted.values());
			if (stat.size() != 0) {
				stat.get(0).setDelta("0.0");
			}
			for(int i = 1; i < stat.size(); i++){
				Statistics temp = stat.get(i);
				float delta = Float.parseFloat(temp.getWeight()) - Float.parseFloat(stat.get(i-1).getWeight());
				String stringDelta =  (delta > 0) ? "+" + delta : Float.toString(delta);
				temp.setDelta(stringDelta.substring(0, stringDelta.indexOf(".") + 2));
			}

			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAll();
		}
		return stat;
	}


	@Override
	public Statistics save(Statistics stat, long user_id) throws SQLException {
		Long id = null;
		String dotWeight = null;
		try {
			startConnection();
			String respond = null;

			ResultSet rs = st.executeQuery("select id, date from statistics where date = '" + stat.getDate() + "';");

			while (rs.next()) {
				respond = rs.getString("date");
				id = rs.getLong("id");
			}

			if (respond == null) {
				dotWeight = stat.getWeight().replace(",", ".");
				String queryStudent = "insert into statistics (user_id, date, weight)" +
						" values (" + user_id + ", '" + stat.getDate() + "', '" + dotWeight + "');";

				st.executeUpdate(queryStudent);
				ResultSet potentialId = st.executeQuery("select id from statistics where weight = '" + stat.getWeight() + "'and date= '" + stat.getDate() + "' and user_id='"+user_id+"';");
				while (potentialId.next()) {
					id = potentialId.getLong("id");
				}

				potentialId.close();
			} else {

				String queryStudent = "update statistics set weight ='" + dotWeight + "' where date = '" + stat.getDate() + "' and user_id='"+user_id+"';";
				st.executeUpdate(queryStudent);

			}

			rs.close();

		}finally {
			closeAll();
		}
		stat.setId(id);

		return stat;
	}


	@Override
	public void delete(Long id) throws SQLException {
		try {
			startConnection();

			String query = "delete from statistics where id = '" + id + "';";
			st.executeUpdate(query);

		} finally {
			closeAll();
		}
	}

	@Override
	public boolean[] check_user(String email, String password) throws SQLException {
		boolean[] result = {false, false};
		try {
			startConnection();
			ResultSet rs = st.executeQuery("select * from users where email = '" + email + "';");
			while (rs.next()) {
				result[0] = true;
				String pass = rs.getString("password");
				if (pass.equals(password)) {
					result[1] = true;
				}
			}
			rs.close();
		}
		catch (Exception e) { e.printStackTrace(); }
		finally { closeAll(); }
		return result;
	}

	@Override
	public User create_user(User new_user) throws SQLException {
		System.out.println(new_user.getName());
		System.out.println(new_user.getId());
		try {
			startConnection();
			boolean userExistence = checkDB(new_user);
			if (!userExistence) {
				String queryUser = "insert into users (email, password, name, age, height)" +
						" values ('" + new_user.getEmail() + "', '" + new_user.getPassword() + "', '" +
						new_user.getName() + "', '" + new_user.getAge() + "', '" + new_user.getHeight() + "');";
				st.executeUpdate(queryUser);
//			String query_id = "select * from users where email='"+new_user.getEmail()+"';";
				String query_id = "select * from users where id = (select max(id) from users);";
				ResultSet rs = st.executeQuery(query_id);
				while (rs.next()) {
					new_user.setId(rs.getLong(1));
				}
			}else{
				System.out.println("AAAAA");
				String query = "UPDATE users SET name = '" + new_user.getName() +"', password = '" + new_user.getPassword() +
						"', age = '" + new_user.getAge() +
						"', height = '" + new_user.getHeight() +
						"' WHERE id = " + new_user.getId() + ";";
				st.executeUpdate(query);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			closeAll();
		}
		return new_user;
	}

	public boolean checkDB(User user) throws SQLException {
		String queryCheck = "SELECT EXISTS(SELECT id FROM users WHERE email = '"+ user.getEmail() + "');";
		ResultSet rs = st.executeQuery(queryCheck);

		while (rs.next()){
			if (rs.getString(1).equals("0")){
				rs.close();
				return false;
			}
		}
		rs.close();
		return true;
	}

	@Override
	public void user_set_params(User user) throws SQLException {
		try {
			System.out.println("get email in set_params: " + user.getEmail());
			startConnection();
			String query = "select * from users where email='" + user.getEmail() + "';";
			ResultSet rs = st.executeQuery(query);
			while (rs.next()) {
				user.setId(rs.getLong(1));
				user.setEmail(rs.getString(2));
				user.setPassword(rs.getString(3));
				user.setName(rs.getString(4));
				user.setAge(rs.getString(5));
				user.setHeight(rs.getString(6));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			closeAll();
		}
		System.out.println("in set_params: " + user.getId());
	}

	@Override
	public Statistics findStatistics(Long id) throws SQLException {
		Statistics targetStatistics;
		try{
			startConnection();
			ResultSet rs = st.executeQuery("select * from statistics where id = "+ id +";");
			Statistics statistics = new Statistics();
			while (rs.next()) {
				statistics.setId(rs.getLong(1));
				statistics.setWeight(rs.getString(4));
				statistics.setDate(rs.getString(3));
			}
			targetStatistics = statistics;
		}finally {
			closeAll();
		}

		return targetStatistics;
	}

	@Override
	public Statistics editStatistics(Statistics statistics) throws SQLException {
		try{
			startConnection();

			String query = "UPDATE statistics SET date = '" + statistics.getDate() + "', weight = '" + statistics.getWeight() +
					"' WHERE id = " + statistics.getId() + ";";
			st.executeUpdate(query);

		}finally {
			closeAll();
		}
		return statistics;
	}

}
