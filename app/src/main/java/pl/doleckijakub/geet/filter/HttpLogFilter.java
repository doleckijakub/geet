package pl.doleckijakub.geet.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class HttpLogFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpLogFilter.class);

    private String formatRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString();

        return query == null ? String.format("%s %s", method, path) : String.format("%s %s?%s", method, path, query);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(httpResponse);



            try {
                chain.doFilter(request, cachingResponse);
            } catch (Exception e) {
                LOGGER.error("{} - Error", formatRequest(httpRequest), e);

                try {
                    cachingResponse.resetBuffer();
                    cachingResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    cachingResponse.setContentType("text/plain;charset=UTF-8");
                    cachingResponse.getWriter().write("Internal server error");
                } catch (IOException e1) {
                    LOGGER.error("Error writing error response body", e1);
                }

                throw e;
            } finally {
                int status = cachingResponse.getStatus();
                int responseSize = cachingResponse.getContentSize();

                LOGGER.debug("{} - {} {}", formatRequest(httpRequest), status, responseSize);

                cachingResponse.copyBodyToResponse();
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}

