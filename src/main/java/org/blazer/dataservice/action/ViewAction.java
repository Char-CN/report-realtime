package org.blazer.dataservice.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.blazer.dataservice.body.DataSourceBody;
import org.blazer.dataservice.body.GroupBody;
import org.blazer.dataservice.body.view.ViewConfigBody;
import org.blazer.dataservice.dao.CustomJdbcDao;
import org.blazer.dataservice.exception.UnknowDataSourceException;
import org.blazer.dataservice.service.ViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller(value = "viewAction")
@RequestMapping("/view")
public class ViewAction extends BaseAction {

	private static Logger logger = LoggerFactory.getLogger(ViewAction.class);

	@Autowired
	ViewService viewService;

	@Autowired
	CustomJdbcDao customJdbcDao;

	@ResponseBody
	@RequestMapping("/getTree")
	public List<GroupBody> getGroup(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		// String str = "[{\"text\" : \"Item1\",\"state\" : \"closed\", \"id\" :
		// \"-1\"}, {\"text\" : \"Item2\",\"state\" : \"closed\", \"id\" :
		// \"2\"}]";
		// if (getParamMap(request).get("id").equals("-1")) {
		// return str;
		// }
		return viewService.findTreeById(getParamMap(request));
	}

	@ResponseBody
	@RequestMapping("/getConfigsByGroupId")
	public List<ViewConfigBody> getConfigsByGroupId(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		return viewService.getConfigsByGroupId(getParamMap(request));
	}

	@ResponseBody
	@RequestMapping("/getConfigById")
	public ViewConfigBody getConfigById(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("map : " + getParamMap(request));
		return viewService.getConfigById(getParamMap(request));
	}

	@ResponseBody
	@RequestMapping("/getDataSourceAll")
	public List<DataSourceBody> getDataSourceAll(HttpServletRequest request, HttpServletResponse response) throws UnknowDataSourceException {
		logger.debug("map : " + getParamMap(request));
		List<DataSourceBody> list = new ArrayList<DataSourceBody>();
		for (Integer i : customJdbcDao.getKeySet()) {
			DataSourceBody dsb = new DataSourceBody();
			dsb.setId(i);
			dsb.setDatabaseName(customJdbcDao.getDataSourceBean(i).getDatabase_name());
			dsb.setTitle(customJdbcDao.getDataSourceBean(i).getTitle());
			dsb.setRemark(customJdbcDao.getDataSourceBean(i).getRemark());
			list.add(dsb);
		}
		return list;
	}

}