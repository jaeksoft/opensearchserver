<%@ page language="java" contentType="text/html;charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<html>

<body style="margin: 0; padding: 0;">
<f:view>
	<a4j:loadStyle src="/styles/gisi.css" />

	<h:panelGrid columns="1" width="100%" cellpadding="0" cellspacing="0">
		<jsp:include page="/header.jsp" />
		<a4j:outputPanel>
			<rich:messages id="globalMessages" globalOnly="false"
				styleClass="gisiErrorMessage">
				<f:facet name="errorMarker">
					<h:graphicImage value="/images/common/error.gif" />
				</f:facet>
			</rich:messages>
		</a4j:outputPanel>

		<rich:tabPanel headerClass="gisiOngletMenu"
			activeTabClass="gisiOngletMenuActif"
			inactiveTabClass="gisiOngletMenuInactif" contentClass="gisiBordure1"
			id="MAIN">

			<rich:tab id="LIST" label="LIST">
				<f:subview id="documentList">
					<jsp:include page="/pages/documentList.jsp" />
				</f:subview>
			</rich:tab>

			<rich:tab id="CONTENT" label="NEW DOCUMENT">
				<f:subview id="documentInsert">
					<jsp:include page="/pages/documentInsert.jsp" />
				</f:subview>
			</rich:tab>

		</rich:tabPanel>

	</h:panelGrid>
</f:view>
</body>
</html>

