package net.gisiinteractive.gipublish.controller.request.filters;

import java.io.IOException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gisiinteractive.common.exceptions.BusinessException;
import net.gisiinteractive.gipublish.common.utils.GisiLogger;

public class GisiFilter implements Filter {
	public static final String EXCEPTION = "exception";
	protected FilterConfig filterConfig;
	private static GisiLogger logger = GisiLogger.getLogger(GisiFilter.class);

	protected SimpleDateFormat dateFormat = new SimpleDateFormat(
			"mm.dd HH:mm:ss");

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} catch (Exception ex) {
			manageException((HttpServletRequest) request,
					(HttpServletResponse) response, ex);
		}

	}

	private void manageException(HttpServletRequest request,
			HttpServletResponse response, Exception ex) throws IOException {
		Principal userPrincipal = ((HttpServletRequest) request)
				.getUserPrincipal();
		try {
			boolean invalidate = false, writeToLog = true;
			BusinessException bex = extractBusinessExceptionCause(ex);
			if (bex != null) {
				if (bex.getType() != BusinessException.Type.FATAL) {
					invalidate = false;
				}

				if (bex.getType() == BusinessException.Type.WARN) {
					// FacesContext context = FacesContext.getCurrentInstance();
					// context.addMessage(null, new
					// FacesMessage(ex.getMessage()));
				}

				if (bex.getType() == BusinessException.Type.MUTE)
					writeToLog = false;

			}

			if (writeToLog) {
				logger.error("gisiError: "
						+ dateFormat.format(new Date())
						+ " (user : "
						+ (userPrincipal != null ? userPrincipal.getName()
								: null) + ") error : \n\t" + ex.getMessage());
			}

			if (invalidate)
				request.getSession().invalidate();

			if (ex != null)
				request.setAttribute(EXCEPTION, ex);
			else
				request.setAttribute(EXCEPTION, "");

			request.getRequestDispatcher("/error500.jsp").include(request,
					response);

		} catch (ServletException sEx) {
			logger.error("unable to redirect to error page", sEx);
		} finally {
			System.out.println("Error catched");
			ex.printStackTrace();
		}
	}

	private BusinessException extractBusinessExceptionCause(Exception ex) {
		Throwable current = ex;

		while (current != null && !(current instanceof BusinessException)) {
			current = current.getCause();
		}
		return (BusinessException) current;
	}

}
