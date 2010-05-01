<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<h:form id="documentListForm">
	<a4j:poll id="documentListRefresh" interval="60000"
		reRender="documentListForm" />

	<h:panelGrid id="listContent" columns="1" width="100%">

		<rich:dataTable id="articleTable" width="100%" cellpadding="0"
			cellspacing="0" border="0" var="document"
			value="#{documentList.documents}"
			rows="#{documentList.filter.rowCount}" styleClass="gisiDocumentListe"
			headerClass="gisiDocumentListeHeader"
			rowClasses="gisiDocumentListePair, gisiDocumentListeImpair">

			<f:facet name="header">
				<rich:columnGroup>
					<rich:column colspan="11">
						<a4j:jsFunction name="enterSearch"
							actionListener="#{documentList.updateFilterLstr}" reRender="MAIN"
							focus="titleDocSearch" />
						<h:outputLabel for="titleDocSearch" value="Search" />
						<rich:spacer width="3px" />
						<h:inputText size="22"
							value="#{documentList.filter.fullTextSearch}" id="titleDocSearch">
						</h:inputText>
						<rich:hotKey selector="#titleDocSearch" key="return"
							handler="enterSearch(); return false;" />

						<rich:spacer width="5px" />
						<a4j:commandButton id="searchDocs" value="OK"
							actionListener="#{documentList.updateFilterLstr}"
							reRender="searchDocs">
							<a4j:support event="oncomplete" reRender="documentListForm" />
						</a4j:commandButton>
					</rich:column>

					<rich:column breakBefore="true" />

					<rich:column>
						<h:outputLabel for="idDocSearch" value="id" />
						<h:inputText id="idDocSearch" value="#{documentList.filter.id}"
							size="4">
						</h:inputText>
						<rich:hotKey selector="#idDocSearch" key="return"
							handler="enterSearch(); return false;" />
					</rich:column>

					<rich:column>
						<h:outputLabel value="Title" />
					</rich:column>

					<rich:column>
						<h:outputLabel value="Description" />
					</rich:column>
					<rich:column>
						<a4j:commandLink value="Delete All"
							actionListener="#{documentList.deleteAllLstr}"
							reRender="articleTable,docScroller,compteur" />
					</rich:column>

				</rich:columnGroup>
			</f:facet>

			<rich:column styleClass="gisiListeDocumentLigne">
				<h:selectBooleanCheckbox value="" id="selectDocCheckBox">
					<a4j:support event="onclick" reRender="buttons"
						actionListener="#{documentList.rowsSelectedLstr}">
						<a4j:support event="oncomplete" reRender="selectAllCheckbox" />
					</a4j:support>
				</h:selectBooleanCheckbox>
			</rich:column>

			<rich:column styleClass="gisiListeDocumentLigne">
				<h:outputText value="#{document.id}" />
			</rich:column>

			<rich:column styleClass="gisiListeDocumentLigne">
				<h:panelGrid columns="1">
					<h:outputText value="#{document.title}" />
				</h:panelGrid>
			</rich:column>

			<rich:column styleClass="gisiListeDocumentLigne">
				<h:panelGrid columns="1">
					<h:outputText value="#{document.description}" />
				</h:panelGrid>
			</rich:column>

			<rich:column styleClass="gisiListeDocumentLigne">
				<a4j:commandLink value="Delete"
					actionListener="#{documentList.deleteLstr}"
					reRender="articleTable,docScroller,compteur" />
			</rich:column>

		</rich:dataTable>


		<h:panelGrid columns="3" styleClass="gisiListeDocumentNavigation"
			width="100%">

			<h:panelGrid columns="3" id="compteur">
				<h:outputLabel value="#{documentList.documentCount}" />
				<h:outputLabel value=" document(s)" />
			</h:panelGrid>

			<rich:datascroller id="docScroller" for="articleTable"
				page="#{documentList.currentPage}" maxPages="20"
				style="display:block;text-align:center"
				inactiveStyleClass="gisiNavigationInactif"
				selectedStyleClass="gisiNavigationActif" styleClass="gisiNavigation"
				ajaxSingle="true" reRender="articleTable" />



			<h:panelGroup style="display:block;text-align:right">
				<h:outputLabel value="Lines:" for="rowCount"></h:outputLabel>
				<h:selectOneListbox id="rowCount"
					value="#{documentList.filter.rowCount}" size="1">
					<f:selectItem itemValue="10" />
					<f:selectItem itemValue="20" />
					<f:selectItem itemValue="30" />
					<f:selectItem itemValue="50" />
					<f:convertNumber integerOnly="true" />
					<a4j:support event="onchange"
						actionListener="#{documentList.resetDocumentsLstr}"
						reRender="documentListForm" />
				</h:selectOneListbox>
			</h:panelGroup>
		</h:panelGrid>
	</h:panelGrid>

</h:form>
