package jrails;

public class View {
    public static Html empty() {
        return Html.of("");
    }

    public static Html br() {
        return Html.of("<br/>");
    }

    public static Html t(Object o) {
        return Html.of(o == null ? "" : o.toString());
    }

    public static Html p(Html child) {
        return Html.of("<p>" + child.toString() + "</p>");
    }

    public static Html div(Html child) {
        return Html.of("<div>" + child.toString() + "</div>");
    }

    public static Html strong(Html child) {
        return Html.of("<strong>" + child.toString() + "</strong>");
    }

    public static Html h1(Html child) {
        return Html.of("<h1>" + child.toString() + "</h1>");
    }

    public static Html tr(Html child) {
        return Html.of("<tr>" + child.toString() + "</tr>");
    }

    public static Html th(Html child) {
        return Html.of("<th>" + child.toString() + "</th>");
    }

    public static Html td(Html child) {
        return Html.of("<td>" + child.toString() + "</td>");
    }

    public static Html table(Html child) {
        return Html.of("<table>" + child.toString() + "</table>");
    }

    public static Html thead(Html child) {
        return Html.of("<thead>" + child.toString() + "</thead>");
    }

    public static Html tbody(Html child) {
        return Html.of("<tbody>" + child.toString() + "</tbody>");
    }

    public static Html textarea(String name, Html child) {
        return Html.of("<textarea name=\"" + name + "\">" + child.toString() + "</textarea>");
    }

    public static Html link_to(String text, String url) {
        return Html.of("<a href=\"" + url + "\">" + text + "</a>");
    }

    public static Html form(String action, Html child) {
        return Html.of("<form action=\"" + action + "\" accept-charset=\"UTF-8\" method=\"post\">" + child.toString() + "</form>");
    }

    public static Html submit(String value) {
        return Html.of("<input type=\"submit\" value=\"" + value + "\"/>");
    }
}