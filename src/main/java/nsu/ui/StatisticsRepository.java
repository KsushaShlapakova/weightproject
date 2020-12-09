/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package nsu.ui;

import java.sql.SQLException;
import java.util.ArrayList;

public interface StatisticsRepository {


	ArrayList<Statistics> findAll(User user) throws SQLException;

	Statistics save(Statistics message, long user_id) throws SQLException;

	Statistics findStatistics(Long id) throws SQLException;

	void delete(Long id) throws SQLException;

	boolean[] check_user(String email, String password) throws SQLException;

	User create_user(User user) throws SQLException;

	void user_set_params(User user) throws SQLException;

	Statistics editStatistics(Statistics statistics) throws SQLException;

}
