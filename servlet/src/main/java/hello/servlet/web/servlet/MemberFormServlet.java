package hello.servlet.web.servlet;

import hello.servlet.domain.member.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;
import java.io.PrintWriter;


@WebServlet(name = "memberFormServlet", urlPatterns = "/servlet/members/new-form")
public class MemberFormServlet extends HttpServlet {
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");

        PrintWriter w = response.getWriter();
        w.write("<!DOCTYPE html>\n" +
        "<html>\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <title>Title</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "<form action=\"/servlet/members/save\" method=\"post\">\n" +
        "    username: <input type=\"text\" name=\"username\" />\n" +
        "    age:      <input type=\"text\" name=\"age\" />\n" +
        " <button type=\"submit\">전송</button>\n" + "</form>\n" +
        "</body>\n" +
        "</html>\n");
    }
}
