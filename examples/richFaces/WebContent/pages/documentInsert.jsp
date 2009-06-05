<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<h:form id="documentInsertForm">
	<h:panelGrid columns="1" width="100%" cellpadding="0" cellspacing="0">
		<h:panelGrid columns="3" width="30%">
			<h:outputLabel value="Insert mass example contents :" />
			<h:inputText value="#{documentInsert.numberToInsert}" size="4" />
			<a4j:commandButton value="Send to OSS"
				actionListener="#{documentInsert.insertDefaultContentLstr}"
				reRender="documentList:documentListForm" />
		</h:panelGrid>
		<h:outputLabel
			value="You can insert many default articles in one click to test indexation speed and search over mulitiples documents (max to insert in a row : 10 000)." />
	</h:panelGrid>
	<rich:spacer height="50" />

	<h:panelGrid columns="3" width="30%">
		<h:outputLabel value="id" />
		<h:inputText value="#{documentInsert.newDoc.id}" size="4" />
		<rich:spacer width="100"></rich:spacer>

		<h:outputLabel value="title" />
		<h:inputText value="#{documentInsert.newDoc.title}" size="20" />
		<rich:spacer width="100"></rich:spacer>
		<h:outputLabel value="description" />
		<h:inputTextarea value="#{documentInsert.newDoc.description}"
			cols="100" rows="20" />
		<rich:spacer width="100"></rich:spacer>

		<rich:spacer />
		<a4j:commandButton value="Send to OSS"
			actionListener="#{documentInsert.insertLstr}"
			reRender="documentList:documentListForm" />

		<rich:spacer width="100"></rich:spacer>
	</h:panelGrid>



</h:form>
