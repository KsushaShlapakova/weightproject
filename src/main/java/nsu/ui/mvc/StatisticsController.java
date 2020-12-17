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

import nsu.ui.PhotoDays;
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
		user.setInstance(null);
		PhotoDays pd = new PhotoDays();
		pd.setInstance(null);
		return "stat/login";
	}

	@RequestMapping("login_wrong_password")
	public String login_wrong_password(@ModelAttribute User user) {
		return "stat/login_wrong_password";
	}

	// оставляю  на всякий случай старую версию
//	@RequestMapping("profile")
//	public String profile(@ModelAttribute User user) {
//		return "stat/profile";
//	}

	@RequestMapping("profile")
	public ModelAndView profile() {
		User user1 = User.getInstance();
		if (user1.getId() == null) {
			return new ModelAndView("redirect:/login");
		}
		return new ModelAndView("stat/profile", "user", user1);
	}
//	@ModelAttribute PhotoDays photoDays

	@RequestMapping("progress")
	public ModelAndView progress() throws SQLException {
		// передача ранней и поздней фотографии

		User user = User.getInstance();
		PhotoDays pd = PhotoDays.getInstance();
		if (user.getId() == null) {
			return new ModelAndView("redirect:/login");
		}
		if (pd.getDate1() == null){
			pd = this.statisticsRepository.findPhotoDay(user);
		}
		System.out.println(pd.getDate1());

		return new ModelAndView("stat/progress", "photoDays", pd);
	}

	@RequestMapping("edit/{id}")
	public ModelAndView createEditForm(@PathVariable("id") Statistics statistics) throws SQLException {
		User user1 = User.getInstance();
		System.out.println("Фотка есть или че "+statistics.getPhoto());
		System.out.println("Фотка есть или че "+statistics.getPhotoName());
		if (user1.getId() == null) {
			return new ModelAndView("redirect:/login");
		}
		return new ModelAndView("stat/edit", "statistics", statistics);
	}

//	@RequestMapping("/history")
//	public ModelAndView history(@ModelAttribute User user) throws SQLException {
//
//		Iterable<Statistics> statistics = this.statisticsRepository.findAll();
//		return new ModelAndView("stat/history", "statistics", statistics);
//	}

	@RequestMapping("/history")
	public ModelAndView history() throws SQLException {
		User user_hist = User.getInstance();
		if (user_hist.getId() == null) {
			return new ModelAndView("redirect:/login");
		}
		System.out.println("in history: " + user_hist.getId());
		Iterable<Statistics> statistics = this.statisticsRepository.findAll(user_hist);
//		for (Statistics statistic: statistics) {
//			System.out.println(statistic.getPhoto());
//		}
		return new ModelAndView("stat/history", "statistics", statistics);
	}

	@RequestMapping("/delete/{id}")
	public ModelAndView delete(@PathVariable(value = "id") long id) throws SQLException {
		System.out.println(id);
		User user_hist = User.getInstance();
		if (user_hist.getId() == null) {
			return new ModelAndView("redirect:/login");
		}
		this.statisticsRepository.delete(id);
		return new ModelAndView("redirect:/history");
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
				return new ModelAndView("redirect:/profile");
			}
			else {
				// wrong password
				System.out.println("wrong password");
//				return new ModelAndView("redirect:/login");
				return new ModelAndView("redirect:/login_wrong_password");
			}
		}
		// create a new user
		System.out.println("create a new user");
		user.setId((long) 0);
		user.setInstance(user);
		return new ModelAndView("redirect:/profile");
	}


	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public ModelAndView create_user(@Valid User new_user, BindingResult result,
									RedirectAttributes redirect) throws SQLException {
		System.out.println("email");
		System.out.println(new_user.getEmail());
//		System.out.println(new_user);
//		User user=new User();
//		System.out.println(user);
		if (result.hasErrors()) {
			return new ModelAndView("stat/profile", "createErrors", result.getAllErrors());
		}
		new_user = this.statisticsRepository.create_user(new_user);
		new_user.setInstance(new_user);

		return new ModelAndView("redirect:/profile");
		//return history(new_user);
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public ModelAndView createEdit(@Valid Statistics statistics, BindingResult result,
								   RedirectAttributes redirect) throws SQLException {
		if (result.hasErrors()) {
			return new ModelAndView("stat/edit", "formErrors", result.getAllErrors());
		}
		ModelAndView mv = null;

		this.statisticsRepository.editStatistics(statistics);

		redirect.addFlashAttribute("globalStatistics", "Successfully edited the statistics");
		return new ModelAndView("redirect:/history");
	}

	@RequestMapping(value = "/progress", method = RequestMethod.POST)
	public ModelAndView showPhoto(@Valid PhotoDays photoDays, BindingResult result,
								  RedirectAttributes redirect) throws SQLException {
		if (result.hasErrors()) {
			return new ModelAndView("stat/progress", "formErrors", result.getAllErrors());
		}
		User user = User.getInstance();
		System.out.println(photoDays.getDate1());
		System.out.println(photoDays.getDate2());


		photoDays = this.statisticsRepository.findPhoto(photoDays, user);
		photoDays.setInstance(photoDays);
		System.out.println(photoDays.getPhoto1());
		System.out.println(photoDays.getPhoto2());

		return new ModelAndView("redirect:/progress");

	}

	@RequestMapping("foo")
	public String foo() {
		throw new RuntimeException("Expected exception in controller");
	}

}