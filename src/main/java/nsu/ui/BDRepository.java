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


import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
				statistics.setWeight(rs.getString("weight"));
				statistics.setDate(rs.getString("date"));
				statistics.setPhoto(rs.getString("photo"));
				statistics.setPhotoName(rs.getString("photo_name"));

				sorted.put(LocalDate.parse(statistics.getDate(), formatter), statistics);
			}

			stat = new ArrayList<>(sorted.values());
			if (stat.size() != 0) {
				stat.get(0).setDelta("0.0");
			}
			for(int i = 1; i < stat.size(); i++){
				Statistics temp = stat.get(i);
				temp.setWeight(temp.getWeight().replace(",","."));
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
		String dotWeight = stat.getWeight().replace(",", ".");
		try {
			startConnection();
			String respond = null;

			ResultSet rs = st.executeQuery("select id, date from statistics where date = '" + stat.getDate() + "' and user_id='"+user_id+"';");

			while (rs.next()) {
				respond = rs.getString("date");
				id = rs.getLong("id");
			}

			if (respond == null) {
				// прописать если getPhoto=""
				if (stat.getPhoto() == null){
					String queryStudent = "insert into statistics (user_id, date, weight, photo)" +
							" values(?,?,?,?)";
					PreparedStatement ps = con.prepareStatement(queryStudent);
					ps.setLong(1, user_id);
					ps.setString(2, stat.getDate());
					ps.setString(3, dotWeight);
					ps.setString(4, stat.getPhoto());
					ps.executeUpdate();

					st.executeUpdate(queryStudent);
					ResultSet potentialId = st.executeQuery("select id from statistics where weight = '" + stat.getWeight() + "'and date= '" + stat.getDate() + "' and user_id='" + user_id + "';");
					while (potentialId.next()) {
						id = potentialId.getLong("id");
					}

					potentialId.close();

					ps.close();
				}
				else {
					String encodedString = "";

					// Впишите свой путь каталога, где хранятся фотки
					File file = new File("/Users/sarantuaa/Downloads/wallpaper/"+stat.getPhoto());
					FileInputStream imageInFile = new FileInputStream(file);
					byte imageData[] = new byte[(int) file.length()];
					imageInFile.read(imageData);
					encodedString = Base64.getEncoder().encodeToString(imageData);

					String queryStudent = "insert into statistics (user_id, date, weight, photo, photo_name)" +
							" values(?,?,?,?,?)";
					PreparedStatement ps = con.prepareStatement(queryStudent);
					ps.setLong(1, user_id);
					ps.setString(2, stat.getDate());
					ps.setString(3, dotWeight);
					ps.setString(4, encodedString);
					ps.setString(5,stat.getPhoto());
					ps.executeUpdate();

					st.executeUpdate(queryStudent);
					ResultSet potentialId = st.executeQuery("select id from statistics where weight = '" + stat.getWeight() + "'and date= '" + stat.getDate() + "' and user_id='" + user_id + "';");
					while (potentialId.next()) {
						id = potentialId.getLong("id");
					}

					potentialId.close();

					ps.close();

				}

			} else {

				if (stat.getPhoto().equals("")){
					String queryStudent = "update statistics set weight ='" + dotWeight + "', photo='"+stat.getPhoto()+"' where date = '" + stat.getDate() + "' and user_id='"+user_id+"';";
					st.executeUpdate(queryStudent);
				} else {
					String encodedString = "";

					// Впишите свой путь каталога, где хранятся фотки
					File file = new File("/Users/sarantuaa/Downloads/wallpaper/"+stat.getPhoto());
					FileInputStream imageInFile = new FileInputStream(file);
					byte imageData[] = new byte[(int) file.length()];
					imageInFile.read(imageData);
					encodedString = Base64.getEncoder().encodeToString(imageData);

					String queryStudent = "update statistics set weight ='" + dotWeight + "', photo='"+encodedString+"', photo_name='"+stat.getPhoto()+"' where date = '" + stat.getDate() + "' and user_id='" + user_id + "';";
					st.executeUpdate(queryStudent);
				}

			}
			rs.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
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

				String query_id = "select * from users where id = (select max(id) from users);";
				ResultSet rs = st.executeQuery(query_id);
				while (rs.next()) {
					new_user.setId(rs.getLong("id"));
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
				user.setId(rs.getLong("id"));
				user.setEmail(rs.getString("email"));
				user.setPassword(rs.getString("password"));
				user.setName(rs.getString("name"));
				user.setAge(rs.getString("age"));
				user.setHeight(rs.getString("height"));
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
				statistics.setId(rs.getLong("id"));
				statistics.setWeight(rs.getString("weight"));
				statistics.setDate(rs.getString("date"));
				statistics.setPhoto(rs.getString("photo"));
				statistics.setPhotoName(rs.getString("photo_name"));
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

			if (statistics.getPhoto().equals("")) {

				String query = "UPDATE statistics SET date = '" + statistics.getDate() + "', weight = '" + statistics.getWeight() +
						"', photo='"+statistics.getPhoto()+"', photo_name='"+statistics.getPhoto()+"' WHERE id = " + statistics.getId() + ";";
				st.executeUpdate(query);
			} else {
				String encodedString = "";

				// Впишите свой путь каталога, где хранятся фотки
				File file = new File("/Users/sarantuaa/Downloads/wallpaper/"+statistics.getPhoto());
				FileInputStream imageInFile = new FileInputStream(file);
				byte imageData[] = new byte[(int) file.length()];
				imageInFile.read(imageData);
				encodedString = Base64.getEncoder().encodeToString(imageData);

				String query = "UPDATE statistics SET date = '" + statistics.getDate() + "', weight = '" + statistics.getWeight() +
						"', photo='"+encodedString+"', photo_name='"+statistics.getPhoto()+"' WHERE id = " + statistics.getId() + ";";
				st.executeUpdate(query);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			closeAll();
		}
		return statistics;
	}

	@Override
	public Statistics findStatisticsByDate(Long id, String date) throws SQLException {
		Statistics targetStatistics;
		try{
			startConnection();
			ResultSet rs = st.executeQuery("select * from statistics where id = '"+ id +"' and date='"+date+"';");
			Statistics statistics = new Statistics();
			while (rs.next()) {
				statistics.setId(rs.getLong("id"));
				statistics.setWeight(rs.getString("weight"));
				statistics.setDate(rs.getString("date"));
				statistics.setPhoto(rs.getString("photo"));
			}
			targetStatistics = statistics;
		}finally {
			closeAll();
		}

		return targetStatistics;
	}

	@Override
	public PhotoDays findPhotoDay(User user) throws SQLException {
		PhotoDays pd = new PhotoDays();
		ArrayList<Statistics> statistics = findAll(user);
		LinkedHashMap<String, String> photoDays = new LinkedHashMap<>();
		for (Statistics statistic: statistics) {
			if (!statistic.getPhoto().equals("")) {
				photoDays.put(statistic.getDate(), statistic.getPhoto());
			}
		}

		if (photoDays.isEmpty()){
			LocalDate date = LocalDate.now();
			pd.setDate1(String.valueOf(date));
			pd.setDate2(String.valueOf(date));
			pd.setPhoto1(null);
			pd.setPhoto2(null);
			return pd;
		}
		pd.setDate1((String) photoDays.keySet().toArray()[0]);
		pd.setDate2((String) photoDays.keySet().toArray()[photoDays.keySet().size()-1]);
		pd.setPhoto1(photoDays.get(pd.getDate1()));
		pd.setPhoto2(photoDays.get(pd.getDate2()));
		return pd;
	}

	@Override
	public PhotoDays findPhoto(PhotoDays photoDays, User user) throws SQLException {
		PhotoDays pd = new PhotoDays();
		try{
			startConnection();
			ResultSet rs = st.executeQuery("select photo from statistics where date='"+ photoDays.getDate1() + "' and user_id = '" + user.getId()+"';");

			while (rs.next()) {
				pd.setPhoto1(rs.getString("photo"));
			}


			rs = st.executeQuery("select photo from statistics where date='"+ photoDays.getDate2() + "' and user_id ='" + user.getId()+"';");
			while (rs.next()) {
				pd.setPhoto2(rs.getString("photo"));
			}

			pd.setDate1(photoDays.getDate1());
			pd.setDate2(photoDays.getDate2());
		}finally {
			closeAll();
		}
		return pd;
	}

	@Override
	public ArrayList<Object[]> dynamics(User user) throws SQLException {
		ArrayList<Object[]> hM = new ArrayList<>();
		Object[] names = new Object[2];
		names[0] = "Дата";
		names[1] = "Вес";
		hM.add(names);
		try{
			startConnection();
			ResultSet rs = st.executeQuery("select * from statistics where user_id = "+ user.getId() +" order by date asc;");
			while (rs.next()) {
				Object[] point = new Object[2];
				point[0] = rs.getString("date").substring(5, rs.getString("date").length());
				point[1] = rs.getFloat("weight");
				hM.add(point);
			}
		}finally {
			closeAll();
		}

		if (hM.size() > 1){
			Object[] weightFirst = (Object[]) hM.toArray()[1];
			Object[] weightLast = (Object[]) hM.toArray()[hM.size()-1];

			Float imt = null;
			if (user.getHeight() != null){
				imt = IMT((Float) weightLast[1], Float.parseFloat(user.getHeight()));
			}

			Float sevenDays = null;
			Float thirtyDays = null;

			if (hM.size() <= 7) {
				sevenDays = (Float) weightLast[1] - (Float) weightFirst[1];
				thirtyDays = (Float) weightLast[1] - (Float) weightFirst[1];
			}else if (hM.size() > 7 || hM.size() < 31){
				Object[] weightSeven = (Object[]) hM.toArray()[hM.size()-8];
				sevenDays = (Float) weightLast[1] - (Float) weightSeven[1];
				thirtyDays = (Float) weightLast[1] - (Float) weightFirst[1];
			}else if (hM.size() >= 31){
				Object[] weightSeven = (Object[]) hM.toArray()[hM.size()-8];
				Object[] weightThirty = (Object[]) hM.toArray()[hM.size()-31];
				sevenDays = (Float) weightLast[1] - (Float) weightSeven[1];
				thirtyDays = (Float) weightLast[1] - (Float) weightThirty[1];
			}
			Object[] results = new Object[4];
			if (sevenDays > 0) {
				results[0] = "+ " + sevenDays;
			}else {
				results[0] = ""+sevenDays;
			}
			if (thirtyDays > 0) {
				results[1] = "+ " + thirtyDays;
			}else {
				results[1] = ""+thirtyDays;
			}

			if (imt != null) {
				results[2] = ""+category(imt);
			}else{
				results[2] = null;
			}
			results[3] = imt;

			hM.add(results);


		}

		return hM;

	}

	public Float IMT(Float weight, Float height){
		String result = String.format("%.3f", weight/((height/100)*(height/100)));
		return Float.parseFloat(result.replace(",", "."));

	}

	public String category(Float imt){
		String result = null;
		if (imt <= 16){
			result = "выраженный дефицит массы тела";
		}else if (imt > 16 && imt <= 18.5){
			result = "недостаточная масса тела";
		}else if (imt > 18.5 && imt <= 25){
			result = "нормальный вес";
		}else if (imt > 25 && imt <= 30){
			result = "избыточная масса тела";
		}else if (imt > 30 && imt <= 35){
			result = "ожирение певрой степени";
		}else if (imt > 35 && imt <= 40){
			result = "ожирение второй степени";
		}else if (imt > 40){
			result = "ожирение третьей степени";
		}
		return result;
	}
}
