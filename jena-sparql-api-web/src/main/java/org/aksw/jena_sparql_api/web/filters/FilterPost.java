package org.aksw.jena_sparql_api.web.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * Source: https://github.com/Atmosphere/atmosphere/wiki/Enabling-CORS
 *
 */
@WebFilter
public class FilterPost
    implements Filter
{
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {


        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;

        // This call causes the post data to become available via subsequent calls
        // to getParameterMap()

        // If we don't do it here, jersey will consume the data and we won't be able to access it here anymore
        if(req.getMethod().equals("POST")) {
            req.getParameterMap();
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() { }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}