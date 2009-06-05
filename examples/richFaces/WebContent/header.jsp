<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>


<h:form id="headerForm">
	<h:panelGrid cellpadding="0" cellspacing="0" styleClass="gisiHeader">
		<h:panelGrid cellpadding="0" cellspacing="0" columns="2" width="100%"
			style="height:100px">
			<h:graphicImage value="/images/logo.jpg" />
			<h:panelGrid columns="1" rowClasses="gisiLogoutRow,gisiWaitRequestRow">
			<a4j:status id="waitStatus" startText="Please wait..."
				startStyleClass="gisiWaitRequest" />
			</h:panelGrid>
		</h:panelGrid>
	</h:panelGrid>
</h:form>
