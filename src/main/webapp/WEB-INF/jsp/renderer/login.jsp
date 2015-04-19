<%@ page import="com.jaeksoft.searchlib.renderer.Renderer"%>
<div class="osscmnrdr oss-login-div">
	<fieldset>
		<legend>Please, login</legend>
		<br />
		<form method="post" action="renderer">
			<table>
				<tr>
					<td align="right"><label for="username">Username: </label></td>
					<td><input type="text" name="username" id="username" size="40" /></td>
				</tr>
				<tr>
					<td align="right"><label for="password">Password: </label></td>
					<td><input type="password" name="password" id="password"
						size="40" /></td>
				</tr>
				<tr>
					<td />
					<td><input type="submit" value="Login" /></td>
				</tr>
			</table>
		<%
			String[] hiddenParameterList = (String[]) request
					.getAttribute("hiddenParameterList");
			for (String p : hiddenParameterList) {
				String v = request.getParameter(p);
				if (v != null) {
		%>
		<input type="hidden" name="<%=p%>" value="<%=v%>" />
		<% } } %>
		</form>
	</fieldset>
	<% String error = (String)request.getAttribute("error"); if (error != null) { %>
	<div id="osscmnrdr oss-error"><%=error%></div>
	<% } %>
</div>