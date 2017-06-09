package org.blazer.dataservice.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.blazer.dataservice.body.ConfigBody;
import org.blazer.dataservice.body.ConfigDetailBody;
import org.blazer.dataservice.body.ParamsBody;
import org.blazer.dataservice.cache.ConfigCache;
import org.blazer.dataservice.dao.Dao;
import org.blazer.dataservice.model.ConfigDetailModel;
import org.blazer.dataservice.model.ConfigModel;
import org.blazer.dataservice.util.IntegerUtil;
import org.blazer.dataservice.util.SqlUtil;
import org.blazer.dataservice.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/dataservice")
public class DataServiceAction extends BaseAction {

	private static Logger logger = LoggerFactory.getLogger(DataServiceAction.class);

	@Autowired
	ConfigCache configCache;

	/**
	 * 根据id与参数获取配置执行的结果值
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getconfig")
	public ConfigBody getConfig(HttpServletRequest request, HttpServletResponse response) {
		ConfigBody cb = new ConfigBody();
		try {
			HashMap<String, String> paramMap = getParamMap(request);
			Integer id = IntegerUtil.getInt0(paramMap.get("id"));
			String detailsId = StringUtil.getStr(paramMap.get("detailsid"));
			if (detailsId != null) {
				detailsId = "," + detailsId + ",";
			}
			String detailsKey = StringUtil.getStr(paramMap.get("detailskey"));
			if (detailsKey != null) {
				detailsKey = "," + detailsKey + ",";
			}
			ConfigModel config = configCache.get(id);

			if (!"mysql".equals(config.getDataSourceModel().getDatabase_name())) {
				throw new Exception("只支持查询mysql数据库,不允许查询[" + config.getDataSourceModel().getDatabase_name() + "]");
			}

			if (!config.isInterface()) {
				throw new Exception("该配置不是一个接口。无法执行！");
			}

			cb.setId(config.getId());
			cb.setConfigName(config.getConfigName());
			cb.setConfigType(config.getConfigType());
			cb.setIsInterface(config.isInterface() ? 1 : 0);
			cb.setIsTask(config.isTask() ? 1 : 0);
			cb.setDetails(new HashMap<String, ConfigDetailBody>());

			List<ConfigDetailModel> detailList = config.getDetailList();
			for (ConfigDetailModel detail : detailList) {
				// 匹配details id
				if (detailsId != null && !detailsId.contains("," + detail.getId() + ",")) {
					continue;
				}
				// 匹配details key
				if (detailsKey != null && !detailsKey.contains("," + detail.getKey() + ",")) {
					continue;
				}
				ConfigDetailBody cdb = new ConfigDetailBody();
				String sql = detail.getValues();

				// 替换参数
				for (String key : paramMap.keySet()) {
					// 防止SQL注入
					sql = sql.replace("${" + key + "}", SqlUtil.TransactSQLInjection(paramMap.get(key)));
				}

				Dao dao = detail.getDataSource();
				List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();

				String errorMessage = StringUtils.EMPTY;
				try {
					String oldSql = detail.getValues().toLowerCase();
					if (oldSql.contains("select")) {
						values = dao.find(sql);
					} else if (oldSql.contains("insert") || oldSql.contains("update") || oldSql.contains("delete") || oldSql.contains("create")) {
						dao.update(sql);
					} else {
						values = dao.find(sql);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					errorMessage = e.getMessage();
				}

				cdb.setErrorMessage(errorMessage);
				cdb.setId(detail.getId());
				cdb.setValues(values);
				cb.getDetails().put(detail.getKey(), cdb);
			}
			cb.success();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			cb.error();
			cb.setMessage(e.getMessage());
		}
		return cb;
	}

	/**
	 * 根据id获取所有配置的参数
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getparams")
	public ParamsBody getParams(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, String> paramMap = getParamMap(request);
		ParamsBody body = new ParamsBody();
		Integer id = IntegerUtil.getInt0(paramMap.get("id"));

		ConfigModel config = configCache.get(id);
		List<ConfigDetailModel> detailList = config.getDetailList();

		body.setParams(new ArrayList<String>());
		body.setDetails(new HashMap<String, List<String>>());

		for (ConfigDetailModel detail : detailList) {
			String key = detail.getKey();
			String sql = detail.getValues();
			List<String> tmpList = new ArrayList<String>();
			for (String param : SqlUtil.ExtractParams(sql)) {
				if (!body.getParams().contains(param)) {
					body.getParams().add(param);
				}
				if (!tmpList.contains(param)) {
					tmpList.add(param);
				}
			}
			body.getDetails().put(key, tmpList);
		}

		return body;
	}

}
