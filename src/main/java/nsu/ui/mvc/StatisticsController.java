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

package nsu.ui.mvc;

import javax.validation.Valid;

import nsu.ui.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nsu.ui.Statistics;
import nsu.ui.StatisticsRepository;

import java.sql.SQLException;

/**
 * @author Rob Winch
 */
@Controller
@RequestMapping("/")
public class StatisticsController {
	private final StatisticsRepository statisticsRepository;

	@Autowired
	public StatisticsController(StatisticsRepository statisticsRepository) {
		this.statisticsRepository = statisticsRepository;
	}

	@RequestMapping("/")
	public ModelAndView red() throws SQLException {
		return new ModelAndView("redirect:/login");
	}

	@RequestMapping("login")
	public String login(@ModelAttribute User user) {
		return "stat/login";
	}

	// оставляю  на всякий случай старую версию
//	@RequestMapping("profile")
//	public String profile(@ModelAttribute User user) {
//		return "stat/profile";
//	}

	@RequestMapping("profile")
	public ModelAndView profile(@ModelAttribute User user) {
		User user1 = User.getInstance();
		if (user1.getId() == null) {
			return new ModelAndView("redirect:/login");
		}
		return new ModelAndView("stat/profile", "user", user1);
	}

//	@RequestMapping("/history")
//	public ModelAndView history(@ModelAttribute User user) throws SQLException {
//
//		Iterable<Statistics> statistics = this.statisticsRepository.findAll();
//		return new ModelAndView("stat/history", "statistics", statistics);
//	}

	@RequestMapping("/history")
	public ModelAndView history(@ModelAttribute User user) throws SQLException {
		User user_hist = User.getInstance();
		if (user_hist.getId() == null) {
			return new ModelAndView("redirect:/login");
		}
		System.out.println("in history: " + user_hist.getId());
		Iterable<Statistics> statistics = this.statisticsRepository.findAll(user_hist);
		return new ModelAndView("stat/history", "statistics", statistics);
	}

	@RequestMapping("/delete/{id}")
	public String delete(@PathVariable(value = "id") long id) throws SQLException {
		System.out.println(id);
		this.statisticsRepository.delete(id);
		return "redirect:/history";
	}

	@RequestMapping("{id}")
	public ModelAndView view(@PathVariable("id") Statistics statistics) {
		return new ModelAndView("stat/view", "statistics", statistics);
	}

//	@RequestMapping("create")
//	public String createForm(@ModelAttribute Statistics statistics) {
//		return "stat/create";
//	}

	@RequestMapping("create")
	public ModelAndView createForm(@ModelAttribute Statistics statistics) {
		User user = User.getInstance();
		if (user.getId() == null) {
			return new ModelAndView("redirect:/login");
		}
		return new ModelAndView("stat/create");
	}

//	@RequestMapping(value = "/create", method = RequestMethod.POST)
//	public ModelAndView create(@Valid Statistics statistics, BindingResult result,
//                               RedirectAttributes redirect) throws SQLException {
//		if (result.hasErrors()) {
//			return new ModelAndView("stat/create", "createErrors", result.getAllErrors());
//		}
//		statistics = this.statisticsRepository.save(statistics, this.current_user);
//		//redirect.addFlashAttribute("globalStatistics", "Successfully added");
//		return new ModelAndView("redirect:/history");
//	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ModelAndView create(@Valid Statistics statistics, BindingResult result,
							   RedirectAttributes redirect) throws SQLException {
		User user = User.getInstance();
		if (user.getId() == null) {
			return new ModelAndView("stat/login");
		}
		else if (result.hasErrors()) {
			return new ModelAndView("stat/create", "createErrors", result.getAllErrors());
		}
		User user1 = User.getInstance();
		System.out.println(user1.getId());
		statistics = this.statisticsRepository.save(statistics, user.getId());
		//redirect.addFlashAttribute("globalStatistics", "Successfully added");
		return new ModelAndView("redirect:/history");
	}


	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ModelAndView loginPost(@Valid User user, BindingResult result,
								  RedirectAttributes redirect) throws SQLException {
		if (result.hasErrors()) {
			return new ModelAndView("stat/login", "createErrors", result.getAllErrors());
		}
		// проверка е-мэйл и пароля
		if ((this.statisticsRepository.check_user(user.getEmail(), user.getPassword()))[0]) {
			if ((this.statisticsRepository.check_user(user.getEmail(), user.getPassword()))[1]) {
				this.statisticsRepository.user_set_params(user);
				user.setInstance(user);
				System.out.println("in login: " + user.getId());
				return new ModelAndView("redirect:/history");
			}
			else {
				// wrong password
				return new ModelAndView("stat/login");
			}
		}
		// create a new user
		return new ModelAndView("stat/profile", "user", user);
	}


	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public ModelAndView create_user(@Valid User new_user, BindingResult result,
									RedirectAttributes redirect) throws SQLException {
		if (result.hasErrors()) {
			return new ModelAndView("stat/profile", "createErrors", result.getAllErrors());
		}
		User user = this.statisticsRepository.create_user(new_user);
		user.setInstance(user);

		return new ModelAndView("redirect:/history");
		//return history(new_user);
	}

	@RequestMapping("foo")
	public String foo() {
		throw new RuntimeException("Expected exception in controller");
	}

}
