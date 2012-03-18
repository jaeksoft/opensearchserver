<%@ Page Language="VB" Debug="true" %>

<%@ Import Namespace="System.Globalization" %>
<%@ Import Namespace="System.Xml" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>OpenSearchServer</title>
</head>
<body>
    <form action="search.aspx" method="get">
    <input type="text" name="q" value="<%=HttpUtility.HtmlEncode(Request.QueryString("q"))%>" />
    <input type="submit" value="Search" />
    </form>
    <% 
        Dim query As String = Request.QueryString("q")
        Dim p As String = Request.QueryString("p")
        Dim rows As Long = 10
        Dim start As Long = 0
        If p <> Nothing then start = rows * Long.Parse(p)
        If query <> Nothing Then
            Dim search As OpenSearchServer.Search = New OpenSearchServer.Search("http://localhost:8080", "indexname", Nothing, Nothing)
            Dim result As OpenSearchServer.Result = search.search("search", query, start, rows)
            Dim numFound As Long = result.getNumFound
            Dim paging As OpenSearchServer.Paging = result.getPaging(11)
            Dim documents As String = "No result"
            If numFound = 1 Then
                documents = "1 result"
            ElseIf numFound > 1 Then
                documents = numFound & " results"
            End If
                 
    %>
    <p>
        <%= documents%></p>
        <%= paging.getCurrentPage%>/<%=paging.getTotalPages %>
    <%
        For i As Integer = 0 To result.getDocumentsCount() - 1
                
            Dim title As String = result.getSnippet(i, "title")
            Dim content As String = result.getSnippet(i, "content")
            Dim url As String = result.getField(i, "url")
                           
    %>
    <div>
        <h3 style="margin-bottom: 0px">
            <a href="<%= link%>">
                <%= title%></a></h3>
        <div>
            <%= content%></div>
    </div>
    <%
    Next
                                        
End If%>
</body>
</html>
