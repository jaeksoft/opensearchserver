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
        If query <> Nothing Then
            Dim url As String = "http://localhost:8080/search?use=indexname&qt=search&q=" + HttpUtility.HtmlEncode(query)
            
            Dim doc As XmlDocument = New XmlDocument()
            doc.Load(XmlReader.Create(url))
            
            Dim nodes As XmlNodeList = doc.SelectNodes("/response/result/doc")
            Dim resultNode As XmlNode = doc.SelectSingleNode("/response/result")      
            Dim count As String = resultNode.Attributes.GetNamedItem("numFound").Value
            
            Dim results As String = "No result"
            If count = 1 Then
                documents = "1 result"
            ElseIf count > 1 Then
                documents = count + " results"
            End If
                 
    %>
    <p>
        <%= results%></p>
    <%
        For Each node As XmlNode In nodes
                
            Dim title As String = node.SelectSingleNode("snippet[@name='title']").InnerText
            Dim content As String = node.SelectSingleNode("snippet[@name='content']").InnerText
            Dim url As String = node.SelectSingleNode("field[@name='url']").InnerText
                
    %>
    <div>
        <h3 style="margin-bottom: 0px">
            <a href="<%= url%>">
                <%= title%></a></h3>
        <div>
            <%= content%></div>
    </div>
    <%
    Next
                                        
End If%>
</body>
</html>
