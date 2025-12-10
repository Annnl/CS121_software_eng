package jrails;

public class Html {
    private final String content;
    public Html() {
        this.content = "";
    }
    public Html(String content) {
        this.content = content;
    }

    public static Html of(String content) {
        return new Html(content);
    }

    public String toString() {
        return content;
    }

    public Html seq(Html h) {
        return new Html(this.content + h.content);
    }

    public Html br() {
        return new Html(this.content + "<br/>");
    }

    public Html t(Object o) {
        return new Html(this.content + (o == null ? "" : o.toString()));
    }

    public Html p(Html child) {
        return new Html(this.content + "<p>" + child.content + "</p>");
    }

    public Html div(Html child) {
        return new Html(this.content + "<div>" + child.content + "</div>");
    }

    public Html strong(Html child) {
        return new Html(this.content + "<strong>" + child.content + "</strong>");
    }

    public Html h1(Html child) {
        return new Html(this.content + "<h1>" + child.content + "</h1>");
    }

    public Html tr(Html child) {
        return new Html(this.content + "<tr>" + child.content + "</tr>");
    }

    public Html th(Html child) {
        return new Html(this.content + "<th>" + child.content + "</th>");
    }

    public Html td(Html child) {
        return new Html(this.content + "<td>" + child.content + "</td>");
    }

    public Html table(Html child) {
        return new Html(this.content + "<table>" + child.content + "</table>");
    }

    public Html thead(Html child) {
        return new Html(this.content + "<thead>" + child.content + "</thead>");
    }

    public Html tbody(Html child) {
        return new Html(this.content + "<tbody>" + child.content + "</tbody>");
    }

    public Html textarea(String name, Html child) {
        return new Html(this.content + "<textarea name=\"" + name + "\">" + child.content + "</textarea>");
    }

    public Html link_to(String text, String url) {
        return new Html(this.content + "<a href=\"" + url + "\">" + text + "</a>");
    }

    public Html form(String action, Html child) {
        return new Html(this.content + "<form action=\"" + action + "\" accept-charset=\"UTF-8\" method=\"post\">" + child.content + "</form>");
    }

    public Html submit(String value) {
        return new Html(this.content + "<input type=\"submit\" value=\"" + value + "\"/>");
    }
}