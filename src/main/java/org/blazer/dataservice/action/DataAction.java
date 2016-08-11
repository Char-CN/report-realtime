package org.blazer.dataservice.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blazer.dataservice.body.ConfigBody;
import org.blazer.dataservice.body.ParamsBody;
import org.blazer.dataservice.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/dataservice")
public class DataAction extends BaseAction {

	@Autowired
	private DataService dataService;

	@ResponseBody
	@RequestMapping("/getconfig")
	public ConfigBody getConfig(HttpServletRequest request, HttpServletResponse response) {
		return dataService.getConfigById(getParamMap(request));
	}

	@ResponseBody
	@RequestMapping("/getparams")
	public ParamsBody getparams(HttpServletRequest request, HttpServletResponse response) {
		return dataService.getParamsById(getParamMap(request));
	}

	@ResponseBody
	@RequestMapping("/getdetail")
	public String getDetail(HttpServletRequest request, HttpServletResponse response) {
		return dataService.getConfigById(getParamMap(request)).toString();
	}

}
