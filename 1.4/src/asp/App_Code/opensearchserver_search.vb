Imports Microsoft.VisualBasic
Imports System.Xml

Namespace OpenSearchServer

    Public Class Search

        Private url As String
        Private indexName As String
        Private login As String
        Private apikey As String

        Public Sub New(ByVal url As String, ByVal indexName As String, ByVal login As String, ByVal apikey As String)
            Me.url = url
            Me.indexName = indexName
            Me.login = login
            Me.apikey = apikey
        End Sub

        Private Function buildBaseUrl(ByVal cmd As String) As StringBuilder
            Dim u As StringBuilder = New StringBuilder()
            u.Append(url)
            u.Append("/search?use=")
            u.Append(HttpUtility.HtmlEncode(indexName))
            If login <> Nothing Then
                u.Append("&login=")
                u.Append(HttpUtility.HtmlEncode(login))
            End If
            If apikey <> Nothing Then
                u.Append("&key=")
                u.Append(HttpUtility.HtmlEncode(apikey))
            End If
            Return u
        End Function

        Public Function search(ByVal template As String, ByVal query As String, ByVal start As Long, ByVal rows As Long) As Result
            Dim u As StringBuilder = buildBaseUrl("search")
            u.Append("&qt=")
            u.Append(HttpUtility.HtmlEncode(template))
            u.Append("&q=")
            u.Append(HttpUtility.HtmlEncode(query))
            If start <> Nothing Then
                u.Append("&start=")
                u.Append(start.ToString)
            End If
            If rows <> Nothing Then
                u.Append("&rows=")
                u.Append(rows.ToString)
            End If
            Dim doc As XmlDocument = New XmlDocument()
            doc.Load(XmlReader.Create(u.ToString))
            Return New Result(doc)
        End Function

    End Class

End Namespace