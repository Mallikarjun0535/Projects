<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<style>
table {
	font-family: arial, sans-serif;
	border-collapse: collapse;
	width: 100%;
}

td, th {
	border: 1px solid #dddddd;
	text-align: left;
	padding: 8px;
}

tr:nth-child(even) {
	background-color: #dddddd;
}
</style>
</head>
<body>
	<h1>Vehicles List</h1>
	<table>
		<tr>
			<th>VehicleName</th>
			<th>Manfaturing site</th>
			<th>alias</th>
			<th>status</th>
		</tr>
			<c:forEach items="${vehiclelist}" var="vehicle">
				<tr>
					<td><c:out value="${vehicle.vehiclename}" /></td>
					<td><c:out value="${vehicle.manfaturingSite}" /></td>
					<td><c:out value="${vehicle.alias}" /></td>
					<td><c:out value="${vehicle.status}" /></td>
				</tr>
			</c:forEach>
	</table>
</body>
</html>