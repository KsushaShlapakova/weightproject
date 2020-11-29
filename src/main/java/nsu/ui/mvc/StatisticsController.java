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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
		return new ModelAndView("redirect:/history");
	}

	@RequestMapping("/history")
	public ModelAndView history() throws SQLException {
		Iterable<Statistics> statistics = this.statisticsRepository.findAll().values();
		return new ModelAndView("stat/history", "statistics", statistics);
	}

	@RequestMapping("{id}")
	public ModelAndView view(@PathVariable("id") Statistics statistics) {
		return new ModelAndView("stat/view", "statistics", statistics);
	}

	@RequestMapping(params = "create", method = RequestMethod.GET)
	public String createForm(@ModelAttribute Statistics statistics) {
		return "stat/create";
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ModelAndView create(@Valid Statistics statistics, BindingResult result,
                               RedirectAttributes redirect) throws SQLException {
		System.out.println(statistics.getId());
		if (result.hasErrors()) {
			return new ModelAndView("stat/create", "createErrors", result.getAllErrors());
		}
		statistics = this.statisticsRepository.save(statistics);
		//redirect.addFlashAttribute("globalStatistics", "Successfully added");
		System.out.println(statistics.getId());
		return new ModelAndView("redirect:/history");
	}

	@RequestMapping("foo")
	public String foo() {
		throw new RuntimeException("Expected exception in controller");
	}

}
