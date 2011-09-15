Imports Microsoft.VisualBasic
Imports System.Xml

Namespace OpenSearchServer

    Public Class Paging

        Private totalPages As Long
        Private currentPage As Long
        Private leftPage As Long
        Private rightPage As Long

        Public Sub New(ByRef result As Result, ByVal maxPages As Integer)
            Dim numFound As Long = result.getNumFound
            Dim rows As Long = result.getRows
            Dim start As Long = result.getStart
            totalPages = 0
            currentPage = 0
            leftPage = 0
            rightPage = 0
            If numFound > 0 Then
                totalPages = (numFound + rows - 1) \ rows
                currentPage = (start + rows) \ rows
                leftPage = currentPage - maxPages \ 2
                If (leftPage < 1) Then leftPage = 1
                rightPage = leftPage + maxPages - 1
                If (rightPage > totalPages) Then
                    rightPage = totalPages
                    leftPage = rightPage - maxPages + 1
                    If (leftPage < 1) Then leftPage = 1
                End If

            End If


        End Sub

        Public Function getTotalPages() As Long
            Return totalPages
        End Function

        Public Function getCurrentPage() As Long
            Return currentPage
        End Function

        Public Function getLeftPage() As Long
            Return leftPage
        End Function

        Public Function getRightPage() As Long
            Return leftPage
        End Function

        Public Function getRenderAsLi(ByRef classForPage As String, ByRef classForCurrentPage As String, ByRef currentUrl As String, ByRef pageParameter As String) As String
            Dim sb As StringBuilder = New StringBuilder()
            If (leftPage = 0) Then Return Nothing
            For i As Long = leftPage To rightPage
                sb.Append("<li")
                If (i = currentPage) Then
                    If (classForCurrentPage <> Nothing) Then
                        sb.Append(" class=""")
                        sb.Append(classForCurrentPage)
                        sb.Append("""")
                    End If
                ElseIf (classForPage <> Nothing) Then
                    sb.Append(" class=""")
                    sb.Append(classForPage)
                    sb.Append("""")
                End If
                sb.Append(">")
                sb.Append("&nbsp;")
                If (currentUrl <> Nothing) Then
                    sb.Append("<a href=""")
                    sb.Append(currentUrl)
                    sb.Append("&")
                    sb.Append(pageParameter)
                    sb.Append("=")
                    sb.Append(i.ToString)
                    sb.Append(""">")
                End If
                sb.Append(i.ToString)
                If (currentUrl <> Nothing) Then sb.Append("</a>")
                sb.Append("&nbsp;")
                sb.Append("</li>")
            Next
            Return sb.ToString
        End Function

    End Class

End Namespace