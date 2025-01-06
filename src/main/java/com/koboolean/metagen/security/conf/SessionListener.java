package com.koboolean.metagen.security.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

@Component
public class SessionListener  implements ApplicationListener<SessionDestroyedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SessionListener.class);

    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
        logger.debug("Session destroyed: " + event.getId());
    }
}
