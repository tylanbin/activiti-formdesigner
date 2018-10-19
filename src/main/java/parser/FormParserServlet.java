package parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import util.UuidUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings("serial")
public class FormParserServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 接收参数
		request.setCharacterEncoding("utf-8");
		String content = request.getParameter("content");
		// 处理数据
		String html = "";
		String json = "";
		try {
			if (!StringUtils.isEmpty(content)) {
				Map<String, String> map = generateForm(content);
				html = map.get("genHtml");
				json = map.get("genJson");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		request.setAttribute("html", html);
		request.setAttribute("json", json);
		// 跳转页面
		String context = request.getContextPath() + "/";
		request.getRequestDispatcher(context + "view.jsp").forward(request, response);
	}
	
	public Map<String, String> generateForm(String html) throws Exception {
		// 需要处理的属性名
		List<String> list = Arrays.asList(new String[] {
			"leipiplugins", "title", "orgtype",
			// 列表控件需要的属性名
			"orgtitle", "orgcolwidth", "orgcolvalue"
		});
		ObjectMapper om = new ObjectMapper();
		Map<String, String> map = new HashMap<String, String>();// 用于返回结果
		List<Object> json = new ArrayList<Object>();// 用于封装json
		// 使用正则匹配html标签和属性
		Pattern tagsReg = Pattern.compile("(?s)(<label(((?!<label).)*leipiplugins=\"(radios|checkboxs|select)\".*?)>(.*?)</label>|<(img|input|textarea|select).*?(</select>|</textarea>|/>))");
		Pattern attrReg = Pattern.compile("(?s)((\\w+)=\"(.?|.+?)\")");
		// 先进行html标签的匹配
		Matcher widgets = tagsReg.matcher(html);
		while (widgets.find()) {
			// 记录每个html标签的属性map
			Map<String, String> map_attrs = new HashMap<String, String>();
			// 取到控件的html，进行属性的匹配
			String widget = widgets.group();
			Matcher attrs = attrReg.matcher(widget);
			while (attrs.find()) {
				String attr = attrs.group();
				// 以=来拆分属性对
				String[] arr = attr.split("=");
				if (arr.length == 2) {
					// 只解析有用的属性：标签类型
					if (list.contains(arr[0])) {
						map_attrs.put(arr[0], arr[1].replace("\"", ""));
					}
				}
			}
			// 处理html标签，生成新的html，每个html标签都使用一个新的name
			String html_new = "";
			String uuid = UuidUtils.base58Uuid();
			String type = map_attrs.get("leipiplugins");
			String title = map_attrs.get("title");
			if ("radios".equals(type) || "checkboxs".equals(type)) {
				// 单选/复选
				html_new = widget
						// 处理包在外边的label标签
						.replaceAll("label", "span")
						.replaceFirst("name=\"(.?|.+?)\"", "")
						// 处理里面的input
						.replaceAll("name=\"(.?|.+?)\"", "name=\"" + uuid + "\"");
			} else if ("select".equals(type)) {
				// 下拉
				html_new = widget
						// 处理包在外边的label标签
						.replaceAll("label", "span")
						// 处理select的name
						.replaceAll("name=\"(.?|.+?)\"", "name=\"" + uuid + "\"");
			} else if ("date".equals(type)) {
				// 时间控件
				String orgtype = map_attrs.get("orgtype");
				String fmt = "yyyy-MM-dd HH:mm";
				if ("sys_time".equals(orgtype)) {
					// 日期+时间（默认的时间格式）
				} else if ("sys_date".equals(orgtype)) {
					fmt = "yyyy-MM-dd";// 日期
				} else if ("sys_month".equals(orgtype)) {
					fmt = "yyyy-MM";// 年月
				} else if ("sys_year".equals(orgtype)) {
					fmt = "yyyy";// 年
				}
				html_new = widget
						.replaceAll("leipiplugins=\"(.?|.+?)\"", "class=\"Wdate\" onfocus=\"WdatePicker({dateFmt:'" + fmt + "',lang:'zh-cn'})\"")
						.replaceAll("name=\"(.?|.+?)\"", "name=\"" + uuid + "\"");
			} else if ("macros".equals(type)) {
				// 宏控件（该控件需要在表单使用时才能处理）
				html_new = widget
						.replaceAll("name=\"(.?|.+?)\"", "name=\"" + uuid + "\"");
			} else if ("listctrl".equals(type)) {
				// 列表控件（使用.split("`", -1)以保留最后的空值）
				String[] titles = map_attrs.get("orgtitle").split("`", -1);
				String[] widths = map_attrs.get("orgcolwidth").split("`", -1);
				String[] values = map_attrs.get("orgcolvalue").split("`", -1);
				// 构造表格
				StringBuffer sb = new StringBuffer("<table id=\"" + uuid + "\" title=\"" + title + "\" class=\"listctrl\">");
				// 表格的头部分
				sb.append("<thead>");
				// 加入添加行的按钮
				sb.append("<tr><th colspan=\"" + (titles.length + 1) + "\" style=\"text-align:right;\">" + "<button onclick=\"listctrl_row_add('#" + uuid + "')\">添加一行</button></th></tr>");
				// 生成表头
				sb.append("<tr>");
				for (int i = 0; i < titles.length; i++) {
					sb.append("<th>" + titles[i] + "</th>");
				}
				sb.append("<th>操作</th>");
				sb.append("</tr>");
				sb.append("</thead>");
				// 预置一行作为模板，用于添加行时参考
				sb.append("<tbody>");
				// 存储一个list，用于存储该二维表的各列属性
				ArrayNode arr = om.createArrayNode();
				sb.append("<tr class=\"template\" style=\"display: none;\">");
				for (int i = 0; i < titles.length; i++) {
					// 每列都生成新的name
					String name = UuidUtils.base58Uuid();
					sb.append("<td><input type=\"text\" name=\"" + name + "\" value=\"" + values[i].trim() + "\" style=\"width:" + widths[i].trim() + "px;\"/></td>");
					// 构造每列属性的json对象
					ObjectNode obj = om.createObjectNode();
					obj.put("name", name);
					obj.put("title", titles[i]);
					obj.put("width", widths[i]);
					obj.put("defaultVal", values[i].trim());
					arr.add(obj);
				}
				sb.append("<td><a class=\"btn-row-del\" href=\"javascript:;\" onclick=\"listctrl_row_del('#" + uuid + "', this)\">删除本行</a></td>");
				sb.append("</tr></tbody>");
				
				// 这里需要一个hidden来存储一个json，用于保存二维表的属性结构，用于动态增删行操作
				ObjectNode props = om.createObjectNode();
				props.put("id", uuid);
				props.set("cols", arr);
				// 为了与生成的json的双引号进行区分，注意value两侧是'
				sb.append("<input id=\"" + uuid + "_prop\" class=\"listctrl_prop\" type=\"hidden\" value='" + om.writeValueAsString(props) + "' />");
				// 此外还需要一个hidden用于记录二维表的行数，记录的行数会在查看历史数据时使用
				sb.append("<input id=\"" + uuid + "_rows\" class=\"listctrl_rows\" type=\"hidden\" name=\"" + uuid + "_rows\" value=\"0\" />");
				
				sb.append("</table>");
				html_new = sb.toString();
				
				// 特殊处理列表控件的json
				Map<String, Object> listctrl = new HashMap<String, Object>();
				listctrl.put("formproperty_id", uuid);
				listctrl.put("formproperty_type", "listctrl");
				listctrl.put("formproperty_name", title);
				listctrl.put("formproperty_prop", arr);
				json.add(listctrl);
			} else {
				// 其他标签仅需要替换name即可
				html_new = widget
						.replaceAll("name=\"(.?|.+?)\"", "name=\"" + uuid + "\"");
			}
			// 最后将整个html标签进行替换
			if (!StringUtils.isEmpty(html_new)) {
				html = html.replace(widget, html_new);
				if ("listctrl".equals(type)) {
					// 列表控件不作为普通控件记录
				} else {
					json.add(packageMap(uuid, "string", title));
				}
			}
		}
		// 标记处理
		html = html.replaceAll("\\{\\|-", "").replaceAll("-\\|\\}", "");
		map.put("genHtml", html);
		map.put("genJson", om.writeValueAsString(json));
		// System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(json));
		return map;
	}
	
	private Map<String, Object> packageMap(String id, String type, String name) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formproperty_id", id);
		map.put("formproperty_type", type);
		map.put("formproperty_name", name);
		return map;
	}

}
