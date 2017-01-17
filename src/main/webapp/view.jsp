<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<% request.setCharacterEncoding("utf-8"); %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Activiti Form Designer Viwer</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<script type="text/javascript" charset="utf-8" src="assets/js.Date.js"></script>
<script type="text/javascript" charset="utf-8" src="assets/jquery-1.8.3.min.js"></script>
<script type="text/javascript" charset="utf-8" src="assets/My97DatePicker/WdatePicker.js"></script>
<style type="text/css">
	table {
		border-color: #666 !important;
		border-width: 1px 0 0 1px;
		border-style: dotted;
		border-collapse: collapse;
		margin: auto;
	}
	table tr {
		height: 30px;
	}
	table td, table th {
		border-color: #666 !important;
		border-width: 0 1px 1px 0;
		border-style: dotted;
		vertical-align: middle;
		text-align: center;
		height: 26px;
		padding: 2px 3px;
		min-width: 60px;
		word-break: keep-all;
	}
	.listctrl {
		font-size: 12px;
	}
</style>
<script type="text/javascript">
	$(function() {
		// 处理表单
		// 遍历每个表格
		$('table').each(function(i, table) {
			// 遍历每个表格中的宏控件，进行处理
			$(table).find('[leipiplugins="macros"]').each(function(i, input) {
				var type = $(input).attr('orgtype');
				var val = '';
				switch (type) {
					case 'sys_time':
						// 当前日期+时间
						val = new Date().format('yyyy-MM-dd HH:mm');
						break;
					case 'sys_date':
						// 当前日期
						val = new Date().format('yyyy-MM-dd');
						break;
					case 'sys_month':
						// 当前年月
						val = new Date().format('yyyy-MM');
						break;
					case 'sys_year':
						// 当前年份
						val = new Date().format('yyyy');
						break;
					case 'sys_uid':
						// 当前用户id（这里应该通过系统代码获取，这里只做示例）
						val = '1101';
						// 真实使用中，有两种处理方式（推荐第一种，减少数据访问量）
						// 第一就是获取表单时提前查询，这里使用el表达式{user.id}获取
						// 第二是在这里发起ajax请求进行获取
						break;
					case 'sys_name':
						// 当前用户姓名（这里应该通过系统代码获取，这里只做示例）
						val = '张三';
						break;
					case 'sys_dept':
						// 当前用户部门（这里应该通过系统代码获取，这里只做示例）
						val = '研发部';
						break;
				}
				// 设置值，并修改为只读
				$(input).val(val);
				$(input).attr('readonly', 'readonly');
			})
		})
	})
	// 列表的处理方法（可以提取出一个js文件）
	function listctrl_row_add(id) {
		// 增加行
		$(id + ' tbody .template').clone(true)
			.removeClass('template')
			.find('.btn-row-del').show().end()
			.find('input').end()
			.appendTo($(id + ' tbody'));
		$(id + ' tbody tr:last').show();
		// 更新行记录
		var count = parseInt($(id + '_rows').val());
		$(id + '_rows').val(count + 1);
	}
	function listctrl_row_del(id, btn) {
		// 删除本行
		$(btn).parent().parent().remove();
		// 更新二维表的行记录数
		// 更新行记录
		var count = parseInt($(id + '_rows').val());
		$(id + '_rows').val(count - 1);
	}
</script>
</head>
<body>
	<div>${html}</div>
	<div style="text-align: center;">
		<button onclick="alert($('#json').val())">查看JSON</button>
		<input type="hidden" id="json" value='${json }' />
	</div>
</body>
</html>
