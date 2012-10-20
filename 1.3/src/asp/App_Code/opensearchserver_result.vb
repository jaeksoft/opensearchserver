Imports Microsoft.VisualBasic
Imports System.Xml

Namespace OpenSearchServer

    Public Class Result

        Private resultNode As XmlNode
        Private resultDocs As XmlNodeList

        Public Sub New(ByRef xml As XmlDocument)
            resultDocs = xml.SelectNodes("/response/result/doc")
            resultNode = xml.SelectSingleNode("/response/result")
        End Sub

        Public Function getPaging(ByVal maxPages As Integer) As Paging
            Return New Paging(Me, maxPages)
        End Function

        Protected Function getNamedAttribute(ByRef node As XmlNode, ByRef attributeName As String) As String
            Dim attrs As XmlAttributeCollection = node.Attributes
            If attrs Is Nothing Then Return Nothing
            Dim namedAttr As XmlNode = attrs.GetNamedItem(attributeName)
            If namedAttr Is Nothing Then Return Nothing
            Return namedAttr.Value
        End Function

        Public Function getNumFound() As Long
            Dim attr As String = getNamedAttribute(resultNode, "numFound")
            If attr Is Nothing Then Return 0
            Return Long.Parse(attr)
        End Function

        Public Function getStart() As Long
            Dim attr As String = getNamedAttribute(resultNode, "start")
            If attr Is Nothing Then Return 0
            Return Long.Parse(attr)
        End Function

        Public Function getRows() As Long
            Dim attr As String = getNamedAttribute(resultNode, "rows")
            If attr Is Nothing Then Return 0
            Return Long.Parse(attr)
        End Function

        Public Function getDocumentsCount() As Integer
            Return resultDocs.Count
        End Function

        Protected Function getDocumentSubNode(ByVal pos As Integer, ByRef xpath As String) As XmlNode
            Dim docNode As XmlNode = resultDocs.ItemOf(pos)
            If docNode Is Nothing Then Return Nothing
            Return docNode.SelectSingleNode(xpath)
        End Function

        Public Function getField(ByVal pos As Integer, ByRef name As String) As String
            Dim fieldNode = getDocumentSubNode(pos, "field[@name='" + name + "']")
            If fieldNode Is Nothing Then Return Nothing
            Return fieldNode.InnerText
        End Function

        Public Function getSnippet(ByVal pos As Integer, ByRef name As String) As String
            Dim snippetNode = getDocumentSubNode(pos, "snippet[@name='" + name + "']")
            If snippetNode Is Nothing Then Return Nothing
            Return snippetNode.InnerText
        End Function

        Public Function isSnippetHighlighted(ByVal pos As Integer, ByRef name As String) As Boolean
            Dim snippetNode = getDocumentSubNode(pos, "snippet[@name='" + name + "']")
            If snippetNode Is Nothing Then Return Nothing
            Dim attr As String = getNamedAttribute(snippetNode, "highlighted")
            If attr Is Nothing Then Return Nothing
            Return attr.ToLower = "yes"
        End Function

    End Class

End Namespace
