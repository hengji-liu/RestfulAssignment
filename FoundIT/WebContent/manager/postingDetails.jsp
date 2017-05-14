<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset==UTF-8">
<title>Job posting Details</title>
</head>
<body>
	<table border="1">
		<tr>
			<td>Company name</td>
			<td>${posting.companyName}</td>
		</tr>
		<tr>
			<td>Salary rate</td>
			<td>${posting.salaryRate }</td>
		</tr>
		<tr>
			<td>Position type</td>
			<td>${posting.positionType}</td>
		</tr>
		<tr>
			<td>Location</td>
			<td>${posting.location }</td>
		</tr>
		<tr>
			<td>Descriptions</td>
			<td>${posting.descriptions }</td>
		</tr>
	</table>

		<c:choose>
			<c:when test="${size == 0}">
				<a href="manager?method=gotoUpdatePosting&pid=${posting.jobId }">Click
					to modify this posting</a>
			</c:when>
			<c:otherwise>
				<table border="1">
				<tr>
					<th>Candidate details</th>
					<th>Cover letter</th>
				</tr>
					<c:forEach var="app" items="${applications}">
						<td>${app.candidateDetails }</td>
						<td>${app.coverLetter }</td>
					</c:forEach>
				</table>
			</c:otherwise>
		</c:choose>


</body>
</html>