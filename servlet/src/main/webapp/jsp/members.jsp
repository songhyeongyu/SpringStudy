<%@ page import="hello.servlet.domain.member.MemberRepository" %>
<%@ page import="hello.servlet.domain.member.Member" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  MemberRepository memberRepository = MemberRepository.getInstance();
  List<Member> members = memberRepository.findALl();
%>
<html>
<head>
    <title>Title</title>
</head>
<body>

<%
  for (Member member : members) {
    out.write("   <tr>");
    out.write("       <td>" + member.getId() + "</td>");
    out.write("       <td>" + member.getUsername() + "</td>");
    out.write("       <td>" + member.getAge() + "</td>");
    out.write("   <tr>");
  }
%>
</body>
</html>
