Imports Microsoft.VisualBasic
Imports System.Xml

Namespace OpenSearchServer

    Public Class Paging

        Private totalPages As Integer
        Private currentPage As Integer

        Public Sub New(ByRef result As Result, ByVal maxPages As Integer)
            Dim numFound As Long = result.getNumFound
            Dim rows As Long = result.getRows
            Dim start As Long = result.getStart
            totalPages = 0
            currentPage = 0
            If numFound > 0 Then
                totalPages = (numFound + rows - 1) \ rows
                currentPage = (start + rows) \ rows
            End If

        End Sub

        Public Function getTotalPages() As Long
            Return totalPages
        End Function

        Public Function getCurrentPage() As Long
            Return currentPage
        End Function

    End Class

End Namespace