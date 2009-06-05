package net.gisiinteractive.common.view.jsf;

import static javax.faces.webapp.FacesServlet.LIFECYCLE_ID_ATTR;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.webapp.FacesServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is designed to make accessible the JSF context to servlets by
 * extending it. The method service is made final so you must override the
 * abstract method {@link #jsfService(ServletRequest, ServletResponse)}. This
 * class is mainly a copy of the sun RI implementation of {@link FacesServlet},
 * it it only doesn't execute the JSF phases but gives you control through it
 * abstract defined method {@link #jsfService(ServletRequest, ServletResponse)}
 * 
 * @author zhamdi
 * 
 */
public abstract class AbstractFacesServlet implements Servlet {

	/**
	 * The <code>Logger</code> for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(
			AbstractFacesServlet.class.getName(), "javax.faces.LogStrings");

	/**
	 * <p>
	 * Factory for {@link FacesContext} instances.
	 * </p>
	 */
	private FacesContextFactory facesContextFactory = null;

	/**
	 * <p>
	 * The {@link Lifecycle} instance to use for request processing.
	 * </p>
	 */
	private Lifecycle lifecycle = null;

	/**
	 * <p>
	 * The <code>ServletConfig</code> instance for this servlet.
	 * </p>
	 */
	private ServletConfig servletConfig = null;

	/**
	 * <p>
	 * Release all resources acquired at startup time.
	 * </p>
	 */
	public void destroy() {

		facesContextFactory = null;
		lifecycle = null;
		servletConfig = null;

	}

	/**
	 * <p>
	 * Return the <code>ServletConfig</code> instance for this servlet.
	 * </p>
	 */
	public ServletConfig getServletConfig() {

		return (this.servletConfig);

	}

	/**
	 * <p>
	 * Return information about this Servlet.
	 * </p>
	 */
	public String getServletInfo() {

		return (this.getClass().getName());

	}

	/**
	 * <p>
	 * Acquire the factory instances we will require.
	 * </p>
	 * 
	 * @throws ServletException
	 *             if, for any reason, the startup of this Faces application
	 *             failed. This includes errors in the config file that is
	 *             parsed before or during the processing of this
	 *             <code>init()</code> method.
	 */
	public void init(ServletConfig servletConfig) throws ServletException {

		// Save our ServletConfig instance
		this.servletConfig = servletConfig;

		// Acquire our FacesContextFactory instance
		try {
			facesContextFactory = (FacesContextFactory) FactoryFinder
					.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
		} catch (FacesException e) {
			ResourceBundle rb = LOGGER.getResourceBundle();
			String msg = rb.getString("severe.webapp.facesservlet.init_failed");
			Throwable rootCause = (e.getCause() != null) ? e.getCause() : e;
			LOGGER.log(Level.SEVERE, msg, rootCause);
			throw new UnavailableException(msg);
		}

		// Acquire our Lifecycle instance
		try {
			LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder
					.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
			String lifecycleId;

			// First look in the servlet init-param set
			if (null == (lifecycleId = servletConfig
					.getInitParameter(LIFECYCLE_ID_ATTR))) {
				// If not found, look in the context-param set
				lifecycleId = servletConfig.getServletContext()
						.getInitParameter(LIFECYCLE_ID_ATTR);
			}

			if (lifecycleId == null) {
				lifecycleId = LifecycleFactory.DEFAULT_LIFECYCLE;
			}
			lifecycle = lifecycleFactory.getLifecycle(lifecycleId);
		} catch (FacesException e) {
			Throwable rootCause = e.getCause();
			if (rootCause == null) {
				throw e;
			} else {
				throw new ServletException(e.getMessage(), rootCause);
			}
		}

	}

	public abstract void jsfService(ServletRequest request,
			ServletResponse response) throws IOException, ServletException;

	/**
	 * <p>
	 * Process an incoming request, and create the corresponding response, by
	 * executing the request processing lifecycle.
	 * </p>
	 * 
	 * <p>
	 * If the <code>request</code> and <code>response</code> arguments to this
	 * method are not instances of <code>HttpServletRequest</code> and
	 * <code>HttpServletResponse</code>, respectively, the results of invoking
	 * this method are undefined.
	 * </p>
	 * 
	 * <p>
	 * This method must respond to requests that start with the following
	 * strings by invoking the <code>sendError</code> method on the response
	 * argument (cast to <code>HttpServletResponse</code>), passing the code
	 * <code>HttpServletResponse.SC_NOT_FOUND</code> as the argument.
	 * </p>
	 * 
	 * <ul>
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * /WEB-INF/
	 * /WEB-INF
	 * /META-INF/
	 * /META-INF
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * </ul>
	 * 
	 * 
	 * 
	 * @param request
	 *            The servlet request we are processing
	 * @param response
	 *            The servlet response we are creating
	 * 
	 * @throws IOException
	 *             if an input/output error occurs during processing
	 * @throws ServletException
	 *             if a servlet error occurs during processing
	 */
	public final void service(ServletRequest request, ServletResponse response)
			throws IOException, ServletException {

		// If prefix mapped, then ensure requests for /WEB-INF are
		// not processed.
		String pathInfo = ((HttpServletRequest) request).getPathInfo();
		if (pathInfo != null) {
			pathInfo = pathInfo.toUpperCase();
			if (pathInfo.startsWith("/WEB-INF/") || pathInfo.equals("/WEB-INF")
					|| pathInfo.startsWith("/META-INF/")
					|| pathInfo.equals("/META-INF")) {
				((HttpServletResponse) response)
						.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		}

		// Acquire the FacesContext instance for this request
		FacesContext context = facesContextFactory
				.getFacesContext(servletConfig.getServletContext(), request,
						response, lifecycle);

		// Execute the request processing lifecycle for this request
		try {
			// here comes the specific code.
			jsfService(request, response);
		} catch (FacesException e) {
			Throwable t = e.getCause();
			if (t == null) {
				throw new ServletException(e.getMessage(), e);
			} else {
				if (t instanceof ServletException) {
					throw ((ServletException) t);
				} else if (t instanceof IOException) {
					throw ((IOException) t);
				} else {
					throw new ServletException(t.getMessage(), t);
				}
			}
		} finally {
			// Release the FacesContext instance for this request
			context.release();
		}

	}

}